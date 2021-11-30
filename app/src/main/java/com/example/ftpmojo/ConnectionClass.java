package com.example.ftpmojo;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;


public class ConnectionClass {


    String ip = "43.254.161.61:1433";
    String un = "sa";
    String password = "Sa@2006";

//    String ip = "192.168.224.68";
//    String un = "test";
//    String password = "123";


    String jdbcdriver = "net.sourceforge.jtds.jdbc.Driver";
    String db = "tv9_mojo";


    @SuppressLint("NewApi")
    public Connection CONN() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL;
        try {

            Class.forName(jdbcdriver);
            ConnURL = "jdbc:jtds:sqlserver://" + ip + ";"
                    + "databaseName=" + db + ";user=" + un + ";password="
                    + password + ";";

            conn = DriverManager.getConnection(ConnURL);
        } catch (Exception se) {
            Log.e("ERRO", se.getMessage());
        }
        return conn;
    }

}
