package com.paldan.sms2group;

/**
 * Created by daniele on 05/03/16.
 */
public class Contact {
    public String id;
    public String displayName;
    public String phoneNumber;

    public Contact(String id, String displayName, String phoneNumber) {
        this.id = id;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
    }
}
