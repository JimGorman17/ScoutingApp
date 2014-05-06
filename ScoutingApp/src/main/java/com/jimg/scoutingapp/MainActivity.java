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
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.jimg.scoutingapp.asynctasks.GetAuthTokenAsyncTask;
import com.jimg.scoutingapp.fragments.AllTeamsFragment;
import com.jimg.scoutingapp.fragments.FlaggedCommentsFragment;
import com.jimg.scoutingapp.fragments.PlaceholderFragment;
import com.jimg.scoutingapp.fragments.PlayerFragment;
import com.jimg.scoutingapp.fragments.TeamFragment;
import com.jimg.scoutingapp.fragments.TopUsersFragment;
import com.jimg.scoutingapp.helpers.ErrorHelpers;
import com.jimg.scoutingapp.helpers.LogHelpers;
import com.jimg.scoutingapp.helpers.TeamHelpers;
import com.jimg.scoutingapp.pojos.PlayerPojo;
import com.jimg.scoutingapp.pojos.TeamPojo;
import com.jimg.scoutingapp.pojos.TeamTriplet;
import com.jimg.scoutingapp.utilityclasses.LocationUtils;
import com.jimg.scoutingapp.utilityclasses.Pair;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import butterknife.ButterKnife;
import butterknife.InjectView;
import icepick.Icepick;
import icepick.Icicle;

