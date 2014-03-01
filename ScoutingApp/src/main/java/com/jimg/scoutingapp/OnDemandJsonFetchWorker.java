package com.jimg.scoutingapp;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

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

        try {
            getEntity(bundle, messenger, entityToRetrieve);
        } catch (Exception e) {
            Bundle data = new Bundle();
            data.putString(Constants.errorMessageExtra, e.getMessage());
            data.putString(Constants.stackTraceExtra, StackTraceHelpers.getStackTraceAsString(e));
            Message message = Message.obtain();
            message.setData(data);
            try {
                messenger.send(message);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void getEntity(Bundle bundle, Messenger messenger, Constants.Entities entityToRetrieve) throws Exception {
        switch (entityToRetrieve)
        {
            case PlayersByTeamId:
                Integer teamId = (Integer) bundle.get(Constants.teamIdExtra);
                new Player().getAllByTeamId(messenger, teamId);
                break;
            case Team:
                new Team().getAll(messenger);
                break;
            default:
                throw new IllegalArgumentException(entityToRetrieve.ordinal() + " has not been implemented.");
        }
    }
}
