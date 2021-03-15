package com.sngur.learnkhasi;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.core.app.NotificationCompat;

public class MyFirebaseInstanceIdService extends FirebaseMessagingService  {
    private static final String TAG = "Learn Khasi";

    public MyFirebaseInstanceIdService(){

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String datatitle = "0";
        String datamessage = "0";
        //Log.d(TAG, "From: " + remoteMessage.getFrom());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            datatitle = remoteMessage.getData().get("title");
            datamessage = remoteMessage.getData().get("message");

            String click_action = remoteMessage.getNotification().getClickAction();
            //Calling method to generate notification
            sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle(), datamessage, click_action);
        }

        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        //sendNotification(remoteMessage.getNotification().getBody());
    }


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
//        Log.e(TAG, "Refreshed token: " + token);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);
    }

    private void sendNotification(String messageBody, String messageTitle, String datamessage2, String click_action) {
        Intent intent = new Intent(click_action);
        intent.putExtra("message", datamessage2);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notimanager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String channelID = "XNZ";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            @SuppressLint("WrongConstant")
            NotificationChannel notichannel = new NotificationChannel(channelID,
                    "EDMT Notification",NotificationManager.IMPORTANCE_MAX);
            notichannel.setDescription("Learn Khasi");
            notichannel.enableLights(true);
            notichannel.setLightColor(Color.RED);
            notichannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notichannel.enableVibration(true);

            notimanager.createNotificationChannel(notichannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,channelID);
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