public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    // region Handles to UI widgets
    public Menu mMenu;
    public ProgressDialog mProgressDialog;

    @InjectView(R.id.welcome_layout) LinearLayout mWelcomeLayout;
    @InjectView(R.id.favorite_team_layout) LinearLayout mFavoriteTeamLayout;
    @InjectView(R.id.welcome_message_text_view) TextView mWelcomeMessageTextView;
    @InjectView(R.id.favorite_team_spinner) Spinner mFavoriteTeamSpinner;
    @InjectView(R.id.choose_your_favorite_team_text_view) TextView mChooseYourFavoriteTeamTextView;
    @InjectView(R.id.your_favorite_team_is_text_view) TextView mYourFavoriteTeamIsTextView;
    @InjectView(R.id.submit_favorite_team_button) ImageButton mSubmitFavoriteTeamButton;
    @InjectView(R.id.edit_favorite_team_button) ImageButton mEditFavoriteTeamButton;
    @InjectView(R.id.support_link_text_view) TextView mSupportLinkTextView;

    @InjectView(R.id.sign_in_button) SignInButton mSignInButton;
    @InjectView(R.id.sign_out_button) Button mSignOutButton;
    @InjectView(R.id.revoke_access_button) Button mRevokeButton;
    @InjectView(R.id.sign_in_status) TextView mStatus;
    // endregion

    private NetworkChangeReceiver mNetworkChangeReceiver;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;

    private static final String FAVORITE_TEAM_TAG = "FavoriteTeam";
    private static final String SIGN_IN_STATUS_TAG = "SignInStatus";

    @Icicle Date mAppStartDate;
    @Icicle ArrayList<TeamTriplet> mRawLeague;
    @Icicle Integer mFavoriteTeamId;
    @Icicle TreeMap<String, ArrayList<Pair<Integer, String>>> mTeamTreeMapForMenu;

    @Icicle public String mAuthToken;
    @Icicle public TreeMap<Integer, String> mTeamNamesTreeMap;
    @Icicle public TreeMap<Integer, TreeMap<String, PlayerPojo>> mPlayerTreeMap;

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

    //endregion
    public Constants.SignInStatus mSignInStatus = Constants.SignInStatus.SignedOut;
    @SuppressWarnings("FieldCanBeLocal")
    private GetAuthTokenAsyncTask getAuthTokenAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        Icepick.restoreInstanceState(this, savedInstanceState);

        //region Google Api
        mSignInButton.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);
        mRevokeButton.setOnClickListener(this);
        //endregion

        mSubmitFavoriteTeamButton.setOnClickListener(this);
        mEditFavoriteTeamButton.setOnClickListener(this);

        mSupportLinkTextView.setText(Html.fromHtml("<a href=\"mailto:"+getString(R.string.app_name)+"@yahoo.com"+"?subject="+getString(R.string.app_name)+ ": " + "Feedback" + "\" >"+"Send Feedback to " + getString(R.string.app_name)  +"</a>"));
        mSupportLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            ArrayAdapter<TeamTriplet> arrayAdapter = new ArrayAdapter<TeamTriplet>(MainActivity.this, android.R.layout.simple_spinner_item, mRawLeague);
            mFavoriteTeamSpinner.setAdapter(arrayAdapter);
            showFavoriteTeamLayout();
            //region Google Api
            mSignInProgress = savedInstanceState.getInt(SAVED_PROGRESS, STATE_DEFAULT);
            //endregion
            mSignInStatus = Constants.SignInStatus.values()[savedInstanceState.getInt(SIGN_IN_STATUS_TAG)];
        }

        mGoogleApiClient = buildGoogleApiClient();

        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        mLocationClient = new LocationClient(this, this, this);
    }

    private void showFavoriteTeamLayout() {
        if (0 < mFavoriteTeamId) {
            mWelcomeMessageTextView.setVisibility(View.GONE);
            setFavoriteTeamSpinnerPositionByTeamId(mFavoriteTeamId);
            mWelcomeLayout.setVisibility(View.GONE);
            mFavoriteTeamLayout.setVisibility(View.VISIBLE);

            TeamTriplet selectedTeam = (TeamTriplet) mFavoriteTeamSpinner.getSelectedItem();
            mYourFavoriteTeamIsTextView.setText(getResources().getString(R.string.your_favorite_team_is).replace("{0}", selectedTeam.name));
        }
    }

    private void showWelcomeLayout() {
        mWelcomeLayout.setVisibility(View.VISIBLE);
        mFavoriteTeamLayout.setVisibility(View.GONE);
    }

    private void setFavoriteTeamSpinnerPositionByTeamId(Integer favoriteTeamId) {
        for (int i = 0; i < mFavoriteTeamSpinner.getCount(); i++) {
            TeamTriplet teamTriplet = (TeamTriplet) mFavoriteTeamSpinner.getItemAtPosition(i);
            if (teamTriplet.id == favoriteTeamId) {
                mFavoriteTeamSpinner.setSelection(i);
            }
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            try {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            } catch (Exception e) {
                // nothing
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SIGN_IN_STATUS_TAG, mSignInStatus.getValue());
        //region Google Api
        outState.putInt(SAVED_PROGRESS, mSignInProgress);
        //endregion

        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
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

    @Override
    public void onClick(View v) {
        if (!mGoogleApiClient.isConnecting()) {
            // We only process button clicks when GoogleApiClient is not transitioning
            // between connected and not connected.
            switch (v.getId()) {
                case R.id.sign_in_button:
                    changeSignInStatus(Constants.SignInStatus.SignedOut, getResources().getString(R.string.status_signing_in));
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
                case R.id.submit_favorite_team_button:
                    submitFavoriteTeam();
                    break;
                case R.id.edit_favorite_team_button:
                    showWelcomeLayout();
                    break;
            }
        }
    }

    private void changeSignInStatus(Constants.SignInStatus signInStatus, String signInStatusText) {
        mSignInStatus = signInStatus;
        mStatus.setText(signInStatusText);
        displayCustomMenuItems();
    }

    private void displayCustomMenuItems() {
        if (mMenu != null) {
            final MenuItem flaggedCommentsMenuItem = mMenu.findItem(Constants.FLAGGED_COMMENTS_REPORT_ID);
            if (flaggedCommentsMenuItem != null) {
                if (mSignInStatus == Constants.SignInStatus.SignedIn) {
                    JsonObject json = new JsonObject();
                    json.addProperty(Constants.authTokenExtra, mAuthToken);

                    Ion.with(this, Constants.restServiceUrlBase + "User/GetAdminStatus?" + Constants.getJson)
                            .setJsonObjectBody(json)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    if (e != null) {
                                        LogHelpers.LogError(e.getMessage(), ErrorHelpers.getStackTraceAsString(e), MainActivity.this);
                                    } else {
                                        final JsonElement isAdmin = result.get("IsAdmin");
                                        flaggedCommentsMenuItem.setVisible(isAdmin != null && isAdmin.getAsBoolean());
                                    }
                                }
                            });
                } else {
                    flaggedCommentsMenuItem.setVisible(false);
                }
            }
        }
    }

    private void submitFavoriteTeam() {
        TeamTriplet selectedTeam = (TeamTriplet) mFavoriteTeamSpinner.getSelectedItem();
        mFavoriteTeamId = selectedTeam.id;
        mEditor.putInt(FAVORITE_TEAM_TAG, mFavoriteTeamId);
        mEditor.commit();
        if (mSignInStatus == Constants.SignInStatus.SignedIn) {
            updateFavoriteTeam();
        }
        showFavoriteTeamLayout();
    }

    private void updateFavoriteTeam() {
        if (    mAuthToken == null || mAuthToken.isEmpty() ||
                mFavoriteTeamId == null || mFavoriteTeamId <= 0) {
            return;
        }

        LogHelpers.ProcessAndThreadId("MainActivity.updateFavoriteTeam");

        JsonObject json = new JsonObject();
        json.addProperty(Constants.authTokenExtra, mAuthToken);
        json.addProperty(Constants.teamIdExtra, mFavoriteTeamId);

        Ion.with(this, Constants.restServiceUrlBase + "User/UpdateFavoriteTeam?" + Constants.getJson)
                .setJsonObjectBody(json)
                .asJsonObject();
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

        Integer favoriteTeamId = mPrefs.getInt(FAVORITE_TEAM_TAG, 0);
        if (mLocationClient.isConnected() && favoriteTeamId == 0) {
            Location lastLocation = getLastLocation();
            setFavoriteTeamToClosestTeam(lastLocation);
        }

        if (mGoogleApiClient.isConnected()) {
            // Reaching onConnected means we consider the user signed in.
            if (mAuthToken == null) {
                getAuthTokenInAsyncTask();
            } else {
                updateUiForSignIn(mAuthToken);
            }
        }
    }

    public Location getLastLocation() {
        Location lastLocation = null;
        // If Google Play Services is available
        if (servicesConnected()) {
            lastLocation = mLocationClient.getLastLocation();
        }

        return lastLocation;
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

    private void setFavoriteTeamToClosestTeam(Location lastLocation) {
        LogHelpers.ProcessAndThreadId("MainActivity.setFavoriteTeamToClosestTeam");

        String getClosestTeamUrl = Constants.restServiceUrlBase + "Team/GetClosestTeam?Latitude={0}&Longitude={1}&" + Constants.getJson;
        // mProgressDialog = ProgressDialog.show(this, "", getString(R.string.please_wait_message), false); mProgressDialog should already be displayed.

        Ion.with(this, getClosestTeamUrl.replace("{0}", Double.toString(lastLocation.getLatitude())).replace("{1}", Double.toString(lastLocation.getLongitude())))
                .as(new TypeToken<TeamPojo>(){})
                .setCallback(new FutureCallback<TeamPojo>() {
                    @Override
                    public void onCompleted(Exception e, TeamPojo result) {
                        if (e != null) {
                            ErrorHelpers.handleError(getString(R.string.failure_to_load_message), e.getMessage(), ErrorHelpers.getStackTraceAsString(e), MainActivity.this);
                        } else {
                            setFavoriteTeamSpinnerPositionByTeamId(result.teamId);
                        }
                    }
                });
    }

    private void getAuthTokenInAsyncTask() {
        getAuthTokenAsyncTask = new GetAuthTokenAsyncTask();
        getAuthTokenAsyncTask.execute(new Pair<MainActivity, GoogleApiClient>(this, mGoogleApiClient));
    }

    public void updateUiForSignIn(String authToken) {
        if (authToken != null && !authToken.isEmpty()) {
            mAuthToken = authToken;
            updateFavoriteTeam();

            // Update the user interface to reflect that the user is signed in.
            mSignInButton.setEnabled(false);
            mSignOutButton.setEnabled(true);
            mRevokeButton.setEnabled(true);

            // Retrieve some profile information to personalize our app for the user.
            Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

            changeSignInStatus(Constants.SignInStatus.SignedIn, String.format(
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

                ErrorHelpers.handleError(getString(R.string.failed_to_resolve_connection_failure), e.getMessage(), ErrorHelpers.getStackTraceAsString(e), this);
            }
        }
    }

    private void onSignedOut() {
        // Update the UI to reflect that the user is signed out.
        mSignInButton.setEnabled(true);
        mSignOutButton.setEnabled(false);
        mRevokeButton.setEnabled(false);
        mAuthToken = null;

        changeSignInStatus(Constants.SignInStatus.SignedOut, getResources().getString(R.string.status_signed_out));
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
                                    changeSignInStatus(Constants.SignInStatus.SignedOut, getResources().getString(R.string.status_signed_out));
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
                                            changeSignInStatus(Constants.SignInStatus.SignedOut, getResources().getString(R.string.status_signed_out));
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        if (!menu.hasVisibleItems()) {
            populateMenu(menu);
        }
        return true;
    }

    private void populateMenu(Menu menu) {
        if (mTeamTreeMapForMenu == null) {
            return;
        }

        SubMenu teamsMenuItem = menu.addSubMenu(Menu.NONE, Menu.NONE, Menu.NONE, getString(R.string.comments_action_bar_title));
        teamsMenuItem.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        for (String key : mTeamTreeMapForMenu.keySet()) {
            SubMenu subMenu = teamsMenuItem.addSubMenu(key);
            for (Pair<Integer, String> team : mTeamTreeMapForMenu.get(key)) {
                subMenu.add(Menu.NONE, team.first, Menu.NONE, team.second);
            }
        }

        SubMenu reportsMenuItem = menu.addSubMenu(Menu.NONE, Menu.NONE, Menu.NONE, getString(R.string.reports_action_bar_title));
        reportsMenuItem.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        reportsMenuItem.addSubMenu(Menu.NONE, Constants.FLAGGED_COMMENTS_REPORT_ID, Menu.NONE, Constants.FLAGGED_COMMENTS_REPORT_TITLE).getItem().setVisible(false); // We'll show this if we confirm that the user is an admin.
        reportsMenuItem.addSubMenu(Menu.NONE, Constants.ALL_TEAMS_REPORT_ID, Menu.NONE, Constants.ALL_TEAMS_REPORT_TITLE);
        reportsMenuItem.addSubMenu(Menu.NONE, Constants.TOP_USERS_REPORT_ID, Menu.NONE, Constants.TOP_USERS_REPORT_TITLE);

        displayCustomMenuItems();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId <= 0) {
            return false;
        }

        FragmentManager fm = getFragmentManager();
        if (itemId == android.R.id.home) {
            Fragment fragment = new PlaceholderFragment();

            if (0 < fm.getBackStackEntryCount()) {
                addFragmentToBackStack(fm, fragment);
            }
        } else if (itemId == Constants.FLAGGED_COMMENTS_REPORT_ID) {
            Fragment fragment = new FlaggedCommentsFragment();
            addFragmentToBackStack(fm, fragment);
        } else if (itemId == Constants.ALL_TEAMS_REPORT_ID) {
            Fragment fragment = new AllTeamsFragment();
            addFragmentToBackStack(fm, fragment);
        } else if (itemId == Constants.TOP_USERS_REPORT_ID) {
            Fragment fragment = new TopUsersFragment();
            addFragmentToBackStack(fm, fragment);
        } else {
            String title = mTeamNamesTreeMap.get(itemId);
            Fragment fragment = TeamFragment.newInstance(title, itemId);

            addFragmentToBackStack(fm, fragment);
        }
        return true;
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

            if (wifi.isAvailable() || mobile.isAvailable()) {
                retrieveDataForMenu();
                mWelcomeMessageTextView.setText(R.string.welcome_message);
                mChooseYourFavoriteTeamTextView.setVisibility(View.VISIBLE);
                mFavoriteTeamSpinner.setVisibility(View.VISIBLE);
                mSubmitFavoriteTeamButton.setVisibility(View.VISIBLE);
            } else {
                mWelcomeMessageTextView.setVisibility(View.VISIBLE);
                mWelcomeMessageTextView.setText(R.string.please_connect_to_internet_message);
                mChooseYourFavoriteTeamTextView.setVisibility(View.GONE);
                mFavoriteTeamSpinner.setVisibility(View.GONE);
                mSubmitFavoriteTeamButton.setVisibility(View.GONE);
            }
        }
    }

    private void retrieveDataForMenu() {
        Boolean semaphoreAcquired = mMenuLoaderSemaphore.tryAcquire();
        if (!semaphoreAcquired) {
            return;
        }

        long elapsedTimeSinceAppStartInDays = 0;
        if (mAppStartDate != null) {
            elapsedTimeSinceAppStartInDays = (new Date().getTime() - mAppStartDate.getTime()) / (1000 * 60 * 60 * 24);
        }

        if (mTeamTreeMapForMenu == null || 0 < elapsedTimeSinceAppStartInDays) {
            LogHelpers.ProcessAndThreadId("MainActivity.retrieveDataForMenu");

            mProgressDialog = ProgressDialog.show(MainActivity.this, "", getString(R.string.please_wait_message), false);
            Ion.with(this, Constants.restServiceUrlBase + "Team/GetAll?" + Constants.getJson)
                    .progressDialog(mProgressDialog)
                    .as(new TypeToken<ArrayList<TeamPojo>>() {
                    })
                    .setCallback(new FutureCallback<ArrayList<TeamPojo>>() {
                        @Override
                        public void onCompleted(Exception e, ArrayList<TeamPojo> result) {
                            if (e != null) {
                                ErrorHelpers.handleError(getString(R.string.failure_to_load_message), e.getMessage(), ErrorHelpers.getStackTraceAsString(e), MainActivity.this);
                            } else {
                                mRawLeague = new ArrayList<TeamTriplet>();
                                for (TeamPojo team : result) {
                                    TeamTriplet teamToReturn = new TeamTriplet(team.teamId, team.location + " " + team.nickname, team.conference + " " + team.division);
                                    mRawLeague.add(teamToReturn);
                                }

                                ArrayAdapter<TeamTriplet> arrayAdapter = new ArrayAdapter<TeamTriplet>(MainActivity.this, android.R.layout.simple_spinner_item, mRawLeague);
                                mFavoriteTeamSpinner.setAdapter(arrayAdapter);

                                mFavoriteTeamId = mPrefs.getInt(FAVORITE_TEAM_TAG, 0);
                                showFavoriteTeamLayout();

                                mAppStartDate = new Date();
                                mTeamNamesTreeMap = TeamHelpers.convertRawLeagueToTeamTreeMap(mRawLeague);
                                mPlayerTreeMap = new TreeMap<Integer, TreeMap<String, PlayerPojo>>();
                                mTeamTreeMapForMenu = TeamHelpers.convertRawLeagueToDivisions(mRawLeague);
                                invalidateOptionsMenu();
                            }

                            dismissProgressDialog();
                            mMenuLoaderSemaphore.release();
                        }
                    });
        } else {
            mMenuLoaderSemaphore.release();
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
}
