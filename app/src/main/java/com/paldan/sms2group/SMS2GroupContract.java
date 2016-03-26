package com.paldan.sms2group;

import android.provider.BaseColumns;

/**
 * Created by daniele on 24/02/16.
 */
public class SMS2GroupContract {
    // Empty constructor to prevent accidental instantiation
    public SMS2GroupContract() {}

    public static abstract class GroupsEntry implements BaseColumns {
        public static final String TABLE_NAME_GROUPS = "groups";
        public static final String TABLE_GROUPS_COLUMN_GROUP_NAME = "name";
    }

    public static abstract class ContactsGroupEntry implements BaseColumns {
        public static final String TABLE_NAME_CONTACTS = "contacts";
        public static final String TABLE_CONTACTS_COLUMN_GROUP_ID = "groupid";
        public static final String TABLE_CONTACTS_COLUMN_CONTACT_ID = "contactId";
    }
}
