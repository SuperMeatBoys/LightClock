package com.gesuper.lightclock.model;

import java.util.Date;
import java.util.Random;

import android.content.ContentValues;
import android.database.Cursor;

public class AlertItemModel {
	
	public static final String TAG = "AlertItemModel";
	//_id alert_type bg_color_id alert_date create_date modify_date content version
	public static final String ID = "id";
	public static final String ALERT_TYPE = "alert_type";
	public static final String BG_COLOR_ID = "bg_color_id";
	public static final String ALERT_DATE = "alert_date";
	public static final String CREATE_DATE = "create_date";
	public static final String MODIFY_DATE = "modify_date";
	public static final String SHORT_CONTENT = "short_content";
	public static final String CONTENT = "content";
	public static final String VERSION = "version";
	public static final String SEQUENCE = "sequence";
	
	public static final String[] mColumns = {
		AlertItemModel.ID,
		AlertItemModel.ALERT_TYPE,
		AlertItemModel.BG_COLOR_ID,
		AlertItemModel.ALERT_DATE,
		AlertItemModel.CREATE_DATE,
		AlertItemModel.MODIFY_DATE,
		AlertItemModel.SHORT_CONTENT,
		AlertItemModel.CONTENT,
		AlertItemModel.VERSION,
		AlertItemModel.SEQUENCE
	};
	
	public static final int APP_VERSION = 1;
	
	private static final int ID_COLUMN = 0;
	private static final int ALERT_TYPE_COLUMN = 1;
	private static final int BG_COLOR_ID_COLUMN = 2;
	private static final int ALERT_DATE_COLUMN = 3;
	private static final int CREATE_DATE_COLUMN = 4;
	private static final int MODIFY_DATE_COLUMN = 5;
	private static final int SHORT_CONTENT_COLUMN = 6;
	private static final int CONTENT_COLUMN = 7;
	private static final int VERSION_COLUMN = 8;
	private static final int SEQUENCE_COLUMN = 9;
	
	private String mId;
	private String mAlertType;
	private String mBgColorId;
	private String mAlertDate;
	private String mCreateDate;
	private String mModifyDate;
	private String mContent;
	private String mShortContent;
	private String mVersion;
	private String mSequence;
	
	public AlertItemModel(){
		Random rand = new Random();
		this.mId = "-1";
		this.mAlertType = "0";
		this.mBgColorId = String.valueOf(rand.nextInt(BgColor.COLOR_COUNT) + 1);
		this.mAlertDate =  "0";
		this.mCreateDate  = String.valueOf(new Date().getTime());
		this.mModifyDate = String.valueOf(new Date().getTime());
		this.mShortContent  = "";
		this.mContent =  "";
		this.mVersion =  String.valueOf(AlertItemModel.APP_VERSION);
		this.mSequence = "0";
	}
	
	public AlertItemModel(Cursor cursor){
		this.mId = cursor.getString(ID_COLUMN);
		this.mAlertType = cursor.getString(ALERT_TYPE_COLUMN);
		this.mBgColorId = cursor.getString(BG_COLOR_ID_COLUMN);
		this.mAlertDate = cursor.getString(ALERT_DATE_COLUMN);
		this.mCreateDate = cursor.getString(CREATE_DATE_COLUMN);
		this.mModifyDate = cursor.getString(MODIFY_DATE_COLUMN);
		
		this.mShortContent = cursor.getString(SHORT_CONTENT_COLUMN);
		this.mContent = cursor.getString(CONTENT_COLUMN);
		
		this.mVersion = cursor.getString(VERSION_COLUMN);
		this.mSequence = cursor.getString(SEQUENCE_COLUMN);
	}
	
	public AlertItemModel(ContentValues values){
		this.mId = values.getAsString(ID);
		this.mAlertType = values.getAsString(ALERT_TYPE);
		this.mBgColorId = values.getAsString(BG_COLOR_ID);
		this.mAlertDate = values.getAsString(ALERT_DATE);
		this.mCreateDate = values.getAsString(CREATE_DATE);
		this.mModifyDate = values.getAsString(MODIFY_DATE);
		this.mShortContent = values.getAsString(SHORT_CONTENT);
		this.mContent = values.getAsString(CONTENT);
		this.mVersion = values.getAsString(VERSION);
		this.mSequence = values.getAsString(SEQUENCE);
	}
	
	public AlertItemModel(long id, int alertType,int bgColorId, 
			long alertDate, long createDate,
			long modifyDate, String shortContent, String content, 
			int  version, int sequence){
		this.mId = String.valueOf(id);
		
		this.mAlertType = String.valueOf(alertType);
		this.mBgColorId = String.valueOf(bgColorId);
		this.mAlertDate = String.valueOf(alertDate);
		this.mCreateDate = String.valueOf(createDate);
		this.mModifyDate = String.valueOf(modifyDate);
		this.mShortContent = shortContent;
		this.mContent = content;
		this.mVersion = String.valueOf(version);
		this.mSequence = String.valueOf(sequence);
	}
	
	public ContentValues formatContentValuesWithoutId(){
		ContentValues values = new ContentValues();
		values.put(ALERT_TYPE, this.mAlertType);
		values.put(BG_COLOR_ID, this.mBgColorId);
		values.put(ALERT_DATE, this.mAlertDate);
		values.put(CREATE_DATE, this.mCreateDate);
		values.put(MODIFY_DATE, this.mModifyDate);
		values.put(SHORT_CONTENT, this.mShortContent);
		values.put(CONTENT, this.mContent);
		values.put(VERSION, this.mVersion);
		values.put(SEQUENCE, this.mSequence);
		
		return values;
	}

	public void setId(Long id) {
		this.mId = String.valueOf(id);
	}
	
	public long getId(){
		return Long.valueOf(this.mId);
	}

	public int getAlertType(){
		return Integer.valueOf(this.mAlertType);
	}
	
	public void setBgColorId(int id){
		this.mBgColorId = String.valueOf(id);
	}
	
	public int getBgColorId(){
		return Integer.valueOf(this.mBgColorId);
	}
	 
	public long getAlertDate(){
		return Long.valueOf(this.mAlertDate);
	}
	 
	public long getCreateDate(){
		return Long.valueOf(this.mCreateDate);
	}
	 
	public long getModifyDate(){
		return Long.valueOf(this.mModifyDate);
	}
	
	public String getShortContent() {
		// TODO Auto-generated method stub
		return this.mShortContent;
	}
	
	public String getShortContentForTextView(){

		if(this.mShortContent.length() == 0)
			return this.mContent;
		int len = this.mShortContent.length();
		String content = this.mShortContent.substring(0, len-3);
		return content + "......";
	}
	
	public void setShortContent(String shortContent){
		this.mShortContent = shortContent;
	}
	
	public void setContent(String content){
		this.mContent = content;
	}
	 
	public String getContent(){
		return this.mContent;
	}
	 
	public int getVersion(){
		return Integer.valueOf(this.mVersion);
	}
	
	public void setSequence(int sequence){
		this.mSequence = String.valueOf(sequence); 
	}
	
	public int getSequence(){
		return Integer.valueOf(this.mSequence);
	}
	
	public boolean hasAlert(){
		return (mAlertDate == "0");
	}
}
