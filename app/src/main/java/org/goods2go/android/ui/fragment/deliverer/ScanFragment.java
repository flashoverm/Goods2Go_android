package org.goods2go.android.ui.fragment.deliverer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.Result;

import org.goods2go.android.util.PermissionHandler;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanFragment extends Fragment
        implements ZXingScannerView.ResultHandler{


    public static ScanFragment newInstance(ScanResultListener scanResultListener) {
        ScanFragment fragment = new ScanFragment();
        fragment.setScanResultListener(scanResultListener);
        return fragment;
    }

    public interface ScanResultListener {
        void onScanResult(String result);
    }

    private ScanResultListener scanResultListener;
    private ZXingScannerView scannerView;

    public void setScanResultListener(ScanResultListener scanResultListener){
        this.scanResultListener = scanResultListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        scannerView = new ZXingScannerView(getActivity());
        PermissionHandler.requestCameraPermission(getActivity());
        return scannerView;
    }

    @Override
    public void handleResult(Result rawResult) {
        String result = rawResult.getText();
        scanResultListener.onScanResult(result);
        getFragmentManager().popBackStack();
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }
}
