package org.goods2go.android.ui.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

public class PickTimeDialog extends AbstractDialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    public static final String TAG = "PickTimeDialog";
    private static final String FIELD_NUMBER = "fieldNumber";
    private static final String FIELD_TITLE = "title";

    public interface TimeSetListener{
        void onTimeSet(int hours, int minutes, int fieldNumber);
    }

    private int fieldNumber;
    private TimeSetListener timeSetListener;

    public static PickTimeDialog newInstance(int fieldNumber, int titleResId) {
        Bundle args = new Bundle();
        args.putInt(FIELD_NUMBER, fieldNumber);
        args.putInt(FIELD_TITLE, titleResId);

        PickTimeDialog fragment = new PickTimeDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onAttachCompatible(Context context) {
        try {
            timeSetListener = (TimeSetListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getTargetFragment().toString()
                    + " must implement TimeSetListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        fieldNumber = getArguments().getInt(FIELD_NUMBER);


        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                getActivity(), this,
                hour, minute,DateFormat.is24HourFormat(getActivity()));
        dialog.setTitle(getArguments().getInt(FIELD_TITLE));
        return dialog;
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
        timeSetListener.onTimeSet(hours, minutes, fieldNumber);
    }
}
