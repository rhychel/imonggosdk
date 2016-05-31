package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.objects.ExtendedAttributes;
import net.nueca.concessioengine.objects.Values;
import net.nueca.concessioengine.tools.PriceTools;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.BranchProduct;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.NumberTools;
import net.nueca.imonggosdk.tools.TimerTools;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Calendar;

import me.grantland.widget.AutofitTextView;

/**
 * Created by rhymart on 11/26/15.
 */
public class SimpleSalesQuantityDialog extends BaseQuantityDialog {

    private AutofitTextView tvProductName;
    private TextView tvRetailPrice, tvInStock, tvSubtotal, tvUnit, tvExpectedQty, tvOutright, tvDiscrepancy,
        tvExpectedPrice, tvActualPrice;
    private EditText etQuantity, etExpectedQty, etOutright, etDiscrepancy, etRetailPrice,
        etExpectedPrice, etActualPrice;
    private Spinner spUnits;
    private Button btnCancel, btnSave;
    private LinearLayout llSubtotal;

    private LinearLayout llInvoicePurpose, llExpiryDate, llBrand, llExpectedQty, llOutright, llDiscrepancy, llRetailPrice,
        llExpectedPrice, llActualPrice;
    private Spinner spInvoicePurpose, spBrands;
    private Button btnExpiryDate;
    private SwitchCompat swcBadStock;

    private boolean forceSellableUnit = false;
    private String subtotal, retailPrice, ovPrice;

    private Unit defaultUnit;

    private Product product;

    private InvoicePurpose invP;

    protected SimpleSalesQuantityDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public SimpleSalesQuantityDialog(Context context, int theme) {
        super(context, theme);
    }

    public SimpleSalesQuantityDialog(Context context) {
        super(context);
    }

    private TextWatcher onQuantityChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(hasExpectedQty) {
                BigDecimal orig_qty = NumberTools.toBigDecimal(etExpectedQty.getText().toString());
                BigDecimal rcv_qty = NumberTools.toBigDecimal(etQuantity.getText().toString());
                BigDecimal ret_qty = NumberTools.toBigDecimal(etOutright.getText().toString());
                BigDecimal dsc_qty = orig_qty.subtract(rcv_qty.add(ret_qty));

                etDiscrepancy.setText(NumberTools.separateInCommas(dsc_qty.doubleValue()));
            }
        }
        @Override
        public void afterTextChanged(Editable editable) {
            String quantity = etQuantity.getText().toString();
            subtotal = String.valueOf(NumberTools.toDouble(quantity.equals("-") ? "0" : quantity) * NumberTools.toDouble(retailPrice));
            tvSubtotal.setText("P"+NumberTools.separateInCommas(subtotal));
            Log.e("SUBTOTAL AFTER", subtotal);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.simple_quantity_dialog2);

