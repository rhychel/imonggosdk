package net.nueca.imonggosdk.widgets;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by gama on 9/1/15.
 */
public class ModifiedNumpad extends Numpad {
    public ModifiedNumpad(Context context, AttributeSet attrs) {
        super(context, attrs);
        button00.setText("-");
        buttonDot.setVisibility(INVISIBLE);
    }

    @Override
    protected void doWrite(String str) {
        String text = mTextHolder.getTextView().getText().toString();
        text += str;
        mTextHolder.getTextView().setText(text);
    }

    @Override
    protected void doBackspace() {
        String text = mTextHolder.getTextView().getText().toString();
        if(text.isEmpty())
            return;
        text = text.substring(0, text.length()-1);
        mTextHolder.getTextView().setText(text);
    }

    @Override
    protected void doClear() {
        mTextHolder.getTextView().setText("");
    }
}
