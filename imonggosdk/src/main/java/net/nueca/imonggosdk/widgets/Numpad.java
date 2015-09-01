package net.nueca.imonggosdk.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.StringTokenizer;

/**
 * Created by gama on 5/16/15.
 *
 *  xmlns:numpad="http://schemas.android.com/apk/res-auto"
 */
public class Numpad extends LinearLayout implements View.OnClickListener {
    private ViewGroup keypadView, llControls;
    protected TextHolder mTextHolder;

    private String DEFAULT_TEXT = "0";
    private String FORMAT = "%,1.0f";
    private String PREFIX= "";
    private boolean BEGIN_FROM_DECIMAL;
    private int MAX_DECIMAL_LENGTH = 2;
    private int MAX_WHOLE_NUM_DIGIT = 12;
    //private int MAX_LENGTH = 16;
    private boolean isNegative = false;

    public static final int DEFAULT_KEYPAD_BUTTON_TEXTCOLOR = R.color.keypad_button_text;
    public static final int DEFAULT_KEYPAD_BUTTON_BG = R.drawable.keypad_button;
    public static final int DEFAULT_KEYPAD_BUTTON_BG_CIRCLE = R.drawable.keypad_button_circle;
    public static final int DEFAULT_KEYPAD_BG = R.color.keypad_default_bg;

    //private List<View> buttonList;
    private List<TextHolder> textViewList;

    protected Button button00, buttonDot;
    private List<Button> buttonNum;
    private ImageButton ibtnGo, ibtnNegative, ibtnBksp, ibtnMore;

    private boolean handleBackButton = false, showMoreButton = true, showNegativeButton = true,
            showGoButton = true, showControlButtons = true, alwaysShowDecimal = false;
    private int fontSize = 20;
    private ColorStateList fontColor, backgroundColor;
    private int buttonBgResource, goButtonBgResource;

    public Numpad(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keypadView = (ViewGroup) inflater.inflate(R.layout.imonggosdk_numpad, this, true);

        //if(!isInEditMode()) {
            //buttonList = new ArrayList<>();
            buttonNum = new ArrayList<>();

            //vsExtraButtons = (ViewStub)keypadView.findViewById(R.id.vsExtraButton);
            llControls = (ViewGroup) findViewById(R.id.llControls);

            ibtnMore = (ImageButton) findViewById(R.id.ibtnMore);
            ibtnGo = (ImageButton) findViewById(R.id.ibtnGo);
            ibtnNegative = (ImageButton) findViewById(R.id.ibtnNegative);
            ibtnBksp = (ImageButton) keypadView.findViewById(R.id.ibtnBksp);

            button00 = (Button) findViewById(R.id.button00);
            //buttonDash = findViewById(R.id.buttonDash);
            buttonDot = (Button) findViewById(R.id.buttonDot);

            final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Numpad);
            handleBackButton = typedArray.getBoolean(R.styleable.Numpad_handleBackButton, false);
            showMoreButton = typedArray.getBoolean(R.styleable.Numpad_showMoreButton, true);
            showNegativeButton = typedArray.getBoolean(R.styleable.Numpad_showNegativeButton, true);
            showGoButton = typedArray.getBoolean(R.styleable.Numpad_showGoButton, true);
            showControlButtons = typedArray.getBoolean(R.styleable.Numpad_showControlButtons, true);
            alwaysShowDecimal = typedArray.getBoolean(R.styleable.Numpad_alwaysShowDecimal, false);
            fontSize = typedArray.getInteger(R.styleable.Numpad_fontSize, 20);
            fontColor = typedArray.getColorStateList(R.styleable.Numpad_fontColor);
            buttonBgResource = typedArray.getResourceId(R.styleable.Numpad_buttonBgResource, DEFAULT_KEYPAD_BUTTON_BG);
            goButtonBgResource = typedArray.getResourceId(R.styleable.Numpad_goButtonBgResource, DEFAULT_KEYPAD_BUTTON_BG_CIRCLE);
            backgroundColor = typedArray.getColorStateList(R.styleable.Numpad_backgroundColor);

            /*if(goButtonBgResource == DEFAULT_KEYPAD_BUTTON_BG && Build.VERSION.SDK_INT >= 21) {
                TypedValue colorPrimary = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.colorPrimary,colorPrimary,true);
                if(colorPrimary != null)
                    goButtonBgResource = colorPrimary.resourceId;
            }*/

