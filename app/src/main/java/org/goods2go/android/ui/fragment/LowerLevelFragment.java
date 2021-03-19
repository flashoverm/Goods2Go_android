package org.goods2go.android.ui.fragment;

public class LowerLevelFragment extends NetworkFragment {

    private String previousTitle;

    protected void setTitle(int resId){
        previousTitle = getActivity().getTitle().toString();
        getActivity().setTitle(resId);
    }

    protected void backToPreviousFragment(){
        getActivity().setTitle(previousTitle);
        getFragmentManager().popBackStack();
    }
}
