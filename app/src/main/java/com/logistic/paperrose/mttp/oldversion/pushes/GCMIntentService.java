package com.logistic.paperrose.mttp.oldversion.pushes;

/**
 * Created by paperrose on 15.12.2014.
 */

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class GCMIntentService extends IntentService {

    String timestamp;
    String title;
    String description;
    String type;
    String record_id;
    private Handler handler;
    public GCMIntentService() {
        super("GcmIntentService");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        handler = new Handler();
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (getSharedPreferences("UserPreference", 0).getString("access_token", "").isEmpty()) return;
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        timestamp = extras.getString("date");
        title = extras.getString("title");
        type = extras.getString("type");
        record_id = extras.getString("record_id");
        try {
            description = extras.getString("description");
            if (description == null) {
                description = extras.getString("data");
            }
        } catch (Exception e) {
            description = extras.getString("data");
        }


        showToast();
        Log.i("GCM", "Received : (" +messageType+")  "+extras.getString("title"));

        GCMBroadcastReceiver.completeWakefulIntent(intent);

    }


    ArrayList<PushItem> pushes = new ArrayList<PushItem>();

    public void addPushToHistory(PushItem item) {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor prefsEditor = historyPrefs.edit();
        String jsonH = historyPrefs.getString("history", "");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<PushItem>>(){}.getType();
        if (jsonH == "")
            pushes = new ArrayList<PushItem>();
        else
            pushes = gson.fromJson(jsonH, type);
        pushes.add(item);
        String json = gson.toJson(pushes);
        prefsEditor.putString("history", json);
        prefsEditor.commit();
    }



    private ArrayList<TripleTableField> loadSettings() {
        SharedPreferences historyPrefs = getSharedPreferences("UserPreference", 0);
        Gson gson = new Gson();
        String jsonH = historyPrefs.getString("settings_all", "");
        String jsonS = historyPrefs.getString("settings_selected", "");
        Type type = new TypeToken<ArrayList<TripleTableField>>(){}.getType();
        if (jsonH != "") {
            ApplicationParameters.tableFields = gson.fromJson(jsonH, type);
        }
        if (jsonS == "") {
            ApplicationParameters.chosenTableFields = new ArrayList<TripleTableField>();
            return new ArrayList<TripleTableField>();
        } else {
            ApplicationParameters.chosenTableFields = gson.fromJson(jsonS, type);
            return gson.fromJson(jsonS, type);
        }
    }

    private String unreadCountText(int count) {
        int cn1 = count%100;
        int cn2 = count%10;
        if (cn1 > 10 && cn1 < 19) {
            return "новых сообщений";
        }
        if (cn2 == 1) return "новое сообщение";
        if (cn2 == 0) return "новых сообщений";
        if (cn2 > 4) return "новых сообщений";
        return "новых сообщения";

    }

    public void showToast(){
        handler.post(new Runnable() {
            public void run() {
                if (timestamp == null) return;
                if (ApplicationParameters.chosenTableFields.size() == 0) {
                    loadSettings();
                }
                Context context = getApplicationContext();
                //Toast.makeText(context, title + "\n" + description , Toast.LENGTH_LONG).show();
                PushItem pi = new PushItem(timestamp, title, description);
                pi.setType(type);
                pi.record_id = record_id;
                addPushToHistory(pi);
                Intent intent = new Intent("refresh_push_history");
                intent.putExtra("date", timestamp);
                intent.putExtra("title", title);
                intent.putExtra("description", description);
                intent.putExtra("type", type);
                intent.putExtra("record_id", record_id);
                SharedPreferences unread = getSharedPreferences("UserPreference", 0);
                int unread_count = unread.getInt("unread", 0);
                unread_count++;
                SharedPreferences.Editor prefsEditor = unread.edit();
                prefsEditor.putInt("unread", unread_count);
                prefsEditor.commit();
              /*  try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                Intent intent2 = new Intent("refresh_push_count");

                String txt =  "У вас " + Integer.toString(unread_count) + " " + unreadCountText(unread_count) + ".";
                Notification notif = new Notification(R.drawable.g_soft_logo_canavara,txt, System.currentTimeMillis() );
                notif.flags = Notification.FLAG_AUTO_CANCEL;

                notif.defaults |= Notification.DEFAULT_SOUND;
                notif.defaults |= Notification.DEFAULT_VIBRATE;

                Intent notificationIntent = new Intent(context, MyActivity.class);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

                notif.setLatestEventInfo(context, getResources().getString(R.string.company), txt, contentIntent);
                if (ApplicationParameters.lightScreen) {
                    PowerManager.WakeLock screenOn = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "example");
                    screenOn.acquire();
                }
                String ns = Context.NOTIFICATION_SERVICE;
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
                mNotificationManager.notify(1, notif);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);







            }
        });

    }
}
