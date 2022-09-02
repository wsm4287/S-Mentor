package com.example.s_mentor.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.s_mentor.ProfileActivity;
import com.example.s_mentor.R;
import com.example.s_mentor.RegActivity;
import com.example.s_mentor.chat.ChatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class PushNotification extends FirebaseMessagingService {

    String name, id, text, id2, type;


    @SuppressLint("NewApi")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        name = message.getData().get("name");
        id = message.getNotification().getTitle();
        text = message.getNotification().getBody();
        id2 = message.getData().get("email");
        type = message.getData().get("type");
        String CHANNEL_ID  = "MESSAGE";

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Message Notification",
                NotificationManager.IMPORTANCE_DEFAULT);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);


        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(name)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_baseline_message_24)
                .setAutoCancel(true);

        if (!type.equals("XX")) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("email", id2);
            intent.putExtra("email2", id);
            intent.putExtra("type", type);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            notification.setContentIntent(pendingIntent);
        }
        NotificationManagerCompat.from(this).notify(1, notification.build());


    }

}
