package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.widgets.ModifiedNumpad;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by rhy on 5/2/16
 * 1. Pullout Request - PROCEED
 *     * Reason
 *     * Source
 *     * Destination
 * 2. Receiving - SEARCH
 *     * Branch
 *     * Receipt No.
 *     * btnManual
 * 3. Pullout - SAVE
 *     * Pullout Reference No.
 */
public class SearchDRDialog2 extends BaseAppCompatDialog {

    private AutofitTextView tvTitle;
    private LinearLayout llReason, llSourceBranch, llDestinationBranch, llReceiptNo;
    private TextInputLayout tilPulloutRef;

    private Spinner spReason, spSourceBranch, spDestinationBranch;
    private EditText etPulloutRef, etReceiptNo;
    private Button btnSave, btnCancel, btnManual;

    private TextView tvNotFound;
    private ArrayAdapter<Branch> sourceBranch;
    private ArrayAdapter<Branch> destinationBranch;

    private SearchDRDialogListener dialogListener;
    private ImonggoDBHelper2 dbHelper;
    private User user;
    private boolean isNotFound = false, isFromSource = false, isFromDestination = false;

    private Animation animation;
    public SearchDRDialog2(Context context, ImonggoDBHelper2 dbHelper, User user, ConcessioModule concessioModule) {
        this(context, dbHelper, user, concessioModule, DialogTools.NO_THEME);
    }

    public SearchDRDialog2(Context context, ImonggoDBHelper2 dbHelper, User user, ConcessioModule concessioModule, int theme) {
        super(context, theme);
        this.dbHelper = dbHelper;
        this.user = user;
        this.concessioModule = concessioModule;
    }

