package com.example.ftpmojo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    EditText name, phone, editTextEmail;
    Button registerbtn;
    TextView log_in;
    ConnectionClass connectionClass;
    String MobileNo_Str, UserName, editTextEmail_str;
    Boolean CheckEditText = false;
    SharedPreferences sharedPreferences;
    Spinner bureau_spinner;
    int bureau_spinner_value;

    String[] bureau = { "General", "Political",
            "Metro", "Regional", "Crime",
            "Film & Lifestyle", "Sports" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectionClass = new ConnectionClass();
        setContentView(R.layout.activity_register);
        log_in = (TextView) findViewById(R.id.login_in_signup);
        name = (EditText) findViewById(R.id.editTextF_Name);
        phone = (EditText) findViewById(R.id.editTextMobile);
        registerbtn = (Button) findViewById(R.id.registerButton);
        editTextEmail = findViewById(R.id.editTextEmail);
        sharedPreferences = getSharedPreferences("SHARED_PREF", MODE_PRIVATE);
        bureau_spinner = findViewById(R.id.bureau_spinner);
        bureau_spinner.setOnItemSelectedListener(this);

        ArrayAdapter ad
                = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                bureau);

        ad.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);
        bureau_spinner.setAdapter(ad);

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckEditTextIsEmptyOrNot();
                if (CheckEditText) {

                    // If EditText is not empty and CheckEditText = True then this block will execute.

                    MobileNo_Str = phone.getText().toString();
                    UserName = name.getText().toString();
                    editTextEmail_str = editTextEmail.getText().toString();
                    //int bureau = bureau_spinner_value;
                    new registeruserTask(RegisterActivity.this).execute();

                } else {

                    // If EditText is empty then this block will execute .
                    Toast.makeText(RegisterActivity.this, "Please fill all fields.", Toast.LENGTH_LONG).show();

                }

            }
        });

        log_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });

        phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (phone.getText().toString().trim().length() < 10) {
                        Toast.makeText(RegisterActivity.this, "Enter valid phone number", Toast.LENGTH_SHORT).show();
                        phone.setBackgroundColor(getColor(R.color.colorDeccent));
                        registerbtn.setClickable(false);
                    } else {
                        phone.setBackgroundColor(getColor(R.color.white));
                        registerbtn.setClickable(true);
                    }
                } else {
                    phone.setBackgroundColor(getColor(R.color.white));
                    registerbtn.setClickable(true);
                }
            }
        });

    }

    public void CheckEditTextIsEmptyOrNot() {
        MobileNo_Str = phone.getText().toString();
        UserName = name.getText().toString();
        editTextEmail_str = editTextEmail.getText().toString();
        CheckEditText = !TextUtils.isEmpty(MobileNo_Str) && !TextUtils.isEmpty(UserName) && !TextUtils.isEmpty(editTextEmail_str);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(getApplicationContext(),
                bureau[i],
                Toast.LENGTH_LONG)
                .show();
        bureau_spinner_value = i;
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public class registeruserTask extends BackgroundTask {

        String z = "";
        Boolean isSuccess = false;
        ProgressDialog loading = new ProgressDialog(RegisterActivity.this);

        public registeruserTask(Activity activity) {
            super(activity);
        }

        @Override
        public void onPreExecute() {
            loading.setMessage("\tSigning up...");
            loading.setCancelable(false);
            loading.show();
        }

        @Override
        public String doInBackground() {

            if (UserName.trim().equals("") || MobileNo_Str.trim().equals("") || editTextEmail_str.trim().equals("")) {
                z = "Please enter all fields";

            } else {
                Random r = new Random();
                int ri = r.nextInt(9999 - 0000);
                String Password_Str = String.valueOf(ri);
                try {
                    Connection con = connectionClass.CONN();
                    if (con == null) {
                        z = "Error connecting server. Try again later";
                    } else {
//                        String sql = "INSERT INTO users (username, userphone, useremail, password) VALUES ('" + UserName + "','" + MobileNo_Str + "','" + editTextEmail_str + "','" + Password_Str + "')";
//                        Statement stmt = con.createStatement();
//                        stmt.executeUpdate(sql);
                        PreparedStatement checkIfOldUser = con.prepareStatement("select * from users where userphone= ? or useremail= ?");
                        checkIfOldUser.setString(1, MobileNo_Str);
                        checkIfOldUser.setString(2, editTextEmail_str);
                        ResultSet resultSet = checkIfOldUser.executeQuery();
                        if (!resultSet.next()) {
                            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO users (username, userphone, useremail, password, bureau) VALUES (?,?,?,?,?)");
                            preparedStatement.setString(1, UserName);
                            preparedStatement.setString(2, MobileNo_Str);
                            preparedStatement.setString(3, editTextEmail_str);
                            preparedStatement.setString(4, Password_Str);
                            preparedStatement.setInt(5, bureau_spinner_value);
                            preparedStatement.executeUpdate();
                            z = "Signup successful";
                        } else z = "User Account Exists";
                    }

                } catch (Exception e) {
                    isSuccess = false;
                    z = e.getMessage();
                }
            }
            return z;
        }

        @Override
        public void onPostExecute(String s) {

            loading.dismiss();
            name.setText("");
            phone.setText("");
            editTextEmail.setText("");
            Toast.makeText(RegisterActivity.this, s, Toast.LENGTH_SHORT).show();
            if (s.equals("Signup successful")) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra("NEW_LOGIN", true);
                startActivity(intent);
            } else if (s.equals("User Account Exists")) {
                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("User Account Exists")
                        .setMessage("You are already a registered user. Go to login page?")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }
}
