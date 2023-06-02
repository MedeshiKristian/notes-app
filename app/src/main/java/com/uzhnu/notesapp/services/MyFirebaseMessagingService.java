package com.uzhnu.notesapp.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.api.LogDescriptor;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.activities.MainActivity;
import com.uzhnu.notesapp.utilities.Constants;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(Constants.TAG, "From: " + remoteMessage.getFrom());

        /*if (remoteMessage.getData().size() > 0) {
            Log.d(Constants.TAG, "Message data payload: " + remoteMessage.getData());

            if (*//* Check if data needs to be processed by long running job *//* true) {
                scheduleJob();
            } else {
                handleNow();
            }

        }*/

        if (remoteMessage.getNotification() != null) {
            Log.d(Constants.TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String notificationBody = remoteMessage.getNotification().getBody();
            if (remoteMessage.getNotification().getBody() != null) {
                sendNotification(notificationBody);
            }
        }
    }
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(Constants.TAG, "Refreshed token: " + token);

        // sendRegistrationToServer(token);
    }
    private void scheduleJob() {
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class).build();
        WorkManager.getInstance(this).beginWith(work).enqueue();
    }
    private void handleNow() {
        Log.d(Constants.TAG, "Short lived task is done.");
    }

    /*private void sendRegistrationToServer(@NonNull String token) {
        // TODO: Implement this method to send token to your app server.
    }*/

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.outline_notifications_24)
                        .setContentTitle("Content Title")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