    private TextWatcher forInput = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            isNotFound = false;
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.simple_searchdr_dialog);
        super.setCancelable(false);

        llReason = (LinearLayout) super.findViewById(R.id.llReason);
        llSourceBranch = (LinearLayout) super.findViewById(R.id.llSourceBranch);
        llDestinationBranch = (LinearLayout) super.findViewById(R.id.llDestinationBranch);
        llReceiptNo = (LinearLayout) super.findViewById(R.id.llReceiptNo);
        tilPulloutRef = (TextInputLayout) super.findViewById(R.id.tilPulloutRef);
        btnSave = (Button) super.findViewById(R.id.btnSave);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);
        tvNotFound = (TextView) super.findViewById(R.id.tvNotFound);
        tvTitle = (AutofitTextView) super.findViewById(R.id.tvTitle);

        switch (concessioModule) {
            case RECEIVE_BRANCH_PULLOUT: { // Pullout Confirmation
                tilPulloutRef.setVisibility(View.VISIBLE);
                etPulloutRef = (EditText) super.findViewById(R.id.etPulloutRef);
                etPulloutRef.addTextChangedListener(forInput);
                tvNotFound.setVisibility(View.VISIBLE);
                btnSave.setOnClickListener(withReference);
                tvTitle.setText("Pullout");

            } break;
            case RECEIVE_BRANCH: { // Receiving
                llReceiptNo.setVisibility(View.VISIBLE);
                llSourceBranch.setVisibility(View.VISIBLE);
                spSourceBranch = (Spinner) super.findViewById(R.id.spSourceBranch);
                btnManual = (Button) super.findViewById(R.id.btnManual);
                etReceiptNo = (EditText) super.findViewById(R.id.etReceiptNo);
                ((TextView) findViewById(R.id.tvSourceBranchLabel)).setText("Branch");
                etReceiptNo.addTextChangedListener(forInput);
                tvNotFound.setVisibility(View.VISIBLE);

                sourceBranch = new ArrayAdapter<>(getContext(), R.layout.spinner_dropdown_item_list_light, Branch.allUserBranches(getContext(), dbHelper, user));
                spSourceBranch.setAdapter(sourceBranch);
                btnSave.setOnClickListener(withReference);
                tvTitle.setText("Receiving");

                btnManual.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(etReceiptNo.getText().length() == 0)
                            return;
                        if(dialogListener != null)
                            dialogListener.onManualReceive(etReceiptNo.getText().toString(),
                                    (Branch) spSourceBranch.getSelectedItem());

                        btnManual.setVisibility(View.GONE);
                        dismiss();
                    }
                });
            } break;
            case RELEASE_BRANCH: { // Pullout -- on a different dialog... as a fragment
                llReason.setVisibility(View.VISIBLE);
                spReason = (Spinner) super.findViewById(R.id.spReason);
                spSourceBranch = (Spinner) super.findViewById(R.id.spSourceBranch);
                spDestinationBranch = (Spinner) super.findViewById(R.id.spDestinationBranch);
                btnSave.setText("PROCEED");
                tvTitle.setText("Pullout Request");

                sourceBranch = new ArrayAdapter<>(getContext(), R.layout.spinner_dropdown_item_list_light, Branch.allUserBranches(getContext(), dbHelper, user));
                destinationBranch = new ArrayAdapter<>(getContext(), R.layout.spinner_dropdown_item_list_light, new ArrayList<Branch>());

                spSourceBranch.setAdapter(sourceBranch);
                spDestinationBranch.setAdapter(destinationBranch);
                spSourceBranch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(isFromDestination)
                            return;
                        List<Branch> branches = Branch.allUserBranches(getContext(), dbHelper, user);
                        branches.remove(sourceBranch.getItem(position));

                        destinationBranch.clear();
                        destinationBranch.addAll(branches);
                        destinationBranch.notifyDataSetChanged();
                        isFromSource = true;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                spDestinationBranch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(isFromSource)
                            return;
                        List<Branch> branches = Branch.allUserBranches(getContext(), dbHelper, user);
                        branches.remove(destinationBranch.getItem(position));

                        sourceBranch.clear();
                        sourceBranch.addAll(branches);
                        sourceBranch.notifyDataSetChanged();
                        isFromDestination = true;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            } break;
        }

        tvNotFound.setText("Type the reference...");

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogListener == null)
                    cancel();

                if (dialogListener != null && dialogListener.onCancel())
                    cancel();
            }
        });

        animation = AnimationUtils.loadAnimation(getContext(),R.anim.shrink);

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if(etPulloutRef != null)
                    etPulloutRef.requestFocus();
                if(etReceiptNo != null)
                    etReceiptNo.requestFocus();
                resetUI();
            }
        });
    }

    private void resetUI() {
        if(etReceiptNo != null)
            etReceiptNo.setText("");
        if(etPulloutRef != null)
            etPulloutRef.setText("");
        if(tvNotFound != null) {
            tvNotFound.setVisibility(View.VISIBLE);
            tvNotFound.setText("type the reference");
            tvNotFound.setTextColor(ContextCompat.getColor(getContext(), R.color.accentLogin));
        }
    }

    private View.OnClickListener withReference = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isNotFound = false;

            Document document = null;
            String reference = etReceiptNo != null ? etReceiptNo.getText().toString() : etPulloutRef.getText().toString();

            try {
                document = search(reference, spSourceBranch != null ? (Branch) spSourceBranch.getSelectedItem() : null);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if(document != null) {
                if(document.getIntransit_status().equals("Received")) {
                    if(btnManual != null)
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

            if(btnManual != null)
                btnManual.setVisibility(isNotFound && reference.trim().length() > 0 ?
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
                dialogListener.onSearch(reference,
                        spSourceBranch != null ? (Branch) spSourceBranch.getSelectedItem() : null, document);
            dismiss();
        }
    };

    public Document search(String drNo, Branch branch) throws SQLException {
        Log.e("search", drNo + " from " + dbHelper.fetchObjectsList(Document.class).size() + " document(s) for branch '"
                + (branch != null ? branch.getName() : "no branch") + "'");

        QueryBuilder<Document, Integer> queryBuilder = dbHelper.fetchObjectsInt(Document.class).queryBuilder();

        Where<Document, Integer> whereDoc = queryBuilder.where();
        if(branch != null)
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

    @Override
    public void show() {
        super.show();
    }

    public SearchDRDialogListener getDialogListener() {
        return dialogListener;
    }

    public void setDialogListener(SearchDRDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    public interface SearchDRDialogListener {
        /** return true if should dismiss dialog **/
        boolean onCancel();
        void onSearch(String deliveryReceiptNo, Branch target_branch, Document document);
        void onManualReceive(String deliveryReceiptNo, Branch target_branch);
    }
}
