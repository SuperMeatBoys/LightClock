package com.gesuper.lightclock.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelperModel extends SQLiteOpenHelper{

	public static final String TAG = "DbHelperModel";
	public static final String DB_NAME = "alert.db";
	
	private static final int DB_VERSION = 4;
	
	public interface TABLE {
		public static final String ALERTS = "alerts";
	}
	
	private static final String CREATE_ALERTS_TABLE_SQL = 
			"CREATE TABLE " + TABLE.ALERTS + " (" +
					AlertItemModel.ID + " INTEGER PRIMARY KEY," +
					AlertItemModel.ALERT_TYPE + " INTEGER NOT NULL DEFAULT 0," +
					AlertItemModel.BG_COLOR_ID + " INTEGER NOT NULL DEFAULT 0," +
					AlertItemModel.ALERT_DATE + " INTEGER NOT NULL DEFAULT 0," +
					AlertItemModel.CREATE_DATE + " INTEGER NOT NULL DEFAULT 0," +
					AlertItemModel.MODIFY_DATE + " INTEGER NOT NULL DEFAULT 0," +
					AlertItemModel.SHORT_CONTENT + " TEXT　 DEFAULT ''," +
					AlertItemModel.CONTENT + " TEXT　NOT NULL DEFAULT ''," +
					AlertItemModel.VERSION + " INTEGER NOT NULL DEFAULT 0," +
					AlertItemModel.SEQUENCE + " INTEGER NOT NULL DEFAULT 0" +
			")";
	
	private SQLiteDatabase db;
	
	public static DBHelperModel instance;
	
	public DBHelperModel(Context context){
		super(context, DB_NAME, null, DB_VERSION);
		this.db = this.getWritableDatabase();
	}

	public static DBHelperModel getInstance(Context context){
		if(instance == null){
			instance = new DBHelperModel(context);
		}
		return  instance;
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

	private void createTableAlerts(SQLiteDatabase db) {
		db.execSQL(CREATE_ALERTS_TABLE_SQL);
		Log.d(TAG, CREATE_ALERTS_TABLE_SQL);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		createTableAlerts(db);
	}
	
	public void runSql(String sql){
		db = getWritableDatabase();
		db.execSQL(sql);
	}
	
	public long insert(ContentValues values){
		if(this.db.isOpen() && this.db.isReadOnly()){
			db.close();
			db = this.getWritableDatabase();
		}
		long id = db.insert(TABLE.ALERTS, null, values);
		return id;
	}
	
	public Cursor query(String[] columns, String selection,
			String[] selectionArgs, String orderBy){
		if(this.db.isOpen() && !this.db.isReadOnly()){
			db.close();
			db = this.getReadableDatabase();
		}
		Cursor cursor = db.query(TABLE.ALERTS, columns, selection, selectionArgs, null, null, orderBy);
		return cursor;
	}
	
	public boolean update(ContentValues values, String whereClause, String[] whereArgs){
		if(this.db.isOpen() && this.db.isReadOnly())
			db = this.getWritableDatabase();
		int rows = db.update(TABLE.ALERTS, values, whereClause, whereArgs);
		if(rows < 0)
			return false;
		return true;
	}
	
	public boolean delete(String whereClause, String[] whereArgs){
		if(this.db.isOpen() && this.db.isReadOnly())
			db = this.getWritableDatabase();
		int rows = db.delete(TABLE.ALERTS, whereClause, whereArgs);
		if(rows<1)
			return false;
		return true;
	}
	
	public Cursor getAll(){
		return null;
	}
	
	public void close(){
		this.db.close();
	}
}
