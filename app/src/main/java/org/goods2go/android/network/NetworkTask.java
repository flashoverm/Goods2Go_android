package org.goods2go.android.network;

import android.os.AsyncTask;

public abstract class NetworkTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected NetworkException networkException;

    protected abstract Result runInBackground(Params[] params) throws NetworkException;

    @Override
    protected Result doInBackground(Params[] params) {
        try{
            return runInBackground(params);
        } catch(NetworkException e) {
            this.networkException = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Result result) {

    }
}
