package com.logistic.paperrose.mttp.oldversion.search;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

/**
 * Created by paperrose on 25.12.2014.
 */
public class DelayedFilteringField extends AutoCompleteTextView {
    private static final int MESSAGE_TEXT_CHANGED = 100;
    private static final int DEFAULT_AUTOCOMPLETE_DELAY = 750;

    private int mAutoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY;
    private ProgressBar mLoadingIndicator;
    public boolean enoughToFilterT = true;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        DelayedFilteringField.super.performFiltering((CharSequence) msg.obj, msg.arg1);
        }
    };

    public DelayedFilteringField(Context context) {
        super(context);
    }

    public DelayedFilteringField(Context context, AttributeSet attrs) {
        super(context, attrs);
        //делаем запрос на сервер
        //если count меньше 100, то устанавливаем thresold = 1

    }



    public void setLoadingIndicator(ProgressBar progressBar) {
        mLoadingIndicator = progressBar;
    }

    public void setAutoCompleteDelay(int autoCompleteDelay) {
        mAutoCompleteDelay = autoCompleteDelay;
    }

    @Override
    public boolean enoughToFilter() {
        return enoughToFilterT || super.enoughToFilter();
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }
        mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, text), mAutoCompleteDelay);
    }

    @Override
    public void onFilterComplete(int count) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(View.GONE);
        }
        super.onFilterComplete(count);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isPopupShowing()) {
            InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            // inputManager.isAcceptingText() will not work because view is still focused.
            if (mIsKeyboardVisible) { // Is keyboard visible?
                // Hide keyboard.
                inputManager.hideSoftInputFromWindow(getWindowToken(), 0);
                mIsKeyboardVisible = false;

                // Consume event.
                return true;
            } else {
                // Do nothing.
            }
        }

        return super.onKeyPreIme(keyCode, event);
    }


    private boolean mIsKeyboardVisible;
    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        mIsKeyboardVisible = focused;
        if (enoughToFilterT && focused && getAdapter() != null) {
            performFiltering(getText(), 0);
        }
    }
}
