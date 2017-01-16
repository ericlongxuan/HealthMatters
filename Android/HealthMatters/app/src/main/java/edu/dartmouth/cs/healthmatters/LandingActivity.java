package edu.dartmouth.cs.healthmatters;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.DialogPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.radiusnetworks.proximity.ProximityKitBeacon;
import com.radiusnetworks.proximity.ProximityKitGeofenceRegion;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LandingActivity extends Activity {
    public static String DB_FILEPATH = "/data/data/edu.dartmouth.cs.healthmatters/databases/HealthMatters.db";
    private boolean askPopup = false;
    private boolean askPoll = false;
    public static final String TAG = "LandingActivity";
    Map<String, TableRow> rowMap = new HashMap<String, TableRow>();
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private Button btnLogin, btnUpload;
    private TextView tvStatus;
    private String username, password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            checkForPermissions();
        } else {
            startService(new Intent(getBaseContext(), BeaconMonitor.class));

        }

        btnLogin = (Button)findViewById(R.id.btnSignIn);
        btnUpload = (Button)findViewById(R.id.btnUpload);
        tvStatus = (TextView) findViewById(R.id.tvStatus);



        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               dumpDb();
                uploadData();

            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(i);
            }
        });




    }
    private void checkForPermissions() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(Manifest.permission.ACCESS_COARSE_LOCATION)){
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//            permissionsNeeded.add("Location");

        }
        if (!addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            permissionsNeeded.add("Write");

        }

        if (permissionsNeeded.size() > 0) {
            requestPermissions(permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                    1);
            return;
        } else
        {
            startService(new Intent(getBaseContext(), BeaconMonitor.class));

        }

    }
    private boolean addPermission(String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
            // Check for Rationale Optio
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedpreferences = getSharedPreferences(Globals.LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        username= sharedpreferences.getString(Globals.USERNAME_KEY, "");
        password = sharedpreferences.getString(Globals.PASSWORD_KEY,"");
        if(username.equals("")){
            tvStatus.setText("Please Login. Your Response is not getting uploaded.");
            btnLogin.setEnabled(true);
        } else{
            tvStatus.setText("Logged in as- "+username);
            btnLogin.setEnabled(false);
        }
        dumpDb();
        uploadData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
            {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    startService(new Intent(getBaseContext(), BeaconMonitor.class));

                } else {
                    // Permission Denied
                    Toast.makeText(getApplicationContext(), "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void dumpDb() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            File file = new File(sd, "HealthMatters");
            if (!file.exists()) {
                file.mkdirs();
            }
            if (sd.canWrite()) {
                String currentDBPath = "//data//edu.dartmouth.cs.healthmatters//databases//HealthMatters.db";
                String backupDBPath = (int)System.currentTimeMillis()/1000+"_healthmatters"+".db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(file, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    DBHelper myDB = new DBHelper(this);
                    myDB.dumpDb();
                    Toast.makeText(getApplicationContext(),"Data Dump Successful", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
        }
    }

    public void uploadData(){
        if(!username.equals("")){
            String url = Globals.UPLOAD_URL + "/" + username + "/" + password;
            new UploadData().execute(url);
        }
    }

    private class UploadData extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... params) {
            File sd = Environment.getExternalStorageDirectory();
            File parentDir = new File(sd, "HealthMatters");
            File[] files = parentDir.listFiles();
            for(int i=0;i<files.length;i++){
                if(HistoryUploader.uploadFileApache(files[i], params[0])){
                    files[i].delete();
                }
            }
            return "Done";
        }
    }

    /**
     * Turn the Proximity Kit manager on and update the UI accordingly.
     */
    private void startManager() {
        AndroidProximityKitReferenceApplication app = (AndroidProximityKitReferenceApplication) getApplication();

        app.startManager();
    }

    /**
     * Turn the Proximity Kit manager off and update the UI accordingly.
     */
    private void stopManager() {
        AndroidProximityKitReferenceApplication app = (AndroidProximityKitReferenceApplication) getApplication();

        app.stopManager();

    }
}
