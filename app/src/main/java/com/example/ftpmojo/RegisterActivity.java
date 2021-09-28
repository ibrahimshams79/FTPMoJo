package com.example.ftpmojo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {
    EditText name, phone, editTextEmail;
    Button registerbtn;
    TextView status, log_in;
    Connection con;
    Statement stmt;
    ConnectionClass connectionClass;
    String MobileNo_Str, Password_Str, UserName, editTextEmail_str;
    Boolean CheckEditText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectionClass = new ConnectionClass();
        setContentView(R.layout.activity_register);
        log_in = (TextView) findViewById(R.id.login_in_signup);
        name = (EditText) findViewById(R.id.editTextF_Name);
        phone = (EditText) findViewById(R.id.editTextMobile);
//        password = (EditText) findViewById(R.id.editTextPassword);
        registerbtn = (Button) findViewById(R.id.registerButton);
        editTextEmail = findViewById(R.id.editTextEmail);

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckEditTextIsEmptyOrNot();
                if (CheckEditText) {

                    // If EditText is not empty and CheckEditText = True then this block will execute.

//                    UserLoginFunction(MobileNo_Str, Password_Str);
                    MobileNo_Str = phone.getText().toString();
//                    Password_Str = password.getText().toString();
                    UserName = name.getText().toString();
                    editTextEmail_str = editTextEmail.getText().toString();
                    new RegisterActivity.registeruser().execute(UserName, MobileNo_Str, editTextEmail_str, Password_Str);

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
//        Password_Str = password.getText().toString();
        UserName = name.getText().toString();
        editTextEmail_str = editTextEmail.getText().toString();
        CheckEditText = !TextUtils.isEmpty(MobileNo_Str) && !TextUtils.isEmpty(UserName) && !TextUtils.isEmpty(editTextEmail_str);
    }

    public class registeruser extends AsyncTask<String, String, String> {

        String z = "";
        Boolean isSuccess = false;
        ProgressDialog loading = new ProgressDialog(RegisterActivity.this);
        private FirebaseAuth mAuth;

        @Override
        protected void onPreExecute() {
            loading.setMessage("\tSigning up...");
            loading.setCancelable(false);
            loading.show();
        }

        @Override
        protected void onPostExecute(String s) {
            loading.dismiss();
            name.setText("");
            phone.setText("");
//            password.setText("");
            editTextEmail.setText("");
            Toast.makeText(RegisterActivity.this, s, Toast.LENGTH_SHORT).show();
            if (s.equals("Signup successful")) {
//                mAuth.createUserWithEmailAndPassword(editTextEmail_str, Password_Str)
//                        .addOnCompleteListener((Executor) this, new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if (task.isSuccessful()) {
//                                    // Sign in success, update UI with the signed-in user's information
//                                    Log.d(TAG, "createUserWithEmail:success");
////                                    FirebaseUser user = mAuth.getCurrentUser();
////                                    updateUI(user);
//                                } else {
//                                    // If sign in fails, display a message to the user.
//                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                                    Toast.makeText(RegisterActivity.this, "Authentication failed.",
//                                            Toast.LENGTH_SHORT).show();
////                                    updateUI(null);
//                                }
//
//                                // ...
//                            }
//                        });
                Intent intent = new Intent(RegisterActivity.this, FTPActivity.class);
//                                intent.putExtra("url", "http://" + hostNamesCopy.get(position) + "/tv9/");
                startActivity(intent);
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            if (UserName.trim().equals("") || MobileNo_Str.trim().equals("") || editTextEmail_str.trim().equals("")) {
                z = "Please enter all fields";

            } else {
                Random r = new Random();
                int ri = r.nextInt(9999-0000);
                String Password_Str = String.valueOf(ri);
                try {
                    Connection con = connectionClass.CONN();
                    if (con == null) {
                        z = "Check Your Internet Connection";
                    } else {
                        String sql = "INSERT INTO users (username, userphone, useremail, password) VALUES ('" + UserName + "','" + MobileNo_Str + "','" + editTextEmail_str + "','" + Password_Str + "')";
                        Statement stmt = con.createStatement();
                        stmt.executeUpdate(sql);
                        z = "Signup successful";
                    }

                } catch (Exception e) {
                    isSuccess = false;
                    z = e.getMessage();
                }
            }
            return z;
        }

    }
}
