package org.goods2go.android.ui.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.goods2go.models.Address;

import org.goods2go.android.Goods2GoApplication;
import org.goods2go.android.R;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.adapter.AddressAdapter;

import java.util.List;

public class AddressHistoryDialog extends AbstractDialogFragment
        implements AddressAdapter.OnClickListener{

    public static final String TAG = "AddressHistoryDialog";

    @Override
    public void onClick(Address address) {
        listener.onAddressSelected(address);
        this.dismiss();
    }

    public interface AddressSelectedListener{
        void onAddressSelected(Address address);
    }

    private AddressSelectedListener listener;
    private View content;
    private View progress;
    private TextView noEntry;

    public static AddressHistoryDialog newInstance() {
        AddressHistoryDialog fragment = new AddressHistoryDialog();
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_list_recycleview, null);

        progress = dialogView.findViewById(R.id.progress);
        content = dialogView.findViewById(R.id.list);
        noEntry = dialogView.findViewById(R.id.no_entries);

        List<Address> history = Goods2GoApplication
                .getCurrentUser(getActivity()).getAddresshistory();
        if(history != null){
            displayList(history);
        }

        builder.setView(dialogView)
                .setTitle(getResources().getString(R.string.text_address_history))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddressHistoryDialog.this.getDialog().cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        return dialog;
    }

    private void displayList(List<Address> result){
        if (result.size() > 0 && content instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) content;
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.addItemDecoration(
                    new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
            AddressAdapter adapter = new AddressAdapter(this, result);
            recyclerView.setAdapter(adapter);
        } else {
            content.setVisibility(View.GONE);
            noEntry.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onAttachCompatible(Context context) {
        try {
            listener = (AddressSelectedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getTargetFragment().toString()
                    + " must implement AddressSelectedListener");
        }
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        content.setVisibility(show ? View.GONE : View.VISIBLE);
        content.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                content.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        progress.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showResult(int stringId){
        ((NetworkActivity)getActivity()).showResult(stringId);
    }
}