//        TimerTools.start("SalesQuantity");
        tvProductName = (AutofitTextView) super.findViewById(R.id.tvProductName);
        tvRetailPrice = (TextView) super.findViewById(R.id.tvRetailPrice);
        tvInStock = (TextView) super.findViewById(R.id.tvInStock);
        tvSubtotal = (TextView) super.findViewById(R.id.tvSubtotal);
        tvUnit = (TextView) super.findViewById(R.id.tvUnit);
        etQuantity = (EditText) super.findViewById(R.id.etQuantity);
        spUnits = (Spinner) super.findViewById(R.id.spUnits);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);
        btnSave = (Button) super.findViewById(R.id.btnSave);
        llSubtotal = (LinearLayout) super.findViewById(R.id.llSubtotal);

        etQuantity.setSelectAllOnFocus(true);
        etQuantity.addTextChangedListener(onQuantityChanged);

        boolean hasValues = selectedProductItem.getValues().size() > 0;

        if(hasExpectedQty) {
            llExpectedQty = (LinearLayout) super.findViewById(R.id.llExpectedQty);
            tvExpectedQty = (TextView) super.findViewById(R.id.tvExpectedQty);
            etExpectedQty = (EditText) super.findViewById(R.id.etExpectedQty);
            etOutright= (EditText) super.findViewById(R.id.etOutright);
            etDiscrepancy = (EditText) super.findViewById(R.id.etDiscrepancy);

            if (isMultiValue) {
                if (valuePosition > -1) {
                    etExpectedQty.setText(selectedProductItem.getOriginalQuantity(valuePosition));
                    etDiscrepancy.setText(selectedProductItem.getDiscrepancy(valuePosition));
                }
                else {
                    etExpectedQty.setText("0");
                    etDiscrepancy.setText("0");
                }
            } else {
                etExpectedQty.setText(selectedProductItem.getOriginalQuantity());
                Log.e("Discrepancy", selectedProductItem.getDiscrepancy());
                etDiscrepancy.setText(selectedProductItem.getDiscrepancy());
            }


            llExpectedQty.setVisibility(View.VISIBLE);
        }

        if(hasExpectedPrice) {
            llExpectedPrice = (LinearLayout) super.findViewById(R.id.llExpectedPrice);
            tvExpectedPrice = (TextView) super.findViewById(R.id.tvExpectedPrice);
            etExpectedPrice = (EditText) super.findViewById(R.id.etExpectedPrice);

            if (isMultiValue) {
                if (valuePosition > -1) {
                    etExpectedPrice.setText("" + selectedProductItem.getRetail_price(valuePosition));
                }
                else {
                    etExpectedPrice.setText("" + selectedProductItem.getRetail_price());
                }
            } else {
                etExpectedPrice.setText("" + selectedProductItem.getRetail_price());
            }

            llExpectedPrice.setVisibility(View.VISIBLE);
        }

        if(hasActualPrice) {
            llActualPrice = (LinearLayout) super.findViewById(R.id.llActualPrice);
            tvActualPrice = (TextView) super.findViewById(R.id.tvActualPrice);
            etActualPrice = (EditText) super.findViewById(R.id.etActualPrice);

            if (isMultiValue) {
                if (valuePosition > -1) {
                    etActualPrice.setText("" + selectedProductItem.getValuesRetailPrice(valuePosition));
                }
                else {
                    etActualPrice.setText("" + selectedProductItem.getRetail_price());
                }
            } else {
                etActualPrice.setText("" + selectedProductItem.getValuesRetailPrice(0));
            }

            llActualPrice.setVisibility(View.VISIBLE);
        }

        if(hasOutright) {
            llOutright = (LinearLayout) super.findViewById(R.id.llOutright);
            tvOutright = (TextView) super.findViewById(R.id.tvOutright);
            etOutright = (EditText) super.findViewById(R.id.etOutright);
            etOutright.setSelectAllOnFocus(true);
            etOutright.addTextChangedListener(onQuantityChanged);

            if (isMultiValue) {
                if (valuePosition > -1)
                    etOutright.setText(selectedProductItem.getReturn(valuePosition));
                else
                    etOutright.setText("0");
            } else
                etOutright.setText(selectedProductItem.getReturn());

            llOutright.setVisibility(View.VISIBLE);
        }
        if(hasDiscrepancy) {
            llDiscrepancy = (LinearLayout) super.findViewById(R.id.llDiscrepancy);
            tvDiscrepancy = (TextView) super.findViewById(R.id.tvDiscrepancy);
            etDiscrepancy = (EditText) super.findViewById(R.id.etDiscrepancy);

            if (isMultiValue) {
                if (valuePosition > -1)
                    etDiscrepancy.setText(selectedProductItem.getDiscrepancy(valuePosition));
                else
                    etDiscrepancy.setText("0");
            } else
                etDiscrepancy.setText(selectedProductItem.getDiscrepancy());

            llDiscrepancy.setVisibility(View.VISIBLE);
        }

        if(hasBrand) {
            llBrand = (LinearLayout) super.findViewById(R.id.llBrand);
            spBrands = (Spinner) super.findViewById(R.id.spBrands);

            llBrand.setVisibility(View.VISIBLE);

            brandsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, brandList);
            spBrands.setAdapter(brandsAdapter);

            // Check if there are existing values
            if (selectedProductItem.getValues().size() > 0) {
                if (isMultiValue) {
                    if (valuePosition > -1) // check if you are editing a value from the list
                        spBrands.setSelection(brandList.indexOf(selectedProductItem.getValues().get(valuePosition).getExtendedAttributes().getBrand()));
                } else
                    spBrands.setSelection(brandList.indexOf(selectedProductItem.getValues().get(0).getExtendedAttributes().getBrand()));
            }
        }

        invP = new InvoicePurpose();
        invP.setId(-1);
        invP.setName("--");
        if(hasInvoicePurpose) {
            llInvoicePurpose = (LinearLayout) super.findViewById(R.id.llInvoicePurpose);
            spInvoicePurpose = (Spinner) super.findViewById(R.id.spInvoicePurpose);

            llInvoicePurpose.setVisibility(View.VISIBLE);
            invoicePurposesAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_light, invoicePurposeList);
            invoicePurposesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
            spInvoicePurpose.setAdapter(invoicePurposesAdapter);
            if(hasExpiryDate) { // this is for the expiry date
                llExpiryDate = (LinearLayout) super.findViewById(R.id.llExpiryDate);
                btnExpiryDate = (Button) super.findViewById(R.id.btnExpiryDate);

                Calendar now = Calendar.getInstance();
                date = DateTimeTools.convertToDate(now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1) + "-" + now.get(Calendar.DAY_OF_MONTH), "yyyy-M-d", "yyyy-MM-dd");
                btnExpiryDate.setText(date);

                spInvoicePurpose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.e(">>>", "invoice purposes extras: " + invoicePurposeList.get(position).getExtras());
                        Log.e(">>>", "invoice purposes ["+invoicePurposeList.get(position).getName()+"]: " + invoicePurposeList.get(position).getId()+" id");
                        if(invoicePurposeList.get(position).getExtras() != null && invoicePurposeList.get(position).getExtras().require_date()) {
                            llExpiryDate.setVisibility(View.VISIBLE);
                        }
                        else
                            llExpiryDate.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                btnExpiryDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeliveryDatePicker(fragmentManager, btnExpiryDate);
                    }
                });
            }

        }

        if(hasBadStock) {
            swcBadStock = (SwitchCompat) super.findViewById(R.id.swcBadStock);
            swcBadStock.setVisibility(View.VISIBLE);
            swcBadStock.setChecked(true);
            swcBadStock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.e("badStock", "onCheckedChanged-"+isChecked);
                    if(hasInvoicePurpose) {
                        if(isChecked) {
//                            if(invoicePurposeList.indexOf(invoicePurpose) > 0) {
//                                selectedIndex--;
//                                spInvoicePurpose.setSelection(selectedIndex);
//                            }
                            int invpIndex = invoicePurposeList.indexOf(invP);
                            Log.e("invpIndex", invpIndex+"");
                            if(invpIndex > -1)
                                invoicePurposesAdapter.remove(invP);
                            invoicePurposesAdapter.notifyDataSetChanged();
                            spInvoicePurpose.setSelection(0);
                            if(invoicePurposeList.get(0).getExtras() != null && invoicePurposeList.get(0).getExtras().require_date())
                                llExpiryDate.setVisibility(View.VISIBLE);
                        }
                        else {
//                            selectedIndex++;
                            invoicePurposeList.add(0, invP);
                            invoicePurposesAdapter.notifyDataSetChanged();
                            spInvoicePurpose.setSelection(0);
                            llExpiryDate.setVisibility(View.GONE);
//                            invoicePurposesAdapter.add(invoicePurpose);
                        }
                    }
                }
            });
        }

        product = selectedProductItem.getProduct();

        unitsAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_light, unitList);
        unitsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
        spUnits.setAdapter(unitsAdapter);
        spUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Double price = PriceTools.identifyRetailPrice(getHelper(), product,
                        salesBranch, salesCustomerGroup, salesCustomer, unitsAdapter.getItem(i));
                retailPrice = "P"+NumberTools.separateInCommas(price);
                subtotal = String.valueOf(NumberTools.toDouble(etQuantity.getText().toString()) * NumberTools.toDouble(retailPrice));
                tvSubtotal.setText("P"+NumberTools.separateInCommas(subtotal));

                if(etRetailPrice != null) {
                    if(ovPrice != null)
                        etRetailPrice.setText(ovPrice);
                    else
                        etRetailPrice.setText(NumberTools.formatDouble(price, ProductsAdapterHelper.getDecimalPlace()) + "");
                    ovPrice = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Double price = PriceTools.identifyRetailPrice(getHelper(), product,
                        salesBranch, salesCustomerGroup, salesCustomer, defaultUnit);
                retailPrice = "P"+NumberTools.separateInCommas(price);
                subtotal = String.valueOf(NumberTools.toDouble(etQuantity.getText().toString()) * NumberTools.toDouble(retailPrice));
                tvSubtotal.setText("P"+NumberTools.separateInCommas(subtotal));

                if(etRetailPrice != null) {
                    if (ovPrice != null)
                        etRetailPrice.setText(ovPrice);
                    else
                        etRetailPrice.setText(NumberTools.formatDouble(price, ProductsAdapterHelper.getDecimalPlace()) + "");
                    ovPrice = null;
                }
            }
        });

        if(canOverridePrice) {
            llRetailPrice = (LinearLayout) super.findViewById(R.id.llRetailPrice);
            etRetailPrice = (EditText) super.findViewById(R.id.etRetailPrice);
            llRetailPrice.setVisibility(View.VISIBLE);
            etRetailPrice.setSelectAllOnFocus(true);
            etRetailPrice.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    retailPrice = etRetailPrice.getText().toString();
                    onQuantityChanged.afterTextChanged(null); // update the subtotal
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }

        if (hasValues) {
            if (isMultiValue) {
                if (valuePosition > -1) {
                    Values value = selectedProductItem.getValues().get(valuePosition);
                    spUnits.setSelection(unitList.indexOf(value.getUnit() != null? value.getUnit() : defaultUnit));
                    if(hasBadStock)
                        swcBadStock.setChecked(value.isBadStock());
                    if(hasInvoicePurpose) {
                        date = value.getExpiry_date();
                        InvoicePurpose ip = value.getInvoicePurpose();
                        if(ip != null)
                            spInvoicePurpose.setSelection(invoicePurposeList.indexOf(ip));
                        else {
                            invP = new InvoicePurpose();
                            invP.setId(-1);
                            invP.setName("--");
                        }
                    }

                    if(canOverridePrice)
                        ovPrice = value.getUnit_retail_price()+"";
//                        etRetailPrice.setText();
                }
            } else {
                Values value = selectedProductItem.getValues().get(0);
                spUnits.setSelection(unitList.indexOf(value.getUnit() != null? value.getUnit() : defaultUnit));
                if(hasBadStock)
                    swcBadStock.setChecked(value.isBadStock());
                if(hasInvoicePurpose) {
                    date = value.getExpiry_date();
                    InvoicePurpose ip = value.getInvoicePurpose();
                    if(ip != null)
                        spInvoicePurpose.setSelection(invoicePurposeList.indexOf(ip));
                    else {
                        invP = new InvoicePurpose();
                        invP.setId(-1);
                        invP.setName("--");
                    }
                }
                if(canOverridePrice)
                    ovPrice = value.getUnit_retail_price()+"";
//                etRetailPrice.setText(value.getUnit_retail_price()+"");
            }
            if(hasExpiryDate) {
                if(date == null) {
                    Log.e("date", "isnull");
                    Calendar now = Calendar.getInstance();
                    date = now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1) + "-" + now.get(Calendar.DAY_OF_MONTH);
                }
                else
                    Log.e("date", "is not null="+date);
                date = DateTimeTools.convertToDate(date, "yyyy-M-d", "yyyy-MM-dd");
                btnExpiryDate.setText(date);
            }
        }

        if(getHelper() != null && (salesCustomer != null || salesCustomerGroup != null || salesBranch != null)) {
            if(forceSellableUnit) {
                try {
                    BranchProduct branchProduct = getHelper().fetchForeignCollection(product.getBranchProducts().closeableIterator(), new ImonggoDBHelper2.Conditional<BranchProduct>() {
                        @Override
                        public boolean validate(BranchProduct obj) {
                            if(obj.getUnit() == null)
                                return true;
                            if(product.getExtras().getDefault_selling_unit() != null && !product.getExtras().getDefault_selling_unit().equals(""))
                                return obj.getUnit().getId() == Integer.parseInt(product.getExtras().getDefault_selling_unit());
//                            return obj.getUnit() == null;
                            return false;
                        }
                    }, 0);
                    Unit unit = null;
                    if(branchProduct != null)
                        unit = branchProduct.getUnit();
                    Double retail_price = PriceTools.identifyRetailPrice(getHelper(), product, salesBranch, null, null, unit);

                    if(retail_price == null)
                        retail_price = product.getRetail_price();
                    Log.e("identified retail_price", retail_price.toString());
                    retailPrice = String.format("P%s", NumberTools.separateInCommas(retail_price));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else {
                defaultUnit = spUnits.getSelectedItem() instanceof Unit ? (Unit) spUnits.getSelectedItem() : null;
                if (product.getExtras() != null && defaultUnit == null) {
                    try {
                        defaultUnit = getHelper().fetchObjects(Unit.class).queryBuilder().where()
                                .eq("id", product.getExtras().getDefault_selling_unit()).queryForFirst();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                Log.e(getClass().getSimpleName(), "calling PriceTools.identifyRetailPrice");
                retailPrice = "P"+NumberTools.separateInCommas(PriceTools.identifyRetailPrice(getHelper(), product,
                        salesBranch, salesCustomerGroup, salesCustomer, (Unit) spUnits.getSelectedItem()));
            }

        }

        if(isUnitDisplay) {
            spUnits.setVisibility(View.GONE);
            tvUnit.setText(((Unit) spUnits.getSelectedItem()).getName());
            tvUnit.setVisibility(View.VISIBLE);
        }

        tvProductName.setText(product.getName());

        if (isMultiValue) {
            if (valuePosition > -1)
                etQuantity.setText(selectedProductItem.getQuantity(valuePosition));
            else
                etQuantity.setText("0");
        } else
            etQuantity.setText(selectedProductItem.getQuantity());

        if(hasStock) {
            Unit unit = Unit.fetchById(getHelper(), Unit.class, product.getExtras().getDefault_selling_unit());
            double invQuantity = Double.valueOf(product.getInStock());;
            String unitName = product.getBase_unit_name();
            if(product.getExtras() != null && product.getExtras().getDefault_selling_unit() != null && !product.getExtras().getDefault_selling_unit().isEmpty()) {
                if(unit != null) {
                    invQuantity = Double.valueOf(product.getInStock(unit.getQuantity(), ProductsAdapterHelper.getDecimalPlace()));
                    unitName = unit.getName();
                }
            }
            tvInStock.setText(String.format("In Stock: %.2f %s", invQuantity, unitName));
        }
        else
            tvInStock.setVisibility(View.GONE);

        if(hasPrice)
            tvRetailPrice.setText(retailPrice);
        else
            tvRetailPrice.setVisibility(View.GONE);

        subtotal = String.valueOf(NumberTools.toDouble(etQuantity.getText().toString()) * NumberTools.toDouble(retailPrice));
        tvSubtotal.setText("P"+NumberTools.separateInCommas(subtotal));

        llSubtotal.setVisibility(!hasSubtotal ? View.GONE : View.VISIBLE);

        btnSave.setOnClickListener(onSaveClicked);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etQuantity, InputMethodManager.SHOW_IMPLICIT);

                offsetSpinnerBelowv21(spUnits);

//                TimerTools.duration("Sales Quantity");
            }
        });
    }

    public void setRetailPrice(String retailPrice) {
        this.retailPrice = retailPrice;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }

    public void setForceSellableUnit(boolean forceSellableUnit) {
        this.forceSellableUnit = forceSellableUnit;
    }

    private boolean checkForValues() {
        String quantity = etQuantity.getText().toString().replace(",", "");

        boolean hasQty = !quantity.equals("") && Double.valueOf(quantity) != 0.0; // true
        boolean hasReturns = etOutright != null && !NumberTools.toBigDecimal(etOutright.getText().toString()).equals(BigDecimal.ZERO); // false
        boolean hasDiscrepancy = etDiscrepancy != null && !NumberTools.toBigDecimal(etDiscrepancy.getText().toString()).equals(BigDecimal.ZERO); // false

        return hasQty || hasReturns || hasDiscrepancy;
    }

    private View.OnClickListener onSaveClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String quantity = etQuantity.getText().toString().replace(",", "");

            if(quantity == null || quantity.length() == 0)
                quantity = "0";

            if (Double.valueOf(quantity) == 0.0 && !isMultiValue)
                selectedProductItem.removeAll(); // TODO handle this
            else if (isMultiValue && !checkForValues()) {
                if (multiQuantityDialogListener != null)
                    multiQuantityDialogListener.onSave(null);
                dismiss();
                return;
            } else {
                Log.e("sales quantity", "has value in anyway possible");

                Unit unit = hasUnits ? ((Unit) spUnits.getSelectedItem()) : null;
                Values values;
                if (valuePosition > -1)
                    values = selectedProductItem.getValues().get(valuePosition);
                else
                    values = new Values();

                Log.e("SALES_QUANTITY_DIALOG", unit != null? unit.getName() : "null");
                Log.e("SALES_QUANTITY_DIALOG", "is Helper NULL? " + (getHelper() == null));

                if(getHelper() == null)
                    values.setValue(quantity, unit);
                else {
                    Log.e("sales quantity", "Quantity is = "+quantity);
                    Price price = PriceTools.identifyPrice(getHelper(), selectedProductItem.getProduct(),
                            salesBranch, salesCustomerGroup, salesCustomer, unit);

                    Log.e("Simple Sales QTY Dialog", "VALUES PRICE : isNull? " + (price == null));
                    if(price != null) {
//                        price.setUnit(unit);
                        values.setValue(quantity, price, salesCustomer != null ? salesCustomer.getDiscount_text() : null);
                        values.setUnit(unit);
                        Log.e(">>>>>>", "Price is not null | unit = "+unit);
                    }
                    else if(canOverridePrice) {
                        Log.e(">>>>>>", "canOverridePrice = "+canOverridePrice+" | unit = "+unit);
                        values.setValue(quantity, unit, Double.valueOf(retailPrice));
                        values.setUnit(unit);
                    }
                    else {
                        if(salesBranch == null)
                            Log.e("sales branch", "shit its null");
                        else
                            Log.e("sales branch", "its not null shoot!");

                        values.setValue(quantity, unit, PriceTools.identifyRetailPrice(getHelper(), selectedProductItem.getProduct(),
                                salesBranch, salesCustomerGroup, salesCustomer, unit),
                                salesCustomer != null ? salesCustomer.getDiscount_text() : null);
                    }
                }
                Log.e("sales quantity", "VALUES QTY : " + values.getQuantity());
                if(hasExpiryDate) {
                    date = btnExpiryDate.getText().toString();
                }

                if(hasInvoicePurpose) {
                    InvoicePurpose invoicePurpose = (InvoicePurpose) spInvoicePurpose.getSelectedItem();
                    if(invoicePurpose.getId() == -1)
                        values.setInvoicePurpose(null);
                    else {
                        values.setInvoicePurpose(invoicePurpose);
                        if(invoicePurpose.getExtras().require_date())
                            values.setExpiry_date(date);
                    }
                }
                if(hasBadStock)
                    values.setBadStock(swcBadStock.isChecked());

                if(hasBrand) {
                    ExtendedAttributes extendedAttributes = new ExtendedAttributes();
                    extendedAttributes.setBrand(((String) spBrands.getSelectedItem()));

                    values.setExtendedAttributes(extendedAttributes);
                }

                ExtendedAttributes extendedAttributes = values.getExtendedAttributes();
                if(hasOutright) {
                    if (extendedAttributes == null)
                        extendedAttributes = new ExtendedAttributes();
                    extendedAttributes.setOutright_return(NumberTools.toBigDecimal(etOutright.getText().toString()).toString());
                }
                if(hasDiscrepancy || etDiscrepancy != null) {
                    if (extendedAttributes == null)
                        extendedAttributes = new ExtendedAttributes();
                    extendedAttributes.setDiscrepancy(NumberTools.toBigDecimal(etDiscrepancy.getText().toString()).toString());
                }
                values.setExtendedAttributes(extendedAttributes);

                if(canOverridePrice) {
                    values.setUnit_retail_price(Double.valueOf(etRetailPrice.getText().toString()));
                    values.setRetail_price(Double.valueOf(etRetailPrice.getText().toString()));
                }

                if (isMultiValue) {
                    if (multiQuantityDialogListener != null)
                        multiQuantityDialogListener.onSave(values);
                    dismiss();
                    return;
                } else
                    selectedProductItem.addValues(values);
            }

            if (quantityDialogListener != null)
                quantityDialogListener.onSave(selectedProductItem, listPosition);
            dismiss();
        }
    };

}
