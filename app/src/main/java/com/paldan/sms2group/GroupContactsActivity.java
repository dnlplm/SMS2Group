package com.paldan.sms2group;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.os.Build.*;

public class GroupContactsActivity extends AppCompatActivity {

    public final static String EXTRA_GROUP_ID = "com.paldan.sms2group.GROUP_ID";
    public final static String EXTRA_GROUP_NAME = "com.paldan.sms2group.GROUP_NAME";
    static final int PICK_CONTACT = 1;

    private SMS2GroupDbHelper mDbHelper;
    ContactsAdapter mAdapter;
    ArrayList<Contact> mArrayOfContacts;
    private Map<Integer,Boolean> mSelection;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle(getIntent().getStringExtra(GroupContactsActivity.EXTRA_GROUP_NAME) + " " + getString(R.string.title_activity_group_contacts));

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fabGroupContacts2);
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) fab2.getLayoutParams();
            p.setMargins(0, 0, dpToPx(this, 16), 0); // get rid of margins since shadow area is now the margin
            fab2.setLayoutParams(p);
        }

        mDbHelper = new SMS2GroupDbHelper(getApplicationContext());
        mArrayOfContacts = new ArrayList<>();
        mSelection = new HashMap<>();

        buildContactsList();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabGroupContacts1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numbers = "";
                Contact cnt;

                Iterator<Contact> contactsIterator = mArrayOfContacts.iterator();
                while (contactsIterator.hasNext()) {
                    cnt = contactsIterator.next();
                    numbers += cnt.phoneNumber;
                    if (contactsIterator.hasNext())
                        numbers += ",";
                }
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms: " + numbers)));
            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fabGroupContacts2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSelection.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, PICK_CONTACT);
                } else {
                    // A contact is selected: we should delete it
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();
                    ListView lv = (ListView) findViewById(R.id.listViewContacts);
                    Iterator it = mSelection.entrySet().iterator();

                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();

                        Contact cnt = (Contact) lv.getItemAtPosition((Integer) pair.getKey());
                        View v = lv.getChildAt((Integer) pair.getKey());
                        v.setBackgroundColor(Color.TRANSPARENT);

                        String selectionContacts =
                                SMS2GroupContract.ContactsGroupEntry.TABLE_CONTACTS_COLUMN_CONTACT_ID + " LIKE ?" +
                                        " AND " + SMS2GroupContract.ContactsGroupEntry.TABLE_CONTACTS_COLUMN_GROUP_ID + " LIKE ?";

                        String[] selectionContactsArgs = {
                                cnt.id,
                                getIntent().getStringExtra(GroupContactsActivity.EXTRA_GROUP_ID)};

                        db.delete(SMS2GroupContract.ContactsGroupEntry.TABLE_NAME_CONTACTS,
                                selectionContacts,
                                selectionContactsArgs);
                        it.remove();
                    }

                    getListArray();
                    mAdapter.notifyDataSetChanged();
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabGroupContacts2);
                    fab.setImageResource(android.R.drawable.ic_menu_add);
                }
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public static int dpToPx(Context context, float dp) {
        // Reference http://stackoverflow.com/questions/8309354/formula-px-to-dp-dp-to-px-android
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + 0.5f);
    }

    void getListArray() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                SMS2GroupContract.ContactsGroupEntry.TABLE_CONTACTS_COLUMN_GROUP_ID,
                SMS2GroupContract.ContactsGroupEntry.TABLE_CONTACTS_COLUMN_CONTACT_ID
        };

        String selectionContacts =
                SMS2GroupContract.ContactsGroupEntry.TABLE_CONTACTS_COLUMN_GROUP_ID + " LIKE ?";
        String[] selectionGroupsArgs = {
                getIntent().getStringExtra(GroupContactsActivity.EXTRA_GROUP_ID) };

        String sortOrder =
                SMS2GroupContract.ContactsGroupEntry._ID + " DESC";

        Cursor c = db.query(
                SMS2GroupContract.ContactsGroupEntry.TABLE_NAME_CONTACTS,
                projection,
                selectionContacts,
                selectionGroupsArgs,
                null,
                null,
                sortOrder
        );

        // Clean the data source: needed when rebuilding the listview
        mArrayOfContacts.clear();

        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex(SMS2GroupContract.ContactsGroupEntry.TABLE_CONTACTS_COLUMN_CONTACT_ID));

            Cursor contactPhoneAndroid = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
            if (contactPhoneAndroid != null) {
                if (contactPhoneAndroid.moveToFirst()) {
                    String cNumber = contactPhoneAndroid.getString(contactPhoneAndroid.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String nameContact = contactPhoneAndroid.getString(contactPhoneAndroid.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    contactPhoneAndroid.close();
                    Contact newContact = new Contact(id, nameContact, cNumber);
                    mArrayOfContacts.add(newContact);
                }
            }
        }
        c.close();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabGroupContacts1);
        if (mArrayOfContacts.size() == 0)
            fab.hide();
        else
            fab.show();
    }

    private void buildContactsList() {
        getListArray();
        mAdapter = new ContactsAdapter(this, mArrayOfContacts);
        ListView listView = (ListView) findViewById(R.id.listViewContacts);
        listView.setEmptyView(findViewById(R.id.empty_contacts));
        listView.setAdapter(mAdapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Return true not to perform the single click action
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabGroupContacts2);
                fab.setImageResource(android.R.drawable.ic_menu_delete);

                if (mSelection.containsKey(position)) {
                    view.setBackgroundColor(Color.TRANSPARENT);
                    mSelection.remove(position);
                    if (mSelection.isEmpty())
                        fab.setImageResource(android.R.drawable.ic_menu_add);
                } else {
                    view.setBackgroundColor(Color.GRAY);
                    mSelection.put(position, true);
                    fab.setImageResource(android.R.drawable.ic_menu_delete);
                }

                return true;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == PICK_CONTACT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor c = getContentResolver().query(contactData, null, null, null, null);
                if (c != null) {
                    if (c.moveToFirst()) {
                        int id = c.getInt(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        // Check if the contact is already present in the group
                        SQLiteDatabase db = mDbHelper.getReadableDatabase();

                        String[] projection = {
                                SMS2GroupContract.ContactsGroupEntry.TABLE_CONTACTS_COLUMN_GROUP_ID,
                                SMS2GroupContract.ContactsGroupEntry.TABLE_CONTACTS_COLUMN_CONTACT_ID
                        };

                        String selectionContacts =
                                SMS2GroupContract.ContactsGroupEntry.TABLE_CONTACTS_COLUMN_CONTACT_ID + " LIKE ?" +
                                " AND " + SMS2GroupContract.ContactsGroupEntry.TABLE_CONTACTS_COLUMN_GROUP_ID + " LIKE ?";
                        String[] selectionContactsArgs = {
                                String.valueOf(id),
                                getIntent().getStringExtra(GroupContactsActivity.EXTRA_GROUP_ID)};

                        String sortOrder =
                                SMS2GroupContract.ContactsGroupEntry._ID + " DESC";

                        Cursor contact = db.query(
                                SMS2GroupContract.ContactsGroupEntry.TABLE_NAME_CONTACTS,
                                projection,
                                selectionContacts,
                                selectionContactsArgs,
                                null,
                                null,
                                sortOrder
                        );
                        if (contact != null) {
                            if (contact.getCount() == 0) {
                                // Check if the contact has a phone number
                                Cursor contactPhoneAndroid = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                                if (contactPhoneAndroid != null) {
                                    if (!contactPhoneAndroid.moveToFirst()) {
                                        contactPhoneAndroid.close();
                                        contact.close();
                                        c.close();
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.contact_no_phone),
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    contactPhoneAndroid.close();
                                }

                                // The contact has the phone number: if it is not present in the group, add it
                                db = mDbHelper.getWritableDatabase();

                                // Create a new map of values, where column names are the keys
                                ContentValues values = new ContentValues();
                                values.put(SMS2GroupContract.ContactsGroupEntry.TABLE_CONTACTS_COLUMN_GROUP_ID, getIntent().getStringExtra(GroupContactsActivity.EXTRA_GROUP_ID));
                                values.put(SMS2GroupContract.ContactsGroupEntry.TABLE_CONTACTS_COLUMN_CONTACT_ID, id);

                                // Insert the new row, returning the primary key value of the new row
                                long newRowId = db.insert(
                                        SMS2GroupContract.ContactsGroupEntry.TABLE_NAME_CONTACTS,
                                        null,
                                        values);

                                if (newRowId != -1) {
                                    getListArray();
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.error_contact_db),
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.contact_already_present),
                                        Toast.LENGTH_SHORT).show();
                            }
                            contact.close();
                        }
                    }
                    c.close();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GroupContacts Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.paldan.sms2group/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GroupContacts Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.paldan.sms2group/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
