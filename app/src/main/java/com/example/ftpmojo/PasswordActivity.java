package com.example.ftpmojo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PasswordActivity extends AppCompatActivity {
    TextView signup_in_pwd, log_in;
    EditText pwdTextEmail;
    Button forgot_pwd_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        signup_in_pwd = findViewById(R.id.signup_in_password);
        log_in = findViewById(R.id.back_to_login);
        pwdTextEmail = findViewById(R.id.pwdTextEmail);
        forgot_pwd_btn = findViewById(R.id.forgotbtn);

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
                                    final View cusView = LayoutInflater.from(PasswordActivity.this).inflate(R.layout.newpassword, null);
                                    final AlertDialog.Builder cusDia = new AlertDialog.Builder(PasswordActivity.this);
                                    cusDia.setTitle("Reset Password\n");
                                    cusDia.setView(cusView);

                                    cusDia.setPositiveButton("save", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            EditText prePwd = cusView.findViewById(R.id.prev_pass);
                                            EditText newpwd = cusView.findViewById(R.id.new_password);
                                            final String prepwdString = prePwd.getText().toString().trim();
                                            final String newpwdString = newpwd.getText().toString().trim();

                                            if (TextUtils.isEmpty(prepwdString) || TextUtils.isEmpty(newpwdString))
                                                Toast.makeText(PasswordActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                                            else {
                                                String updatequery = "UPDATE users SET password='" + newpwdString + "' where password='" + prepwdString + "'";
                                                int rs3 = 0;
                                                try (Statement stmt3 = con.createStatement()) {
                                                    rs3 = stmt3.executeUpdate(updatequery);
                                                } catch (SQLException throwables) {
                                                    throwables.printStackTrace();
                                                }
                                                if (rs3 > 0) {
                                                    Toast.makeText(PasswordActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(PasswordActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    });
                                    cusDia.create().show();

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