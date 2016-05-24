package net.nueca.dizonwarehouse.dialogs;

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

import com.j256.ormlite.stmt.Where;

import net.nueca.dizonwarehouse.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.dialogs.DialogTools;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.order.Order;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by gama on 01/04/2016.
 */
public class SearchOrdersDialog extends AppCompatDialog {
//    private AutofitTextView tvTitle;
    private EditText etSearch;
    private TextView tvNotFound;

    private ImonggoDBHelper2 dbHelper2;
    private Session session;

    private Button btnSearch, btnCancel;

    private OnSearchListener onSearchListener;

    private String order_type = "";
    private int branch_id;
    private Order foundOrder;

    public SearchOrdersDialog(Context context, ImonggoDBHelper2 dbHelper2, Session session) {
        super(context);
        this.dbHelper2 = dbHelper2;
        this.session = session;
    }

    public SearchOrdersDialog(Context context, ImonggoDBHelper2 dbHelper2, Session session, int theme) {
        super(context, theme);
        this.dbHelper2 = dbHelper2;
        this.session = session;
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
                String reference = etSearch.getText().toString();
                try {
                    Where<Order, ?> whereOrder = dbHelper2.fetchObjects(Order.class).queryBuilder().where().eq("reference", reference)
                            .and().like("order_type_code", order_type);

                    foundOrder = whereOrder.queryForFirst();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if(foundOrder == null) {
                    Order.fetchByReference(getContext(), String.valueOf(branch_id), reference, order_type, session, new VolleyRequestListener() {
                        @Override
                        public void onStart(Table table, RequestType requestType) {
                            DialogTools.showIndeterminateProgressDialog(getContext(), null, "Searching for order...", false, net.nueca.concessioengine
                                    .R.style.AppCompatDialogStyle_Light_NoTitle);
                        }

                        @Override
                        public void onSuccess(Table table, RequestType requestType, Object response) {
                            DialogTools.hideIndeterminateProgressDialog();
                            Log.e("RESPONSE", response.toString());
                            try {
                                JSONObject jsonObject = null;
                                if(response instanceof JSONObject)
                                    jsonObject = (JSONObject) response;
                                else if (response instanceof JSONArray) {
                                    if(((JSONArray) response).length() > 0)
                                        jsonObject = ((JSONArray) response).getJSONObject(0);
                                }
                                else if (response instanceof String) {
                                    jsonObject = new JSONObject((String) response);
                                }

                                if(jsonObject == null) { // not found
                                    toggleNotFound(true);
                                }
                                else {
                                    foundOrder = Order.fromJSONObject(jsonObject);
                                    foundOrder.setReturnId(jsonObject.getInt("id"));
                                    foundOrder.setBranch_id(branch_id);
                                    foundOrder.insertTo(dbHelper2);

                                    if(onSearchListener != null) {
                                        onSearchListener.onFound(foundOrder);
                                        dismiss();
                                    }
                                    else
                                        dismiss();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                            DialogTools.hideIndeterminateProgressDialog();
                            Log.e("RESPONSE " + responseCode, response == null? "null" : response.toString());
                            toggleNotFound(true);
                        }

                        @Override
                        public void onRequestError() {
                            DialogTools.hideIndeterminateProgressDialog();
                            toggleNotFound(true);
                        }
                    });
                }
                else {
                    if(onSearchListener != null) {
                        onSearchListener.onFound(foundOrder);
                        dismiss();
                    }
                    else
                        dismiss();
                }
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

    public SearchOrdersDialog setTitle(String title) {
//        this.title = title;
//        if(tvTitle != null)
//            tvTitle.setText(title);
        super.setTitle(title);
        return this;
    }

    public void setOrder_type(String order_type) {
        this.order_type = order_type;
    }

    public void setBranch_id(int branch_id) {
        this.branch_id = branch_id;
    }

    public SearchOrdersDialog setOnSearchListener(OnSearchListener listener) {
        this.onSearchListener = listener;
        return this;
    }

    public interface OnSearchListener {
        void onFound(Order foundOrder);
        void onCancel();
    }
}
