package com.pocketapps.pockalendar;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import com.google.api.services.gmail.model.Message;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;
import com.pocketapps.pockalendar.Utils.Utils;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by chandrima on 20/04/18.
 */

public class GmailApi {
    private static final String TAG = GmailApi.class.getSimpleName();
    private static final String[] SCOPES = { GmailScopes.GMAIL_SEND };
    private static final String USERID = "me";
    private static GmailApi sGmailApi;
    GoogleAccountCredential mCredential;
    private PokalSharedPreferences mPokalSharedPreferences;

    public interface GoogleEmailEventListening {
      void onUserRecoverableAuthExceptionDuringEmailSend(UserRecoverableAuthIOException e);
    }

    private ArrayList<GoogleEmailEventListening> mGoogleEmailEventListenings = new ArrayList<>();

    private GmailApi(Context context) {
        mPokalSharedPreferences = PokalSharedPreferences.getInstance(context);
        mCredential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
        if (mPokalSharedPreferences.isGoogleCalendarSyncOn() && !Utils.needGoogleCalendarPermissions(context)) {
            String accountName = mPokalSharedPreferences.getEmail();
            if (accountName != null && Utils.isGmailAccount(accountName)) {
                mCredential.setSelectedAccountName(accountName);
            }
        }
    }

    // Singleton
    public static GmailApi getInstance(Context context) {
        if (sGmailApi == null) {
            sGmailApi = new GmailApi(context);
        }
        return sGmailApi;
    }

    public void registerGoogleEmailEventListener(GoogleEmailEventListening listener) {
        mGoogleEmailEventListenings.add(listener);
    }

    public void deregisterGoogleEmailEventListener(GoogleEmailEventListening listener) {
        mGoogleEmailEventListenings.remove(listener);
    }

    public void sendEmail(String to, String subject, String bodyText) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(to);
        arrayList.add(mCredential.getSelectedAccountName());
        arrayList.add(subject);
        arrayList.add(bodyText);
        new SendEmailTask(mCredential).execute(arrayList);
    }
    //------------------------------------------------------Private methods----------------------------------------------//

    private class SendEmailTask extends AsyncTask<ArrayList<String>, Void, Void> {
        private com.google.api.services.gmail.Gmail mService = null;

        SendEmailTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Pokal")
                    .build();
        }

        @Override
        protected Void doInBackground(ArrayList<String>[] arrayLists) {
            MimeMessage message = createEmail(arrayLists[0].get(0), arrayLists[0].get(1), arrayLists[0].get(2), arrayLists[0].get(3));
            if (message != null) {
                sendMessage(mService, USERID, message);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private MimeMessage createEmail(String to, String from, String subject, String bodyText) {
            try {
                Properties props = new Properties();
                Session session = Session.getDefaultInstance(props, null);
                MimeMessage email = new MimeMessage(session);
                email.setFrom(new InternetAddress(from));
                email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
                email.setSubject(subject);
                email.setText(bodyText);
                return email;
            } catch (MessagingException e) {
                Log.d(TAG, "MessagingException in createEmail" + e.getStackTrace());
                return null;
            }
        }

        public Message createMessageWithEmail(MimeMessage emailContent) {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                emailContent.writeTo(buffer);
                byte[] bytes = buffer.toByteArray();
                String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
                Message message = new Message();
                message.setRaw(encodedEmail);
                return message;
            } catch (MessagingException e) {
                Log.d(TAG, "MessagingException in createMessageWithEamil" + e.getStackTrace());
                return null;
            } catch (IOException e) {
                Log.d(TAG, "IOException in createEmail" + e.getStackTrace());
                return null;
            }
        }

        public Message sendMessage(Gmail service, String userId, MimeMessage emailContent) {
            try {
                Message message = createMessageWithEmail(emailContent);
                message = service.users().messages().send(userId, message).execute();
                Log.d(TAG, "Message id: " + message.getId());
                Log.d(TAG, message.toPrettyString());
                return message;
            } catch (Exception e) {
                if (e instanceof UserRecoverableAuthIOException) {
                    Log.d(TAG, "UserRecoverableAithIOException in sendMessage" + e.getStackTrace());
                    for (GoogleEmailEventListening listener : mGoogleEmailEventListenings) {
                        listener.onUserRecoverableAuthExceptionDuringEmailSend((UserRecoverableAuthIOException)e);
                    }
                }
                Log.d(TAG, "Exception in sendMessage" + e.getStackTrace());
                return null;
            }
        }
    }
}
