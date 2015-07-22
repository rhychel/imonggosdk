package net.nueca.imonggosdk.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
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
 */
public class Keypad extends DrawerLayout implements View.OnClickListener {
    private ViewGroup keypadView;
    private TextHolder mTextHolder;
    private DrawerLayout drawer_layout;
    private View drawer;

    private String DEFAULT_TEXT = "0";
    private String FORMAT = "%,1.0f";
    private int MAX_LENGTH = 16;
    private String PREFIX= "";
    private boolean BEGIN_FROM_DECIMAL;
    private int DECIMAL_LENGTH = 2;
    private boolean isNegative = false, disableAnimation = false, handleBackbutton = true, hasOkayButton = true;

    public static final int DEFAULT_KEYPAD_BUTTON_TEXTCOLOR = R.color.keypad_button_text;
    public static final int DEFAULT_KEYPAD_BUTTON_BG = R.drawable.keypad_button;
    public static final int DEFAULT_KEYPAD_BUTTON_BG_CIRCLE = R.drawable.keypad_button_circle;
    public static final int DEFAULT_KEYPAD_BG = R.color.keypad_default_bg;

    private List<View> buttonList;
    private List<Button> extraButtonList;
    private Animation ANIM_IN, ANIM_OUT, ANIM_CLICK;
    private List<TextHolder> textViewList;

    private View button00, buttonDot, ibtnMore;
    private ImageButton ibtnGo, ibtnNegative;

    private int buttonTextColor,buttonBackground, keypadBackground, goButtonBackground;

