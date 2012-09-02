package ps.age.hbb;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBWraper {
	  private static final String DATABASE_NAME = "database.db";
	  private static final int DATABASE_VERSION = 1;
	  private Context context;
	  private SQLiteDatabase db;
	  OpenHelper openHelper;
	  public static final String RECORD_TABLE       = "record";
	  public static final String RECORD_PATH        = "path";
	  public static final String RECORD_EXTRA       = "extra";
	  public static final String RECORD_MARK_FIRST  = "first";
	  public static final String RECORD_MARK_SECOND = "second";
	  public static final String RECORD_MARK_THIRD  = "third";
	  public static final String RECORD_MARK_FOURTH = "fourth";
	  public static final String RECORD_ID          =  "_id";
	  public static final String RECORD_TIME        = "time";


	  public DBWraper(Context context) {
		  this.context = context;
		  openHelper = new OpenHelper(this.context);
		  this.db = openHelper.getWritableDatabase();
		  }
	  public boolean insertRecord(RecordItem item) {
		  ContentValues values = new ContentValues();
		  
		  values.put(RECORD_PATH, item.getPath());
		  values.put(RECORD_EXTRA, item.getExtra());
		  values.put(RECORD_TIME, item.getTime());
		  
		  values.put(RECORD_MARK_FIRST, item.getFirstMark());
		  values.put(RECORD_MARK_SECOND, item.getSecondMark());
		  values.put(RECORD_MARK_THIRD, item.getThirdMark());
		  values.put(RECORD_MARK_FOURTH, item.getFourthMark());
		  
		  // inserted successfully
		  if(db.insert(RECORD_TABLE, null, values)!= -1)
			  return true;
		  
		  return false;
	  }
	  public boolean updateRecord(RecordItem item){
		  ContentValues values = new ContentValues();
		  values.put(RECORD_ID, item.getId());
		  values.put(RECORD_PATH, item.getPath());
		  values.put(RECORD_EXTRA, item.getExtra());
		  values.put(RECORD_TIME, item.getTime());
		  values.put(RECORD_MARK_FIRST, item.getFirstMark());
		  values.put(RECORD_MARK_SECOND, item.getSecondMark());
		  values.put(RECORD_MARK_THIRD, item.getThirdMark());
		  values.put(RECORD_MARK_FOURTH, item.getFourthMark());
		  
		  int numRows = db.update(RECORD_TABLE, values, "_id=?", new String[] { Long.toString(item.getId()) });
		  if(numRows == 1)
			  return true;		  
		  return false;
	  }
	  public ArrayList<RecordItem> getRecordsList(){
		  
		  Cursor cursor = db.query(RECORD_TABLE, null, null, null, null, null, RECORD_ID);
		  ArrayList<RecordItem> list = null;
		  if((cursor != null) && cursor.moveToFirst()){
			  int path 	    = cursor.getColumnIndex(RECORD_PATH);
			  int id        = cursor.getColumnIndex(RECORD_ID);
			  int time      = cursor.getColumnIndex(RECORD_TIME);
			  int extra    	= cursor.getColumnIndex(RECORD_EXTRA);
			  int first     = cursor.getColumnIndex(RECORD_MARK_FIRST);
			  int second 	= cursor.getColumnIndex(RECORD_MARK_SECOND);
			  int third	    = cursor.getColumnIndex(RECORD_MARK_THIRD);
			  int fourth    = cursor.getColumnIndex(RECORD_MARK_FOURTH);
			  list = new ArrayList<RecordItem>();
			  do{
				  RecordItem item = new RecordItem();
				  item.setId(cursor.getLong(id));
				  item.setTime(cursor.getLong(time));
				  item.setPath(cursor.getString(path));
				  item.setExtra(cursor.getString(extra));
				  item.setFirstMark(cursor.getLong(first));
				  item.setSecondMark(cursor.getLong(second));
				  item.setThirdMark(cursor.getLong(third));
				  item.setFourthMark(cursor.getLong(fourth));
				  list.add(item);
			  }while(cursor.moveToNext());
			  
		  }
		  return list;
	  }
	  public void close(){
		  openHelper.close();
	  }
	  private static class OpenHelper extends SQLiteOpenHelper {
		  OpenHelper(Context context) {
			  super(context, DATABASE_NAME, null, DATABASE_VERSION);
			  }
		  @Override 
		  public void onCreate(SQLiteDatabase db) {
			  db.execSQL("CREATE TABLE " + RECORD_TABLE + "("+RECORD_ID+" INTEGER PRIMARY KEY autoincrement, "+
					    RECORD_PATH+" TEXT NOT NULL, "+RECORD_EXTRA+" TEXT NULL, "+
					    RECORD_TIME+" INTEGER NOT NULL, "+
					  	RECORD_MARK_FIRST+" INTEGER NULL, "+RECORD_MARK_SECOND+" INTEGER NULL,"+
					  	RECORD_MARK_THIRD +" INTEGER NULL, "+RECORD_MARK_FOURTH+" INTEGER NULL)");
			  }
		  @Override 
		  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			  db.execSQL("DROP TABLE IF EXISTS " + RECORD_TABLE);
			  onCreate(db);
			  }
		  }
	  }
