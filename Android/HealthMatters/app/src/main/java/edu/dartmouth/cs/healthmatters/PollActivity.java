package edu.dartmouth.cs.healthmatters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class PollActivity extends Activity {
    private int pollid = -1;
    private Button btnSubmit, btnYes, btnNo;
    private TextView tvQuestion;
    private DiscreteSeekBar seekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                pollid=0;
            } else {
                pollid= extras.getInt(Globals.POLL_INTENT_EXTRA);

            }
        }
        tvQuestion = (TextView) findViewById(R.id.tvQuestion);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnYes = (Button) findViewById(R.id.btnYes);
        btnNo = (Button) findViewById(R.id.btnNo);
        seekBar = (DiscreteSeekBar) findViewById(R.id.discreteSeek);
        tvQuestion.setText(Globals.POLL_QUESTIONS_LIST[pollid]);
        if (pollid==6){
            seekBar.setVisibility(View.GONE);
            btnYes.setVisibility(View.VISIBLE);
            btnNo.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.GONE);
        } else {
            seekBar.setVisibility(View.VISIBLE);
            btnYes.setVisibility(View.GONE);
            btnNo.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.VISIBLE);

            if(pollid==4){
                seekBar.setMin(1);
                seekBar.setMax(5);
                seekBar.setProgress(1);
            }
            if (pollid==5){
                seekBar.setMin(1);
                seekBar.setMax(7);
                seekBar.setProgress(1);

            }

        }
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper myDb = new DBHelper(getBaseContext());
                myDb.insertPollResponse(pollid, System.currentTimeMillis(), 1);
                handleResponse(pollid, 1);

            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper myDb = new DBHelper(getBaseContext());
                myDb.insertPollResponse(pollid, System.currentTimeMillis(), 0);
                handleResponse(pollid, 0);

            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper myDb = new DBHelper(getBaseContext());
                myDb.insertPollResponse(pollid, System.currentTimeMillis(), seekBar.getProgress());
//                Toast.makeText(getBaseContext(), "Poll responses- "+ myDb.numberOfRowsPolls(), Toast.LENGTH_SHORT).show();
                handleResponse(pollid, seekBar.getProgress());

            }
        });

    }
    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(),"Cannot Go Back", Toast.LENGTH_SHORT).show();
    }
    public void handleResponse(int id, int value) {
        String response="Sample Response";
        if(id==0){
            if(value<7){
                response = "Trouble sleeping?\n" +
                        "\n" +
                        "Close the book, turn off devices, and put on some socks. Did you know that warm hands and feet are associated with falling asleep more quickly? \n" +
                        "\n" +
                        "And try these study tips:\n" +
                        "• Study during periods of optimal brain function (usually around 6-8 p.m.)\n" +
                        "• Avoid studying in early afternoons, usually the time of least alertness\n";
            } else {
                response = "69% of Dartmouth students, on average, report getting 7 or more hours a sleep per night.";
            }
        } else if(id==1){
            if(value<5){
                response="To maintain a healthy level of stress, practice deep breathing to rest your brain and focus in the moment.";
            } else if(value>=5 && value<=7){
                response="Take a few deeeeeep breaths to rest your brain and focus in the moment.";
            } else if(value>7){
                response = "Take a few deeeeeep breaths to rest your brain and focus in the moment. \n" +
                        "\n" +
                        "Consider stopping by the Student Wellness Center for a check-In or make an appointment with Counseling and Human Development at Dick’s House.\n";
            }
        } else if(id==2){
            response = "What can you do today to boost your happiness level?";
        } else if(id==3){
            response="Over half of Dartmouth students eat at least 3 servings of fruits and vegetables a day.";
        } else if(id==4){
            if (value<4){
                response = "You’re not alone!  About 1 out of 4 Dartmouth students report that they don’t usually put off tasks that need to be done, even if they don’t like them! \n";
            } else {
                response = "You’re not alone! Over half of Dartmouth students report procrastinating on tasks they don’t like.  \n" +
                        "And students with this tendency reported feeling very nervous and stressed in the last past 30 days. \n";
            }
        } else if (id==5){
             if(value<4){
                 response="Bouncing back from challenges can be hard. Finding trusted support can help, like friends or resources on campus." +
                         "\n" +
                         "Consider checking out Counseling and Human Development at Dick’s House to see if there may be helpful resources for you. ";
             } else if(value>3&&value<6){
                 response = "Think about times you’ve felt good about managing a challenge.  What helped to get you through? Draw on what’s worked in the past to get through the tough times. ";
             } else {
                 response = "Good for you!  You’ve figured out what works for you to bounce back from tough stuff. Use these resources when things get challenging in the future.";
             }

        } else{
             response="About 1 out of 5 Dartmouth students have used campus resources for their psychological well-being or mental health in the last 6 months. Support is there if you need it.";

        }
        Intent i = new Intent(getApplicationContext(), PollResponseActivity.class);
        i.putExtra(Globals.POLL_RESPONSE_INTENT_EXTRA, response);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();

    }
}
