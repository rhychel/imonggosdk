package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.widgets.ModifiedNumpad;

import java.sql.SQLException;

/**
 * Created by gama on 06/11/2015.
 */
public class SimpleSearchDocumentDialog extends BaseAppCompatDialog {

    private ImonggoDBHelper2 dbHelper;

    private ModifiedNumpad npInput;
    private EditText etReference;
    private TextView tvNotFound;
    private Button btnSearch, btnCancel;

    private SearchDocumentDialogListener dialogListener;

    private Animation animation;

    public SimpleSearchDocumentDialog(Context context, ImonggoDBHelper2 dbHelper) {
        super(context);
        this.dbHelper = dbHelper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.simple_search_document_dialog);
        super.setCancelable(false);

        etReference = (EditText) super.findViewById(R.id.etReference);
        tvNotFound = (TextView) super.findViewById(R.id.tvNotFound);
        tvNotFound.setVisibility(View.INVISIBLE);

        npInput = (ModifiedNumpad) super.findViewById(R.id.npInput);
        npInput.addTextHolder(etReference, "etReference", false, false, null);
        npInput.getTextHolderWithTag("etReference").setEnableDot(false);

        btnSearch = (Button) super.findViewById(R.id.btnSearch);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogListener != null) {
                    try {
                        Document result = search(etReference.getText().toString());

                        boolean shouldDismiss;
                        if(result != null) {
                            tvNotFound.setVisibility(View.INVISIBLE);
                            shouldDismiss = dialogListener.onFound(result);
                        }
                        else {
                            tvNotFound.setVisibility(View.VISIBLE);
                            tvNotFound.startAnimation(animation);
                            shouldDismiss = dialogListener.onNotFound();
                        }

                        if(shouldDismiss)
                            dismiss();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogListener != null) {
                    if (dialogListener.onCancel())
                        cancel();
                }
                else
                    cancel();
            }
        });

        animation = AnimationUtils.loadAnimation(getContext(), R.anim.shrink);
    }

    public Document search(String reference) throws SQLException {
        return dbHelper.fetchObjects(Document.class).queryBuilder().where()
                .eq("reference", reference).and()
                .eq("intransit_status", "Intransit")
                .queryForFirst();
    }

    public void setDialogListener(SearchDocumentDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    public interface SearchDocumentDialogListener {
        boolean onCancel();
        boolean onFound(Document document);
        boolean onNotFound();
    }
}
