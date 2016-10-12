package com.logistic.paperrose.mttp.oldversion;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.settings.PushSettingsActivity;

/**
 * Created by paperrose on 12.01.2015.
 */
public class AutoCompleteTextPreference extends EditTextPreference {
    public AutoCompleteTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AutoCompleteTextPreference(Context context) {
        super(context);
        mEditText = new AutoCompleteTextView(context);
        setLayoutResource(R.layout.text_edit_list_layout);
        mEditText.setThreshold(0);
        
        //Button remove =
        //The adapter of your choice
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        mEditText.setAdapter(adapter);
    }

    @Override
    protected View onCreateView (final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View customRow = inflater.inflate(R.layout.text_edit_list_layout, null);
        PushSettingsActivity activity = (PushSettingsActivity)inflater.getContext();
        activity.findViewById(android.R.id.list);

        ((Button) customRow.findViewById(R.id.removeButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        customRow.setClickable(true);
        return customRow;
    }

    private static AutoCompleteTextView mEditText = null;

    public AutoCompleteTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEditText = new AutoCompleteTextView(context, attrs);
        mEditText.setThreshold(0);
        //The adapter of your choice
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        mEditText.setAdapter(adapter);
    }
    private static final String[] COUNTRIES = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };

    @Override
    protected void onBindDialogView(View view) {
        AutoCompleteTextView editText = mEditText;
        editText.setText(getText());

        ViewParent oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = mEditText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }
}
