package com.jimg.scoutingapp;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

/**
 * Created by Jim on 2/9/14.
 */
public class OnDemandJsonFetchWorker extends IntentService {
    Handler mHandler;

    public OnDemandJsonFetchWorker(){
        super("OnDemandJsonFetchWorker");
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogHelper.ProcessAndThreadId("OnDemandJsonFetchWorker.onHandleIntent");

        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            throw new IllegalArgumentException("Bundle is empty.");
        }

        Messenger messenger = (Messenger) bundle.get(Constants.messengerExtra);
        Constants.Entities entityToRetrieve = (Constants.Entities) bundle.get(Constants.entityToRetrieveExtra);

        switch (entityToRetrieve)
        {
            case Team:
                new Team().getAll(messenger);
                break;
            default:
                throw new IllegalArgumentException(entityToRetrieve.ordinal() + " has not been implemented.");
        }
    }
}
