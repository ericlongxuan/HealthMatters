package edu.dartmouth.cs.healthmatters;

import android.app.AlertDialog;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.radiusnetworks.proximity.KitConfig;
import com.radiusnetworks.proximity.ProximityKitBeacon;
import com.radiusnetworks.proximity.ProximityKitBeaconRegion;
import com.radiusnetworks.proximity.ProximityKitGeofenceNotifier;
import com.radiusnetworks.proximity.ProximityKitGeofenceRegion;
import com.radiusnetworks.proximity.ProximityKitManager;
import com.radiusnetworks.proximity.ProximityKitMonitorNotifier;
import com.radiusnetworks.proximity.ProximityKitRangeNotifier;
import com.radiusnetworks.proximity.ProximityKitSyncNotifier;
import com.radiusnetworks.proximity.beacon.BeaconManager;
import com.radiusnetworks.proximity.geofence.GooglePlayServicesException;
import com.radiusnetworks.proximity.model.KitBeacon;
import com.radiusnetworks.proximity.model.KitOverlay;

import org.altbeacon.beacon.BeaconParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 */
public class AndroidProximityKitReferenceApplication
        extends Application
        implements ProximityKitMonitorNotifier,
        ProximityKitRangeNotifier,
        ProximityKitSyncNotifier{

    private boolean askPopup = false;
    private boolean askPoll = false;

    /**
     * Custom metadata key specific to the associated kit
     */
    private static final String MAIN_OFFICE_LOCATION = "main-office";

    /**
     * General logging tag
     */
    public static final String TAG = "PKReferenceApplication";

    /**
     * Singleton storage for an instance of the manager
     */
    private static ProximityKitManager pkManager = null;

    /**
     * Object to use as a thread-safe lock
     */
    private static final Object pkManagerLock = new Object();

    /**
     * Flag for tracking if the app was started in the background.
     */
    private boolean haveDetectedBeaconsSinceBoot = false;

    /**
     * Reference to the main activity - used in callbacks
     */
    private LandingActivity mainActivity = null;

    private BeaconMonitor beaconService = null;

    @Override
    /**
     * It is the job of the application to ensure that Google Play services is available before
     * enabling geofences in Proximity Kit.
     *
     * A good place to do this is when we set the Proximity Kit manager instance. However there are
     * issues with this decision. See the notes in <code>servicesConnected()</code> for details.
     *
     * This is also where we are setting the notifier callbacks. Be aware that, currently, only one
     * notifier can be set per notifier type. This means if the app (this demo app does not) sets
     * another notifier somewhere else, it will overwrite this notifier.
     *
     * @see #servicesConnected()
     * @see <a href="https://developer.android.com/google/play-services/setup.html">
     *          Setup Google Play services
     *      </a>
     */
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() called");

        /*
         * The app is responsible for handling the singleton instance of the Proximity Kit manager.
         * To ensure we have a single instance we synchronize our creation process.
         *
         * While this is not necessary inside an `Application` subclass it is necessary if the
         * single manager instance is created inside an `Activity` or other Android/Java component.
         * We're including the pattern here to show a method of ensuring a singleton instance.
         */
        synchronized (pkManagerLock) {
            if (pkManager == null) {
                pkManager = ProximityKitManager.getInstance(this, loadConfig());
            }
        }

        /* ----- begin code only for debugging ---- */

        pkManager.debugOn();

        /* ----- end code only for debugging ------ */

        /*
         * All current versions of ProximityKit Android use the AltBeacon/android-beacon-library
         * under the hood.
         *
         * By default only the AltBeacon format is picked up on Android devices. However, you are
         * free to configure your own custom format by registering a parser with your
         * ProximityKitManager's BeaconManager.
         */
        BeaconManager beaconManager = pkManager.getBeaconManager();
        beaconManager.getBeaconParsers().add(
                new BeaconParser().setBeaconLayout(
                        "m:2-5=c0decafe,i:6-13,i:14-17,p:18-18,d:19-22,d:23-26"
                )
        );
        try{
            // set the duration of the scan to be 1.1 seconds
            pkManager.getBeaconManager().setBackgroundScanPeriod(2000l);
            // set the time between each scan to be 1 hour (3600 seconds)//50000l
            pkManager.getBeaconManager().setBackgroundBetweenScanPeriod(15000l);//3600000l
            pkManager.getBeaconManager().updateScanPeriods();
            //beaconManager.startMonitoringBeaconsInRegion(region);

        }  catch (RemoteException e) {
            //Log.e(TAG, "Cannot talk to service");
        }



        /*
         * Set desired callbacks before calling `start()`.
         *
         * We can set these notifications after calling `start()`. However, this means we will miss
         * any notifications posted in the time between those actions.
         *
         * You are free to set only the notifiers you want callbacks for. We are setting all of them
         * to demonstrate how each set works.
         */
        pkManager.setProximityKitSyncNotifier(this);
        pkManager.setProximityKitMonitorNotifier(this);
        pkManager.setProximityKitRangeNotifier(this);

        /*
         * Now that we potentially have geofences setup and our notifiers are registered, we are
         * ready to start Proximity Kit.
         *
         * We could start it right now with:
         *
         *      pkManager.start();
         *
         * Instead we are letting the user decide when to start or stop in the UI.
         */
    }

    /**
     * Start the Proximity Kit Manager.
     * <p/>
     * Allows the app to control when the Proximity Kit manager is running. This can similarly used
     * by libraries to hook into when Proximity Kit manager should run.
     */
    public void startManager() {
        pkManager.start();
    }

    /**
     * Stop the Proximity Kit Manager.
     * <p/>
     * Allows the app to control when the Proximity Kit manager is running. This can similarly used
     * by libraries to hook into when Proximity Kit manager should run.
     */
    public void stopManager() {
        pkManager.stop();
    }



    /**
     * Set main activity for app display related callbacks.
     *
     * @param beaconService
     *         <code>Activity</code> to send app display related callbacks
     */
    public void setBeaconService(BeaconMonitor beaconService) {
        this.beaconService = beaconService;
    }

    /***********************************************************************************************
     * START ProximityKitSyncNotifier
     **********************************************************************************************/

    @Override
    /**
     * Called when data has been sync'd with the Proximity Kit server.
     */
    public void didSync() {
        Log.d(TAG, "didSync(): Sycn'd with server");

        // Access every beacon configured in the kit, printing out the value of an attribute
        // named "myKey"
        for (KitBeacon beacon : pkManager.getKit().getBeacons()) {
            Log.d(
                    TAG,
                    "For beacon: " + beacon.getProximityUuid() + " " + beacon.getMajor() + " " +
                            beacon.getMinor() + ", the value of welcomeMessage is " +
                            beacon.getAttributes().get("welcomeMessage")
            );
        }

        // Access every geofence configured in the kit, printing out the value of an attribute
        // named "myKey"
        for (KitOverlay overlay : pkManager.getKit().getOverlays()) {
            Log.d(
                    TAG,
                    "For geofence: (" + overlay.getLatitude() + ", " + overlay.getLongitude() +
                            ") with radius " + overlay.getRadius() + ", the value of myKey is " +
                            overlay.getAttributes().get("myKey")
            );
        }
    }

    @Override
    /**
     * Called when syncing with the Proximity Kit server failed.
     *
     * @param e     The exception encountered while syncing
     */
    public void didFailSync(Exception e) {
        Log.d(TAG, "didFailSync() called with exception: " + e);
    }

    /***********************************************************************************************
     * END ProximityKitSyncNotifier
     **********************************************************************************************/

    /***********************************************************************************************
     * START ProximityKitRangeNotifier
     * ********************************************************************************************/

    @Override
    /**
     * Called whenever the Proximity Kit manager sees registered beacons.
     *
     * @param beacons   a collection of <code>ProximityKitBeacon</code> instances seen in the most
     *                  recent ranging cycle.
     * @param region    The <code>ProximityKitBeaconRegion</code> instance that was used to start
     *                  ranging for these beacons.
     */
    public void didRangeBeaconsInRegion(Collection<ProximityKitBeacon> beacons, ProximityKitBeaconRegion region) {
        if (beacons.size() == 0) {
            return;
        }

        Log.d(TAG, "didRangeBeaconsInRegion: size=" + beacons.size() + " region=" + region);

        for (ProximityKitBeacon beacon : beacons) {
            Log.d(
                    TAG,
                    "I have a beacon with data: " + beacon + " attributes=" +
                            beacon.getAttributes()
            );

            // We've wrapped up further behavior in some internal helper methods
            // Check their docs for details on additional things which you can do we beacon data
            handleBeacon(beacon);
        }
    }

    /***********************************************************************************************
     * END ProximityKitRangeNotifier
     **********************************************************************************************/

    /***********************************************************************************************
     * START ProximityKitMonitorNotifier
     **********************************************************************************************/

    @Override
    /**
     * Called when at least one beacon in a <code>ProximityKitBeaconRegion</code> is visible.
     *
     * @param region    an <code>ProximityKitBeaconRegion</code> which defines the criteria of
     *                  beacons being monitored
     */
    public void didEnterRegion(ProximityKitBeaconRegion region) {
        // In this example, this class sends a notification to the user whenever an beacon
        // matching a Region (defined above) are first seen.
        Log.d(
                TAG,
                "ENTER beacon region: " + region + " " +
                        region.getAttributes().get("welcomeMessage")
        );

        // Attempt to open the app now that we've entered a region if we started in the background
        tryAutoLaunch();

        // Notify the user that we've seen a beacon
        sendNotification(region);
    }

    @Override
    /**
     * Called when no more beacons in a <code>ProximityKitBeaconRegion</code> are visible.
     *
     * @param region    an <code>ProximityKitBeaconRegion</code> that defines the criteria of
     *                  beacons being monitored
     */
    public void didExitRegion(ProximityKitBeaconRegion region) {
        Log.d(TAG, "didExitRegion called with region: " + region);
    }

    @Override
    /**
     * Called when a the state of a <code>Region</code> changes.
     *
     * @param state     set to <code>ProximityKitMonitorNotifier.INSIDE</code> when at least one
     *                  beacon in a <code>ProximityKitBeaconRegion</code> is now visible; set to
     *                  <code>ProximityKitMonitorNotifier.OUTSIDE</code> when no more beacons in the
     *                  <code>ProximityKitBeaconRegion</code> are visible
     * @param region    an <code>ProximityKitBeaconRegion</code> that defines the criteria of
     *                  beacons being monitored
     */
    public void didDetermineStateForRegion(int state, ProximityKitBeaconRegion region) {
        Log.d(TAG, "didDeterineStateForRegion called with state: " + state + "\tregion: " + region);
        DBHelper myDb = new DBHelper(this);
        int locid = getLocationID(region);
        long timestamp = -1;
        switch (state) {
            case ProximityKitMonitorNotifier.INSIDE:
                //timestamp=myDb.getLastTimestamp(locid);
                handleState(locid, state);
                showContent();

                String welcomeMessage = region.getAttributes().get("welcomeMessage");
                if (welcomeMessage != null) {
                    Log.d(TAG, "Beacon " + region + " says: " + welcomeMessage);
                }
                break;
            case ProximityKitMonitorNotifier.OUTSIDE:
                myDb.insertLocation(locid, System.currentTimeMillis(), state);

                String goodbyeMessage = region.getAttributes().get("goodbyeMessage");
                if (goodbyeMessage != null) {
                    Log.d(TAG, "Beacon " + region + " says: " + goodbyeMessage);
                }
                break;
            default:
                Log.d(TAG, "Received unknown state: " + state);
                break;
        }
    }

    public void handleState(int locid, int state){
        long currentTimestamp = System.currentTimeMillis();
        DBHelper myDb = new DBHelper(this);

        myDb.insertLocation(locid, currentTimestamp, state );
//                Toast.makeText(getApplicationContext(), "Total locations-"+myDb.numberOfRowsLocations(), Toast.LENGTH_SHORT).show();
        SharedPreferences sharedpreferences = getSharedPreferences(Globals.POPUP_PREFERENCES, Context.MODE_PRIVATE);
        long lastTimestamp= sharedpreferences.getLong(Globals.POPUP_TIMESTAMP, 0);
        int dayCount = sharedpreferences.getInt(Globals.DAY_COUNT_KEY,0);
        Calendar lastcal = Calendar.getInstance();
        lastcal.setTimeInMillis(lastTimestamp);
        int lastday = lastcal.get(Calendar.DATE);
        Log.d("TAGG", "Lasttime--"+lastTimestamp);
        if(lastTimestamp!=0){
            if(currentTimestamp-lastTimestamp >= Globals.TIME_GAP ){
                Calendar todaycal = Calendar.getInstance();
                todaycal.setTimeInMillis(currentTimestamp);
                int today = todaycal.get(Calendar.DATE);
                if (today>lastday){
                    dayCount=0;
                }
                if(dayCount<Globals.DAY_COUNT){
                    askPopup=true;
                }
                if(dayCount==1 && dayCount<Globals.DAY_COUNT){
                    askPopup=false;
                    askPoll=true;
                }
                dayCount++;
                sharedpreferences = getSharedPreferences(Globals.POPUP_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putLong(Globals.POPUP_TIMESTAMP, currentTimestamp);
                editor.putInt(Globals.DAY_COUNT_KEY, dayCount);
                editor.commit();


            }
        } else {
            sharedpreferences = getSharedPreferences(Globals.POPUP_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();

            editor.putLong(Globals.POPUP_TIMESTAMP, currentTimestamp);
            editor.putInt(Globals.DAY_COUNT_KEY, 1);

            editor.commit();
            askPopup=true;
        }

    }

    public void showContent() {
        Log.d("TAGG", "showContent");
        if(askPopup) {
            SharedPreferences sharedpreferences = getSharedPreferences(Globals.POPUP_PREFERENCES, Context.MODE_PRIVATE);
            Set<String> set = sharedpreferences.getStringSet(Globals.POPUP_QUESTIONS, null);

            if (set == null|| set.size()==0) {
                set = new HashSet<String>(Arrays.asList(Globals.SELF_AFFIRM_INDEX));
            }
            ArrayList<String> data = new ArrayList<String>();
            data.addAll(set);
            int element = Integer.parseInt(data.get(0));
            data.remove(0);
            set.clear();
            set.addAll(data);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putStringSet(Globals.POPUP_QUESTIONS, set);
            editor.commit();

            Intent i = new Intent(getApplicationContext(), SelfAffirm.class);
            i.putExtra(Globals.POPUP_INTENT_EXTRA, element);
            i.putExtra(Globals.POLL_INTENT_EXTRA, -1);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            askPoll=false;
            askPopup = false;
            startActivity(i);
        }
        int elementPoll=-1;
        if (askPoll){
            SharedPreferences sharedpreferencesPoll = getSharedPreferences(Globals.POLL_PREFERENCES, Context.MODE_PRIVATE);
            Set<String> setPoll = sharedpreferencesPoll.getStringSet(Globals.POLL_QUESTIONS, null);

            if (setPoll == null || setPoll.size()==0) {
                setPoll = new HashSet<String>(Arrays.asList(Globals.POLL_INDEX));
            }
            ArrayList<String> dataPoll = new ArrayList<String>();
            dataPoll.addAll(setPoll);
            elementPoll = Integer.parseInt(dataPoll.get(0));
            dataPoll.remove(0);
            setPoll.clear();
            setPoll.addAll(dataPoll);
            SharedPreferences.Editor editorPoll = sharedpreferencesPoll.edit();
            editorPoll.putStringSet(Globals.POLL_QUESTIONS, setPoll);
            editorPoll.commit();
            Intent i = new Intent(getApplicationContext(), PollActivity.class);
            i.putExtra(Globals.POLL_INTENT_EXTRA, elementPoll);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            askPoll=false;
            askPopup = false;
            startActivity(i);
        }
    }

    /***********************************************************************************************
     * END ProximityKitMonitorNotifier
     **********************************************************************************************/


    /***********************************************************************************************
     * START App Helpers
     **********************************************************************************************/

    /**
     * App helper method to notify an activity when we see a beacon.
     *
     * @param beacon
     *         <code>org.altbeacon.beacon.Beacon</code> instance of the
     *         beacon seen
     */
    private void handleBeacon(ProximityKitBeacon beacon) {
        if (beaconService == null || beacon == null) {
            return;
        }

        // We could instead call beacon.toString() which wraps up the identifiers


        String displayString = beacon.getId1() + " " +
                beacon.getId2().toInt() + " " + beacon.getId3().toInt() +
                "\nWelcome message: " + beacon.getAttributes().get("welcomeMessage");

        // We've elected to notify our only view of the beacon and a message to display
    }

    /**
     * App helper method to force Proximity Kit to sync.
     * <p/>
     * The Proximity Kit manager should automatically sync every hour, however, we can force an
     * ad-hoc sync anytime we want. This demonstrates how to do that.
     */
    private void forceSync() {
        Log.d(TAG, "Forcing a sync with the Proximity Kit server");
        pkManager.sync();
    }

    /**
     * Generate a consistent ID string given three identifier tokens.
     *
     * @param id1
     *         Identifier token 1
     * @param id2
     *         Identifier token 2
     * @param id3
     *         Identifier token 3
     * @return An ID string representing the three tokens.
     */
    private String generateId(Object id1, Object id2, Object id3) {
        return id1.toString() + "-" + id2 + "-" + id3;
    }

    /**
     * Generate the app's Proximity Kit configuration.
     * <p/>
     * This loads the properties for a kit from a {@code .properties} file bundled in the app. This
     * file was be downloaded from the <a href="https://proximitykit.radiusnetworks.com">Proximity
     * Kit server</a>.
     * <p/>
     * For newer Android applications, the file can be added to the {@code /assets} folder:
     * <p/>
     * <pre>
     * {@code Properties properties = new Properties();
     * try {
     *     properties.load(getAssets().open("ProximityKit.properties"));
     * } catch (IOException e) {
     *     throw new IllegalStateException("Unable to load properties file!", e);
     * }
     * new Configuration(properties);
     * }
     * </pre>
     * <p/>
     * For older Android applications, or if you just prefer using Java resources, the file can be
     * added to the {@code /resources} folder:
     * <p/>
     * <pre>
     * {@code Properties properties = new Properties();
     * InputStream in = getClassLoader().getResourceAsStream("ProximityKit.properties");
     * if (in == null) {
     *     throw new IllegalStateException("Unable to find ProximityKit.properties files");
     * }
     * try {
     *     properties.load(in);
     * } catch (IOException e) {
     *     throw new IllegalStateException("Unable to load properties file!", e);
     * }
     * new Configuration(properties);
     * }
     * </pre>
     * <p/>
     * These details could just as easily been statically compiled into the app. They also could
     * have been downloaded from a 3rd party server.
     *
     * @return A new {@link KitConfig} configured for the app's kit.
     */
    private KitConfig loadConfig() {
        Properties properties = new Properties();
        try {
            properties.load(getAssets().open("ProximityKit.properties"));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load properties file!", e);
        }
        return new KitConfig(properties);
    }

    /**
     * Send a notification stating a beacon is nearby.
     *
     * @param region
     *         The beacon region that was seen.
     */
    private void sendNotification(ProximityKitBeaconRegion region) {
        Log.d(TAG, "Sending notification.");
//        NotificationCompat.Builder builder =
//                new NotificationCompat.Builder(this)
//                        .setContentTitle("Proximity Kit Reference Application")
//                        .setContentText("An beacon is nearby.")
//                        .setSmallIcon(R.drawable.ic_launcher);
//
//        NotificationManager notificationManager =
//                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(1, builder.build());
    }


    /**
     * Attempt to launch the main activity if we were started in the background.
     */
    private void tryAutoLaunch() {
        if (haveDetectedBeaconsSinceBoot) {
            return;
        }

        // If we were started in the background for some reason
        Log.d(TAG, "auto launching MainActivity");

        // The very first time since boot that we detect an beacon, we launch the
        // MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // **IMPORTANT**: Make sure to add android:launchMode="singleInstance" in the manifest
        // to keep multiple copies of this activity from getting created if the user has
        // already manually launched the app.
        //startActivity(intent);
        haveDetectedBeaconsSinceBoot = true;
    }

    public int getLocationID(ProximityKitBeaconRegion beacon){
        int locid = -1;
        if(beacon.getId2().toInt()==1){
            if (beacon.getId3().toInt()==1){
                locid=1;
            }
            if (beacon.getId3().toInt()==3){
                locid=3;
            }
        }
        if(beacon.getId2().toInt()==2){
            if (beacon.getId3().toInt()==1){
                locid=2;
            }
        }
        if(beacon.getId2().toInt()==99){
            if (beacon.getId3().toInt()==10){
                locid=10;
            }
            if (beacon.getId3().toInt()==20){
                locid=20;
            }
        }
        if(beacon.getId2().toInt()==19){
            if (beacon.getId3().toInt()==2){
                locid=30;
            }
            if (beacon.getId3().toInt()==3){
                locid=40;
            }
        }
        return locid;
    }
    /***********************************************************************************************
     * END App Helpers
     **********************************************************************************************/
}
