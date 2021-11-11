package com.utility.finmartcontact.core.controller.contactfetch;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactFetcher {

    private final Context context;

    public ContactFetcher(Context c) {
        this.context = c;
    }

    public Map<Long, List<String>> tempContact() {
        Map<Long, List<String>> contacts = new HashMap<Long, List<String>>();

        String[] projection = {ContactsContract.Data.CONTACT_ID, ContactsContract.Data.DISPLAY_NAME, ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1, ContactsContract.Data.DATA2, ContactsContract.Data.DATA3};

// query only emails/phones/events
        String selection = ContactsContract.Data.MIMETYPE + " IN ('" + Phone.CONTENT_ITEM_TYPE + "', '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "', '" + Email.CONTENT_ITEM_TYPE + "')";
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Data.CONTENT_URI, projection, selection, null, null);

        while (cur != null && cur.moveToNext()) {
            long id = cur.getLong(0);
            String name = cur.getString(1); // full name
            String mime = cur.getString(2); // type of data (phone / birthday / email)
            String data = cur.getString(3); // the actual info, e.g. +1-212-555-1234

            String kind = "unknown";

            switch (mime) {
                case Phone.CONTENT_ITEM_TYPE:
                    kind = "phone";
                    break;
                case ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE:
                    kind = "birthday";
                    break;
                case Email.CONTENT_ITEM_TYPE:
                    kind = "email";
                    break;
            }
            Log.d("TAG", "got " + id + ", " + name + ", " + kind + " - " + data);

            // add info to existing list if this contact-id was already found, or create a new list in case it's new
            List<String> infos;
            if (contacts.containsKey(id)) {
                infos = contacts.get(id);
            } else {
                infos = new ArrayList<String>();
                infos.add("name = " + name.trim());
                contacts.put(id, infos);
            }
            infos.add(kind + " = " + data);
        }
        return contacts;
    }

    public ArrayList<Contact> fetchAll() {
        String[] projectionFields = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
        };
        ArrayList<Contact> listContacts = new ArrayList<>();
        CursorLoader cursorLoader = new CursorLoader(context,
                ContactsContract.Contacts.CONTENT_URI,
                projectionFields, // the columns to retrieve
                null, // the selection criteria (none)
                null, // the selection args (none)
                null // the sort order (default)
        );

        Cursor c = cursorLoader.loadInBackground();

        final Map<String, Contact> contactsMap = new HashMap<>(c.getCount());

        if (c.moveToFirst()) {

            int idIndex = c.getColumnIndex(ContactsContract.Contacts._ID);
            int nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

            do {
                String contactId = c.getString(idIndex);
                String contactDisplayName = c.getString(nameIndex);
                Contact contact = new Contact(contactId, contactDisplayName);
                contactsMap.put(contactId, contact);
                listContacts.add(contact);
            } while (c.moveToNext());
        }

        c.close();

        matchContactNumbers(contactsMap);
        matchContactEmails(contactsMap);

        return listContacts;
    }


    public void matchBirthday(Map<String, Contact> contactsMap) {

        final String[] birthProjection = new String[]{
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
        };
        ContentResolver cr = context.getContentResolver();
        String selection = ContactsContract.Data.MIMETYPE + " IN ('" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "')";


        Cursor cur = new CursorLoader(context,
                Phone.CONTENT_URI,
                birthProjection,
                null,
                null,
                null).loadInBackground();


        if (cur.moveToNext()) {

            final int birthIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);
            final int contactIdColumnIndex = cur.getColumnIndex(Phone.CONTACT_ID);
            final int contactTypeColumnIndex = cur.getColumnIndex(Phone.TYPE);

            while (!cur.isAfterLast()) {
                final String birthdate = cur.getString(birthIndex);
                final String contactId = cur.getString(contactIdColumnIndex);
                Contact contact = contactsMap.get(contactId);
                if (contact == null) {
                    cur.moveToNext();
                    continue;
                }

            }
        }

    }

    public void matchContactNumbers(Map<String, Contact> contactsMap) {
        // Get numbers
        final String[] numberProjection = new String[]{
                Phone.NUMBER,
                Phone.TYPE,
                Phone.CONTACT_ID,
        };

        Cursor phone = new CursorLoader(context,
                Phone.CONTENT_URI,
                numberProjection,
                null,
                null,
                null).loadInBackground();

        if (phone.moveToFirst()) {
            final int contactNumberColumnIndex = phone.getColumnIndex(Phone.NUMBER);
            final int contactTypeColumnIndex = phone.getColumnIndex(Phone.TYPE);
            final int contactIdColumnIndex = phone.getColumnIndex(Phone.CONTACT_ID);

            while (!phone.isAfterLast()) {
                final String number = phone.getString(contactNumberColumnIndex);
                final String contactId = phone.getString(contactIdColumnIndex);
                Contact contact = contactsMap.get(contactId);
                if (contact == null) {
                    phone.moveToNext();
                    continue;
                }
                final int type = phone.getInt(contactTypeColumnIndex);
                String customLabel = "Custom";
                CharSequence phoneType = ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel);
                contact.addNumber(number, phoneType.toString());
                phone.moveToNext();
            }
        }

        phone.close();
    }

    public void matchContactEmails(Map<String, Contact> contactsMap) {
        // Get email
        final String[] emailProjection = new String[]{
                Email.DATA,
                Email.TYPE,
                Email.CONTACT_ID,
        };

        Cursor email = new CursorLoader(context,
                Email.CONTENT_URI,
                emailProjection,
                null,
                null,
                null).loadInBackground();

        if (email.moveToFirst()) {
            final int contactEmailColumnIndex = email.getColumnIndex(Email.DATA);
            final int contactTypeColumnIndex = email.getColumnIndex(Email.TYPE);
            final int contactIdColumnsIndex = email.getColumnIndex(Email.CONTACT_ID);

            while (!email.isAfterLast()) {
                final String address = email.getString(contactEmailColumnIndex);
                final String contactId = email.getString(contactIdColumnsIndex);
                final int type = email.getInt(contactTypeColumnIndex);
                String customLabel = "Custom";
                Contact contact = contactsMap.get(contactId);
                if (contact == null) {
                    email.moveToNext();
                    continue;
                }
                CharSequence emailType = ContactsContract.CommonDataKinds.Email.getTypeLabel(context.getResources(), type, customLabel);
                contact.addEmail(address, emailType.toString());
                email.moveToNext();
            }
        }

        email.close();
    }
}