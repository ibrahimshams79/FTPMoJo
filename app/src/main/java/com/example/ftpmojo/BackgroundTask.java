package com.example.ftpmojo;

import android.app.Activity;

public abstract class BackgroundTask {

    private Activity activity;

    public BackgroundTask(Activity activity) {
        this.activity = activity;
    }

    private void startBackground() {
        onPreExecute();
        new Thread(new Runnable() {
            public void run() {

                String r = doInBackground();
                activity.runOnUiThread(new Runnable() {
                    public void run() {

                        onPostExecute(r);
                    }
                });
            }
        }).start();
    }

    public void execute() {
        startBackground();
    }

    public abstract void onPreExecute();

    public abstract String doInBackground();

    public abstract void onPostExecute(String r);

}
