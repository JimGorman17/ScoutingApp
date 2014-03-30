package com.jimg.scoutingapp.intentservices;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.helpers.ErrorHelpers;
import com.jimg.scoutingapp.helpers.LogHelpers;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jim on 3/8/14.
 */
public class PostPlayerCommentIntentService extends IntentService {
    @SuppressWarnings("FieldCanBeLocal")
    private final String mPostUrl = Constants.restServiceUrlBase + "Comment/Create?" + Constants.getJson;

    public PostPlayerCommentIntentService() {
        super("PostPlayerCommentIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogHelpers.ProcessAndThreadId("PostPlayerCommentIntentService.onHandleIntent");

        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            throw new IllegalArgumentException("Bundle is empty.");
        }

        Messenger messenger = (Messenger) bundle.get(Constants.messengerExtra);
        String authToken = bundle.getString(Constants.authTokenExtra);
        Integer playerId = bundle.getInt(Constants.playerIdExtra);
        String comment = bundle.getString(Constants.commentExtra);

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(mPostUrl);
        HttpResponse httpResponse = null;
        Exception ex = null;
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair(Constants.authTokenExtra, authToken));
            nameValuePairs.add(new BasicNameValuePair(Constants.playerIdExtra, playerId.toString()));
            nameValuePairs.add(new BasicNameValuePair(Constants.commentExtra, comment));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            httpResponse = httpclient.execute(httppost);
        } catch (Exception e) {
            ex = e;
        }

        if (httpResponse == null || httpResponse.getStatusLine().getStatusCode() != 200) {
            Bundle data = new Bundle();
            data.putString(Constants.errorMessageExtra, ex != null ? ex.getMessage() : httpResponse.getStatusLine().getReasonPhrase());
            if (ex != null) {
                data.putString(Constants.stackTraceExtra, ErrorHelpers.getStackTraceAsString(ex));
            }
            Message message = Message.obtain();
            message.setData(data);
            try {
                messenger.send(message);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        } else {
            Message message = Message.obtain(); // empty message without an error signifies success
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
