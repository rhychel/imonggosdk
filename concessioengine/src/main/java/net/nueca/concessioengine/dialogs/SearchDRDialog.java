package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.widgets.ModifiedNumpad;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 9/1/15.
 */
public class SearchDRDialog extends BaseAppCompatDialog {
    private EditText etDeliveryReceipt;
    private Spinner spnBranch;
    private Button btnSearch, btnCancel, btnManual;
    private ModifiedNumpad npInput;
    private TextView tvNotFound;

    private SearchDRDialogListener dialogListener;
    private List<Branch> branchList;
    private ImonggoDBHelper2 dbHelper;
    private User user;
    private boolean isNotFound = false, hasKeypad = false;

    private Animation animation;
    public SearchDRDialog(Context context, ImonggoDBHelper2 dbHelper, User user) {
        this(context, dbHelper, user, DialogTools.NO_THEME);
    }

    public SearchDRDialog(Context context, ImonggoDBHelper2 dbHelper, User user, int theme) {
        super(context, theme);
        this.dbHelper = dbHelper;
        this.user = user;

        branchList = new ArrayList<>();
//        try {
//            List<BranchUserAssoc> branchUserAssocs = dbHelper.fetchObjects(BranchUserAssoc.class).queryBuilder().where()
//                    .eq("user_id", this.user).query();
//
//            for(BranchUserAssoc branchUser : branchUserAssocs) {
//                if(branchUser.getBranch().getSite_type() != null &&
//                        branchUser.getBranch().getSite_type().toLowerCase().equals("warehouse"))
//                    continue;
//
//                if(branchUser.getBranch().getId() == this.user.getHome_branch_id())
//                    branchList.add(0, branchUser.getBranch());
//                else
//                    branchList.add(branchUser.getBranch());
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.simple_receive_searchdr_dialog);
        super.setCancelable(false);

        tvNotFound = (TextView) super.findViewById(R.id.tvNotFound);
        etDeliveryReceipt = (EditText) super.findViewById(R.id.etDeliveryReceipt);
        etDeliveryReceipt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isNotFound = false;

                tvNotFound.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        spnBranch = (Spinner) super.findViewById(R.id.spnBranch);

        btnManual = (Button) super.findViewById(R.id.btnManual);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);
        btnSearch = (Button) super.findViewById(R.id.btnSearch);

        ArrayAdapter<Branch> branchArrayAdapter = new ArrayAdapter<Branch>(getContext(),
                R.layout.spinner_dropdown_item_list_light, branchList);
        spnBranch.setAdapter(branchArrayAdapter);

        npInput = (ModifiedNumpad) super.findViewById(R.id.npInput);

        if(hasKeypad) {
            npInput.addTextHolder(etDeliveryReceipt, "etDeliveryReceipt", false, false, null);
            npInput.getTextHolderWithTag("etDeliveryReceipt").setEnableDot(false);
        }
        else
            npInput.setVisibility(View.GONE);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogListener == null)
                    cancel();

                if (dialogListener != null && dialogListener.onCancel())
                    cancel();
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNotFound = false;

                Document document = null;

                Log.e("btnSearch", "clicked");
                try {
                    document = search(etDeliveryReceipt.getText().toString(), (Branch) spnBranch.getSelectedItem());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                btnManual.setVisibility(isNotFound && etDeliveryReceipt.getText().length() > 0 ?
                        View.VISIBLE : View.INVISIBLE);
                tvNotFound.setVisibility(isNotFound ? View.VISIBLE : View.INVISIBLE);

                Log.e("isNotFound", "" + isNotFound);
                if (isNotFound) {
                    tvNotFound.startAnimation(animation);
                    return;
                }

                if (dialogListener != null)
                    dialogListener.onSearch(etDeliveryReceipt.getText().toString(),
                            (Branch) spnBranch.getSelectedItem(), document);
                dismiss();
            }
        });
        btnManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etDeliveryReceipt.getText().length() == 0)
                    return;
                if(dialogListener != null)
                    dialogListener.onManualReceive(etDeliveryReceipt.getText().toString(),
                            (Branch) spnBranch.getSelectedItem());

                btnManual.setVisibility(View.GONE);
                dismiss();
            }
        });

        animation = AnimationUtils.loadAnimation(getContext(),R.anim.shrink);
    }

    public Document search(String drNo, Branch branch) throws SQLException {
        Log.e("search", drNo + " from " + dbHelper.fetchObjectsList(Document.class).size() + " document(s) for branch '"
                + branch.getName() + "'");

        Document document = dbHelper.fetchObjects(Document.class).queryBuilder()
                .where()
                .eq("target_branch_id", branch.getId()).and()
                .eq("reference", drNo).and()
                .eq("intransit_status", "Intransit")
                .queryForFirst();

        if(document != null)
            Log.e("Reference " + drNo, document.getTarget_branch_id() + " -- " + branch.getId());

        isNotFound = document == null;
        return document;
    }

    public void showWithText(String txt) {
        show();
        etDeliveryReceipt.setText(txt);
    }

    @Override
    public void show() {
        super.show();
        if(hasKeypad)
            npInput.setIsFirstErase(true);
        else
            etDeliveryReceipt.requestFocus();
    }

    public SearchDRDialogListener getDialogListener() {
        return dialogListener;
    }

    public void setDialogListener(SearchDRDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    public void setHasKeypad(boolean hasKeypad) {
        this.hasKeypad = hasKeypad;
    }

    public void setBranchList(List<Branch> branchList) {
        this.branchList = branchList;
    }

    public interface SearchDRDialogListener {
        /** return true if should dismiss dialog **/
        boolean onCancel();
        void onSearch(String deliveryReceiptNo, Branch target_branch, Document document);
        void onManualReceive(String deliveryReceiptNo, Branch target_branch);
    }
}
