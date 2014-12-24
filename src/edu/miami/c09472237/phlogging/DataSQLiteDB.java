package edu.miami.c09472237.phlogging;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataSQLiteDB {
	
	public static final String DATABASE_NAME = "Phlogging.db";
    private static final int DATABASE_VERSION = 9;
    
    private static final String PHLOG_TABLE_NAME = "Phlog";
    private static final String CREATE_PHLOG_TABLE =
    		"CREATE TABLE IF NOT EXISTS " + PHLOG_TABLE_NAME + 
    		"(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
    		"title TEXT, " +
    		"text TEXT," + 
    		"date TEXT NOT NULL," +
    		"time TEXT NOT NULL," +
    		"image_id TEXT NOT NULL," +
    		"location TEXT," +
    		"orientation TEXT" +
    		");";

    private DatabaseHelper dbHelper;
    private SQLiteDatabase theDB;
    
    public DataSQLiteDB(Context theContext) {
        
        dbHelper = new DatabaseHelper(theContext);
        theDB = dbHelper.getWritableDatabase();
        
    }
    
    public void close() {
        
        dbHelper.close();
        theDB.close();
    }
    
    public boolean addPhlog(ContentValues phlogData) {
        
        return(theDB.insert(PHLOG_TABLE_NAME,null,phlogData) >= 0);
    }
    
    public boolean updatePhlog(long phlogId,ContentValues phlogData) {

        return(theDB.update(PHLOG_TABLE_NAME,phlogData,
"_id =" + phlogId,null) > 0);
    }
    
    public boolean deletePhlog(long phlogId) {

        return(theDB.delete(PHLOG_TABLE_NAME,"_id =" + phlogId,
null) > 0);
    }
    
    public Cursor fetchAllPhlogs() {

        String[] fieldNames = {"_id","title","date","time", "image_id", "location", "orientation", "text"};
        
        return(theDB.query(PHLOG_TABLE_NAME,fieldNames,null,null,
null,null,"_id"));
    }
    
    public ContentValues getPhlogById(long phlogId) {
        
        Cursor cursor;
        ContentValues phlogData;
        
        cursor = theDB.query(PHLOG_TABLE_NAME,null,
"_id = \"" + phlogId + "\"",null,null,null,null);
        phlogData = phlogDataFromCursor(cursor);
        cursor.close();
        return(phlogData);
    }
    
    public ContentValues getPhlogByMediaImageId(long mediaImageId) {
        
        Cursor cursor;
        ContentValues phlogData;
        
        cursor = theDB.query(PHLOG_TABLE_NAME,null,
"image_id = " + mediaImageId,null,null,null,null);
        phlogData = phlogDataFromCursor(cursor);
        cursor.close();
        return(phlogData);
    }
    
    private ContentValues phlogDataFromCursor(Cursor cursor) {
        
        String[] fieldNames;
        int index;
        ContentValues pictureData;

        if (cursor != null && cursor.moveToFirst()) {
            fieldNames = cursor.getColumnNames();
            pictureData = new ContentValues();
            for (index=0;index < fieldNames.length;index++) {
                if (fieldNames[index].equals("_id")) {
                    pictureData.put("_id",cursor.getInt(index));
                } else if (fieldNames[index].equals("title")) {
                    pictureData.put("title",cursor.getString(index));
                } else if (fieldNames[index].equals("date")) {
                    pictureData.put("date",cursor.getString(index));
                } else if (fieldNames[index].equals("time")) {
                    pictureData.put("time",cursor.getString(index));
                } else if (fieldNames[index].equals("image_id")) {
                    pictureData.put("image_id",cursor.getString(index));
                } else if (fieldNames[index].equals("location")) {
                    pictureData.put("location",cursor.getString(index));
                } else if (fieldNames[index].equals("orientation")) {
                    pictureData.put("orientation",cursor.getString(index));
                } else if (fieldNames[index].equals("text")) {
                    pictureData.put("text",cursor.getString(index));
                }
            }
            return(pictureData);
        } else {
            return(null);
        }
    }
    
    
//=============================================================================
    
private static class DatabaseHelper extends SQLiteOpenHelper {
//-------------------------------------------------------------------------
       public DatabaseHelper(Context context) {
               
           super(context,DATABASE_NAME,null,DATABASE_VERSION);
           
       }
//-------------------------------------------------------------------------
       public void onCreate(SQLiteDatabase db) {

           db.execSQL(CREATE_PHLOG_TABLE);
       }
       //-------------------------------------------------------------------------
       @Override
       public void onOpen(SQLiteDatabase db) {
               
           super.onOpen(db);
       }
//-------------------------------------------------------------------------
       public void onUpgrade(SQLiteDatabase db,int oldVersion,
int newVersion) {
               
           db.execSQL("DROP TABLE IF EXISTS "+PHLOG_TABLE_NAME);
           onCreate(db);
       }
//-------------------------------------------------------------------------
}
}
