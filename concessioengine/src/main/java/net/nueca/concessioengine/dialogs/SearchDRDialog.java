package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.widgets.ModifiedNumpad;
import net.nueca.imonggosdk.widgets.Numpad;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 9/1/15.
 */
public class SearchDRDialog extends BaseAppCompatDialog {
    private EditText etDeliveryReceipt;
    private Spinner spnBranch;
    private Button btnSearch, btnCancel;
    private ModifiedNumpad npInput;
    private TextView tvNotFound;

    private SearchDRDialogListener dialogListener;
    private List<Branch> branchList;
    private ImonggoDBHelper dbHelper;
    private User user;
    private boolean isNotFound = false;
    private List<Product> productList;

    public SearchDRDialog(Context context, ImonggoDBHelper dbHelper, User user) {
        super(context);
        this.dbHelper = dbHelper;
        this.user = user;

        branchList = new ArrayList<>();
        try {
            List<BranchUserAssoc> branchUserAssocs = dbHelper.getBranchUserAssocs().queryBuilder().where()
                    .eq("user_id", user).query();
            for(BranchUserAssoc branchUser : branchUserAssocs) {
                if(branchUser.getBranch().getSite_type() != null &&
                    branchUser.getBranch().getSite_type().toLowerCase().equals("warehouse"))
                    continue;

                if(branchUser.getBranch().getId() == user.getHome_branch_id())
                    branchList.add(0, branchUser.getBranch());
                else
                    branchList.add(branchUser.getBranch());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.simple_receive_searchdr_dialog);

        tvNotFound = (TextView) super.findViewById(R.id.tvNotFound);
        etDeliveryReceipt = (EditText) super.findViewById(R.id.etDeliveryReceipt);
        etDeliveryReceipt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isNotFound = false;
                tvNotFound.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        spnBranch = (Spinner) super.findViewById(R.id.spnBranch);

        btnCancel = (Button) super.findViewById(R.id.btnCancel);
        btnSearch = (Button) super.findViewById(R.id.btnSearch);

        ArrayAdapter<Branch> branchArrayAdapter = new ArrayAdapter<Branch>(getContext(),
                R.layout.simple_spinner_item, branchList);

        spnBranch.setAdapter(branchArrayAdapter);

        npInput = (ModifiedNumpad) super.findViewById(R.id.npInput);
        npInput.addTextHolder(etDeliveryReceipt,"etDeliveryReceipt",false,false,null);
        npInput.getTextHolderWithTag("etDeliveryReceipt").setEnableDot(false);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogListener != null)
                    dialogListener.onCancel();
                dismiss();
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNotFound = false;
                productList = new ArrayList<Product>();

                try {
                    search(etDeliveryReceipt.getText().toString(), (Branch)spnBranch.getSelectedItem());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                tvNotFound.setVisibility(isNotFound? View.VISIBLE : View.GONE);

                if (dialogListener != null)
                    dialogListener.onSearch(etDeliveryReceipt.getText().toString(),
                            (Branch)spnBranch.getSelectedItem());
            }
        });
    }

    public void search(String drNo, Branch branch) throws SQLException {
        Document document = dbHelper.getDocuments().queryBuilder()
                .where()
                .eq("target_branch_id", branch.getId()).and()
                .eq("reference", drNo)
                .queryForFirst();

        if(document == null) {
            isNotFound = true;
            return;
        }

        List<DocumentLine> documentLines = document.getDocument_lines();
        for(DocumentLine documentLine : documentLines) {
            Product t_product = dbHelper.getProducts().queryBuilder().where()
                    .eq("id", documentLine.getProduct_id()).queryForFirst();

            if(t_product != null)
                productList.add(t_product);
        }

        isNotFound = productList.size() <= 0;
    }

    public SearchDRDialogListener getDialogListener() {
        return dialogListener;
    }

    public void setDialogListener(SearchDRDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    public interface SearchDRDialogListener {
        public void onCancel();
        public void onSearch(String deliveryReceiptNo, Branch branch);
    }
}
