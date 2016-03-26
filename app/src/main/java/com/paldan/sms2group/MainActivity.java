package com.paldan.sms2group;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private SMS2GroupDbHelper mDbHelper;
    private SimpleCursorAdapter mAdapter;
    private Map<Integer,Boolean> mSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbHelper = new SMS2GroupDbHelper(getApplicationContext());
        mSelection = new HashMap<>();

        updateList();
        buildGroupsList();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabMain1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                //if (mSelectedItem == -1) {
                if (mSelection.isEmpty()) {
                    // There is no group selected: we should then add one
                    EditText groupNameEditText = (EditText) findViewById(R.id.groupNameEditText);
                    String groupName;

                    groupName = String.valueOf(groupNameEditText.getText()).trim();
                    if (groupName.length() == 0) {
                        // Empty group names are not allowed
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.error_group_name),
                                Toast.LENGTH_SHORT).show();
                        groupNameEditText.setText("", TextView.BufferType.EDITABLE);
                        return;
                    }

                    // Group name is valid
                    ContentValues values = new ContentValues();
                    values.put(SMS2GroupContract.GroupsEntry.TABLE_GROUPS_COLUMN_GROUP_NAME,
                            groupName);

                    long newRowId = db.insert(
                            SMS2GroupContract.GroupsEntry.TABLE_NAME_GROUPS,
                            SMS2GroupContract.GroupsEntry.TABLE_GROUPS_COLUMN_GROUP_NAME,
                            values);

                    if (newRowId != -1) {
                        groupNameEditText.setText("", TextView.BufferType.EDITABLE);
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.group_inserted),
                                Toast.LENGTH_SHORT).show();
                        mAdapter.changeCursor(getListCursor());
                    } else {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.error_group_db),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // A group is selected: we should delete it
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.delete_group_title))
                            .setMessage(getString(R.string.delete_group_question))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with delete
                                    SQLiteDatabase db = mDbHelper.getWritableDatabase();
                                    ListView lv = (ListView) findViewById(R.id.listViewMain1);
                                    Iterator it = mSelection.entrySet().iterator();

                                    while (it.hasNext()) {
                                        Map.Entry pair = (Map.Entry)it.next();
                                        Cursor cGroups = (Cursor) lv.getItemAtPosition((Integer) pair.getKey());
                                        View v = lv.getChildAt((Integer) pair.getKey());
                                        v.setBackgroundColor(Color.TRANSPARENT);

                                        String selectionGroups = SMS2GroupContract.GroupsEntry._ID + " LIKE ?";
                                        String[] selectionGroupsArgs = {
                                                cGroups.getString(cGroups.getColumnIndex(SMS2GroupContract.GroupsEntry._ID))};
                                        String selectionContacts =
                                                SMS2GroupContract.ContactsGroupEntry.TABLE_CONTACTS_COLUMN_GROUP_ID + " LIKE ?";
                                        String[] selectionContactsArgs = {
                                                cGroups.getString(cGroups.getColumnIndex(SMS2GroupContract.GroupsEntry._ID))};

                                        db.delete(SMS2GroupContract.GroupsEntry.TABLE_NAME_GROUPS,
                                                selectionGroups,
                                                selectionGroupsArgs);
                                        db.delete(SMS2GroupContract.ContactsGroupEntry.TABLE_NAME_CONTACTS,
                                                selectionContacts,
                                                selectionContactsArgs);
                                        it.remove();
                                    }

                                    mAdapter.changeCursor(getListCursor());

                                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabMain1);
                                    fab.setImageResource(android.R.drawable.ic_menu_add);
                                    buildGroupsList();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do not delete the group
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        getPermissionToReadUserContacts();
    }

    private void buildGroupsList() {
        ListView groupsListView = (ListView) findViewById(R.id.listViewMain1);

        groupsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Return true not to perform the single click action
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabMain1);

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

        groupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView lv = (ListView) findViewById(R.id.listViewMain1);
                Cursor c = (Cursor) lv.getItemAtPosition(position);
                String groupId = c.getString(c.getColumnIndex(SMS2GroupContract.GroupsEntry._ID));
                String groupName = c.getString(c.getColumnIndex(SMS2GroupContract.GroupsEntry.TABLE_GROUPS_COLUMN_GROUP_NAME));

                Intent intent = new Intent(getApplicationContext(), GroupContactsActivity.class);
                intent.putExtra(GroupContactsActivity.EXTRA_GROUP_ID, groupId);
                intent.putExtra(GroupContactsActivity.EXTRA_GROUP_NAME, groupName);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // TODO: uncomment for restoring menu button
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
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
                "Main Page", // TODO: Define a title for the content shown.
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

    private Cursor getListCursor() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                SMS2GroupContract.GroupsEntry._ID,
                SMS2GroupContract.GroupsEntry.TABLE_GROUPS_COLUMN_GROUP_NAME
        };

        String sortOrder =
                SMS2GroupContract.GroupsEntry._ID + " DESC";

        return db.query(
                SMS2GroupContract.GroupsEntry.TABLE_NAME_GROUPS,    // The table to query
                projection,                                         // The columns to return
                null,                                               // The columns for the WHERE clause
                null,                                               // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                sortOrder                                           // The sort order
        );
    }

    private void updateList() {
        Cursor c = getListCursor();

        mAdapter = new SimpleCursorAdapter(this, // Context.
                R.layout.list_group,
                c,
                new String[] { SMS2GroupContract.GroupsEntry._ID,
                        SMS2GroupContract.GroupsEntry.TABLE_GROUPS_COLUMN_GROUP_NAME},
                new int[] { R.id.listGroupTextId, R.id.listGroupTextName},
                0);

        ListView groupsListView = (ListView) findViewById(R.id.listViewMain1);
        groupsListView.setEmptyView(findViewById(R.id.empty_group));
        groupsListView.setAdapter(mAdapter);
    }

    public void getPermissionToReadUserContacts() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_CONTACTS)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }
            }
            */

            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                        READ_CONTACTS_PERMISSIONS_REQUEST);
            }
        }
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.read_permission_granted), Toast.LENGTH_SHORT).show();
            } else {
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(getString(R.string.read_permission_explanation))
                        .setTitle(getString(R.string.alert_read_permission_title));

                builder.setPositiveButton(getString(R.string.alert_read_permission_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getPermissionToReadUserContacts();
                    }
                });
                builder.setNegativeButton(getString(R.string.alert_read_permission_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishAffinity();
                    }
                });

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();

                dialog.show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
