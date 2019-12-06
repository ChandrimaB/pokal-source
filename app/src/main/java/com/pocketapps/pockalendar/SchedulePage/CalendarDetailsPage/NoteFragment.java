package com.pocketapps.pockalendar.SchedulePage.CalendarDetailsPage;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.pocketapps.pockalendar.ContactsCursorAdapter;
import com.pocketapps.pockalendar.GmailApi;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.CalendarItemObjectModel.CalendarItem;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;
import com.pocketapps.pockalendar.Utils.Utils;

import java.util.ArrayList;

/**
 * Created by chandrima on 12/04/18.
 */

public class NoteFragment extends Fragment implements CalendarDetailsActivity.OnSaveCalendarItemListener, View.OnClickListener, GmailApi.GoogleEmailEventListening, SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {
    private EditText mNotesEditor;
    private FrameLayout mFrameLayout;
    private TextView mTextView;
    private ImageButton mEditNoteButton;
    private ImageButton mSendAsEmailButton;
    private ImageButton mSendAsSMSButton;
    private int mOriginalLength;
    private PokalSharedPreferences mPokalSharedPreferences;
    private CalendarItem mCalendarItem;
    private GmailApi mGmailApi;
    private ConstraintLayout mSearchLayout;
    private SearchView mSearchContacts;
    private ImageButton mCancelEmail;
    private Context mContext;
    private Cursor mCursor;
    private ContactsCursorAdapter mContactsCursorAdapter;

    private static final int REQUEST_AUTHORIZATION = 139;
    private static final String MODE = "mode";
    private static final String CONTENT = "content";
    private static final String SEARCHOPEN = "searchopen";
    private static final String SEARCHQUERY = "searchquery";
    private static final String SHAREVIA = "sharevia";

    private enum Mode {
        NEW,
        READONLY,
        EDIT
    }

    private enum ShareVia {
        EMAIL,
        SMS,
        NONE
    }

    private Mode mMode = Mode.NEW;
    private ShareVia mShareVia = ShareVia.NONE;
    private boolean mIsSearchShown;

