package net.nueca.imonggosdk.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import net.nueca.imonggosdk.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 5/26/15.
 *
 * TODO : More button
 */

public class KeypadDialog extends AppCompatDialog {
    private ImageButton ibtnMore, ibtnCancel, ibtnGo, ibtnBksp;
    private TextView mTextHolder;
    private ViewGroup tlButtonArea;

    private String DEFAULT_TEXT = "0";
    private String FORMAT = "%,1.0f";
    private int MAX_LENGTH = 16;
    private String PREFIX= "";
    private boolean BEGIN_FROM_DECIMAL;

    private List<View> buttonList;

    private Button buttonDot;

    public KeypadDialog(Context context, boolean cancelable, OnCancelListener onCancelListener, boolean requireDecimal) {
        super(context, cancelable, onCancelListener);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.imonggosdk_keypad_dialog);

        mTextHolder = (TextView) findViewById(R.id.tvTextHolder);

        ibtnMore = (ImageButton) findViewById(R.id.ibtnMore);
        ibtnGo = (ImageButton) findViewById(R.id.ibtnGo);

        ibtnBksp = (ImageButton) findViewById(R.id.ibtnBksp);
        ibtnBksp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBackspace();
            }
        });
        ibtnBksp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                doClear();
                return true;
            }
        });

        ibtnCancel = (ImageButton) findViewById(R.id.ibtnCancel);
        ibtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        buttonDot = (Button) findViewById(R.id.buttonDot);

        tlButtonArea = (ViewGroup) findViewById(R.id.tlButtonArea);
        initButtons(tlButtonArea);

        BEGIN_FROM_DECIMAL = requireDecimal;
        if(requireDecimal) {
            DEFAULT_TEXT = "0.00";
            FORMAT = "%,1.02f";
            buttonDot.setEnabled(false);
        }
        else {
            DEFAULT_TEXT = "0";
            FORMAT = "%,1.0f";
            buttonDot.setEnabled(true);
        }
        doClear();
    }

    private void initButtons(ViewGroup viewGroup) {
        if(buttonList == null)
            buttonList = new ArrayList<>();

        for(int i=0; i<viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if(child instanceof ViewGroup) {
                initButtons((ViewGroup) child);
            }
            else if(child instanceof Button || child instanceof ImageButton) {
                if (child instanceof Button)
                        child.setOnClickListener(getButtonTextClickListener);
                buttonList.add(child);
            }
        }
    }
    private View.OnClickListener getButtonTextClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view instanceof Button) {
                Button button = (Button) view;
                String btnTxt = button.getText().toString();
                doWrite(btnTxt);
            }
        }
    };

    private void doWrite(String str) {
        String unparsed = mTextHolder.getText().toString();
        unparsed = unparsed.replaceAll("[^0-9.,]", "");
        if(unparsed.length() >= MAX_LENGTH)
            return;

        if(BEGIN_FROM_DECIMAL) {
            String parsed = unparsed.replaceAll("[^0-9]","");
            parsed += str;
            BigDecimal number = new BigDecimal(parsed);
            number = number.divide(new BigDecimal("100"));
            mTextHolder.setText(PREFIX + String.format(FORMAT, number));
        }
        else {
            unparsed = unparsed.replaceAll("[^0-9.,]", "");
            String parsed = unparsed.replaceAll("[^0-9]", "");

            if(str.equals(".")) {
                if (unparsed.contains("."))
                    return;

                BigDecimal number = new BigDecimal(parsed);
                mTextHolder.setText(PREFIX + String.format(FORMAT, number) + ".");
            }
            else { // str not decimal point
                for(int i=0; i<str.length();i++) {
                    if(unparsed.length() > MAX_LENGTH)
                        break;
                    unparsed += str.charAt(i);
                }
                parsed = unparsed.replaceAll("[^0-9.]", "");

                String decimalNum = "";
                if(parsed.contains(".")) {
                    decimalNum = parsed.substring(parsed.indexOf("."));
                    if(decimalNum.length() > 3)
                        decimalNum = decimalNum.substring(0, 3);
                    parsed = parsed.substring(0, parsed.indexOf("."));
                }
                BigDecimal number = new BigDecimal(parsed);
                mTextHolder.setText(PREFIX + String.format(FORMAT, number) + decimalNum);
            }
        }
    }
    private void doBackspace() {
        String unparsed = mTextHolder.getText().toString();
        if(unparsed.equals(DEFAULT_TEXT))
            return;

        if(BEGIN_FROM_DECIMAL) {
            String parsed = unparsed.replaceAll("[^0-9]","");
            if(parsed.length() <= 1) {
                doClear();
                return;
            }
            parsed = parsed.substring(0, parsed.length()-1);

            BigDecimal number = new BigDecimal(parsed);
            number = number.divide(new BigDecimal("100"));
            mTextHolder.setText(PREFIX + String.format(FORMAT, number));
        }
        else {
            String parsed = unparsed.replaceAll("[^0-9.]", "");
            if(parsed.length() <= 1) {
                doClear();
                return;
            }
            parsed = parsed.substring(0, parsed.length()-1);

            String decimalNum = "";
            if(parsed.contains(".")) {
                decimalNum = parsed.substring(parsed.indexOf("."));
                if(decimalNum.length() > 3)
                    return;
                parsed = parsed.substring(0, parsed.indexOf("."));
            }
            BigDecimal number = new BigDecimal(parsed);
            mTextHolder.setText(PREFIX + String.format(FORMAT, number) + decimalNum);
        }
    }

    private void doClear() {
        mTextHolder.setText(PREFIX + DEFAULT_TEXT);
    }
}
