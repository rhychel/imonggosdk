package net.nueca.dizonwarehouse.dialogs;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.nueca.concessioengine.dialogs.BaseAppCompatDialog;
import net.nueca.dizonwarehouse.R;

import me.grantland.widget.AutofitTextView;

/**
 * Created by gama on 01/04/2016.
 */
public class SearchDialog extends AppCompatDialog {
//    private AutofitTextView tvTitle;
    private EditText etSearch;
    private TextView tvNotFound;

    private Button btnSearch, btnCancel;

    private OnSearchListener onSearchListener;

//    private String title;

    public SearchDialog(Context context) {
        super(context);
    }

    public SearchDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.wh_search_dialog);

//        tvTitle = (AutofitTextView) super.findViewById(R.id.atvTitle);
        etSearch = (EditText) super.findViewById(R.id.etSearch);
        tvNotFound = (TextView) super.findViewById(R.id.tvNotFound);

        btnSearch = (Button) super.findViewById(R.id.btnSearch);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);

        tvNotFound.setVisibility(View.INVISIBLE);

//        if(title != null)
//            tvTitle.setText(title);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                toggleNotFound(false);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onSearchListener != null) {
                    boolean isFound = onSearchListener.onSearch(etSearch.getText().toString());
                    toggleNotFound(!isFound);
                    if(isFound)
                        dismiss();
                }
                else
                    dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onSearchListener != null)
                    onSearchListener.onCancel();
                cancel();
            }
        });
    }

    public void toggleNotFound(boolean shouldShow) {
        tvNotFound.setVisibility(shouldShow? View.VISIBLE : View.INVISIBLE);
    }

    public SearchDialog setTitle(String title) {
//        this.title = title;
//        if(tvTitle != null)
//            tvTitle.setText(title);
        super.setTitle(title);
        return this;
    }

    public SearchDialog setOnSearchListener(OnSearchListener listener) {
        this.onSearchListener = listener;
        return this;
    }

    public interface OnSearchListener {
        boolean onSearch(String text);
        void onCancel();
    }
}
