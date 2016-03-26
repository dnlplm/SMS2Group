package com.paldan.sms2group;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.paldan.sms2group.SMS2GroupContract.*;

/**
 * Created by daniele on 24/02/16.
 */
public class SMS2GroupDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SMS2Group.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    // Table groups
    private static final String SQL_CREATE_ENTRIES_GROUPS =
            "CREATE TABLE " + GroupsEntry.TABLE_NAME_GROUPS + " (" +
                    GroupsEntry._ID + " INTEGER PRIMARY KEY," +
                    GroupsEntry.TABLE_GROUPS_COLUMN_GROUP_NAME + TEXT_TYPE +
            " )";

    // Table contacts
    private static final String SQL_CREATE_ENTRIES_CONTACTS =
            "CREATE TABLE " + ContactsGroupEntry.TABLE_NAME_CONTACTS + " (" +
                    ContactsGroupEntry._ID + " INTEGER PRIMARY KEY," +
                    ContactsGroupEntry.TABLE_CONTACTS_COLUMN_GROUP_ID + INT_TYPE + COMMA_SEP +
                    ContactsGroupEntry.TABLE_CONTACTS_COLUMN_CONTACT_ID + INT_TYPE +
                    " )";

    public SMS2GroupDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_GROUPS);
        db.execSQL(SQL_CREATE_ENTRIES_CONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DB should be kept from previous versions
    }
}
