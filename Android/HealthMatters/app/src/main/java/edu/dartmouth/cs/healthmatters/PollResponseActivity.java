package edu.dartmouth.cs.healthmatters;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.OutputStream;

public class PollResponseActivity extends Activity {
    private String response="";
    private TextView tvFeedback;
    private ImageButton btnLike, btnDislike, btnShare;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_response);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                response="Sample Response";
            } else {
                response= extras.getString(Globals.POLL_RESPONSE_INTENT_EXTRA);

            }
        }
        tvFeedback = (TextView) findViewById(R.id.tvFeedback);
        tvFeedback.setText(response);
        btnLike = (ImageButton) findViewById(R.id.plike);
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper myDb = new DBHelper(getBaseContext());

                myDb.insertPollResponseLikes(response, System.currentTimeMillis(), 1);

                finish();
            }
        });

        btnDislike = (ImageButton) findViewById(R.id.pdislike);
        btnDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper myDb = new DBHelper(getBaseContext());
                myDb.insertPollResponseLikes(response, System.currentTimeMillis(), 0);

                finish();
            }
        });
        btnShare = (ImageButton) findViewById(R.id.pshare);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper myDb = new DBHelper(getBaseContext());
                myDb.insertPollResponseLikes(response, System.currentTimeMillis(), 2);
                handleShare();
            }
        });
    }

    private void handleShare(){
        View v1 = getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "title");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);


        OutputStream outstream;
        try {
            outstream = getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
            outstream.close();
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Share Image"));



    }

}
