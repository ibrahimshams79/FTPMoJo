package com.example.ftpmojo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

class MyAppsNotificationManager {

    private Context context;

    private static MyAppsNotificationManager instance;
    private final NotificationManagerCompat notificationManagerCompat;
    private NotificationManager notificationManager;

    private MyAppsNotificationManager(Context context) {
        this.context = context;
        notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static MyAppsNotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new MyAppsNotificationManager(context);
        }
        return instance;
    }

    public void registerNotificationChannelChannel(String channelId, String channelName, String channelDescription) {
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.setShowBadge(true);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    public void registerNotificationChannel2(String channelId, String channelName, String channelDescription) {
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.setShowBadge(true);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    public void triggerNotification(Class targetNotificationActivity, String channelId, String title, String text, String bigText, int priority, boolean autoCancel, int notificationId) {

        Intent intent = new Intent(context, targetNotificationActivity);
        intent.putExtra("NotificationFragment", "Notification");
//        intent.putExtra("count", title);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(priority)
                .setContentIntent(pendingIntent)
                .setChannelId(channelId)
                .setAutoCancel(autoCancel);

//        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(notificationId, builder.build());
    }

    public void triggerNotification(Class targetNotificationActivity, String channelId, String title, String text, String bigText, int priority, boolean autoCancel, int notificationId, int pendingIntentFlag) {

        Intent intent = new Intent(context, targetNotificationActivity);
        intent.putExtra("NotificationFragment", "Notification");
        intent.putExtra("count", title);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlag);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.logo3)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(priority)
                .setContentIntent(pendingIntent)
                .setChannelId(channelId)
                .setAutoCancel(autoCancel);

        notificationManagerCompat.notify(notificationId, builder.build());
    }

    public void updateProgress(Class targetNotificationActivity, String title, String text, String channelId, int notificationId, int i, int pendingIntentflag, Boolean autocancel) {

        Intent intent = new Intent(context, targetNotificationActivity);
        intent.putExtra("count", title);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentflag);

        Notification.Builder builder = new Notification.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
//                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_icon_large))
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setChannelId(channelId)
                .setCustomContentView(new RemoteViews(context.getApplicationContext()
                        .getPackageName(), R.layout.upload_progress_bar))
                .setProgress(100, i, false)
                .setOngoing(true);

//        Bitmap androidImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.big_pic);
//        builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(androidImage).setBigContentTitle(bigpictureString));
        notificationManager.notify(notificationId, builder.build());
    }

//    public void updateProgress (Class targetNotificationActivity, String title, String text, String channelId, int notificationId, int i, int pendingIntentflag, Boolean autocancel){
//        int progress = 0;
//        Notification notification;
////        NotificationManager notificationManager;
////        int id = 10;
//
//        Intent intent = new Intent();
//        final PendingIntent pendingIntent = PendingIntent.getActivity(
//                context.getApplicationContext(), 0, intent, pendingIntentflag);
//        notification = new Notification(R.drawable.ic_file_upload_black_24dp,
//                "Uploading file", System.currentTimeMillis());
//        notification.flags = notification.flags
//                | Notification.FLAG_ONGOING_EVENT;
//        notification.contentView = new RemoteViews(context.getApplicationContext()
//                .getPackageName(), R.layout.upload_progress_bar);
//        notification.contentIntent = pendingIntent;
//        notification.contentView.setImageViewResource(R.drawable.ic_file_upload_black_24dp,
//                R.drawable.ic_file_upload_black_24dp);
//        notification.contentView.setTextViewText(R.id.title,
//                "Uploading...");
//        notification.contentView.setProgressBar(R.id.noti_progressbar, 100,
//                        i, false);
//
////                notificationManager = (NotificationManager) context.getApplicationContext()
////                .getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(notificationId, notification);
//    }


    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }


}