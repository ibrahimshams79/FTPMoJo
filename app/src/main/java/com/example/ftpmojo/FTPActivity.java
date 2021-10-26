package com.example.ftpmojo;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import it.sauronsoftware.ftp4j.FTPClient;


public class FTPActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static FTPClient client;
    public static String currentUser;
    public static String currentHost;
    public static String currentPassword;
    public static int currentPort = 21;
    public ProgressBar loadingView;
    public ListView ftpListView;
    private SharedPreferences sharedPreferences;

    public static void login() throws Exception {
        client = new FTPClient();
        client.connect(currentHost, currentPort);
        client.login(currentUser, currentPassword);
    }
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer;
    Toolbar toolbar;

    private List<HashMap<String, Object>> homeSimpleAdaptList;
    private SimpleAdapter homeSimpleAdapter;

    private List<HashMap<String, Object>> ftpSimpleAdaptList;
    private SimpleAdapter ftpSimpleAdapter;

    private Button newFTPButton;

    private TextView loadText;

    private ArrayList<String> hostNamesCopy;
    private ArrayList<String> userNamesCopy;
    private ArrayList<String> userPasswordsCopy;
    private ArrayList<String> canLoginCopy;
    private ArrayList<String> loginDirectly;
    String UserIDfromSF, UserNamefromSF;
    String uid, username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);

        initViews();
        setupData();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadingView.setVisibility(View.INVISIBLE);
        loadText.setVisibility(View.INVISIBLE);

        if (FTPActivity.client != null) {
            try {
                FTPActivity.client.disconnect(true);
                FTPActivity.client = null;

                showToast("signed out");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //    This method implements the "My ftp" data display for ListView
    private void setupData() {

        //The following is the data from hardpreference to get the host number, user name, user password, and sign-in
        hostNamesCopy = new ArrayList<>(Arrays.asList(getSharedPreference("host_name")));
        userNamesCopy = new ArrayList<>(Arrays.asList(getSharedPreference("user_name")));
        userPasswordsCopy = new ArrayList<>(Arrays.asList(getSharedPreference("user_password")));
        canLoginCopy = new ArrayList<>(Arrays.asList(getSharedPreference("can_login")));
        loginDirectly = new ArrayList<>(Arrays.asList(getSharedPreference("login_directly")));

        if (hostNamesCopy.size() == 1 && hostNamesCopy.get(0).equals("")) {
            return;
        }

        //Traversing the array, assigning the data to ftpSimpleAdaptList, tells listview to update the data through the bound adapter
        for (int i = 0; i < hostNamesCopy.size(); i++) {
            HashMap<String, Object> hashMap
                    = new HashMap<>();
            hashMap.put("icon", R.drawable.ftp2);
            hashMap.put("name", userNamesCopy.get(i) + "@" + hostNamesCopy.get(i));
            //Note that String comparisons are address comparisons
            if (canLoginCopy.get(i).contentEquals("false")) {
                hashMap.put("name", userNamesCopy.get(i) + "@" + hostNamesCopy.get(i) + "(failure)");
            }
            ftpSimpleAdaptList.add(hashMap);
        }

        //Notify Listview to update the data
        ftpSimpleAdapter.notifyDataSetChanged();
    }

    private void initViews() {
        NavigationView navigationView = findViewById(R.id.reporter_nav_view_home);
        navigationView.setNavigationItemSelectedListener(this);
        drawer = findViewById(R.id.reporter_drawer_layout);
//        toolbar = findViewById(R.id.init_toolbar);
        toolbar = findViewById(R.id.reporter_drawer_toolbar);
        toolbar.setTitle("Connect to FTP server");
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
//        homeListView = findViewById(R.id.home_List_view);
        homeSimpleAdaptList = new ArrayList<>();
        String[] from1 = {"icon", "name"};
        int[] to1 = {R.id.cell_image, R.id.cell_name};
        HashMap<String, Object> hashMap
                = new HashMap<>();
        hashMap.put("icon", R.drawable.home);
        hashMap.put("name", "Local Home");
        homeSimpleAdaptList.add(hashMap);
        homeSimpleAdapter = new SimpleAdapter(getApplicationContext(), homeSimpleAdaptList, R.layout.cell, from1, to1);

        ftpListView = findViewById(R.id.ftp_list_view);
        ftpSimpleAdaptList = new ArrayList<>();
        String[] from2 = {"icon", "name"};
        int[] to2 = {R.id.cell_image, R.id.cell_name};
        ftpSimpleAdapter = new SimpleAdapter(getApplicationContext(), ftpSimpleAdaptList, R.layout.cell, from2, to2);
        ftpListView.setAdapter(ftpSimpleAdapter);

        newFTPButton = findViewById(R.id.new_ftp);

        loadingView = findViewById(R.id.avi1);

        loadText = findViewById(R.id.loadText);

        sharedPreferences = getSharedPreferences("SHARED_PREF", MODE_PRIVATE);
        UserIDfromSF = sharedPreferences.getString("UID", uid);
        UserNamefromSF = sharedPreferences.getString("UserName", username);
    }

    private void setListeners() {
        //Write a click-to-response event for My Local Files: The page jumps into LocalHomeActivity
//        homeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(InitialActivity.this, LocalHomeActivity.class);
//                startActivity(intent);
//            }
//        });

        //Write a click listener for "ftplistView": Click on the appropriate view to log the appropriate user into the ftp
        ftpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                loadText.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            //Get the user's password account number
                            currentUser = userNamesCopy.get(position);
                            currentHost = hostNamesCopy.get(position);
                            currentPassword = userPasswordsCopy.get(position);

                            //The login function
                            login();

                            if (FTPActivity.client.isConnected()) {
//                                loadText.setVisibility(View.INVISIBLE);
                                if (canLoginCopy.get(position).contentEquals("false")) {
                                    canLoginCopy.set(position, "true");
                                    setSharedPreferenceValues("can_login", canLoginCopy);
                                    setSharedPreferenceValues("login_directly", loginDirectly);

                                    HashMap<String, Object> hashMap
                                            = new HashMap<>();
                                    hashMap.put("icon", R.drawable.ftp2);
                                    hashMap.put("name", userNamesCopy.get(position) + "@" + hostNamesCopy.get(position));
                                    ftpSimpleAdaptList.remove(position);
                                    ftpSimpleAdaptList.add(position, hashMap);
                                    hideLoadView();
                                    notifyDataChanged();
                                }
// TODO
                                Intent intent = new Intent(FTPActivity.this, MainActivity.class);
//                                intent.putExtra("url", "http://" + hostNamesCopy.get(position) + "/tv9/");
                                startActivity(intent);
                                showToast("Connected to " + hostNamesCopy.get(position));
                            } else {
                                showToast("\n" + "Login failed");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();

                            if (canLoginCopy.get(position).contentEquals("true")) {
                                canLoginCopy.set(position, "false");
                                setSharedPreferenceValues("can_login", canLoginCopy);

                                HashMap<String, Object> hashMap
                                        = new HashMap<>();
                                hashMap.put("icon", R.drawable.ftp2);
                                hashMap.put("name", userNamesCopy.get(position) + "@" + hostNamesCopy.get(position) + "(failure)");
                                ftpSimpleAdaptList.remove(position);
                                ftpSimpleAdaptList.add(position, hashMap);

                                notifyDataChanged();
                            }

                            showToast("Login failed");
                            hideLoadView();
                        }
                    }
                }).start();
            }
        });

        ftpListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                new AlertDialog.Builder(FTPActivity.this)
                        .setTitle("Delete FTP")
                        .setMessage("Are you sure you want to delete this FTP?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                hostNamesCopy.remove(i);
                                userNamesCopy.remove(i);
                                userPasswordsCopy.remove(i);
                                canLoginCopy.remove(i);
                                setSharedPreferenceValues("can_login", canLoginCopy);
                                setSharedPreferenceValues("host_name", hostNamesCopy);
                                setSharedPreferenceValues("user_name", userNamesCopy);
                                setSharedPreferenceValues("user_password", userPasswordsCopy);

                                ftpSimpleAdaptList.remove(i);

                                //A view was deleted, notifying the update
                                ftpSimpleAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true;
            }
        });


        newFTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View cusView = LayoutInflater.from(FTPActivity.this).inflate(R.layout.login, null);
                final AlertDialog.Builder cusDia = new AlertDialog.Builder(FTPActivity.this);
                cusDia.setTitle("Add FTP address\n");
                cusDia.setView(cusView);

                cusDia.setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        EditText hostName = cusView.findViewById(R.id.host_name);
                        EditText userName = cusView.findViewById(R.id.user_name);
                        EditText userPassword = cusView.findViewById(R.id.user_password);

                        final String hostNameString = hostName.getText().toString().trim();
                        final String userNameString = userName.getText().toString().trim();
                        final String userPasswordString = userPassword.getText().toString().trim();

                        if (TextUtils.isEmpty(hostNameString) || TextUtils.isEmpty(userNameString) || TextUtils.isEmpty(userPasswordString)) {
                            Toast.makeText(FTPActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
//                            new AlertDialog.Builder(FTPActivity.this)
//                                    .setTitle("Login Failed")
//                                    .setMessage("Please fill all fields")
//
//                                    // Specifying a listener allows you to take an action before dismissing the dialog.
//                                    // The dialog is automatically dismissed when a dialog button is clicked.
//                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int which) {
//
//                                        }
//                                    })
//
//                                    // A null listener allows the button to dismiss the dialog and take no further action.
////                                    .setNegativeButton(android.R.string.cancel, null)
//                                    .setIcon(android.R.drawable.ic_dialog_alert)
//                                    .show();

                        } else {
                            loadText.setVisibility(View.VISIBLE);
                            hostNamesCopy.add(hostNameString);
                            userNamesCopy.add(userNameString);
                            userPasswordsCopy.add(userPasswordString);
                            canLoginCopy.add("false");

                            final HashMap<String, Object> hashMap
                                    = new HashMap<>();
                            hashMap.put("icon", R.drawable.ftp2);
                            hashMap.put("name", userNameString + "@" + hostNameString);
                            ftpSimpleAdaptList.add(hashMap);
                            ftpSimpleAdapter.notifyDataSetChanged();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        currentUser = userNameString;
                                        currentHost = hostNameString;
                                        currentPassword = userPasswordString;
                                        login();

                                        if (FTPActivity.client.isConnected()) {
                                            canLoginCopy.remove(canLoginCopy.size() - 1);
                                            canLoginCopy.add("true");
                                            setSharedPreference("can_login", "true");
                                            setSharedPreference("host_name", hostNameString);
                                            setSharedPreference("user_name", userNameString);
                                            setSharedPreference("user_password", userPasswordString);
                                            loadText.setVisibility(View.VISIBLE);
                                            showToast("login successful");
                                            hideLoadView();

//TODO
                                            Intent intent = new Intent(FTPActivity.this, MainActivity.class);
//                                            intent.putExtra("url", "http://" + hostNamesCopy + "/tv9/");
                                            startActivity(intent);
                                        } else {
                                            showToast("\n" + "Login failed");
//                                        showToast("Connected to " +hostNameString);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();

                                        setSharedPreference("can_login", "false");
                                        setSharedPreference("host_name", hostNameString);
                                        setSharedPreference("user_name", userNameString);
                                        setSharedPreference("user_password", userPasswordString);

                                        hashMap.put("name", userNameString + "@" + hostNameString + "(failure)");
                                        ftpSimpleAdaptList.remove(ftpSimpleAdaptList.size() - 1);
                                        ftpSimpleAdaptList.add(hashMap);


                                        notifyDataChanged();
                                        showToast("Login failed");
                                        hideLoadView();
                                    }
                                }
                            }).start();
                        }
                    }
                });

