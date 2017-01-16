package edu.dartmouth.cs.healthmatters;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;

public class SelfAffirm extends Activity {
    ImageButton btnShare, btnLike, btnDislike;
    TextView tvMessage;
    ImageView imgView;
    int id=-1;
    int pollid=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_affirm);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                id=0;
            } else {
                id= extras.getInt(Globals.POPUP_INTENT_EXTRA);
                pollid= extras.getInt(Globals.POLL_INTENT_EXTRA);

            }
        }
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        tvMessage.setText(Globals.SELF_AFFIRM[id]);
        imgView = (ImageView) findViewById(R.id.imageView);
        updateImageView();
        btnLike = (ImageButton) findViewById(R.id.like);
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper myDb = new DBHelper(getBaseContext());

                myDb.insertSelfAffirmLike(id, System.currentTimeMillis(), 1);

                finish();
            }
        });

        btnDislike = (ImageButton) findViewById(R.id.dislike);
        btnDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper myDb = new DBHelper(getBaseContext());
                myDb.insertSelfAffirmLike(id, System.currentTimeMillis(), 0);

                finish();
            }
        });
        btnShare = (ImageButton) findViewById(R.id.share);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper myDb = new DBHelper(getBaseContext());
                myDb.insertSelfAffirmLike(id, System.currentTimeMillis(), 2);

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

    private void updateImageView() {
        switch (id){
            case 0: imgView.setImageResource(R.drawable.s1);
                break;
            case 1: imgView.setImageResource(R.drawable.s2);
                break;
            case 2: imgView.setImageResource(R.drawable.s3);
                break;
            case 3: imgView.setImageResource(R.drawable.s4);
                break;
            case 4: imgView.setImageResource(R.drawable.s5);
                break;
            case 5: imgView.setImageResource(R.drawable.s6);
                break;
            case 6: imgView.setImageResource(R.drawable.s7);
                break;
            case 7: imgView.setImageResource(R.drawable.s8);
                break;
            case 8: imgView.setImageResource(R.drawable.s9);
                break;
            case 9: imgView.setImageResource(R.drawable.s10);
                break;
            case 10: imgView.setImageResource(R.drawable.s11);
                break;
            case 11: imgView.setImageResource(R.drawable.s12);
                break;
            case 12: imgView.setImageResource(R.drawable.s13);
                break;
            case 13: imgView.setImageResource(R.drawable.s14);
                break;
            case 14: imgView.setImageResource(R.drawable.s15);
                break;
            case 15: imgView.setImageResource(R.drawable.s16);
                break;
            default: imgView.setImageResource(R.drawable.s1);
                break;


        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(),"Cannot Go Back", Toast.LENGTH_SHORT).show();
    }
}
