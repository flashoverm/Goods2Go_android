package org.goods2go.android.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.goods2go.models.enums.IdentType;

import org.goods2go.android.R;
import org.goods2go.android.ui.adapter.EnumAdapter;

public class IdentificationView extends LinearLayout {

    private View view;

    private EditText editIdentification;
    private Spinner editIdentType;

    public IdentificationView(Context context) {
        super(context);
        initView();
    }

    public IdentificationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView(){
        view = inflate(getContext(), R.layout.layout_indenfitication, this);
        editIdentification = view.findViewById(R.id.edit_identificator);
        editIdentType = view.findViewById(R.id.edit_ident_type);

        editIdentType.setAdapter(new EnumAdapter(getContext(), IdentType.values()));

    }

    public IdentType getIdentificationType(){
        return (IdentType)editIdentType.getSelectedItem();
    }

    public String getIdentification(){
        editIdentification.setError(null);

        String identification = editIdentification.getText().toString();

        if(TextUtils.isEmpty(identification)){
            editIdentification.setError(getContext().getString(R.string.error_field_required));
            editIdentification.requestFocus();
            return null;
        }
        return identification;
    }
}
