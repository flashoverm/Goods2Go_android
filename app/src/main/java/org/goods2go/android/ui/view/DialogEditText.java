package org.goods2go.android.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.goods2go.android.R;


public class DialogEditText extends LinearLayout {

    private View view;

    private TextInputLayout layout;
    private EditText editText;

    public DialogEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DialogEditText,
                0, 0);

        initView(attributes);
    }

    private void initView(TypedArray attributes){
        view = inflate(getContext(), R.layout.layout_dialog_textedit, this);
        layout = view.findViewById(R.id.textinput_layout);
        editText = view.findViewById(R.id.edit_text);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);

        layout.setHint(attributes.getString(R.styleable.DialogEditText_hint));
        editText.setMaxLines(attributes.getInteger(R.styleable.DialogEditText_maxLines, 1));
    }

    public String getText(){
        return editText.getText().toString();
    }

    public void setOnClickListener(OnClickListener onClickListener){
        editText.setOnClickListener(onClickListener);
    }

    public void setError(CharSequence error) {
        if(error == null){
            layout.setErrorEnabled(false);
        } else {
            layout.setError(error);
        }
    }

    public void setText(String text){
        editText.setText(text);
    }
}
