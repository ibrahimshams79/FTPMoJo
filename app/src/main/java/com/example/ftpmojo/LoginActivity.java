package com.example.ftpmojo;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static com.example.ftpmojo.R.color;
import static com.example.ftpmojo.R.id;
import static com.example.ftpmojo.R.layout;

public class LoginActivity extends Activity {
    ConnectionClass connectionClass;
    EditText mobileNo, password;
    Button btnlogin;
    ProgressBar pbbar;
    String Password_Str, MobileNo_Str;
    boolean CheckEditText = false;
    TextView signup_in_login, forgot_passwordtv;
    SharedPreferences sharedPreferences;
    String uid, username;
    String phoneno, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_login);
        signup_in_login = findViewById(id.signup_in_login);
        connectionClass = new ConnectionClass();
        mobileNo = (EditText) findViewById(id.loginTextMobile);
        password = (EditText) findViewById(id.loginTextPassword);
        btnlogin = (Button) findViewById(id.loginButton);
        forgot_passwordtv = findViewById(id.forgot_password);
        sharedPreferences = getSharedPreferences("SHARED_PREF", MODE_PRIVATE);

        boolean isNewUser = getIntent().getBooleanExtra("NEW_LOGIN", false);

        if (isNewUser) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }else {
             phoneno = sharedPreferences.getString("PHONENO", MobileNo_Str);
             pass = sharedPreferences.getString("PASSWORD", Password_Str);
        }

        if (phoneno != null && pass != null) {
            Intent intent = new Intent(getApplicationContext(), FTPActivity.class);
            startActivity(intent);
        }

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Checking whether EditText is Empty or Not
                CheckEditTextIsEmptyOrNot();

                if (CheckEditText) {

                    // If EditText is not empty and CheckEditText = True then this block will execute.

//                    UserLoginFunction(MobileNo_Str, Password_Str);
                    MobileNo_Str = mobileNo.getText().toString();
                    Password_Str = password.getText().toString();

//                    new AsyncLogin().execute(MobileNo_Str, Password_Str);
                    new Login().execute();

                } else {

                    // If EditText is empty then this block will execute .
                    Toast.makeText(LoginActivity.this, "Please fill all fields.", Toast.LENGTH_LONG).show();

                }
            }
        });

        signup_in_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        forgot_passwordtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, PasswordActivity.class);
                startActivity(intent);
            }
        });
        mobileNo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (mobileNo.getText().toString().trim().length() < 10) {
                        Toast.makeText(LoginActivity.this, "Enter valid phone number", Toast.LENGTH_SHORT).show();
                        mobileNo.setBackgroundColor(getColor(color.colorDeccent));
                        btnlogin.setClickable(false);
                    } else {
                        mobileNo.setBackgroundColor(getColor(color.white));
                        btnlogin.setClickable(true);
                    }
                } else {
                    mobileNo.setBackgroundColor(getColor(color.white));
                    btnlogin.setClickable(true);
                }
            }
        });
    }

    public void CheckEditTextIsEmptyOrNot() {
        MobileNo_Str = mobileNo.getText().toString();
        Password_Str = password.getText().toString();

        CheckEditText = !TextUtils.isEmpty(MobileNo_Str) && !TextUtils.isEmpty(Password_Str);

    }

    public class Login extends AsyncTasks {
        String z = "";
        Boolean isSuccess = false;
        ProgressDialog loading = new ProgressDialog(LoginActivity.this);

        @Override
        public void onPreExecute() {
            loading.setMessage("\tSigning in...");
            loading.setCancelable(false);
            loading.show();
        }

        @Override
        public String doInBackground() {
            if (MobileNo_Str.trim().equals("") || Password_Str.trim().equals(""))
                z = "Please enter User Id and Password";
            else {
                try {
                    Connection con = connectionClass.CONN();
                    if (con == null) {
                        z = "Error in connection with SQL server";
                    } else {
//                        String query = "select * from users where userphone='" + MobileNo_Str + "' and password='" + Password_Str + "'";
//                        Statement stmt = con.createStatement();
//                        ResultSet rs = stmt.executeQuery(query);

                        PreparedStatement preparedStatement = con.prepareStatement("select * from users where userphone= ? and password= ?");
                        preparedStatement.setString(1, MobileNo_Str);
                        preparedStatement.setString(2, Password_Str);
                        ResultSet rs = preparedStatement.executeQuery();
                        if (rs.next()) {
                            z = "Login successful";
                            isSuccess = true;
                            uid = rs.getString("userid");
                            username = rs.getString("username");
                        } else {
                            z = "Invalid Credentials";
                            isSuccess = false;
                        }
                    }
                } catch (Exception ex) {
                    isSuccess = false;
                    z = ex.getMessage();
                }
            }
            return z;
        }

        @Override
        public void onPostExecute(String r) {
            loading.dismiss();
            Toast.makeText(LoginActivity.this, r, Toast.LENGTH_SHORT).show();
            if (r.equals("Login successful")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("PHONENO", MobileNo_Str);
                editor.putString("PASSWORD", Password_Str);
                editor.putString("UID", uid);
                editor.putString("UserName", username);
                editor.apply();
                Intent intent = new Intent(LoginActivity.this, FTPActivity.class);
                startActivity(intent);
            }
        }
    }
}
