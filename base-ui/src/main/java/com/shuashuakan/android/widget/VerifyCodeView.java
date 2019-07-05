package com.shuashuakan.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.shuashuakan.android.base.ui.R;

/**
 * 验证码输入控件
 */
public class VerifyCodeView extends LinearLayout implements View.OnKeyListener {
  private static final int MAX_WEIGHT_SUM = 6;
  private static final int WEIGHT_MIDDLE_PADDING = 12; // dp
  private static final int WEIGHT_WIDTH_HEIGHT = 40; // dp
  private static final int TEXT_SIZE = 20; // sp
  private static final int DEFAULT_INDEX = 0;

  private Context context;
  private Resources resources;
  private int padding;
  private int weightWidthHeight;
  private int currentFocusIndex = DEFAULT_INDEX;
  private FillInVerifyCodeListener fillInVerifyCodeListener;

  public VerifyCodeView(Context context, AttributeSet attrs) {
    this(context, attrs, -1);
  }

  public VerifyCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.context = context;
    resources = getResources();
    padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, WEIGHT_MIDDLE_PADDING,
        context.getResources().getDisplayMetrics());
    weightWidthHeight =
        ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, WEIGHT_WIDTH_HEIGHT,
            context.getResources().getDisplayMetrics()));
    setOrientation(HORIZONTAL);
    setupViews();
    changFocusVerifyCodeView(currentFocusIndex);
  }

  private void setupViews() {
    LayoutParams weightLayoutParams = new LayoutParams(weightWidthHeight, weightWidthHeight);
    for (int i = 0; i < MAX_WEIGHT_SUM; i++) {
      EditText verityCode = new EditText(context);
      verityCode.setOnKeyListener(this);
      verityCode.setBackgroundResource(R.drawable.verify_border_normal_shape);
      verityCode.setGravity(Gravity.CENTER);
      verityCode.setPadding(0, 0, 0, 0);
      verityCode.setFilters(new InputFilter[] { new InputFilter.LengthFilter(1) });
      verityCode.setInputType(InputType.TYPE_CLASS_NUMBER);
      verityCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
      verityCode.setTextColor(resources.getColor(android.R.color.white));
      verityCode.setFocusableInTouchMode(false);
      if (i > 0 && i < MAX_WEIGHT_SUM) {
        weightLayoutParams.leftMargin = padding;
      } else {
        weightLayoutParams.leftMargin = 0;
      }
      verityCode.setLayoutParams(weightLayoutParams);
      verityCode.addTextChangedListener(new VerifyCodeTextWatcher(verityCode, i));
      addView(verityCode);
    }
    LayoutParams rootLayoutParams =
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    setLayoutParams(rootLayoutParams);
  }

  @Override public boolean onKey(View view, int i, KeyEvent keyEvent) {
    if (i == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_UP) {
      if (currentFocusIndex == DEFAULT_INDEX) {
        return false;
      }
      currentFocusIndex -= 1;
      updateViews();
      return true;
    }
    return false;
  }

  private void updateViews() {
    for (int i = 0; i < MAX_WEIGHT_SUM; i++) {
      EditText verifyCodeView = (EditText) getChildAt(i);
      if (i > currentFocusIndex) {
        verifyCodeView.setBackgroundResource(R.drawable.verify_border_normal_shape);
        verifyCodeView.setFocusableInTouchMode(false);
        verifyCodeView.setFocusable(false);
      } else if (i == currentFocusIndex) {
        verifyCodeView.setText("");
        verifyCodeView.setBackgroundResource(R.drawable.verify_border_shape);
        verifyCodeView.setFocusableInTouchMode(true);
        verifyCodeView.setFocusable(true);
        verifyCodeView.requestFocus();
      }
    }
  }

  private class VerifyCodeTextWatcher implements TextWatcher {
    private EditText currentVerityCodeView;
    private final int index;

    VerifyCodeTextWatcher(EditText verityCodeView, int index) {
      this.currentVerityCodeView = verityCodeView;
      this.index = index;
    }

    @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override public void afterTextChanged(Editable editable) {
      if (editable.length() > 0) {
        if (index == DEFAULT_INDEX) {
          if (fillInVerifyCodeListener != null) {
            fillInVerifyCodeListener.onRefillInVerifyCode();
          }
        }
        currentFocusIndex = index + 1;
        currentVerityCodeView.setFocusable(true);
        currentVerityCodeView.setFocusableInTouchMode(false);
        if (currentFocusIndex >= MAX_WEIGHT_SUM) {
          if (fillInVerifyCodeListener != null) {
            fillInVerifyCodeListener.onComplete(getVerifyCode());
          }
        } else {
          changFocusVerifyCodeView(currentFocusIndex);
        }
      }
    }
  }

  private void changFocusVerifyCodeView(int currentFocusIndex) {
    final EditText focusVerifyCodeView = (EditText) getChildAt(currentFocusIndex);
    focusVerifyCodeView.setBackgroundResource(R.drawable.verify_border_shape);
    postDelayed(new Runnable() {
      @Override public void run() {
        focusVerifyCodeView.setFocusableInTouchMode(true);
        focusVerifyCodeView.setFocusable(true);
        focusVerifyCodeView.requestFocus();
        InputMethodManager manager = (InputMethodManager) focusVerifyCodeView.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
          manager.showSoftInput(focusVerifyCodeView, InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
      }
    }, 50);
  }

  private String getVerifyCode() {
    StringBuilder verifyCode = new StringBuilder();
    for (int i = 0; i < MAX_WEIGHT_SUM; i++) {
      verifyCode.append(((EditText) getChildAt(i)).getText());
    }
    return verifyCode.toString();
  }

  public void reset() {
    for (int i = 0; i < MAX_WEIGHT_SUM; i++) {
      getChildAt(i).setFocusableInTouchMode(false);
      getChildAt(i).setFocusable(false);
      getChildAt(i).setBackgroundResource(R.drawable.verify_border_normal_shape);
      ((EditText) getChildAt(i)).setText("");
    }
    currentFocusIndex = DEFAULT_INDEX;
    changFocusVerifyCodeView(currentFocusIndex);
  }

  public void setOnFillInVerifyCodeListener(FillInVerifyCodeListener listener) {
    this.fillInVerifyCodeListener = listener;
  }

  public interface FillInVerifyCodeListener {
    void onComplete(String verifyCode);

    void onRefillInVerifyCode();
  }
}
