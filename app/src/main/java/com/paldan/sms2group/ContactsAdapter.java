package com.paldan.sms2group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by daniele on 05/03/16.
 */
public class ContactsAdapter extends ArrayAdapter<Contact> {
    public ContactsAdapter(Context context, List<Contact> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Contact contact = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_contact, parent, false);
        }

        TextView id = (TextView) convertView.findViewById(R.id.id);
        TextView displayName = (TextView) convertView.findViewById(R.id.displayName);
        TextView phoneNumber = (TextView) convertView.findViewById(R.id.phoneNumber);

        id.setText(contact.id);
        displayName.setText(contact.displayName);
        phoneNumber.setText(contact.phoneNumber);

        return convertView;
    }
}
