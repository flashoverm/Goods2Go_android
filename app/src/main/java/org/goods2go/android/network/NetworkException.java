package org.goods2go.android.network;

import org.goods2go.android.Goods2GoApplication;
import org.goods2go.android.R;
import org.goods2go.android.ui.activity.NetworkActivity;

import java.net.HttpURLConnection;

public class NetworkException extends Exception {

    public static final int URLFORMAT_ERROR = 001;
    public static final int CONNECTION_ERROR = 002;
    public static final int CONNECTION_TIMEOUT = 003;

    public static final int OUTPUTSTREAM_ERROR = 011;
    public static final int OUTPUTSERIALIZATION_ERROR = 012;
    public static final int INPUTSTREAM_ERROR = 013;
    public static final int INPUTSERIALIZATION_ERROR = 014;

    public int httpError;

    public NetworkException(Exception e, int httpError){
        super(e);
        this.httpError = httpError;
    }
    public NetworkException(String message, int httpError) {
        super("HTTP " + httpError + ": " + message);
        this.httpError = httpError;
    }
    public NetworkException(String message, Throwable cause, int httpError) {
        super("HTTP " + httpError + ": " + message, cause);
        this.httpError = httpError;
    }

    public void handleException(NetworkActivity networkActivity){
        int errorMessage = getDefaultErrorMessage();
        if(errorMessage != -1){
            networkActivity.showResult(errorMessage);

            if(httpError == HttpURLConnection.HTTP_UNAUTHORIZED){
                Goods2GoApplication.logout(networkActivity);
            }
            return;
        }
        networkActivity.showResult(R.string.error_network);
    }

    private int getDefaultErrorMessage(){
        switch(httpError){
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                return R.string.error_session_expired;
            case HttpURLConnection.HTTP_FORBIDDEN:
                return R.string.error_forbidden;
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                return R.string.error_internal_server;
            case HttpURLConnection.HTTP_NOT_FOUND:
                return R.string.error_not_found;
            case CONNECTION_TIMEOUT:
                return R.string.error_network_timeout;
        }
        return -1;
    }
}
