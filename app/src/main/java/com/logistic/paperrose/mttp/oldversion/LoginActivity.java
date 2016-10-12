package com.logistic.paperrose.mttp.oldversion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.pushes.MyActivity;
import com.logistic.paperrose.mttp.oldversion.search.TripleTableField;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A login screen that offers login via login/password.

 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor>{

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */

    public class TryMe implements Thread.UncaughtExceptionHandler {

        Thread.UncaughtExceptionHandler oldHandler;

        public TryMe() {
            oldHandler = Thread.getDefaultUncaughtExceptionHandler(); // сохраним ранее установленный обработчик
        }

        @Override
        public void uncaughtException(Thread thread, final Throwable throwable) {

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    final StringWriter errors = new StringWriter();
                    throwable.printStackTrace(new PrintWriter(errors));
                    JSONParser parser = new JSONParser();
                    parser.getJSONFromUrl("http://service.g-soft.ru/error/new", new HashMap<String, String>() {{
                        put("error", errors.toString());
                        put("login", getPref("login"));
                    }}, getApplicationContext());
                    return null;
                }

                @Override
                protected void onPostExecute(Void msg) {
                    ;
                }
            }.execute(null, null, null);


            if(oldHandler != null)

                oldHandler.uncaughtException(thread, throwable);
        }
    }


    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "superuser:superuser"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    public static final String PREFS_NAME = "UserPreference";
    private static final String PREF_USERNAME = "superuser";
    private static final String PREF_PASSWORD = "superuser";

    public void clearRequests(Context context) {
        String history = "";
        SharedPreferences mPrefs = context.getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor editor = mPrefs.edit();
        for (int i = 0; i < ApplicationParameters.request_history.size(); i++) {
            editor.putString("req" + Integer.toString(i + 1), "");
        }
        editor.commit();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new TryMe());
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ApplicationParameters.cached = false;
        clearRequests(LoginActivity.this);
        ApplicationParameters.request_history.clear();
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (getIntent().getBooleanExtra("login", true) == true) {
            createAfterChecking();
        }
    }

    public String getPref(String key) {
        return getSharedPreferences("UserPreference", 0).getString(key, "123");
    }

    public void createAfterChecking() {
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mLoginView = (AutoCompleteTextView) findViewById(R.id.login);
        populateAutoComplete();
        mLoginView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s=editable.toString();
                if(!s.equals(s.toUpperCase()))
                {
                    int t = mLoginView.getSelectionStart();
                    s=s.toUpperCase();
                    mLoginView.setText(s);
                    mLoginView.setSelection(t);
                }
            }
        });
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        CheckBox checkbox = (CheckBox)findViewById(R.id.showCheckBox);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPasswordView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    mPasswordView.setInputType(129);
                }
                mPasswordView.setSelection(mPasswordView.getText().length());
            }
        });
        try {
            if(getPref("login") != "123") {
                mLoginView.setText(getPref("login"));
                mPasswordView.setText(getPref("password"));
                attemptLogin();
            }
        } catch (Exception e) {

        }
    }

    private class CheckToken extends AsyncTask<Pair<String, HashMap<String, String>>, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Pair<String, HashMap<String, String>>... args) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = parser.getJSONFromUrl(args[0].first, args[0].second, getApplicationContext());
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            try {
                if (res.getInt("status") != 0) {
                    createAfterChecking();
                } else {
                    startNewActivity();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static String encryptStringSHA512(String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (md != null) {
            md.update(password.getBytes());
            byte byteData[] = md.digest();
            return bin2hex(byteData);
        } else {
            return password;
        }
    }

    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + 'x', new BigInteger(1, data));
    }





    private String getHashString(String login, String password) {
        return encryptStringSHA512(encryptStringSHA512(login+password) + ApplicationParameters.CLIENT_ID + ApplicationParameters.CLIENT_SECRET);
    }

    private void saveCredentials(String hash, String key) {
        SharedPreferences mPrefs = getSharedPreferences("UserPreference", 0);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, hash);
        editor.commit();
    }
    String SENDER_ID = "665149531559";
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        String registrationId = prefs.getString(ApplicationParameters.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            return "";
        }
        int registeredVersion = prefs.getInt(ApplicationParameters.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = MyActivity.getAppVersion(context);
        if (registeredVersion != currentVersion) {
            return "";
        }
        return registrationId;
    }
    GoogleCloudMessaging gcm;

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ApplicationParameters.PROPERTY_REG_ID, regId);
        editor.commit();
    }


    Context context;
    String regid;
    public boolean checkServerCredentials(final String hash, final String login) throws JSONException {

        context = getApplicationContext();
        gcm = GoogleCloudMessaging.getInstance(this);
        regid = getRegistrationId(context);
        ApplicationParameters.GCM_KEY = regid;
        if (ApplicationParameters.GCM_KEY.isEmpty()) {
            String msg = "";
            try {

                gcm = GoogleCloudMessaging.getInstance(context);
                regid = gcm.register(SENDER_ID);
                msg = "Device registered, registration ID=" + regid;
                ApplicationParameters.GCM_KEY = regid;

            } catch (Exception ex) {
                msg = "Error :" + ex.getMessage();
            }
            JSONParser parser = new JSONParser();
            JSONObject obj = parser.getJSONFromUrl(ApplicationParameters.AUTH_URL, new HashMap<String, String>() {{
                put("client_id", ApplicationParameters.CLIENT_ID);
                put("login", login);
                put("auth_token", hash);
                put("device_id", ApplicationParameters.GCM_KEY);
            }}, getApplicationContext());
            if (obj.has("error")) {
                return false;
            } else {
                try {
                    saveCredentials(obj.getString("access_token"), "access_token");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return true;

           /* new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {


                    return null;
                }

                @Override
                protected void onPostExecute(String msg) {
                    new AsyncTask<Void, Void, JSONObject>() {

                        @Override
                        protected JSONObject doInBackground(Void... voids) {

                            return null;

                        }

                        @Override
                        protected void onPostExecute(JSONObject obj) {
                            if (obj != null)

                            return;
                        }
                    };

                }
            }.execute(null, null, null);*/



        } else {
            JSONParser parser = new JSONParser();
            JSONObject obj = parser.getJSONFromUrl(ApplicationParameters.AUTH_URL, new HashMap<String, String>() {{
                put("client_id", ApplicationParameters.CLIENT_ID);
                put("login", login);
                put("auth_token", hash);
                put("device_id", ApplicationParameters.GCM_KEY);
            }}, getApplicationContext());
            if (obj.has("error")) {
                return false;
            } else {
                saveCredentials(obj.getString("access_token"), "access_token");
                //if ()

                //setTableFields(obj.getJSONArray("fields"));
                //ApplicationParameters.testTableFields();
                return true;
            }
        }
    }



    private void setTableFields(JSONArray arr) {
        ApplicationParameters.tableFields = new ArrayList<TripleTableField>();
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject obj = arr.getJSONObject(i);
                ApplicationParameters.tableFields.add(new TripleTableField(obj.getString("key"), obj.getString("name")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkCredentials() {
        String login = getPref("login");
        String password = getPref("password");// getSharedPreferences("UserPreference", 0).getString("credentials_hash", "");
        if (login.equals("") || login == null)
            return false;
        try {
            if (checkServerCredentials(encryptStringSHA512(encryptStringSHA512(login+password)+ApplicationParameters.CLIENT_ID+ApplicationParameters.CLIENT_SECRET), login))
                //тут будет вставлена проверка хеша с сервером, а код из onCreate перенесен в AsyncTask.PostExecute
                return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private void populateAutoComplete() {
        if (VERSION.SDK_INT >= 14) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, this);
        } else if (VERSION.SDK_INT >= 8) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid login, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mLoginView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String login = mLoginView.getText().toString();
        String password = mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid login address.
        if (TextUtils.isEmpty(login)) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        } else if (!isLoginValid(login)) {
            mLoginView.setError(getString(R.string.error_invalid_email));
            focusView = mLoginView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            CheckBox cb = (CheckBox)findViewById(R.id.saveLoginCheckBox);
            if (cb.isChecked()) {
                saveCredentials(login, "login");
                saveCredentials(password, "password");
            }
        //        saveCredentials(getHashString(login, password), "credentials_hash");
            mAuthTask = new UserLoginTask(login, password);
            mAuthTask.execute((Void) null);
        }
    }



    private boolean isLoginValid(String login) {
        //TODO: Replace this with your own logic
        return login.matches("[A-Z](\\w{1,19})");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return !password.isEmpty();//.matches("(\\w{1,19})");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public void startNewActivity() {
        if (ApplicationParameters.trafficDocuments != null)
            ApplicationParameters.trafficDocuments.clear();
        if (ApplicationParameters.eds != null)
            ApplicationParameters.eds.clear();
        ApplicationParameters.tempResults = null;
        ApplicationParameters.lastResults = null;
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Use an AsyncTask to fetch the user's login addresses on a background thread, and update
     * the login text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<String>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mLoginView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            try {
                if (!checkServerCredentials(encryptStringSHA512(encryptStringSHA512(mEmail+mPassword).toUpperCase() + ApplicationParameters.CLIENT_ID + ApplicationParameters.CLIENT_SECRET), mEmail)) {
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                startNewActivity();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}