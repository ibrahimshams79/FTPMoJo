package com.example.ftpmojo;

import android.app.Application;

public class FTPMoJo extends Application {

    MyAppsNotificationManager myAppsNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        myAppsNotificationManager = MyAppsNotificationManager.getInstance(this);
        myAppsNotificationManager.registerNotificationChannelChannel(
                getString(R.string.NOTIFICATION_CHANNEL_ID),
                getString(R.string.CHANNEL_UPLOAD),
                getString(R.string.CHANNEL_DESCRIPTION));

        myAppsNotificationManager.registerNotificationChannel2(
                getString(R.string.UPLOAD_CHANNEL_ID),
                getString(R.string.CHANNEL_UPLOADING),
                getString(R.string.UPLOADING_CHANNEL_DESCRIPTION));
    }


//    public void triggerNotification(Class targetNotificationActivity, String channelId, String title, String text, String bigText, int priority, boolean autoCancel, int notificationId) {
//        myAppsNotificationManager.triggerNotification(targetNotificationActivity, channelId, title, text, bigText, priority, autoCancel, notificationId);
//    }

    public void triggerNotification(Class targetNotificationActivity, String channelId, String title, String text, String bigText, int priority, boolean autoCancel, int notificationId, int pendingIntentFlag) {
        myAppsNotificationManager.triggerNotification(targetNotificationActivity, channelId, title, text, bigText, priority, autoCancel, notificationId, pendingIntentFlag);
    }

    public void updateNotification(Class targetNotificationActivity, String title, String text, String channelId, int notificationId, int i, int pendingIntentflag, Boolean autocancel) {
        myAppsNotificationManager.updateProgress(targetNotificationActivity, title, text, channelId, notificationId, i, pendingIntentflag, autocancel);
    }

    public void cancelNotification(int notificaitonId) {
        myAppsNotificationManager.cancelNotification(notificaitonId);
    }


}