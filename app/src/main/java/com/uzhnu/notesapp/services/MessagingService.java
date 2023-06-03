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

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.activities.MainActivity;
import com.uzhnu.notesapp.models.NotificationModel;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.firebase.AuthUtil;
import com.uzhnu.notesapp.utilities.firebase.MessagingUtil;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(Constants.TAG, "From: " + remoteMessage.getFrom());
        Log.d(Constants.TAG, "Message data" + remoteMessage.getData());

        NotificationModel notificationModel
                = NotificationModel.toNotification(remoteMessage);
//        if (!notificationModel.getSenderPhoneNumber().equals(AuthUtil.getUserPhoneNumber())) {
            sendNotification(notificationModel);
//        }
    }

    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(Constants.TAG, "Refreshed token: " + token);
    }

    private void sendNotification(@NonNull NotificationModel notificationModel) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.outline_notifications_24)
                        .setContentTitle(notificationModel.getTitle())
                        .setContentText(notificationModel.getText())
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

        notificationManager.notify(notificationModel.getId(), notificationBuilder.build());
    }
}
