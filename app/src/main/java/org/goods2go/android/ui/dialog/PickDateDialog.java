package org.goods2go.android.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import com.goods2go.models.util.DateTime;

import org.goods2go.android.R;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PickDateDialog extends AbstractDialogFragment {

    public static final String TAG = "PickDateDialog";
    private static final String FIELD_NUMBER = "fieldNumber";
    private static final String TITLE = "title";

    public interface DateSetListener{
        void onDateSet(Date date, int fieldNumber);
    }

    private DateSetListener dateSetListener;

    public static PickDateDialog newInstance(int fieldNumber, int title) {
        Bundle args = new Bundle();
        args.putInt(FIELD_NUMBER, fieldNumber);
        args.putInt(TITLE, title);

        PickDateDialog fragment = new PickDateDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onAttachCompatible(Context context) {
        try {
            dateSetListener = (DateSetListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getTargetFragment().toString()
                    + " must implement DateSetListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_datepicker, null);
        final DatePicker datePicker = view.findViewById(R.id.datePicker);
        datePicker.setMinDate(DateTime.getCurrentDate().getTime());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle(getArguments().getInt(TITLE));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                c.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                System.out.println("DateZ: " + c.getTime());
                c.setTimeZone(TimeZone.getTimeZone("UTC"));
                System.out.println("DateT: " + c.getTime());
                Date date = DateTime.removeTime(c.getTime());
                System.out.println("Date: " + date);
                dateSetListener.onDateSet(date, getArguments().getInt(FIELD_NUMBER));
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }
}
