package com.callerq.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "callerq.db";
    private static final int DATABASE_VERSION = 2;
    
    public static final String REMINDER_TABLE_NAME = "Reminder";
    public static final String REMINDER_ID = "_id";
    public static final String REMINDER_CALL_DURATION = "callDuration";
    public static final String REMINDER_CALL_START_TIME = "callStartTime";
    public static final String REMINDER_CREATED_DATETIME = "createdDatetime";
    public static final String REMINDER_IS_MEETING = "isMeeting";
    public static final String REMINDER_MEMO_TEXT = "memoText";
    public static final String REMINDER_SCHEDULE_DATETIME = "scheduleDatetime";
    public static final String REMINDER_CONTACT_NAME = "contactName";
    public static final String REMINDER_CONTACT_COMPANY = "contactCompany";
    public static final String REMINDER_CONTACT_EMAIL = "contactEmail";
    public static final String REMINDER_CONTACT_PHONE_1 = "contactPhone1";
    public static final String REMINDER_CONTACT_PHONE_2 = "contactPhone2";
    public static final String REMINDER_CONTACT_PHONE_3 = "contactPhone3";
    public static final String REMINDER_UPLOADED = "uploaded";    

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create the reminders table
        final String REMINDER_TABLE_CREATE = new StringBuilder()
                .append("CREATE TABLE " + REMINDER_TABLE_NAME + " (")
                .append(REMINDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(REMINDER_CALL_DURATION + " INTEGER NOT NULL, ")
                .append(REMINDER_CALL_START_TIME + " INTEGER NOT NULL, ")
                .append(REMINDER_CREATED_DATETIME + " INTEGER NOT NULL, ")
                .append(REMINDER_IS_MEETING + " INTEGER NOT NULL, ")
                .append(REMINDER_MEMO_TEXT + " TEXT NOT NULL, ")
                .append(REMINDER_SCHEDULE_DATETIME + " INTEGER NOT NULL, ")
                .append(REMINDER_CONTACT_NAME + " TEXT NOT NULL, ")
                .append(REMINDER_CONTACT_COMPANY + " TEXT NOT NULL, ")
                .append(REMINDER_CONTACT_EMAIL + " TEXT NOT NULL, ")
                .append(REMINDER_CONTACT_PHONE_1 + " TEXT NOT NULL, ")
                .append(REMINDER_CONTACT_PHONE_2 + " TEXT NOT NULL, ")
                .append(REMINDER_CONTACT_PHONE_3 + " TEXT NOT NULL, ")
                .append(REMINDER_UPLOADED + " INTEGER NOT NULL")
                .append(");").toString();
        db.execSQL(REMINDER_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	if (oldVersion == 1)
    	{
    		db.execSQL("ALTER TABLE " + REMINDER_TABLE_NAME + " ADD COLUMN " + REMINDER_CONTACT_EMAIL + " TEXT DEFAULT ''");
    	}
    	else
    	{
    		db.execSQL("DROP TABLE IF EXISTS " + REMINDER_TABLE_NAME);
    		onCreate(db);
    	}
    }

}