            if(fontColor == null)
                fontColor = getResources().getColorStateList(DEFAULT_KEYPAD_BUTTON_TEXTCOLOR);
            if(backgroundColor == null)
                backgroundColor = getResources().getColorStateList(DEFAULT_KEYPAD_BG);

            showMoreButton(showMoreButton);
            showNegativeButton(showNegativeButton);
            showGoButton(showGoButton);
            showControlButtons(showControlButtons);

            setAlwaysShowDecimal(alwaysShowDecimal);

        //if(!isInEditMode())
            initAppearance();
            if(!isInEditMode()) {
                initControlButtons();
                initInputButtons();
            }
        //}
    }

    private void initAppearance() {
        if(buttonNum.size() <= 0) {
            buttonNum.add((Button) findViewById(R.id.button0));
            buttonNum.add((Button) findViewById(R.id.button1));
            buttonNum.add((Button) findViewById(R.id.button2));
            buttonNum.add((Button) findViewById(R.id.button3));
            buttonNum.add((Button) findViewById(R.id.button4));
            buttonNum.add((Button) findViewById(R.id.button5));
            buttonNum.add((Button) findViewById(R.id.button6));
            buttonNum.add((Button) findViewById(R.id.button7));
            buttonNum.add((Button) findViewById(R.id.button8));
            buttonNum.add((Button) findViewById(R.id.button9));
        }

        for(int i=0; i<buttonNum.size(); i++) {
            buttonNum.get(i).setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            buttonNum.get(i).setTextColor(fontColor);
            buttonNum.get(i).setBackgroundResource(buttonBgResource);
        }

        button00.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        button00.setTextColor(fontColor);
        button00.setBackgroundResource(buttonBgResource);

        buttonDot.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        buttonDot.setTextColor(fontColor);
        buttonDot.setBackgroundResource(buttonBgResource);

        ibtnMore.setColorFilter(fontColor.getDefaultColor());
        ibtnMore.setBackgroundResource(buttonBgResource);

        ibtnBksp.setColorFilter(fontColor.getDefaultColor());
        ibtnBksp.setBackgroundResource(buttonBgResource);

        ibtnNegative.setColorFilter(fontColor.getDefaultColor());
        ibtnNegative.setBackgroundResource(buttonBgResource);

        ibtnGo.setColorFilter(fontColor.getDefaultColor());
        ibtnGo.setBackgroundResource(goButtonBgResource);

        keypadView.setBackgroundColor(backgroundColor.getDefaultColor());
    }

    private void setAlwaysShowDecimal(boolean alwaysShowDecimal) {
        BEGIN_FROM_DECIMAL = alwaysShowDecimal;
        if(alwaysShowDecimal) {
            FORMAT = "%,1.0" + MAX_DECIMAL_LENGTH + "f";
            DEFAULT_TEXT = String.format(FORMAT,0f);
            setEnableDot(false);
        }
        else {
            DEFAULT_TEXT = "0";
            FORMAT = "%,1.0f";
            setEnableDot(MAX_DECIMAL_LENGTH > 0);
        }
    }

    /*public void setMaxLength(int maxLength) {
        MAX_LENGTH = maxLength;
    }*/
    public void setDecimalLength(int decimalLength) {
        MAX_DECIMAL_LENGTH = decimalLength;
    }
    public void setWholeNumberDigitLength(int wholeNumLen) {
        MAX_WHOLE_NUM_DIGIT = wholeNumLen;
    }

    public void setEnable00(boolean isEnabled) {
        button00.setEnabled(isEnabled);
    }
    public void setEnableDot(boolean isEnabled) {
        buttonDot.setEnabled(isEnabled);
    }
    public void setAllowNegative(boolean allow) {
        ibtnNegative.setEnabled(allow);
    }

    public void changeBackgroundColor(int resource_keypadBG) {
        backgroundColor = getResources().getColorStateList(resource_keypadBG);
        if(backgroundColor != null)
            keypadView.setBackgroundColor(backgroundColor.getDefaultColor());
    }

    public void changeTextColorResource(int resource_textColor) {
        fontColor = getResources().getColorStateList(resource_textColor);
        initAppearance();
    }

    public void changeButtonBackgroundResource(int resource_buttonBG) {
        buttonBgResource = resource_buttonBG;
        initAppearance();
    }
    public void changeGoBackgroundDrawable(int resource_buttonGoBG) {
        goButtonBgResource = resource_buttonGoBG;
        ibtnGo.setBackgroundResource(goButtonBgResource);
    }

    private void changeColor(int resource_textColor, int resource_buttonBG,
                             int resource_buttonGoBG, int resource_keypadBG) {
        changeBackgroundColor(resource_keypadBG);
        changeGoBackgroundDrawable(resource_buttonGoBG);

        fontColor = getResources().getColorStateList(resource_textColor);
        buttonBgResource = resource_buttonBG;
        initAppearance();
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

    private void initControlButtons() {
        setAsBackspaceButton(ibtnBksp);
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
    private void initInputButtons() {
        for(int i=0; i<buttonNum.size(); i++) {
            buttonNum.get(i).setOnClickListener(getButtonTextClickListener);
        }
        buttonDot.setOnClickListener(getButtonTextClickListener);
        button00.setOnClickListener(getButtonTextClickListener);
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

    /*private void buttonFinder(ViewGroup viewGroup, boolean isDefaultButtonHolder) {
        for(int i=0; i<viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if(child instanceof ViewGroup) {
                buttonFinder((ViewGroup) child, isDefaultButtonHolder || child.getId() == R.id.tlContainer);
            }
            else if(child instanceof Button || child instanceof ImageButton) {
                if(isDefaultButtonHolder || child.getId() == R.id.ibtnMore) //{
                    if (child instanceof Button) {
                        child.setOnClickListener(getButtonTextClickListener);
                        ((Button) child).setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                    }
                buttonList.add(child);
            }
        }
    }*/

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

    protected void doWrite(String str) {
        String unparsed = mTextHolder.getTextView().getText().toString();
        unparsed = unparsed.replaceAll("[^0-9.,]", "");

        if(BEGIN_FROM_DECIMAL) {
            String parsed = unparsed.replaceAll("[^0-9]","");
            for(int i=0; i<str.length();i++) {
                countDigits(parsed);
                if(CURRENT_WHOLE_DIGIT+CURRENT_DECIMAL == MAX_WHOLE_NUM_DIGIT+MAX_DECIMAL_LENGTH)
                    break;
                parsed += str.charAt(i);
            }
            BigDecimal number = BigDecimal.ZERO;
            if(parsed.length() > 0)
                number = new BigDecimal(parsed);
            String divisor = String.format("%1$-" + (MAX_DECIMAL_LENGTH +1) + "s", "1").replaceAll("[^0-9]","0");
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
                if(numStr.replaceAll("[0.,]","").length() > 0) // check for non-zeroes
                    numStr = (isNegative?"-":"") + numStr;
                mTextHolder.getTextView().setText(PREFIX + numStr);
            }
            else { // str not decimal point
                for(int i=0; i<str.length();i++) {
                    countDigits(unparsed);
                    if(CURRENT_WHOLE_DIGIT == MAX_WHOLE_NUM_DIGIT) {
                        if(unparsed.contains(".") && CURRENT_DECIMAL == MAX_DECIMAL_LENGTH)
                            break;
                        else if(!unparsed.contains("."))
                            break;
                    }
                    unparsed += str.charAt(i);
                }
                parsed = unparsed.replaceAll("[^0-9.]", "");

                String decimalNum = "";
                if(parsed.contains(".")) {
                    decimalNum = parsed.substring(parsed.indexOf("."));
                    if(decimalNum.length() > MAX_DECIMAL_LENGTH +1)
                        decimalNum = decimalNum.substring(0, MAX_DECIMAL_LENGTH +1);
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
    protected void doBackspace() {
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
            String divisor = String.format("%1$-" + (MAX_DECIMAL_LENGTH +1) + "s", "1").replaceAll("[^0-9]","0");
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
                if(decimalNum.length() > MAX_DECIMAL_LENGTH +1)
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
        countDigits();
    }
    protected void doClear() {
        mTextHolder.getTextView().setText(PREFIX + DEFAULT_TEXT);
        CURRENT_WHOLE_DIGIT = 0;
        CURRENT_DECIMAL = 0;
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

    public TextHolder getTextHolderWithTag(String tag) {
        for(int i=0 ;i< textViewList.size(); i++) {
            if(textViewList.get(i).getTag().equals(tag))
                return textViewList.get(i);
        }
        return null;
    }
    public View getTextHolderViewWithTag(String tag) {
        for(int i=0 ;i< textViewList.size(); i++) {
            if(textViewList.get(i).getTag().equals(tag))
                return textViewList.get(i).mTextHolder;
        }
        return null;
    }

    public void addTextHolder(TextView view, String tag, boolean alwaysShowDecimal, boolean allowNegative,
                              @Nullable TextHolderHelper listener) {
        addTextHolder(new TextHolder(view, tag, listener, alwaysShowDecimal, allowNegative));
    }
    public void addTextHolder(EditText view, String tag, boolean alwaysShowDecimal, boolean allowNegative,
                              @Nullable TextHolderHelper listener) {
        addTextHolder(new TextHolder(view, tag, listener, alwaysShowDecimal, allowNegative));
    }
    public void addTextHolder(TextView view, String tag, boolean alwaysShowDecimal, int wholeNumDigitCount,
                              int decimalPlace, boolean allowNegative, @Nullable TextHolderHelper listener) {
        addTextHolder(new TextHolder(view, tag, listener, alwaysShowDecimal, wholeNumDigitCount, decimalPlace,
                allowNegative));
    }
    public void addTextHolder(EditText view, String tag, boolean alwaysShowDecimal, int wholeNumDigitCount,
                              int decimalPlace, boolean allowNegative, @Nullable TextHolderHelper listener) {
        addTextHolder(new TextHolder(view, tag, listener, alwaysShowDecimal, wholeNumDigitCount, decimalPlace,
                allowNegative));
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

                    setWholeNumberDigitLength(textHolder.getDigitCount());
                    setDecimalLength(textHolder.getDecimalPlace());
                    setAlwaysShowDecimal(textHolder.alwaysShowDecimal);
                    setAllowNegative(textHolder.allowNegative);
                    setEnableDot(textHolder.enableDot);

                    if(textHolder.getTextView().getText().toString().contains("-") && textHolder.allowNegative) {
                        ibtnNegative.callOnClick();
                    }
                    doWrite("");

                    if (textHolder.helper != null) {
                        textHolder.helper.hasFocus(view, true);
                    }

                    countDigits();
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

    public class TextHolder {
        public View mTextHolder;
        public TextHolderHelper helper;
        boolean alwaysShowDecimal;
        private Object TAG;
        private int mDecimalPlace;
        private int mDigitCount;
        private boolean allowNegative = false;
        private boolean enableDot;

        public TextHolder(View view, String tag, TextHolderHelper helper, boolean alwaysShowDecimal, boolean allowNegative)
        {
            this.mTextHolder = view;
            setTag(tag);
            this.helper = helper;
            this.alwaysShowDecimal = alwaysShowDecimal;
            this.mDecimalPlace = 2;
            this.mDigitCount = 12;
            this.allowNegative = allowNegative;
            enableDot = !alwaysShowDecimal;
        }
        public TextHolder(View view, String tag, TextHolderHelper helper, boolean alwaysShowDecimal, int digitCount,
                          int decimalPlace, boolean allowNegative) {
            this.mTextHolder = view;
            setTag(tag);
            this.helper = helper;
            this.alwaysShowDecimal = alwaysShowDecimal;
            this.mDecimalPlace = decimalPlace;
            this.mDigitCount = digitCount;
            this.allowNegative = allowNegative;
            enableDot = !alwaysShowDecimal;
        }

        public TextView getTextView() { return (TextView)mTextHolder; }
        public Object getTag() { return TAG; }
        public void setTag(Object o) { TAG = o; }
        public void setDecimalPlace(int length) { mDecimalPlace = length; }
        public int getDecimalPlace() { return mDecimalPlace; }
        public int getDigitCount() { return mDigitCount; }
        public void setEnableDot(boolean shouldEnable) {
            enableDot = shouldEnable;
        }

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

    private int CURRENT_WHOLE_DIGIT = 0, CURRENT_DECIMAL = 0;
    private void countDigits() {
        TextView textHolder = mTextHolder.getTextView();
        String current_text = textHolder.getText().toString();
        countDigits(current_text);
    }
    private void countDigits(String str) {
        Log.e("str", str);
        if(str.contains(".")) {
            StringTokenizer tokens = new StringTokenizer(str,".");
            Log.e("tokens",tokens.countTokens()+"");

            CURRENT_WHOLE_DIGIT = tokens.nextToken().replaceAll("[^0-9]", "").length();
            if(tokens.hasMoreTokens())
                CURRENT_DECIMAL = tokens.nextToken().length();
            else
                CURRENT_DECIMAL = 0;
        }
        else {
            CURRENT_WHOLE_DIGIT = str.replaceAll("[^0-9]","").length();
            CURRENT_DECIMAL = 0;
        }
        Log.e("DIGIT",CURRENT_WHOLE_DIGIT + " " + CURRENT_DECIMAL);
    }
}
