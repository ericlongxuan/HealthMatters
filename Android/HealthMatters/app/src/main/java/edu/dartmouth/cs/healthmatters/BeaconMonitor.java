package edu.dartmouth.cs.healthmatters;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class BeaconMonitor extends Service {
    public int running=0;
    public BeaconMonitor() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((AndroidProximityKitReferenceApplication) getApplication()).setBeaconService(this);
//        if (isRunning) {
//        SharedPreferences sharedpreferences = getSharedPreferences(Globals.SERVICE_PREFERENCE, Context.MODE_PRIVATE);
//        int servpref= sharedpreferences.getInt(Globals.SERIVCE_PREF_START, 0);
//        Log.d("TAGG", "Service--"+servpref);
//        if(servpref==0) {
//            sharedpreferences = getSharedPreferences(Globals.SERVICE_PREFERENCE, Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedpreferences.edit();
//
//            editor.putInt(Globals.SERIVCE_PREF_START, 1);
//            editor.commit();
//
//
//
//        }
        startManager();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        SharedPreferences sharedpreferences = getSharedPreferences(Globals.SERVICE_PREFERENCE, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedpreferences.edit();
//
//        editor.putLong(Globals.SERIVCE_PREF_START, 0);
//        editor.commit();

        Log.d("Service", "OnDestroy");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Intent notificationIntent = new Intent(this, LandingActivity.class);
        notificationIntent.setAction("edu.dartmouth.cs.healthmatters.LandingActivity");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("HealthMatters")
                .setContentText("Scanning Beacons")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();
        startForeground(101,
                notification);
        return START_STICKY;
    }

    private void startManager() {
        AndroidProximityKitReferenceApplication app = (AndroidProximityKitReferenceApplication) getApplication();

        app.startManager();
    }
}
