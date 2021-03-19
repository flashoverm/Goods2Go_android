package org.goods2go.android.ui.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Build;

public abstract class AbstractDialogFragment extends DialogFragment {

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.onAttachCompatible(context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.onAttachCompatible(activity);
        }
    }

    protected abstract void onAttachCompatible(Context context);

}
