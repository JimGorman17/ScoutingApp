package com.jimg.scoutingapp.intentservices;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

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
 * Created by Jim on 4/5/2014.
 */
public class PostErrorLogIntentService extends IntentService {
    @SuppressWarnings("FieldCanBeLocal")
    private final String mPostUrl = Constants.restServiceUrlBase + "ErrorLog/Create?" + Constants.getJson;

    public PostErrorLogIntentService() {
        super("PostErrorLogIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogHelpers.ProcessAndThreadId("PostErrorLogIntentService.onHandleIntent");

        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            throw new IllegalArgumentException("Bundle is empty.");
        }

        String application = bundle.getString(Constants.applicationExtra);
        String phoneId = bundle.getString(Constants.phoneIdExtra);
        String errorMessage = bundle.getString(Constants.errorMessageExtra);
        String stackTrace = bundle.getString(Constants.stackTraceExtra);
        String authToken = bundle.getString(Constants.authTokenExtra);

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(mPostUrl);
        HttpResponse httpResponse = null;
        Exception ex = null;
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
            nameValuePairs.add(new BasicNameValuePair(Constants.applicationExtra, application));
            nameValuePairs.add(new BasicNameValuePair(Constants.phoneIdExtra, phoneId));
            nameValuePairs.add(new BasicNameValuePair(Constants.errorMessageExtra, errorMessage));
            nameValuePairs.add(new BasicNameValuePair(Constants.stackTraceExtra, stackTrace));
            nameValuePairs.add(new BasicNameValuePair(Constants.authTokenExtra, authToken));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            httpResponse = httpclient.execute(httppost);
        } catch (Exception e) {
            ex = e;
        }

        if (httpResponse == null || httpResponse.getStatusLine().getStatusCode() != 200) {
            String loggedErrorMessage = ex != null ? ex.getMessage() : httpResponse.getStatusLine().getReasonPhrase();
            String loggedExceptionMessage = "";

            if (ex != null) {
                loggedExceptionMessage = ErrorHelpers.getStackTraceAsString(ex);
            }

            LogHelpers.LogError(loggedErrorMessage, loggedExceptionMessage, null);
        }
    }
}
