package net.nueca.imonggosdk.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nueca.imonggosdk.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 5/16/15.
 *
 *  xmlns:numpad="http://schemas.android.com/apk/res-auto"
 */
public class Numpad extends LinearLayout implements View.OnClickListener {
    private ViewGroup keypadView, llControls;
    private TextHolder mTextHolder;

    private String DEFAULT_TEXT = "0";
    private String FORMAT = "%,1.0f";
    private int MAX_LENGTH = 16;
    private String PREFIX= "";
    private boolean BEGIN_FROM_DECIMAL;
    private int DECIMAL_LENGTH = 2;
    private boolean isNegative = false;

    public static final int DEFAULT_KEYPAD_BUTTON_TEXTCOLOR = R.color.keypad_button_text;
    public static final int DEFAULT_KEYPAD_BUTTON_BG = R.drawable.keypad_button;
    public static final int DEFAULT_KEYPAD_BUTTON_BG_CIRCLE = R.drawable.keypad_button_circle;
    public static final int DEFAULT_KEYPAD_BG = R.color.keypad_default_bg;

    private List<View> buttonList;
    private List<Button> extraButtonList;
    private List<TextHolder> textViewList;

    private View button00, buttonDot, ibtnMore;
    private ImageButton ibtnGo, ibtnNegative;

    private int buttonTextColor,buttonBackground, keypadBackground, goButtonBackground;
    private boolean handleBackButton = false, showMoreButton = true, showNegativeButton = true, showGoButton = true, showControlButtons = true, requireDecimal = false;

