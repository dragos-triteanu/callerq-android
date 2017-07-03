package com.callerq.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

public class AddressBookHelper {

    public static final String TAG = "AddressBookHelper:";

    private static AddressBookHelper instance;

    private static List<AddressBookListener> listeners = new ArrayList<AddressBookListener>();

    // class for event listeners for DatabaseHelper events
    public static interface AddressBookListener {

        void contactRetrieved(String requestId, Contact contact);

    }

    public static class Contact {
        public String name;
        public String company;
        public String email;
        public List<String> phoneNumbers;
    }
    
    protected AddressBookHelper() {
    }

    public static AddressBookHelper getInstance() {
        if (instance == null)
            instance = new AddressBookHelper();
        return instance;
    }

    public synchronized void addListener(AddressBookListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(AddressBookListener listener) {
        listeners.remove(listener);
    }

    public void addContact(Context context, Contact contact) {
        new AddContactTask(context).execute(contact);
    }

    public String getContact(Context context, String phoneNumber) {;
        String requestId = UUID.randomUUID().toString();
        new GetContactTask(context, requestId).execute(phoneNumber);
        return requestId;
    }

    public static class AddContactTask extends
            AsyncTask<Contact, Integer, Boolean> {

        private Context context;

        public AddContactTask(Context context) {
            this.context = context;
        }

        protected Boolean doInBackground(Contact... args) {
            Contact contact = args[0];
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            int rawContactInsertIndex = ops.size();

            ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.ACCOUNT_TYPE, null)
                    .withValue(RawContacts.ACCOUNT_NAME, null).build());
            ops.add(ContentProviderOperation
                    .newInsert(Data.CONTENT_URI)
                    .withValueBackReference(
                            Data.RAW_CONTACT_ID,
                            rawContactInsertIndex)
                    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                    .withValue(Phone.NUMBER, contact.phoneNumbers.get(0))
                    .build());
            ops.add(ContentProviderOperation
                    .newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID,
                            rawContactInsertIndex)
                    .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(StructuredName.DISPLAY_NAME, contact.name)
                    .build());
            ops.add(ContentProviderOperation
                    .newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID,
                            rawContactInsertIndex)
                    .withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
                    .withValue(Organization.COMPANY, contact.company)
                    .build());
            try {
                context.getContentResolver().applyBatch(
                        ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                Log.e(TAG,
                        "Error storing contact to address book: "
                                + e.getMessage());
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

    }

    public static class GetContactTask extends
            AsyncTask<String, Integer, Contact> {

        private Context context;
        private String requestId;

        public GetContactTask(Context context, String requestId) {
            this.context = context;
            this.requestId = requestId;
        }

        @Override
        protected Contact doInBackground(String... params) {
            String phoneNumber = params[0];
            Contact contact = new Contact();

            // get the contact's display name
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNumber));
            Cursor people = context.getContentResolver().query(uri,
                    new String[] { PhoneLookup._ID, PhoneLookup.DISPLAY_NAME },
                    null, null, null);
            String contactDisplayName = null;
            String contactId = null;
            if (people.moveToFirst()) {
                do {
                    int nameFieldColumnIndex = people
                            .getColumnIndex(PhoneLookup.DISPLAY_NAME);
                    int idFieldColumnIndex = people
                            .getColumnIndex(PhoneLookup._ID);
                    contactDisplayName = people.getString(nameFieldColumnIndex);
                    contactId = people.getString(idFieldColumnIndex);
                } while ((contactDisplayName == null || contactDisplayName
                        .length() == 0) && people.moveToNext());
                people.close();
            } else {
                // no contact was found
                return null;
            }
            if (contactDisplayName == null) {
                contactDisplayName = new String();
            }

            // get contact's phone numbers
            Cursor phonesCursor = context.getContentResolver().query(
                    Data.CONTENT_URI,
                    new String[] { Data._ID, Phone.NUMBER, Phone.TYPE,
                            Phone.LABEL },
                    Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='"
                            + Phone.CONTENT_ITEM_TYPE + "'",
                    new String[] { String.valueOf(contactId) }, null);
            ArrayList<String> phoneNumbers = new ArrayList<String>();
            phoneNumbers.add(phoneNumber);
            if (phonesCursor.moveToFirst()) {
                String contactPhone = new String();
                do {
                    int numberFieldColumnIndex = phonesCursor
                            .getColumnIndex(Phone.NUMBER);
                    contactPhone = phonesCursor
                            .getString(numberFieldColumnIndex);
                    if (contactPhone.length() != 0 && contactPhone.equalsIgnoreCase(phoneNumber)) {
                        phoneNumbers.add(contactPhone);
                    }
                } while (phonesCursor.moveToNext());
                phonesCursor.close();
            }

            // get the contact's company name
            String company = null;
            Cursor companyCursor = context.getContentResolver().query(
                    Data.CONTENT_URI,
                    new String[] { Organization.COMPANY },
                    Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='"
                            + Organization.CONTENT_ITEM_TYPE + "'",
                    new String[] { String.valueOf(contactId) }, null);
            if (companyCursor.moveToFirst()) {
                do {
                    int companyFieldColumnIndex = companyCursor
                            .getColumnIndex(Organization.COMPANY);
                    company = companyCursor.getString(companyFieldColumnIndex);
                } while ((company == null || company.length() == 0)
                        && companyCursor.moveToNext());
                companyCursor.close();
            }
            if (company == null) {
                company = new String();
            }

            // get the contact's email address
            String email = null;
            Cursor emailCursor = context.getContentResolver().query(
                    Data.CONTENT_URI,
                    new String[] { Data.DATA1 },
                    Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='"
                            + Email.CONTENT_ITEM_TYPE + "'",
                    new String[] { String.valueOf(contactId) }, null);
            if (emailCursor.moveToFirst()) {
                do {
                    int emailFieldColumnIndex = emailCursor
                            .getColumnIndex(Data.DATA1);
                    email = emailCursor.getString(emailFieldColumnIndex);
                } while ((email == null || email.length() == 0)
                        && emailCursor.moveToNext());
                emailCursor.close();
            }
            if (email == null) {
            	email = new String();
            }
            
            contact.name = contactDisplayName;
            contact.phoneNumbers = phoneNumbers;
            contact.company = company;
            contact.email = email;

            return contact;
        }

        @Override
        protected void onPostExecute(Contact result) {
            super.onPostExecute(result);
            for (AddressBookListener listener : listeners) {
                listener.contactRetrieved(requestId, result);
            }
        }
        
    }

}
