package edu.dartmouth.cs.healthmatters;

/**
 * Created by varun on 3/22/16.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "HealthMatters.db";
    public static final String LOCATION_TABLE_NAME = "locations";
    public static final String LOCATION_COLUMN_ID = "locid";
    public static final String LOCATION_COLUMN_TIME = "timestamp";
    public static final String LOCATION_COLUMN_STATE = "state";
    public static final String SELF_AFFIRM_LIKE_TABLE_NAME = "SelfAffirmLikes";
    public static final String SELF_AFFIRM_LIKE_ID = "id";
    public static final String SELF_AFFIRM_LIKE_TIME = "timestamp";
    public static final String SELF_AFFIRM_LIKE_STATE = "liked";
    public static final String POLL_RESPONSE_TABLE = "PollResponseTable";
    public static final String POLL_RESPONSE_ID = "id";
    public static final String POLL_RESPONSE_TIME = "timestamp";
    public static final String POLL_RESPONSE_VALUE = "value";
    public static final String POLL_RESPONSE_LIKED = "PollResponseLikes";

    private HashMap hp;

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table IF NOT EXISTS locations " +
                        "(locid integer,timestamp long,state integer, PRIMARY KEY (locid, timestamp))"
        );
        db.execSQL(
                "create table IF NOT EXISTS SelfAffirmLikes " +
                        "(id integer,timestamp long,liked integer, PRIMARY KEY (id, timestamp))"
        );

        db.execSQL(
                "create table IF NOT EXISTS PollResponseTable " +
                        "(id integer,timestamp long,value integer, PRIMARY KEY (id, timestamp))"
        );

        db.execSQL(
                "create table IF NOT EXISTS PollResponseLikes " +
                        "(response text,timestamp long,liked integer, PRIMARY KEY (timestamp))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS locations");
        db.execSQL("DROP TABLE IF EXISTS SelfAffirmLikes");
        db.execSQL("DROP TABLE IF EXISTS PollResponseTable");
        db.execSQL("DROP TABLE IF EXISTS PollResponseLikes");

        onCreate(db);
    }

    public void dumpDb(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS locations");
        db.execSQL("DROP TABLE IF EXISTS SelfAffirmLikes");
        db.execSQL("DROP TABLE IF EXISTS PollResponseTable");
        db.execSQL("DROP TABLE IF EXISTS PollResponseLikes");
        onCreate(db);
    }

    public boolean insertLocation  (int locid, long timestamp, int state)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("locid", locid);
        contentValues.put("timestamp", timestamp);
        contentValues.put("state", state);
        db.insert(LOCATION_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertSelfAffirmLike (int id, long timestamp, int liked){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("timestamp", timestamp);
        contentValues.put("liked", liked);
        db.insert(SELF_AFFIRM_LIKE_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertPollResponseLikes (String response, long timestamp, int liked){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("response", response);
        contentValues.put("timestamp", timestamp);
        contentValues.put("liked", liked);
        db.insert(POLL_RESPONSE_LIKED, null, contentValues);
        return true;
    }

    public boolean insertPollResponse (int id, long timestamp, int value){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("timestamp", timestamp);
        contentValues.put("value", value);
        db.insert(POLL_RESPONSE_TABLE, null, contentValues);
        return true;
    }


    public long getLastTimestamp(int locid){
        long timestamp=-1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select timestamp from locations where locid=" + locid + " AND state=1 ORDER BY timestamp DESC LIMIT 1", null);
        if (res.getCount()>0) {
            timestamp = res.getLong(0);
        }
        return timestamp;
    }
    public int getCount(int locid, int state){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts where locid="+locid+" AND state="+state+"", null );
        return res.getCount();
    }

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts where id="+id+"", null );
        return res;
    }

    public int numberOfRowsLocations(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, LOCATION_TABLE_NAME);
        return numRows;
    }


    public int numberOfRowsSALiked(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, SELF_AFFIRM_LIKE_TABLE_NAME);
        return numRows;
    }

    public int numberOfRowsPolls(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, POLL_RESPONSE_TABLE);
        return numRows;
    }



    public Integer deleteContact (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllCotacts()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(LOCATION_TABLE_NAME)));
            res.moveToNext();
        }
        return array_list;
    }
}