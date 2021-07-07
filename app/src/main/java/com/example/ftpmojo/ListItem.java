package com.example.ftpmojo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListItem {
    Connection connect;
    String ConnectionResult = "";
    Boolean isSuccess = false;

    public List<Map<String, String>> getList() {
        List<Map<String, String>> data = null;
        data = new ArrayList<Map<String, String>>();
        try {
            ConnectionClass connectionClass = new ConnectionClass();
            connect = connectionClass.CONN();
            if (connect != null) {
                String qu = "Select files from mediaTable where userid='"+6+"'";
                Statement statement = connect.createStatement();
                    ResultSet resultSet = statement.executeQuery(qu);
                    while (resultSet.next()){
                        try {
                            Map<String,String> dtname = new HashMap<String, String>();
                            dtname.put("Files", resultSet.getString("files"));
//                            dtname.put("StoryID", resultSet.getString(Integer.parseInt("storyid")));

                            data.add(dtname);
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }

                    }
                    ConnectionResult ="Success";
                    isSuccess= true;
                    connect.close();


            }else
                ConnectionResult="Failed";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

}
