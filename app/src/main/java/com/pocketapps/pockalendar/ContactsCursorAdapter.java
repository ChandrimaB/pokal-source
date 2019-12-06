package com.pocketapps.pockalendar;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.TextView;

import com.pocketapps.pockalendar.Utils.Utils;

/**
 * Created by chandrima on 22/04/18.
 */

public class ContactsCursorAdapter extends CursorAdapter {
    private LayoutInflater mLayoutInflater;
    private TextView mContactName;
    private TextView mContactPhone;
    private TextView mContactEmail;
    private ContentResolver mContentResolver;
    private Cursor mCursor;
    private Cursor mPhoneCursor;
    private Cursor mEmailCursor;

    private String mName;
    private String mPhone;
    private String mEmail;

    public ContactsCursorAdapter(Context context, Cursor c, int flags, ContentResolver contentResolver) {
        super(context, c, flags);
        mLayoutInflater = LayoutInflater.from(context);
        mContentResolver = contentResolver;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = mLayoutInflater.inflate(R.layout.contacts_display, viewGroup, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        mCursor = cursor;
        mContactName = (TextView)view.findViewById(R.id.contactName);
        mContactPhone = (TextView)view.findViewById(R.id.contactPhone);
        mContactEmail = (TextView)view.findViewById(R.id.contactEmail);

        mName = getName();
        mPhone = getMobilePhone();
        mEmail = getEmail();

        if (!Utils.isEmpty(mName) && Utils.isRealName(mName)) {
            mContactName.setVisibility(View.VISIBLE);
            mContactName.setText(mName);
        }
        if (!Utils.isEmpty(mPhone)) {
            mContactPhone.setVisibility(View.VISIBLE);
            mContactPhone.setText(mPhone);
        }
        if (!Utils.isEmpty(mEmail)) {
            mContactEmail.setVisibility(View.VISIBLE);
            mContactEmail.setText(mEmail);
        }
    }

    public String getName() {
        return  mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
    }

    public String getMobilePhone() {
        mPhoneCursor = getPhoneCursor();
        while (mPhoneCursor.moveToNext()) {
            String number = mPhoneCursor.getString(mPhoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int type = mPhoneCursor.getInt(mPhoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            switch (type) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                   return number;
            }
        }
        return null;
    }

    public String getEmail() {
        mEmailCursor = getEmailCursor();
        if (mEmailCursor.moveToFirst()) {
            return mEmailCursor.getString(mEmailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
        }
        return null;
    }

    public String getEmail(Cursor c) {
        mCursor = c;
        return getEmail();
    }

    public String getMobilePhone(Cursor c) {
        mCursor = c;
        return getMobilePhone();
    }

    public String getBirthDate(Cursor cursor) {
        return null;
    }

    // -------------------------------------------------Private methods------------------------------------------------

    private Cursor getPhoneCursor() {
        if (mCursor.moveToFirst()) {
            String contactId = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID));
            return mContentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
        }
        return null;
    }

    private Cursor getEmailCursor() {
        if (mCursor.moveToFirst()) {
            String contactId = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID));
            return mContentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);
        }
        return null;
    }
}