    public Numpad(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keypadView = (ViewGroup) inflater.inflate(R.layout.imonggosdk_numpad, this, true);

        if(!isInEditMode()) {
            buttonList = new ArrayList<>();
            keypadView.setVisibility(GONE);
            //vsExtraButtons = (ViewStub)keypadView.findViewById(R.id.vsExtraButton);
            llControls = (ViewGroup) findViewById(R.id.llControls);

            ibtnMore = findViewById(R.id.ibtnMore);
            ibtnGo = (ImageButton) findViewById(R.id.ibtnGo);
            ibtnNegative = (ImageButton) findViewById(R.id.ibtnNegative);

            button00 = findViewById(R.id.button00);
            //buttonDash = findViewById(R.id.buttonDash);
            buttonDot = findViewById(R.id.buttonDot);

            final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Numpad);
            handleBackButton = typedArray.getBoolean(R.styleable.Numpad_handleBackButton, false);
            showMoreButton = typedArray.getBoolean(R.styleable.Numpad_showMoreButton, true);
            showNegativeButton = typedArray.getBoolean(R.styleable.Numpad_showNegativeButton, true);
            showGoButton = typedArray.getBoolean(R.styleable.Numpad_showGoButton, true);
            showControlButtons = typedArray.getBoolean(R.styleable.Numpad_showControlButtons, true);
            requireDecimal = typedArray.getBoolean(R.styleable.Numpad_requireDecimal, false);

            initButtons();

            showMoreButton(showMoreButton);
            showNegativeButton(showNegativeButton);
            showGoButton(showGoButton);
            showControlButtons(showControlButtons);

            changeColor(DEFAULT_KEYPAD_BUTTON_TEXTCOLOR,
                    DEFAULT_KEYPAD_BUTTON_BG,
                    DEFAULT_KEYPAD_BUTTON_BG_CIRCLE,
                    DEFAULT_KEYPAD_BG);

            setRequireDecimal(requireDecimal);
        }
    }
    private void setRequireDecimal(boolean requireDecimal) {
        BEGIN_FROM_DECIMAL = requireDecimal;
        if(requireDecimal) {
            FORMAT = "%,1.0" + DECIMAL_LENGTH + "f";
            DEFAULT_TEXT = String.format(FORMAT,0f);
            setEnableDot(false);
        }
        else {
            DEFAULT_TEXT = "0";
            FORMAT = "%,1.0f";
            setEnableDot(DECIMAL_LENGTH > 0);
        }
    }

    public void setMaxLength(int maxLength) {
        MAX_LENGTH = maxLength;
    }
    public void setDecimalLength(int decimalLength) {
        DECIMAL_LENGTH = decimalLength;
    }

    public void setEnable00(boolean isEnabled) {
        button00.setEnabled(isEnabled);
        //button00.setAlpha(isEnabled? 1f : 0.75f);
    }
    public void setEnableDot(boolean isEnabled) {
        buttonDot.setEnabled(isEnabled);
        //button00.setAlpha(isEnabled? 1f : 0.75f);
    }
    public void setAllowNegative(boolean allow) {
        ibtnNegative.setEnabled(allow);
        //button00.setAlpha(isEnabled? 1f : 0.75f);
    }

    public void changeBackgroundDrawable(int resource_keypadBG) {
        keypadBackground = resource_keypadBG;
    }
    public void changeBackgroundColor(int resource_keypadBG) {
        keypadBackground = resource_keypadBG;
    }

    public void changeTextColorResource(int resource_textColor) {
        buttonTextColor = resource_textColor;
        changeTextColor();
    }

    public void changeButtonBackgroundDrawable(int resource_buttonBG) {
        buttonBackground = resource_buttonBG;
        changeBackgroundDrawable();
    }
    public void changeButtonBackgroundColor(int resource_buttonBG) {
        buttonBackground = resource_buttonBG;
        changeBackgroundColor();
    }
    public void changeGoBackgroundDrawable(int resource_buttonGoBG) {
        goButtonBackground = resource_buttonGoBG;
        ibtnGo.setBackgroundResource(goButtonBackground);
    }
    public void changeGoBackgroundColor(int resource_buttonGoBG) {
        goButtonBackground = resource_buttonGoBG;
        ibtnGo.setBackgroundColor(goButtonBackground);
    }

    private void changeColor(int resource_textColor, int resource_buttonBG,
                             int resource_buttonGoBG, int resource_keypadBG) {
        buttonTextColor = resource_textColor;
        buttonBackground = resource_buttonBG;
        keypadBackground = resource_keypadBG;
        goButtonBackground = resource_buttonGoBG;

        Log.e("changeColor","called");
        changeBackgroundDrawable();
        ibtnGo.setBackgroundResource(goButtonBackground);
    }

    private void colorize(Button button) {
        button.setTextColor(getResources().getColorStateList(buttonTextColor));
        button.setBackgroundResource(buttonBackground);
    }
    private void changeTextColor() {
        for(int i=0; i<buttonList.size(); i++) {
            View child = buttonList.get(i);
            if(child instanceof Button) {
                ((Button) child).setTextColor(getResources().getColorStateList(buttonTextColor));
            }
            else if(child instanceof ImageButton) {
                ColorStateList stateList = getResources().getColorStateList(buttonTextColor);
                if(stateList != null)
                    ((ImageButton) child).setColorFilter(stateList.getDefaultColor());
            }
        }
    }

    private void changeBackgroundDrawable() {
        for(int i=0; i<buttonList.size(); i++) {
            View child = buttonList.get(i);
            if(child.getId() == R.id.ibtnGo)
                continue;
            //child.setBackground(getResources().getDrawable(goButtonBackground));
            child.setBackgroundResource(buttonBackground);
        }
    }
    private void changeBackgroundColor() {
        for(int i=0; i<buttonList.size(); i++) {
            View child = buttonList.get(i);
            if(child.getId() == R.id.ibtnGo)
                continue;
            //child.setBackgroundColor(goButtonBackground);
            child.setBackgroundColor(buttonBackground);
        }
    }
    public void showControlButtons(boolean shouldShow) {
        llControls.setVisibility(shouldShow? VISIBLE : GONE);
    }
    public void showMoreButton(boolean shouldShow) {
        ibtnMore.setVisibility(shouldShow? VISIBLE : GONE);
    }
    public void showNegativeButton(boolean shouldShow) {
        ibtnNegative.setVisibility(shouldShow? VISIBLE : GONE);
    }
    public void showGoButton(boolean shouldShow) {
        ibtnGo.setVisibility(shouldShow? VISIBLE : GONE);
    }

    private void setTextHolder(TextHolder textHolder) {
        mTextHolder = textHolder;
    }

    private void initButtons() {
        buttonFinder(keypadView, false);
        setAsBackspaceButton(keypadView.findViewById(R.id.ibtnBksp));
        ibtnMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mTextHolder.helper != null)
                    mTextHolder.helper.onMoreButtonClicked();
            }
        });
        ibtnGo.setOnClickListener(goClickListener);
        ibtnNegative.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleNegative();
                doWrite("");
            }
        });
    }
    private void toggleNegative() {
        isNegative = !isNegative;
        ibtnNegative.setImageResource(isNegative?
                        R.drawable.ic_negative_scale : R.drawable.ic_positive_scale
        );
    }

    public void setAsBackspaceButton(View view) {
        view.setOnClickListener(backspaceClickListener);
        view.setOnLongClickListener(backspaceLongClickListener);
    }

    private void buttonFinder(ViewGroup viewGroup, boolean isDefaultButtonHolder) {
        for(int i=0; i<viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if(child instanceof ViewGroup) {
                buttonFinder((ViewGroup) child, isDefaultButtonHolder || child.getId() == R.id.tlContainer);
            }
            else if(child instanceof Button || child instanceof ImageButton) {
                if(isDefaultButtonHolder || child.getId() == R.id.ibtnMore) //{
                    if (child instanceof Button)
                        child.setOnClickListener(getButtonTextClickListener);
                buttonList.add(child);
            }
        }
    }

    public final View.OnClickListener getButtonTextClickListener =  new View.OnClickListener() {
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
        String unparsed = mTextHolder.getTextView().getText().toString();
        unparsed = unparsed.replaceAll("[^0-9.,]", "");
        if(unparsed.length() >= MAX_LENGTH && str.length() > 0)
            return;

        if(BEGIN_FROM_DECIMAL) {
            String parsed = unparsed.replaceAll("[^0-9]","");
            parsed += str;
            BigDecimal number = BigDecimal.ZERO;
            if(parsed.length() > 0)
                number = new BigDecimal(parsed);
            String divisor = String.format("%1$-" + (DECIMAL_LENGTH+1) + "s", "1").replaceAll("[^0-9]","0");
            number = number.divide(new BigDecimal(divisor));

            String numStr = String.format(FORMAT, number);
            if(numStr.replaceAll("[0.,]","").length() > 0)
                numStr = (isNegative?"-":"") + numStr;
            mTextHolder.getTextView().setText(PREFIX + numStr);
        }
        else {
            unparsed = unparsed.replaceAll("[^0-9.,]", "");
            String parsed = unparsed.replaceAll("[^0-9]", "");

            if(str.equals(".")) {
                if (unparsed.contains("."))
                    return;

                BigDecimal number = BigDecimal.ZERO;
                if(parsed.length() > 0)
                    number = new BigDecimal(parsed);

                String numStr = String.format(FORMAT, number) + ".";
                if(numStr.replaceAll("[0.,]","").length() > 0)
                    numStr = (isNegative?"-":"") + numStr;
                mTextHolder.getTextView().setText(PREFIX + numStr);
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
                    if(decimalNum.length() > DECIMAL_LENGTH+1)
                        decimalNum = decimalNum.substring(0, DECIMAL_LENGTH+1);
                    parsed = parsed.substring(0, parsed.indexOf("."));
                }
                BigDecimal number = BigDecimal.ZERO;
                if(parsed.length() > 0)
                    number = new BigDecimal(parsed);

                String numStr = String.format(FORMAT, number) + decimalNum;
                if(numStr.replaceAll("[0.,]","").length() > 0)
                    numStr = (isNegative?"-":"") + numStr;
                mTextHolder.getTextView().setText(PREFIX + numStr);
            }
        }
    }
    private void doBackspace() {
        String unparsed = mTextHolder.getTextView().getText().toString();
        if(unparsed.equals(DEFAULT_TEXT))
            return;

        if(BEGIN_FROM_DECIMAL) {
            String parsed = unparsed.replaceAll("[^0-9]","");
            if(parsed.length() <= 1) {
                doClear();
                return;
            }
            parsed = parsed.substring(0, parsed.length()-1);

            BigDecimal number = BigDecimal.ZERO;
            if(parsed.length() > 0)
                number = new BigDecimal(parsed);
            String divisor = String.format("%1$-" + (DECIMAL_LENGTH+1) + "s", "1").replaceAll("[^0-9]","0");
            number = number.divide(new BigDecimal(divisor));

            String numStr = String.format(FORMAT, number);
            if(numStr.replaceAll("[0.,]","").length() > 0)
                numStr = (isNegative?"-":"") + numStr;
            mTextHolder.getTextView().setText(PREFIX + numStr);
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
                if(decimalNum.length() > DECIMAL_LENGTH+1)
                    return;
                parsed = parsed.substring(0, parsed.indexOf("."));
            }
            BigDecimal number = BigDecimal.ZERO;
            if(parsed.length() > 0)
                number = new BigDecimal(parsed);

            String numStr = String.format(FORMAT, number) + decimalNum;
            if(numStr.replaceAll("[0.,]","").length() > 0)
                numStr = (isNegative?"-":"") + numStr;
            mTextHolder.getTextView().setText(PREFIX + numStr);
        }
    }
    private void doClear() {
        mTextHolder.getTextView().setText(PREFIX + DEFAULT_TEXT);
    }
    private OnClickListener backspaceClickListener =  new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(getVisibility() != VISIBLE)
                return;
            //view.startAnimation(ANIM_CLICK);
            doBackspace();
        }
    };
    private OnLongClickListener backspaceLongClickListener =  new OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if(getVisibility() != VISIBLE)
                return true;
            //view.startAnimation(ANIM_CLICK);
            doClear();
            return true;
        }
    };
    private OnClickListener goClickListener =  new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(getVisibility() != VISIBLE)
                return;
            //hide();
            if(mTextHolder.helper != null) {
                mTextHolder.helper.onConfirmClick(mTextHolder.getTextView().getText().toString());
            }
        }
    };

    @Override
    public void onClick(View view) {
        if(getVisibility() != VISIBLE)
            show();
    }

    public View getTextHolderWithTag(String tag) {
        for(int i=0 ;i< textViewList.size(); i++) {
            if(textViewList.get(i).getTag().equals(tag))
                return textViewList.get(i).mTextHolder;
        }
        return null;
    }

    public void addTextHolder(TextView view, String tag, boolean requireDecimal, boolean allowNegative,
                              @Nullable TextHolderHelper listener) {
        addTextHolder(new TextHolder(view, tag, listener, requireDecimal, allowNegative));
    }
    public void addTextHolder(EditText view, String tag, boolean requireDecimal, boolean allowNegative,
                              @Nullable TextHolderHelper listener) {
        addTextHolder(new TextHolder(view, tag, listener, requireDecimal, allowNegative));
    }
    public void addTextHolder(TextView view, String tag, boolean requireDecimal, int decimalPlace,
                              boolean allowNegative, @Nullable TextHolderHelper listener) {
        addTextHolder(new TextHolder(view, tag, listener, requireDecimal, decimalPlace, allowNegative));
    }
    public void addTextHolder(EditText view, String tag, boolean requireDecimal, int decimalPlace,
                              boolean allowNegative, @Nullable TextHolderHelper listener) {
        addTextHolder(new TextHolder(view, tag, listener, requireDecimal, decimalPlace, allowNegative));
    }

    private void addTextHolder(final TextHolder textHolder) {
        if(textViewList == null)
            textViewList = new ArrayList<>();

        View view = textHolder.mTextHolder;

        if(view instanceof EditText) {
            ((EditText) view).setInputType(InputType.TYPE_NULL);
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        if(handleBackButton)
                            if(getVisibility() == VISIBLE) {
                                hide();
                                return true;
                            }
                    }
                    return false;
                }
            });
        }
        view.setOnClickListener(this);

        textViewList.add(textHolder);
        view.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    if (getVisibility() != VISIBLE)
                        show();
                    setTextHolder(textHolder);

                    if(isNegative)
                        toggleNegative();

                    setDecimalLength(textHolder.getDecimalPlace());
                    setRequireDecimal(textHolder.requireDecimal);
                    setAllowNegative(textHolder.allowNegative);

                    if(textHolder.getTextView().getText().toString().contains("-")) {
                        ibtnNegative.callOnClick();
                    }
                    doWrite("");

                    if (textHolder.helper != null) {
                        textHolder.helper.hasFocus(view, true);
                    }
                }
            }
        });
    }

    public void hide() {
        setVisibility(GONE);
    }
    public void show() {
        setVisibility(VISIBLE);
    }

    private int toDP(int dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public class TextHolder {
        public View mTextHolder;
        public TextHolderHelper helper;
        boolean requireDecimal;
        private Object TAG;
        private int mDecimalPlace;
        private boolean allowNegative = false;

        public TextHolder(View view, String tag, TextHolderHelper helper, boolean requireDecimal, boolean allowNegative)
        {
            this.mTextHolder = view;
            setTag(tag);
            this.helper = helper;
            this.requireDecimal = requireDecimal;
            this.mDecimalPlace = 2;
            this.allowNegative = allowNegative;
        }
        public TextHolder(View view, String tag, TextHolderHelper helper, boolean requireDecimal, int decimalPlace,
                          boolean allowNegative) {
            this.mTextHolder = view;
            setTag(tag);
            this.helper = helper;
            this.requireDecimal = requireDecimal;
            this.mDecimalPlace = decimalPlace;
            this.allowNegative = allowNegative;
        }

        public TextView getTextView() { return (TextView)mTextHolder; }
        public Object getTag() { return TAG; }
        public void setTag(Object o) { TAG = o; }
        public void setDecimalPlace(int length) { mDecimalPlace = length; }
        public int getDecimalPlace() { return mDecimalPlace; }

        @Override
        public boolean equals(Object o) {
            if(!o.getClass().equals(TextHolder.class))
                return false;
            TextHolder th = (TextHolder) o;
            return th.mTextHolder.equals(this.mTextHolder);
        }
    }
    public interface TextHolderHelper {
        void hasFocus(View focused, boolean hasFocus);
        void onConfirmClick(String text);
        void onMoreButtonClicked();
    }
}
