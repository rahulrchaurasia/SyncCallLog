package com.utility.finmartcontact.utility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.utility.finmartcontact.R;
import com.utility.finmartcontact.core.controller.facade.ApplicationPersistance;
import com.utility.finmartcontact.core.model.NotifyEntity;
import com.utility.finmartcontact.home.HomeActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by IN-RB on 21-02-2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /****************************************************
     (Key)           (Action)
     NL >>>>>>>>>  Notification List
     WB >>>>>>>   WebView
     MSG >>>>>>>  Notification MESSAGE
     Default >>>  Home Page
     **************************************************/
    private NotificationManager mManager;
    private static final String TAG = "MyFirebaseMsgService";
    public static final String CHANNEL_ID = "com.utility.finmartcontact.NotifyID";
    public static final String CHANNEL_NAME = "SYNCALLLOG CHANNEL";
    Bitmap bitmap_image = null;
    String type;
    String WebURL, WebTitle, messageId;
    NotifyEntity notifyEntity;
    ApplicationPersistance prefManager;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        sendNotification(remoteMessage, remoteMessage.getData());
    }



    private void sendNotification(RemoteMessage remoteMessage, Map<String, String> data) {

        notifyEntity = new NotifyEntity();
        prefManager = new ApplicationPersistance(getApplicationContext());
        int NOTIFICATION_ID = 0;

        NOTIFICATION_ID = (int) Math.round(Math.random() * 1000);
        if (remoteMessage.getData().size() == 0) {
            Log.d(TAG, "Message Data Body Empty: ");
            return;
        }
        Log.d(TAG, remoteMessage.getData().get("notifyFlag"));
        Map<String, String> NotifyData = remoteMessage.getData();
        Intent intent;
        if (NotifyData.get("notifyFlag") == null) {
            return;
        } else {
            type = NotifyData.get("notifyFlag");

            // region validate WEBURL
            if (NotifyData.get("web_url") == null) {
                WebURL = "";
            } else {
                WebURL = NotifyData.get("web_url");
            }
            //endregion

            // region validate WEB Title
            if (NotifyData.get("web_title") == null) {
                WebTitle = "";
            } else {
                WebTitle = NotifyData.get("web_title");
            }
            //endregion

            // region validate MESSAGE_ID
            if (NotifyData.get("message_id") == null) {
                messageId = "0";
            } else if (NotifyData.get("message_id").toString().isEmpty()) {
                messageId = "0";
            } else {
                messageId = NotifyData.get("message_id");
            }
            // endregion

            notifyEntity.setNotifyFlag(type);
            notifyEntity.setTitle(NotifyData.get("title"));
            notifyEntity.setBody(NotifyData.get("body"));
            notifyEntity.setMessage_id(messageId);
            notifyEntity.setWeb_url(WebURL);
            notifyEntity.setWeb_title(WebTitle);

            String img_url = NotifyData.get("img_url");
            bitmap_image = getBitmapfromUrl(img_url);
            //  new createBitmapFromURL(NotifyData.get("img_url")).execute();

            intent = new Intent(this, HomeActivity.class);
            intent.putExtra(Constant.PUSH_NOTIFY, notifyEntity);

        }


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) Math.round(Math.random() * 1000), intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        createChannels();

        NotificationCompat.BigPictureStyle BigPicstyle = new NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap_image)
                .setBigContentTitle(NotifyData.get("title"))
                .setSummaryText(NotifyData.get("body"))
                .bigLargeIcon(null);

        NotificationCompat.BigTextStyle BigTextstyle = new NotificationCompat.BigTextStyle()
                .bigText(NotifyData.get("body"))
                .setBigContentTitle(NotifyData.get("title"));
               // .setSummaryText(NotifyData.get("body"));

        NotificationCompat.Builder notificationBuilder = null;


        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);

        if (bitmap_image != null) {
            notificationBuilder.setStyle(BigPicstyle);
            notificationBuilder.setLargeIcon(bitmap_image);
        } else {
            notificationBuilder.setStyle(BigTextstyle);
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.ic_notification);
            notificationBuilder.setColor(getResources().getColor(R.color.colorPrimary));
        } else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        }

        notificationBuilder
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.lightGrey))
                .setContentTitle(NotifyData.get("title"))
                .setContentText(NotifyData.get("body"))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setTicker("SynContact")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setVisibility(NOTIFICATION_ID)
                .setChannelId(CHANNEL_ID)
                .setNumber(1)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setContentIntent(pendingIntent);


        getManager().notify(NOTIFICATION_ID, notificationBuilder.build());

        setNotifyCounter();


    }

    //   .setStyle(new NotificationCompat.BigTextStyle().bigText(NotifyData.get("body")))
    //      builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.finmart_logo_splash));


    private void setNotifyCounter() {
        int notifyCounter = prefManager.getNotificationCounter();
        prefManager.setNotificationCounter(notifyCounter + 1);

        Intent intent = new Intent(Constant.PUSH_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);


    }

    public void createChannels() {
        if (Build.VERSION.SDK_INT >= 26) {


            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.BLUE);
            channel.setDescription("SynContact");
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);    // Notification.VISIBILITY_PRIVATE
            getManager().createNotificationChannel(channel);
        }
    }


    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {

            if (imageUrl.trim().equals("")) {
                return null;
            }
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }

    public class createBitmapFromURL extends AsyncTask<Void, Void, Bitmap> {
        URL NotifyPhotoUrl;
        String imgURL;

        public createBitmapFromURL(String imgURL) {
            this.imgURL = imgURL;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap networkBitmap = null;

            try {
                NotifyPhotoUrl = new URL(imgURL);
                networkBitmap = BitmapFactory.decodeStream(
                        NotifyPhotoUrl.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TAG", "Could not load Bitmap from: " + NotifyPhotoUrl);
            }

            return networkBitmap;
        }

        protected void onPostExecute(Bitmap result) {

            bitmap_image = result;
        }
    }


}