    public Keypad(Context context, AttributeSet attrs) {
        super(context, attrs);
        buttonList = new ArrayList<>();
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keypadView = (ViewGroup)inflater.inflate(R.layout.imonggosdk_keypad, this, true);
        keypadView.setVisibility(GONE);
        drawer_layout = (DrawerLayout)keypadView.findViewById(R.id.drawer_layout);
        drawer = findViewById(R.id.drawer);
        //vsExtraButtons = (ViewStub)keypadView.findViewById(R.id.vsExtraButton);

        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ibtnMore = findViewById(R.id.ibtnMore);
        enableDrawer(false);
        ibtnGo = (ImageButton)findViewById(R.id.ibtnGo);
        ibtnNegative = (ImageButton)findViewById(R.id.ibtnNegative);

        button00 = findViewById(R.id.button00);
        //buttonDash = findViewById(R.id.buttonDash);
        buttonDot = findViewById(R.id.buttonDot);

        ANIM_IN = AnimationUtils.loadAnimation(context, R.anim.abc_slide_in_bottom);
        ANIM_OUT = AnimationUtils.loadAnimation(context, R.anim.abc_slide_out_bottom);
        //ANIM_CLICK = AnimationUtils.loadAnimation(context, R.anim.enlarge);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Keypad);
        handleBackbutton = typedArray.getBoolean(R.styleable.Keypad_handleBackbutton, true);
        hasOkayButton = typedArray.getBoolean(R.styleable.Keypad_hasOkayButton, true);

        initButtons();
        changeColor(DEFAULT_KEYPAD_BUTTON_TEXTCOLOR,
                    DEFAULT_KEYPAD_BUTTON_BG,
                    DEFAULT_KEYPAD_BUTTON_BG_CIRCLE,
                    DEFAULT_KEYPAD_BG);

        setRequireDecimal(false);
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

    private boolean DRAWER_ENABLED = true;
    private void enableDrawer(boolean isEnabled) {
        ibtnMore.setEnabled(isEnabled);
        if(isEnabled) {
            if(extraButtonList != null && extraButtonList.size() > 0) {
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        } else {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }
    public void setEnableDrawer(boolean isEnabled) {
        DRAWER_ENABLED = isEnabled;
        enableDrawer(isEnabled);
    }

    public void changeBackgroundDrawable(int resource_keypadBG) {
        keypadBackground = resource_keypadBG;
        drawer_layout.setBackgroundResource(keypadBackground);
        drawer.setBackgroundResource(keypadBackground);
    }
    public void changeBackgroundColor(int resource_keypadBG) {
        keypadBackground = resource_keypadBG;
        drawer_layout.setBackgroundColor(keypadBackground);
        drawer.setBackgroundColor(keypadBackground);
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

        drawer_layout.setBackgroundResource(keypadBackground);
        drawer.setBackgroundResource(keypadBackground);
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

    private void setTextHolder(TextHolder textHolder) {
        mTextHolder = textHolder;
    }

    public void setDisableAnimation(boolean disableAnimation) {
        this.disableAnimation = disableAnimation;
    }

    private void initButtons() {
        buttonFinder(keypadView, false);
        setAsBackspaceButton(keypadView.findViewById(R.id.ibtnBksp));
        ibtnMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer();
            }
        });
        ibtnGo.setOnClickListener(goClickListener);
        if(!hasOkayButton)
            ibtnGo.setVisibility(View.INVISIBLE);
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
        ibtnNegative.setImageResource(isNegative ?
                        R.drawable.ic_negative_scale : R.drawable.ic_positive_scale
        );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LinearLayout llControls = (LinearLayout) keypadView.findViewById(R.id.llControls);

        // Ideal Width
        if(llControls.getHeight() > 0) {
            int pxP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, llControls.getHeight(), getContext().getResources().getDisplayMetrics());
            getLayoutParams().height = pxP;
        }
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
                //}
                //else {
                //    extraButtonList.add(child);
                //}
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
            if(getVisibility() != VISIBLE || drawer_layout.isDrawerOpen(drawer))
                return;
            //view.startAnimation(ANIM_CLICK);
            doBackspace();
        }
    };
    private OnLongClickListener backspaceLongClickListener =  new OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if(getVisibility() != VISIBLE || drawer_layout.isDrawerOpen(drawer))
                return true;
            //view.startAnimation(ANIM_CLICK);
            doClear();
            return true;
        }
    };
    private OnClickListener goClickListener =  new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(getVisibility() != VISIBLE || drawer_layout.isDrawerOpen(drawer))
                return;
            hide();
            if(mTextHolder.helper != null) {
                mTextHolder.helper.onConfirmClick(mTextHolder.getTextView().getText().toString());
            }
        }
    };

    @Override
    public void onClick(View view) {/*
        if(!textViewList.contains(view)) {
            Log.e("Keypad","can't find from list of TextHolder: " + view.toString());
            return;
        }*/
        if(getVisibility() != VISIBLE)
            show();
        /*if(getVisibility() == VISIBLE)
            hide();
        else
            show();*/
    }

    public void closeDrawer() {
        drawer_layout.closeDrawer(drawer);
    }
    public void openDrawer() {
        drawer_layout.openDrawer(drawer);
    }

    public View getTextHolderWithTag(String tag) {
        for(int i=0 ;i< textViewList.size(); i++) {
            if(textViewList.get(i).getTag().equals(tag))
                return textViewList.get(i).mTextHolder;
        }
        return null;
    }

    public void addTextHolder(TextView view, String tag, boolean requireDecimal, boolean allowNegative, @Nullable TextHolderHelper listener) {
        addTextHolder(new TextHolder(view, tag, listener, requireDecimal, allowNegative));
    }
    public void addTextHolder(EditText view, String tag, boolean requireDecimal, boolean allowNegative, @Nullable TextHolderHelper listener) {
        addTextHolder(new TextHolder(view, tag, listener, requireDecimal, allowNegative));
    }
    public void addTextHolder(TextView view, String tag, boolean requireDecimal, int decimalPlace, boolean allowNegative, @Nullable TextHolderHelper listener) {
        addTextHolder(new TextHolder(view, tag, listener, requireDecimal, decimalPlace, allowNegative));
    }
    public void addTextHolder(EditText view, String tag, boolean requireDecimal, int decimalPlace, boolean allowNegative, @Nullable TextHolderHelper listener) {
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
                        if(handleBackbutton)
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
                    closeDrawer();
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
                        View customDrawer = textHolder.helper.initializeCustomLayout(inflater);
                        enableDrawer(true);

                        if(customDrawer == null) {
                            setDefaultDrawer(textHolder);
                        } else {
                            setCustomDrawer(customDrawer);
                        }


                        if (customDrawer == null && (extraButtonList == null || extraButtonList.size() <= 0)) {
                            setEnableDrawer(false);
                            extraButtonList = null;
                        }


                        textHolder.helper.hasFocus(view, true);
                    } else { // no helper
                        enableDrawer(false);
                    }
                }
            }
        });
    }

    public void hide() {
        closeDrawer();
        if(!disableAnimation)
            startAnimation(ANIM_OUT);
        setVisibility(GONE);
    }

    public void show() {
        setVisibility(VISIBLE);
        if(!disableAnimation)
            startAnimation(ANIM_IN);
    }

    private void setCustomDrawer(View view) {
        ViewGroup vgDrawer = ( (ViewGroup)drawer );
        vgDrawer.removeAllViewsInLayout();
        vgDrawer.addView(view);
    }
    private LayoutInflater inflater = (LayoutInflater) getContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    private void setDefaultDrawer(TextHolder textHolder) {
        View view = inflater.inflate(R.layout.imonggosdk_keypad_default_drawer,null);
        setCustomDrawer(view);

        if(textHolder.extraButtons == null || textHolder.extraButtons.size() <= 0) {
            setEnableDrawer(false);
            return;
        }

        if(extraButtonList == null)
            extraButtonList = new ArrayList<>();

        ViewGroup tableLayout = (ViewGroup)drawer.findViewById(R.id.tlExtraButtons);
        for(int i=0; i<textHolder.extraButtons.size(); i++) {
            ExtraButton obj = textHolder.extraButtons.get(i);
            Button btn = new Button(tableLayout.getContext());
            btn.setPadding(toDP(5),toDP(5),toDP(5),toDP(5));
            btn.setText(obj.mText);
            if(obj.mListener != null)
                btn.setOnClickListener(obj.mListener);
            colorize(btn);
            ( (ViewGroup)tableLayout.getChildAt(i%4) ).addView(btn);
            extraButtonList.add(btn);
        }
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

        private List<ExtraButton> extraButtons;

        public TextHolder(View view, String tag, TextHolderHelper helper, boolean requireDecimal, boolean allowNegative) {
            this.mTextHolder = view;
            setTag(tag);
            this.helper = helper;
            if(helper != null) {
                extraButtons = new ArrayList<>();
                helper.initializeDefaultLayoutButtons(extraButtons);
            }
            this.requireDecimal = requireDecimal;
            this.mDecimalPlace = 2;
            this.allowNegative = allowNegative;
        }
        public TextHolder(View view, String tag, TextHolderHelper helper, boolean requireDecimal, int decimalPlace, boolean allowNegative) {
            this.mTextHolder = view;
            setTag(tag);
            this.helper = helper;
            if(helper != null) {
                extraButtons = new ArrayList<>();
                helper.initializeDefaultLayoutButtons(extraButtons);
            }
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
        @Nullable
        void initializeDefaultLayoutButtons(List<ExtraButton> extraButtons);
        @Nullable
        View initializeCustomLayout(LayoutInflater inflater);
    }


    public static class ExtraButton {
        public ExtraButton(String text, View.OnClickListener listener) {
            mText = text;
            mListener = listener;
        }
        private String mText;
        public OnClickListener mListener;
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }
}
