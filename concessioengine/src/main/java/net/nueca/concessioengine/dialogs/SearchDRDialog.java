package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
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

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
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
    private TextView tvNotFound, tvBranchLabel;

    private SearchDRDialogListener dialogListener;
    private List<Branch> branchList;
    private ImonggoDBHelper2 dbHelper;
    private User user;
    private boolean isNotFound = false, hasKeypad = false, hasBranch = true;

    private Animation animation;
    public SearchDRDialog(Context context, ImonggoDBHelper2 dbHelper, User user) {
        this(context, dbHelper, user, DialogTools.NO_THEME);
    }

    public SearchDRDialog(Context context, ImonggoDBHelper2 dbHelper, User user, int theme) {
        super(context, theme);
        this.dbHelper = dbHelper;
        this.user = user;

        branchList = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.simple_receive_searchdr_dialog);
        super.setCancelable(false);

        tvNotFound = (TextView) super.findViewById(R.id.tvNotFound);
        tvBranchLabel = (TextView) super.findViewById(R.id.tvBranchLabel);

        // AutoCompleteEditView
        etDeliveryReceipt = (EditText) super.findViewById(R.id.etDeliveryReceipt);
        etDeliveryReceipt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isNotFound = false;

//                tvNotFound.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        spnBranch = (Spinner) super.findViewById(R.id.spnBranch);

        btnManual = (Button) super.findViewById(R.id.btnManual);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);
        btnSearch = (Button) super.findViewById(R.id.btnSearch);

        if(concessioModule == ConcessioModule.RECEIVE_BRANCH_PULLOUT)
            ((TextInputLayout) super.findViewById(R.id.tilDeliveryReceipt)).setHint("Pullout Reference Number");
//            etDeliveryReceipt.setHint("Pullout Reference Number");


        tvNotFound.setText("Type the reference...");

        if(hasBranch) {
            ArrayAdapter<Branch> branchArrayAdapter = new ArrayAdapter<Branch>(getContext(),
                    R.layout.spinner_dropdown_item_list_light, branchList);
            spnBranch.setAdapter(branchArrayAdapter);
        }
        else {
            spnBranch.setVisibility(View.GONE);
            tvBranchLabel.setVisibility(View.GONE);
        }

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

                if(document != null) {
                    if(document.getIntransit_status().equals("Received")) {
                        btnManual.setVisibility(View.GONE);
                        tvNotFound.startAnimation(animation);
                        tvNotFound.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
                        if(concessioModule == ConcessioModule.RECEIVE_BRANCH_PULLOUT)
                            tvNotFound.setText("Document already confirmed.");
                        else
                            tvNotFound.setText("Document already received.");
                        return;
                    }
                    if(document.getReturnId() < 0) {
                        tvNotFound.setText("Document is not yet sent.");
                        return;
                    }
                }

                btnManual.setVisibility(isNotFound && etDeliveryReceipt.getText().length() > 0 ?
                        View.VISIBLE : View.INVISIBLE);
                tvNotFound.setVisibility(isNotFound ? View.VISIBLE : View.INVISIBLE);

                Log.e("isNotFound", "" + isNotFound);
                if (isNotFound) {
                    tvNotFound.startAnimation(animation);
                    tvNotFound.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
                    tvNotFound.setText("Document not found.");
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
                + (branch != null ? branch.getName() : "no branch") + "'");

        QueryBuilder<Document, Integer> queryBuilder = dbHelper.fetchObjectsInt(Document.class).queryBuilder();

        Where<Document, Integer> whereDoc = queryBuilder.where();
        if(hasBranch)
            whereDoc.eq("target_branch_id", branch.getId()).and();
        whereDoc.eq("reference", drNo);//.and();
//        whereDoc.eq("intransit_status", "Intransit");

        queryBuilder.setWhere(whereDoc);

        Document document = queryBuilder.queryForFirst();


        if(document != null)
            Log.e("Reference " + drNo, document.getTarget_branch_id() + " -- " + (branch != null ? branch.getId() : "branch is null"));

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

    public void setHasBranch(boolean hasBranch) {
        this.hasBranch = hasBranch;
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
