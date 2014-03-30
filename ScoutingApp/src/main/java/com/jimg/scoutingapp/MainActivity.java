package com.jimg.scoutingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.jimg.scoutingapp.asynctasks.GetAuthTokenAsyncTask;
import com.jimg.scoutingapp.fragments.PlaceholderFragment;
import com.jimg.scoutingapp.fragments.PlayerFragment;
import com.jimg.scoutingapp.fragments.TeamFragment;
import com.jimg.scoutingapp.helpers.ErrorHelpers;
import com.jimg.scoutingapp.helpers.LogHelpers;
import com.jimg.scoutingapp.intentservices.GetJsonIntentService;
import com.jimg.scoutingapp.pojos.PlayerPojo;
import com.jimg.scoutingapp.pojos.TeamTriplet;
import com.jimg.scoutingapp.repositories.Team;
import com.jimg.scoutingapp.utilityclasses.LocationUtils;
import com.jimg.scoutingapp.utilityclasses.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    // region Handles to UI widgets
    public ProgressDialog mProgressDialog;
    private Spinner mFavoriteTeamSpinner;
    private TextView mLocationGreetingTextView;
    // endregion

    private Handler mMenuHandler;
    private NetworkChangeReceiver mNetworkChangeReceiver;

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

    private Location mLastLocation;

    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;

    private static final String RAW_LEAGUE_TAG = "RawLeague";
    private static final String TEAM_NAMES_TAG = "TeamNames";
    private static final String PLAYER_TREEMAP_TAG = "PlayerTreeMap";
    private static final String MENU_TAG = "Menu";
    private static final String APP_START_DATE_TAG = "AppStartDate";
    private static final String SIGN_IN_STATUS_TAG = "SignInStatus";

    private ArrayList<TeamTriplet> mRawLeague;
    public TreeMap<Integer, String> mTeamNamesTreeMap;
    public TreeMap<Integer, TreeMap<String, PlayerPojo>> mPlayerTreeMap;
    private TreeMap<String, ArrayList<Pair<Integer, String>>> mTeamTreeMapForMenu;
    private Date mAppStartDate;
    private Semaphore mMenuLoaderSemaphore = new Semaphore(1, true);

    //region Google Api Fields
    private static final String TAG = "android-plus-quickstart";

    private static final int STATE_DEFAULT = 0;
    private static final int STATE_SIGN_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;

    private static final int RC_SIGN_IN = 0;

    private static final int DIALOG_PLAY_SERVICES_ERROR = 0;

    private static final String SAVED_PROGRESS = "sign_in_progress";

    // GoogleApiClient wraps our service connection to Google Play services and
    // provides access to the users sign in state and Google's APIs.
    private GoogleApiClient mGoogleApiClient;

    // We use mSignInProgress to track whether user has clicked sign in.
    // mSignInProgress can be one of three values:
    //
    //       STATE_DEFAULT: The default state of the application before the user
    //                      has clicked 'sign in', or after they have clicked
    //                      'sign out'.  In this state we will not attempt to
    //                      resolve sign in errors and so will display our
    //                      Activity in a signed out state.
    //       STATE_SIGN_IN: This state indicates that the user has clicked 'sign
    //                      in', so resolve successive errors preventing sign in
    //                      until the user has successfully authorized an account
    //                      for our app.
    //   STATE_IN_PROGRESS: This state indicates that we have started an intent to
    //                      resolve an error, and so we should not start further
    //                      intents until the current intent completes.
    private int mSignInProgress;

    // Used to store the PendingIntent most recently returned by Google Play
    // services until the user clicks 'sign in'.
    private PendingIntent mSignInIntent;

    // Used to store the error code most recently returned by Google Play services
    // until the user clicks 'sign in'.
    private int mSignInError;

    private SignInButton mSignInButton;
    private Button mSignOutButton;
    private Button mRevokeButton;
    private TextView mStatus;
    //endregion
    public Constants.SignInStatus mSignInStatus = Constants.SignInStatus.SignedOut;
    public String mAuthToken;
    private GetAuthTokenAsyncTask getAuthTokenAsyncTask;

    private void ChangeSignInStatus(Constants.SignInStatus signInStatus, String signInStatusText) {
        mSignInStatus = signInStatus;
        mStatus.setText(signInStatusText);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFavoriteTeamSpinner = (Spinner) findViewById(R.id.favorite_team_spinner);
        mLocationGreetingTextView = (TextView) findViewById(R.id.location_greeting_text_view);
        //region Google Api
        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mSignOutButton = (Button) findViewById(R.id.sign_out_button);
        mRevokeButton = (Button) findViewById(R.id.revoke_access_button);
        mStatus = (TextView) findViewById(R.id.sign_in_status);

        mSignInButton.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);
        mRevokeButton.setOnClickListener(this);
        //endregion

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mMenuHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                String errorMessage = reply.getString(Constants.errorMessageExtra);

                if (errorMessage != null) {
                    ErrorHelpers.handleError(MainActivity.this, getString(R.string.failure_to_load_message), errorMessage, reply.getString(Constants.stackTraceExtra));
                } else {
                    mRawLeague = (ArrayList<TeamTriplet>) reply.get(Constants.retrievedEntityExtra);
                    ArrayAdapter<TeamTriplet> arrayAdapter = new ArrayAdapter<TeamTriplet>(MainActivity.this, android.R.layout.simple_spinner_item, mRawLeague);
                    mFavoriteTeamSpinner.setAdapter(arrayAdapter);

                    mAppStartDate = new Date();
                    mTeamNamesTreeMap = Team.convertRawLeagueToTeamTreeMap(mRawLeague);
                    mPlayerTreeMap = new TreeMap<Integer, TreeMap<String, PlayerPojo>>();
                    mTeamTreeMapForMenu = Team.convertRawLeagueToDivisions(mRawLeague);
                    invalidateOptionsMenu();
                }

                mProgressDialog.dismiss();
                mProgressDialog = null;
                mMenuLoaderSemaphore.release();
            }
        };

        if (savedInstanceState != null) {
            mAppStartDate = (Date) savedInstanceState.getSerializable(APP_START_DATE_TAG);
            mRawLeague = (ArrayList<TeamTriplet>) savedInstanceState.getSerializable(RAW_LEAGUE_TAG);
            ArrayAdapter<TeamTriplet> arrayAdapter = new ArrayAdapter<TeamTriplet>(MainActivity.this, android.R.layout.simple_spinner_item, mRawLeague);
            mFavoriteTeamSpinner.setAdapter(arrayAdapter);
            mTeamNamesTreeMap = (TreeMap<Integer, String>) savedInstanceState.getSerializable(TEAM_NAMES_TAG);
            mPlayerTreeMap = (TreeMap<Integer, TreeMap<String, PlayerPojo>>) savedInstanceState.getSerializable(PLAYER_TREEMAP_TAG);
            mTeamTreeMapForMenu = (TreeMap<String, ArrayList<Pair<Integer, String>>>) savedInstanceState.getSerializable(MENU_TAG);
            //region Google Api
            mSignInProgress = savedInstanceState.getInt(SAVED_PROGRESS, STATE_DEFAULT);
            //endregion
            mSignInStatus = Constants.SignInStatus.values()[savedInstanceState.getInt(SIGN_IN_STATUS_TAG)];
        }

        mGoogleApiClient = buildGoogleApiClient();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        mLocationClient = new LocationClient(this, this, this);
    }

    //region Google Api
    private GoogleApiClient buildGoogleApiClient() {
        // When we build the GoogleApiClient we specify where connected and
        // connection failed callbacks should be returned, which Google APIs our
        // app uses and which OAuth 2.0 scopes our app requests.
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, null)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        mLocationClient.disconnect();

        super.onStop();
    }
    //endregion

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(RAW_LEAGUE_TAG, mRawLeague);
        outState.putSerializable(TEAM_NAMES_TAG, mTeamNamesTreeMap);
        outState.putSerializable(PLAYER_TREEMAP_TAG, mPlayerTreeMap);
        outState.putSerializable(MENU_TAG, mTeamTreeMapForMenu);
        outState.putSerializable(APP_START_DATE_TAG, mAppStartDate);
        //region Google Api
        outState.putInt(SAVED_PROGRESS, mSignInProgress);
        //endregion
        outState.putInt(SIGN_IN_STATUS_TAG, mSignInStatus.getValue());
        super.onSaveInstanceState(outState);
    }

    //region Google Api
    @Override
    public void onClick(View v) {
        if (!mGoogleApiClient.isConnecting()) {
            // We only process button clicks when GoogleApiClient is not transitioning
            // between connected and not connected.
            switch (v.getId()) {
                case R.id.sign_in_button:
                    ChangeSignInStatus(Constants.SignInStatus.SignedOut, getResources().getString(R.string.status_signing_in));
                    resolveSignInError();
                    break;
                case R.id.sign_out_button:
                    // We clear the default account on sign out so that Google Play
                    // services will not return an onConnected callback without user
                    // interaction.
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                    mGoogleApiClient.connect();
                    break;
                case R.id.revoke_access_button:
                    // After we revoke permissions for the user with a GoogleApiClient
                    // instance, we must discard it and create a new one.
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    // Our sample has caches no user data from Google+, however we
                    // would normally register a callback on revokeAccessAndDisconnect
                    // to delete user data so that we comply with Google developer
                    // policies.
                    Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
                    mGoogleApiClient = buildGoogleApiClient();
                    mGoogleApiClient.connect();
                    break;
            }
        }
    }

    /* onConnected is called when our Activity successfully connects to Google
     * Play services.  onConnected indicates that an account was selected on the
     * device, that the selected account has granted any requested permissions to
     * our app and that we were able to establish a service connection to Google
     * Play services.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected");

        if (mLocationClient.isConnected() && mLastLocation == null) {
            getLocation();
        }

        if (mGoogleApiClient.isConnected() && mAuthToken == null) {
            // Reaching onConnected means we consider the user signed in.
            getAuthTokenInAsyncTask();
        }
    }

    private void getAuthTokenInAsyncTask() {
        getAuthTokenAsyncTask = new GetAuthTokenAsyncTask();
        getAuthTokenAsyncTask.execute(new Pair<MainActivity, GoogleApiClient>(this, mGoogleApiClient));
    }

    public void getAuthTokenInAsyncTaskCallback(String authToken) {
        if (authToken != null && !authToken.isEmpty()) {
            mAuthToken = authToken;

            // Update the user interface to reflect that the user is signed in.
            mSignInButton.setEnabled(false);
            mSignOutButton.setEnabled(true);
            mRevokeButton.setEnabled(true);

            // Retrieve some profile information to personalize our app for the user.
            Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

            ChangeSignInStatus(Constants.SignInStatus.SignedIn, String.format(
                    getResources().getString(R.string.signed_in_as),
                    currentUser.getDisplayName()));

            // Indicate that the sign in process is complete.
            mSignInProgress = STATE_DEFAULT;
        } else {
            showDialog(DIALOG_PLAY_SERVICES_ERROR);

            mSignInButton.setEnabled(true);
            mSignOutButton.setEnabled(false);
            mRevokeButton.setEnabled(false);
        }
    }

    /* onConnectionFailed is called when our Activity could not connect to Google
     * Play services.  onConnectionFailed indicates that the user needs to select
     * an account, grant permissions or resolve an error in order to sign in.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.i(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());

        if (mSignInProgress != STATE_IN_PROGRESS) {
            // We do not have an intent in progress so we should store the latest
            // error resolution intent for use when the sign in button is clicked.
            mSignInIntent = result.getResolution();
            mSignInError = result.getErrorCode();

            if (mSignInProgress == STATE_SIGN_IN) {
                // STATE_SIGN_IN indicates the user already clicked the sign in button
                // so we should continue processing errors until the user is signed in
                // or they click cancel.
                resolveSignInError();
            }
        }

        // In this sample we consider the user signed out whenever they do not have
        // a connection to Google Play services.
        onSignedOut();

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (result.getErrorCode() != ConnectionResult.SIGN_IN_REQUIRED && result.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                result.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        }
    }

    /* Starts an appropriate intent or dialog for user interaction to resolve
     * the current error preventing the user from being signed in.  This could
     * be a dialog allowing the user to select an account, an activity allowing
     * the user to consent to the permissions being requested by your app, a
     * setting to enable device networking, etc.
     */
    private void resolveSignInError() {
        if (mSignInIntent != null) {
            // We have an intent which will allow our user to sign in or
            // resolve an error.  For example if the user needs to
            // select an account to sign in with, or if they need to consent
            // to the permissions your app is requesting.

            try {
                // Send the pending intent that we stored on the most recent
                // OnConnectionFailed callback.  This will allow the user to
                // resolve the error currently preventing our connection to
                // Google Play services.
                mSignInProgress = STATE_IN_PROGRESS;
                startIntentSenderForResult(mSignInIntent.getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Log.i(TAG, "Sign in intent could not be sent: "
                        + e.getLocalizedMessage());
                // The intent was canceled before it was sent.  Attempt to connect to
                // get an updated ConnectionResult.
                mSignInProgress = STATE_SIGN_IN;
                mGoogleApiClient.connect();
            }
        } else {
            // Google Play services wasn't able to provide an intent for some
            // error types, so we show the default Google Play services error
            // dialog which may still start an intent on our behalf if the
            // user can resolve the issue.
            showDialog(DIALOG_PLAY_SERVICES_ERROR);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    // If the error resolution was successful we should continue
                    // processing errors.
                    mSignInProgress = STATE_SIGN_IN;
                } else {
                    // If the error resolution was not successful or the user canceled,
                    // we should stop processing errors.
                    mSignInProgress = STATE_DEFAULT;
                }

                if (!mGoogleApiClient.isConnecting()) {
                    // If Google Play services resolved the issue with a dialog then
                    // onStart is not called so we need to re-attempt connection here.
                    mGoogleApiClient.connect();
                }
                break;

            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:
                        Log.d(LocationUtils.APPTAG, getString(R.string.resolved));
                        break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));
                        break;
                }
            default:
                Log.d(LocationUtils.APPTAG,
                        getString(R.string.unknown_activity_request_code, requestCode));
                break;
        }
    }

    private void onSignedOut() {
        // Update the UI to reflect that the user is signed out.
        mSignInButton.setEnabled(true);
        mSignOutButton.setEnabled(false);
        mRevokeButton.setEnabled(false);
        mAuthToken = null;

        ChangeSignInStatus(Constants.SignInStatus.SignedOut, getResources().getString(R.string.status_signed_out));
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        // We call connect() to attempt to re-establish the connection or get a
        // ConnectionResult that we can attempt to resolve.
        mGoogleApiClient.connect();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PLAY_SERVICES_ERROR:
                if (GooglePlayServicesUtil.isUserRecoverableError(mSignInError)) {
                    return GooglePlayServicesUtil.getErrorDialog(
                            mSignInError,
                            this,
                            RC_SIGN_IN,
                            new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    Log.e(TAG, "Google Play services resolution cancelled");
                                    mSignInProgress = STATE_DEFAULT;
                                    ChangeSignInStatus(Constants.SignInStatus.SignedOut, getResources().getString(R.string.status_signed_out));
                                }
                            }
                    );
                } else {
                    return new AlertDialog.Builder(this)
                            .setMessage(R.string.play_services_error)
                            .setPositiveButton(R.string.close,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Log.e(TAG, "Google Play services error could not be "
                                                    + "resolved: " + mSignInError);
                                            mSignInProgress = STATE_DEFAULT;
                                            ChangeSignInStatus(Constants.SignInStatus.SignedOut, getResources().getString(R.string.status_signed_out));
                                        }
                                    }
                            ).create();
                }
            default:
                return super.onCreateDialog(id);
        }
    }
    //endregion

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (0 < fm.getBackStackEntryCount()) {
            fm.popBackStackImmediate();
        } else {
            super.onBackPressed();
        }
    }

    public void goBack() {
        FragmentManager fm = getFragmentManager();
        fm.popBackStackImmediate();
    }

    private void populateMenu(Menu menu) {
        if (mTeamTreeMapForMenu == null) {
            return;
        }

        Integer i = 0, j = Menu.FIRST;
        for (String key : mTeamTreeMapForMenu.keySet()) {
            SubMenu subMenu = menu.addSubMenu(key);
            for (Pair<Integer, String> team : mTeamTreeMapForMenu.get(key)) {
                subMenu.add(i, team.first, j, team.second);
                j++;
            }
            i++;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu.hasVisibleItems() == false) {
            populateMenu(menu);
        }
        return true;
    }

    private void retrieveDataForMenu() {
        Boolean semaphoreAcquired = mMenuLoaderSemaphore.tryAcquire();
        if (semaphoreAcquired == false) {
            return;
        }

        long elapsedTimeSinceAppStartInDays = 0;
        if (mAppStartDate != null) {
            elapsedTimeSinceAppStartInDays = (new Date().getTime() - mAppStartDate.getTime()) / (1000 * 60 * 60 * 24);
        }

        if (mTeamTreeMapForMenu == null || 0 < elapsedTimeSinceAppStartInDays) {
            LogHelpers.ProcessAndThreadId("MainActivity.retrieveDataForMenu");

            mProgressDialog = ProgressDialog.show(MainActivity.this, "", getString(R.string.please_wait_message), false);
            Intent serviceIntent = new Intent(this, GetJsonIntentService.class);
            serviceIntent.putExtra(Constants.entityToRetrieveExtra, Constants.Entities.Team);
            serviceIntent.putExtra(Constants.messengerExtra, new Messenger(mMenuHandler));
            startService(serviceIntent);
        } else {
            mMenuLoaderSemaphore.release();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        ReplaceFragmentWithMenuItem(itemId);
        return super.onOptionsItemSelected(item);
    }

    private void ReplaceFragmentWithMenuItem(int itemId) {
        if (itemId <= 0) {
            return;
        }

        FragmentManager fm = getFragmentManager();
        if (itemId == android.R.id.home) {
            Fragment fragment = new PlaceholderFragment();

            if (0 < fm.getBackStackEntryCount()) {
                addFragmentToBackStack(fm, fragment);
            }
        } else {
            String title = mTeamNamesTreeMap.get(itemId);
            Fragment fragment = TeamFragment.newInstance(title, itemId);

            addFragmentToBackStack(fm, fragment);
        }
    }

    private void addFragmentToBackStack(FragmentManager fm, Fragment fragment) {
        fm.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void ReplaceFragmentWithPlayer(String title, HashMap<String, String> playerHashMap) {
        FragmentManager fm = getFragmentManager();
        Fragment fragment;

        fragment = PlayerFragment.newInstance(title, playerHashMap);

        addFragmentToBackStack(fm, fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNetworkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkChangeReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkChangeReceiver);
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final android.net.NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            final android.net.NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            final TextView welcomeMessageTextView = (TextView) findViewById(R.id.welcome_message_text_view);

            if (wifi.isAvailable() || mobile.isAvailable()) {
                retrieveDataForMenu();
                welcomeMessageTextView.setText(R.string.welcome_message);
            } else {
                welcomeMessageTextView.setText(R.string.please_connect_to_internet_message);
            }
        }
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
            }
            return false;
        }
    }

    public void getLocation() {
        // If Google Play Services is available
        if (servicesConnected()) {

            // Get the current location
            mLastLocation = mLocationClient.getLastLocation();

            if (mLastLocation != null) {
                // Display the current location in the UI
                mLocationGreetingTextView.setText(LocationUtils.getLatLng(this, mLastLocation));
            }
        }
    }

    public void getAddress() {
        // In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
            // No geocoder is present. Issue an error message
            Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
            return;
        }

        if (servicesConnected()) {
            // Get the current location
            Location currentLocation = mLocationClient.getLastLocation();

            // Start the background task
            (new MainActivity.GetAddressTask(this)).execute(currentLocation);
        }
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        Log.d(LocationUtils.APPTAG, getString(R.string.disconnected));
    }

    /**
     * An AsyncTask that calls getFromLocation() in the background.
     * The class uses the following generic types:
     * Location - A {@link android.location.Location} object containing the current location,
     * passed as the input parameter to doInBackground()
     * Void     - indicates that progress units are not used by this subclass
     * String   - An address passed to onPostExecute()
     */
    protected class GetAddressTask extends AsyncTask<Location, Void, String> {

        // Store the context passed to the AsyncTask when the system instantiates it.
        Context localContext;

        // Constructor called by the system to instantiate the task
        public GetAddressTask(Context context) {

            // Required by the semantics of AsyncTask
            super();

            // Set a Context for the background task
            localContext = context;
        }

        /**
         * Get a geocoding service instance, pass latitude and longitude to it, format the returned
         * address, and return the address to the UI thread.
         */
        @Override
        protected String doInBackground(Location... params) {
            /*
             * Get a new geocoding service instance, set for localized addresses. This example uses
             * android.location.Geocoder, but other geocoders that conform to address standards
             * can also be used.
             */
            Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

            // Get the current location from the input parameter list
            Location location = params[0];

            // Create a list to contain the result address
            List<Address> addresses = null;

            // Try to get an address for the current location. Catch IO or network problems.
            try {

                /*
                 * Call the synchronous getFromLocation() method with the latitude and
                 * longitude of the current location. Return at most 1 address.
                 */
                addresses = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1
                );

                // Catch network or other I/O problems.
            } catch (IOException exception1) {

                // Log an error and return an error message
                Log.e(LocationUtils.APPTAG, getString(R.string.IO_Exception_getFromLocation));

                // print the stack trace
                exception1.printStackTrace();

                // Return an error message
                return (getString(R.string.IO_Exception_getFromLocation));

                // Catch incorrect latitude or longitude values
            } catch (IllegalArgumentException exception2) {

                // Construct a message containing the invalid arguments
                String errorString = getString(
                        R.string.illegal_argument_exception,
                        location.getLatitude(),
                        location.getLongitude()
                );
                // Log the error and print the stack trace
                Log.e(LocationUtils.APPTAG, errorString);
                exception2.printStackTrace();

                //
                return errorString;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {

                // Get the first address
                Address address = addresses.get(0);

                // Format the first line of address
                String addressText = getString(R.string.address_output_string,

                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",

                        // Locality is usually a city
                        address.getLocality(),

                        // The country of the address
                        address.getCountryName()
                );

                // Return the text
                return addressText;

                // If there aren't any addresses, post a message
            } else {
                return getString(R.string.no_address_found);
            }
        }

        /**
         * A method that's called once doInBackground() completes. Set the text of the
         * UI element that displays the address. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(String address) {

            // Turn off the progress bar
            // mActivityIndicator.setVisibility(View.GONE);

            // Set the address in the UI
            mLocationGreetingTextView.setText(address);
        }
    }

    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                this,
                LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
        }
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