//                cusDia.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });

                cusDia.create().show();
            }
        });
    }


    public String[] getSharedPreference(String key) {
        String regularEx = "#";
        String[] str;
        SharedPreferences sp = getSharedPreferences("FTPHost", Context.MODE_PRIVATE);
        String values;
        values = sp.getString(key, "");
        str = values.split(regularEx);

        return str;
    }

    public void setSharedPreferenceValues(String key, ArrayList<String> values) {
        String regularEx = "#";
        String str = "";
        SharedPreferences sp = getSharedPreferences("FTPHost", Context.MODE_PRIVATE);
        if (values != null && values.size() > 0) {
            for (String value : values) {
                str += value;
                str += regularEx;
            }
            SharedPreferences.Editor et = sp.edit();
            et.putString(key, str);
            et.apply();
        }
    }

    public void setSharedPreference(String key, String value) {
        String regularEx = "#";
        SharedPreferences sp = getSharedPreferences("FTPHost", Context.MODE_PRIVATE);
        String values;
        values = sp.getString(key, "");
        SharedPreferences.Editor et = sp.edit();
        et.putString(key, values + value + regularEx);
        et.apply();
    }


    //Interactive message processing
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Helperclass.SHOW_TOAST:
                    Toast.makeText(FTPActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case Helperclass.NOTIFY_DATA_CHANGED:
                    ftpSimpleAdapter.notifyDataSetChanged();
                    break;
                case Helperclass.SHOW_LOAD_VIEW:
                    loadingView.setVisibility(View.VISIBLE);
                    break;
                case Helperclass.HIDE_LOAD_VIEW:
                    loadText.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    //Short notifications
    private void showToast(String message) {
        Message msg = new Message();
        msg.what = Helperclass.SHOW_TOAST;
        msg.obj = message;
        mHandler.sendMessage(msg);
    }

    //Hide the load icon
    private void hideLoadView() {
        Message msg = new Message();
        msg.what = Helperclass.HIDE_LOAD_VIEW;
        mHandler.sendMessage(msg);
    }

    //Notify listview updates
    private void notifyDataChanged() {
        Message msg = new Message();
        msg.what = Helperclass.NOTIFY_DATA_CHANGED;
        mHandler.sendMessage(msg);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.history_fragment_container,
                    new HistoryFragment()).commit();
        } else if (id == R.id.nav_categories) {
            Toast.makeText(getApplicationContext(), "category Selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_profile) {
            Toast.makeText(getApplicationContext(), "settings Selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Toast.makeText(getApplicationContext(), "Logout Success", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(FTPActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

//        DrawerLayout drawer = findViewById(R.id.reporter_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