    //--------------------------------------------------------Life Cycle methods------------------------------------------------//
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_pokal_note_layout, container, false);

        mContext = getActivity().getApplicationContext();
        mPokalSharedPreferences = PokalSharedPreferences.getInstance(mContext);
        mGmailApi = GmailApi.getInstance(mContext);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mCalendarItem = (CalendarItem) bundle.getSerializable(CalendarDetailsActivity.OPENNOTE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNotesEditor = (EditText) getActivity().findViewById(R.id.editNote);
        mFrameLayout = (FrameLayout) getActivity().findViewById(R.id.notesContainer);
        mTextView = (TextView) getActivity().findViewById(R.id.notes);
        mEditNoteButton = (ImageButton) getActivity().findViewById(R.id.editNoteButton);
        mSendAsEmailButton = (ImageButton) getActivity().findViewById(R.id.sendAsEmailButton);
        mSendAsSMSButton = (ImageButton) getActivity().findViewById(R.id.sendAsTextButton);
        mSearchLayout = (ConstraintLayout) getActivity().findViewById(R.id.enterConatctOrNumberLayout);
        mSearchContacts = (SearchView) getActivity().findViewById(R.id.searchContacts);
        mSearchContacts.setSubmitButtonEnabled(true);
        mSearchContacts.setOnQueryTextListener(this);
        mSearchContacts.setOnSuggestionListener(this);
        mCancelEmail = (ImageButton) getActivity().findViewById(R.id.cancelButton);
        mCancelEmail.setOnClickListener(this);

        if (savedInstanceState != null) {
            mIsSearchShown = savedInstanceState.getBoolean(SEARCHOPEN);
            if (mIsSearchShown) {
                showSelectContactScreen();
                mSearchContacts.setQuery(savedInstanceState.getString(SEARCHQUERY), false);
                mShareVia = (ShareVia) savedInstanceState.getSerializable(SHAREVIA);
            }
            mMode = (Mode) savedInstanceState.getSerializable(MODE);
            mNotesEditor.setText(savedInstanceState.getString(CONTENT));
            if (mMode != null) {
                mNotesEditor.setSelection(mNotesEditor.length());
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                return;
            }
        }

        if (mCalendarItem != null) {
            mMode = Mode.READONLY;
            mNotesEditor.setVisibility(View.GONE);
            mFrameLayout.setVisibility(View.VISIBLE);
            mTextView.setText(mCalendarItem.getDescription());
            mEditNoteButton.setOnClickListener(this);
            mSendAsEmailButton.setOnClickListener(this);
            mSendAsSMSButton.setOnClickListener(this);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            return;
        }
        mNotesEditor.setText(getString(R.string.NoteEditDate, Utils.getCurrentDayAndDate()));
        mOriginalLength = mNotesEditor.length();
        mNotesEditor.setSelection(mOriginalLength);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGmailApi.registerGoogleEmailEventListener(this);
    }

    @Override
    public void onStop() {
        mGmailApi.deregisterGoogleEmailEventListener(this);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SEARCHOPEN, mIsSearchShown);
        outState.putString(SEARCHQUERY, mSearchContacts.getQuery().toString());
        outState.putSerializable(SHAREVIA, mShareVia);

        if (mMode == Mode.READONLY)
            return;

        outState.putSerializable(MODE, mMode);
        outState.putString(CONTENT, mNotesEditor.getText().toString());
    }

    //---------------------------------------------------Event Handlers---------------------------------------------------//
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editNoteButton:
                mMode = Mode.EDIT;
                mNotesEditor.setVisibility(View.VISIBLE);
                mFrameLayout.setVisibility(View.GONE);
                mNotesEditor.setText(mCalendarItem.getDescription());
                mNotesEditor.setSelection(mNotesEditor.length());
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                break;
            case R.id.sendAsEmailButton:
                mShareVia = ShareVia.EMAIL;
                showSelectContactScreen();
                mSearchContacts.setQuery(null, false);
                break;
            case R.id.sendAsTextButton:
                mShareVia = ShareVia.SMS;
                showSelectContactScreen();
                mSearchContacts.setQuery(null, false);
                break;
            case R.id.cancelButton:
                mShareVia = ShareVia.NONE;
                closeSelectContactScreen();
                mSearchContacts.setQuery(null, false);
                break;
        }
    }

    @Override
    public void onSaveCalendarItem() {
        if (mMode == Mode.READONLY)
            return;

        if (mMode == Mode.NEW && mNotesEditor.length() == mOriginalLength)
            return;

        if (mMode == Mode.EDIT && mCalendarItem != null) {
            mPokalSharedPreferences.deleteCalendarItem(mCalendarItem, Utils.POKAL_NOTE);
        }

        saveNote();
    }

    @Override
    public void onUserRecoverableAuthExceptionDuringEmailSend(UserRecoverableAuthIOException e) {
        startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTHORIZATION) {
            showSelectContactScreen();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mCursor.close();
        selectContactAndShare(query, mShareVia);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        ContentResolver contentResolver = getActivity().getContentResolver();

        String[] mProjection = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};

        Uri uri = ContactsContract.Contacts.CONTENT_URI;

        String selection = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + newText + "%"};
        mCursor = contentResolver.query(uri, mProjection, selection, selectionArgs, null);
        mContactsCursorAdapter = new ContactsCursorAdapter(mContext, mCursor, 0, contentResolver);
        mSearchContacts.setSuggestionsAdapter(mContactsCursorAdapter);
        return true;
    }

    @Override
    public boolean onSuggestionSelect(int position) {
        return true;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        Cursor cursor = mSearchContacts.getSuggestionsAdapter().getCursor();
        cursor.moveToPosition(position);
        if (mShareVia == ShareVia.EMAIL) {
            String email = mContactsCursorAdapter.getEmail(cursor);
            mSearchContacts.setQuery(Utils.isEmpty(email) ? "No email found" : email, false);
        }
        if (mShareVia == ShareVia.SMS) {
            String phone = mContactsCursorAdapter.getMobilePhone(cursor);
            mSearchContacts.setQuery(Utils.isEmpty(phone) ? "No phone found" : phone, false);
        }
        cursor.close();
        return true;
    }


    //------------------------------------------------Private methods----------------------------------------------------//

    private void saveNote() {
        CalendarItem calendarItem = new CalendarItem();
        calendarItem.setDate(Utils.getCurrentDayAndDate());
        calendarItem.setType(Utils.POKAL_NOTE);
        calendarItem.setDescription(mNotesEditor.getText().toString());

        ArrayList<CalendarItem> arrayList = new ArrayList<>();
        if (mPokalSharedPreferences.getCalendarItems(Utils.POKAL_NOTE) != null)
            arrayList = mPokalSharedPreferences.getCalendarItems(Utils.POKAL_NOTE);
        arrayList.add(calendarItem);

        mPokalSharedPreferences.saveCalendarItemToStore(arrayList, Utils.POKAL_NOTE);
    }

    private void showSelectContactScreen() {
        mSearchLayout.setVisibility(View.VISIBLE);
        mSearchContacts.setQueryHint(mShareVia == ShareVia.SMS ? getString(R.string.SearchQueryHintText) : getString(R.string.SearchQueryHintEmail));
        mIsSearchShown = true;
        mSendAsSMSButton.setEnabled(false);
        mSendAsEmailButton.setEnabled(false);
        mEditNoteButton.setEnabled(false);
    }

    private void selectContactAndShare(String query, ShareVia shareVia) {
        closeSelectContactScreen();
        if (Utils.isEmpty(query))
            return;

        if (shareVia == ShareVia.EMAIL) {
            if (!Utils.isEmail(query)) {
                return;
            }
            mGmailApi.sendEmail(mSearchContacts.getQuery().toString(), getResources().getString(R.string.ShareNoteViaEmailSubject), mTextView.getText().toString());
            Toast.makeText(mContext, getResources().getString(R.string.NoteEmailedToast), Toast.LENGTH_LONG).show();
            return;
        }

        if (shareVia == ShareVia.SMS) {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
            intent.setType("vnd.android-dir/mms-sms");
            intent.putExtra("address",mSearchContacts.getQuery().toString());
            intent.putExtra("sms_body",mTextView.getText().toString());
            startActivity(intent);
        }
    }

    private void closeSelectContactScreen() {
        mSearchLayout.setVisibility(View.GONE);
        mIsSearchShown = false;
        mSendAsSMSButton.setEnabled(true);
        mSendAsEmailButton.setEnabled(true);
        mEditNoteButton.setEnabled(true);
    }
}
