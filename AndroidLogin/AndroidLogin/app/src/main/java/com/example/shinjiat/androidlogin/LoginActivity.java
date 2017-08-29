package com.example.shinjiat.androidlogin;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG1 = "tag123";
    private Context context = this;
    private ProgressDialog loading;

    //*member table
    //this API verify if the username and password matches any row in database(User login), then it returns 0 or 1(fail or success)
    private String API_LOGIN = "http://cloudangel.16mb.com/login/login.php";

    //*guest table
    //this API inserts UUID and a token(Guest login function), then it returns 0 or 1(fail or success)
    private String API_INSERT = "http://cloudangel.16mb.com/login/insert.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText etUsername = (EditText) findViewById(R.id.loginScreen_USERNAME);
        final EditText etPassword = (EditText) findViewById(R.id.loginScreen_PASSWORD);
        final Button bLogin = (Button) findViewById(R.id.loginScreen_LoginButton);
        final Button bGuest = (Button) findViewById(R.id.loginScreen_GuestButton);


        //login button's function
        bLogin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                //get edittext's values uponbutton is clicked
                String user = etUsername.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                //validate edit text, if field is empty, show a short toast
                if(!validate(user)){
                    Toast.makeText(LoginActivity.this,"Username cannot be empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                //validate edit text, if field is empty, show a toast
                if(!validate(pass)){
                    Toast.makeText(LoginActivity.this,"Password cannot be empty",Toast.LENGTH_SHORT).show();
                    return;
                }

                userLogin(user,pass);
            }
        });

        //guest button's function
        bGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userUUID = getUUID(); //get device's UUID
                guestLogin(userUUID); //insert it into database
            }
        });
    }

    //user login function that send username and password to API, API then return 0(username or password not matched) or 1(username and password matched).
    private void userLogin(final String user, String pass){
        class LoginUser extends AsyncTask<String, Void, String>
        {
            private HTTPHelper ruc = new HTTPHelper();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(LoginActivity.this, "Logging in...",null, true, true); //show a dialog
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if(s.equals("1")){//if success (username and password matched)
                    Toast.makeText(getApplicationContext(),"Hello, "+ user + ".", Toast.LENGTH_SHORT).show();
                }

                else//when username or password don't matched
                {
                    Toast.makeText(getApplicationContext(),"You have entered a wrong username or password.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected String doInBackground(String... params) {

                HashMap<String, String> data = new HashMap<String,String>();
                data.put("username",params[0]);
                data.put("password",params[1]);

                return ruc.sendPostRequest(API_LOGIN,data);
            }
        }

        LoginUser ru = new LoginUser();
        ru.execute(user,pass);
    }

    //guest login will need a token(session) to login, so after a insertion of token to the database, guests will be able to login
    private void guestLogin(final String UUID){
        class LoginUser extends AsyncTask<String, Void, String>
        {

            private HTTPHelper ruc = new HTTPHelper();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(LoginActivity.this, "Requesting token to log in..",null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if(s.equals("1")){//if SQL(insertion of UUID and token) ran successfully
                    Toast.makeText(getApplicationContext(),"Hello and welcome, Guest!", Toast.LENGTH_SHORT).show();
                }

                else//if SQL(insertion of UUID and token) failed
                {
                    Toast.makeText(getApplicationContext(),"Sorry, please try again later.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected String doInBackground(String... params) {

                HashMap<String, String> data = new HashMap<String,String>();
                data.put("UUID",params[0]);
                data.put("token",params[1]);

                return ruc.sendPostRequest(API_INSERT,data);
            }
        }

        String token = generateRandomHexToken(16);
        Log.d(TAG1, "token : " + token);

        LoginUser ru = new LoginUser();
        ru.execute(UUID, token);
    }

    private String generateRandomHexToken(int byteLength)
    {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[byteLength];
        secureRandom.nextBytes(token);
        return new BigInteger(token).toString(16); //hex encoding
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean validate(String content)
    {
        return !Objects.equals(content, "");
    }

    //get device's UUID, which will used for Guest login
    @SuppressLint("HardwareIds")
    private String getUUID()
    {
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        Log.d(TAG1, "UUID : " + deviceId);

        return deviceId;
    }
}
