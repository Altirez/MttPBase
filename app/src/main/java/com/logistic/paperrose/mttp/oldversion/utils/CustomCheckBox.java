package com.logistic.paperrose.mttp.oldversion.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.CheckBox;

import com.logistic.paperrose.mttp.R;

/**
 * Created by paperrose on 21.04.2016.
 */
public class CustomCheckBox extends CheckBox {
    private final Drawable buttonDrawable;

    public CustomCheckBox(Context context) {
        super(context);
        buttonDrawable = getResources().getDrawable(R.drawable.checkbox_style);
        try {
            setButtonDrawable(android.R.color.transparent);
        } catch (Exception e) {
            // DO NOTHING
        }
        setPadding(10, 5, 50, 5);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        buttonDrawable.setState(getDrawableState());

        final int verticalGravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
        final int height = buttonDrawable.getIntrinsicHeight();
        if (buttonDrawable != null) {
            int y = 0;

            switch (verticalGravity) {
                case Gravity.BOTTOM:
                    y = getHeight() - height;
                    break;
                case Gravity.CENTER_VERTICAL:
                    y = (getHeight() - height) / 2;
                    break;
            }

            int buttonWidth = buttonDrawable.getIntrinsicWidth();
            int buttonLeft = getWidth() - buttonWidth - 5;
            buttonDrawable.setBounds(buttonLeft, y, buttonLeft + buttonWidth, y + height);
            buttonDrawable.draw(canvas);
        }
    }
}
