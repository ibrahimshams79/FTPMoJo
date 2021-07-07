package com.example.ftpmojo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class PasswordActivity extends AppCompatActivity {
    TextView signup_in_pwd, log_in;
    EditText pwdTextEmail;
    Button forgot_pwd_btn;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        signup_in_pwd = findViewById(R.id.signup_in_password);
        log_in = findViewById(R.id.back_to_login);
        pwdTextEmail = findViewById(R.id.pwdTextEmail);
        forgot_pwd_btn = findViewById(R.id.forgotbtn);

        firebaseAuth = FirebaseAuth.getInstance();
        signup_in_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PasswordActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        log_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PasswordActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });

        forgot_pwd_btn.setOnClickListener(new View.OnClickListener() {
            private String z;

            @Override
            public void onClick(View view) {
                boolean isSuccess = false;
               
                        ConnectionClass connectionClass;
                String pwdTextEmailstr = pwdTextEmail.getText().toString();
                {
                    if (pwdTextEmailstr.trim().equals(""))
                        Toast.makeText(PasswordActivity.this, "Please enter Registered EmailID", Toast.LENGTH_SHORT).show();
                    else {
                        try {
                            connectionClass = new ConnectionClass();
                            Connection con = connectionClass.CONN();
                            if (con == null) {
                                Toast.makeText(PasswordActivity.this, "Error in connection with SQL server", Toast.LENGTH_SHORT).show();
                            } else {
                                String query = "select * from users where useremail='" + pwdTextEmailstr + "'";

                                Statement stmt2 = con.createStatement();
                                ResultSet rs = stmt2.executeQuery(query);
                                if (rs.next()) {
                                    firebaseAuth.sendPasswordResetEmail(pwdTextEmailstr).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(PasswordActivity.this, "Password reset sent to "+pwdTextEmail, Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @org.jetbrains.annotations.NotNull Exception e) {
                                            Toast.makeText(PasswordActivity.this, "Please try again "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } else {
                                    Toast.makeText(PasswordActivity.this, "User not registered with this Email", Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (Exception ex) {
                            isSuccess = false;
                            z = ex.getMessage();
                        }
                    }
                }
            }
        });

    }
}