package com.example.ftpmojo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArraySet;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private ImageButton image, video, audio, pdf, serversyncbtn;
    private Uri videoURI;
    private String videoPath;
    private long backPressedTime;
    private Toast backToast;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer;
    ProgressDialog pDialog;
    private Button submitStory, attachFiles;
    private EditText storyTitle, storyDescription;
    SharedPreferences sharedPreferences;
    boolean CheckEditText = false;
    String uid, username;
    String UserIDfromSF, UserNamefromSF;
    private String storyTitle_Str;
    private String storyDescription_Str;
    ConnectionClass connectionClass;
    public static final int FILE_PICKER_REQUEST_CODE = 1;
    private static final int SELECT_AUDIO = 2;
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_PICK_PHOTO = 1;
    private static final int CAMERA_PIC_REQUEST = 1111;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 3;
    private String mImageFileLocation = "";
    private String audioPath, PDFPath;
    private String postPath;
    private Uri fileUri;
    private List<Uri> userSelectedImageUriList = null;
    private ArrayList<String> filesPathList;
    private ArrayList<String> filesNamesList;
    int noOfFiles;
    private String filepath;
    private ArrayList<String> files_array_list, uploaded_files_arraylist;
    private ListView files_list_view;
    private ArraySet<String> imagePathList;
    private ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
    ArrayList<filesNameList> fileArrayList = new ArrayList<>();
    private File packageFile;
    private boolean needContinue = false;
    private boolean filesUploaded = false;
    private long needUploadSize = 0L;
    private long uploadSize = 0L;
    private static final String LOGTAG = "FTPClient";
    private BroadcastReceiver connctionChangeReceiver;
    private IntentFilter filter;
    private String selectedFiename;
    private String localHome;
    Toolbar toolbar;
    private ArrayAdapter<String> simpleAdapter;
    private ProgressBar progressBar;
    private TextView loadText;
    private static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private ListView uploaded_files_listview;
    SimpleAdapter ad;
    int counter;
    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == RESULT_OK && result.getData() != null)
                    addToArray(result);
            }
        });

        initViews();
        initDialog();
        verifyStoragePermissions(this);
    }

    private void addToArray(ActivityResult result) {
        String[] filePathColumn = {MediaStore.Files.FileColumns.DATA};
        Intent intent = result.getData();
        Uri fileUri = intent.getData();

//        String filename = getFileName(intent.getData());

        Cursor cursor = getContentResolver().query(fileUri,
                filePathColumn, null, null, null);

        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filepath = cursor.getString(columnIndex);

        filesPathList.add(filepath);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                files_array_list);

        files_list_view.setAdapter(arrayAdapter);
        if (filepath != null) {
            File file = new File(filepath);
            arrayAdapter.add(file.getName());
            filesNamesList.add(file.getName());
        }
        cursor.close();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    FTPActivity.client.changeDirectory(storyTitle_Str);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }

    private void initViews() {
        FirebaseApp.initializeApp(this);
        serversyncbtn = findViewById(R.id.sync_btn);
        progressBar = findViewById(R.id.avi2);
        toolbar = findViewById(R.id.reporter_drawer_toolbar);
        toolbar.setTitle("Send Stories");
        setSupportActionBar(toolbar);
        image = findViewById(R.id.image);
        video = findViewById(R.id.video);
        audio = findViewById(R.id.audio);
        pdf = findViewById(R.id.pdf);
        loadText = findViewById(R.id.loadText1);
        storyTitle = findViewById(R.id.storyTitle);
        storyDescription = findViewById(R.id.storyDesc);
        submitStory = findViewById(R.id.submitStory);
//        attachFiles = findViewById(R.id.attachFiles);
        drawer = findViewById(R.id.reporter_drawer_layout);

        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        sharedPreferences = getSharedPreferences("SHARED_PREF", MODE_PRIVATE);
        UserIDfromSF = sharedPreferences.getString("UID", uid);
        UserNamefromSF = sharedPreferences.getString("UserName", username);

        NavigationView navigationView = findViewById(R.id.reporter_nav_view_home);
        image.setOnClickListener(this);
        video.setOnClickListener(this);
        audio.setOnClickListener(this);
        pdf.setOnClickListener(this);
        serversyncbtn.setOnClickListener(this);
        submitStory.setOnClickListener(this);
        connectionClass = new ConnectionClass();
        navigationView.setNavigationItemSelectedListener(this);

        files_list_view = findViewById(R.id.files_list_view);

        files_array_list = new ArrayList<String>();
        filesPathList = new ArrayList<String>();
        filesNamesList = new ArrayList<String>();

        connctionChangeReceiver = new ConnectionChangeReceiver();
        filter = new IntentFilter(CONNECTIVITY_CHANGE_ACTION);
        uploaded_files_listview = findViewById(R.id.uploaded_files_list_view);
        uploaded_files_arraylist = new ArrayList<String>();

    }

    protected void initDialog() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);

    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                backToast.cancel();
                super.onBackPressed();
                return;
            } else {
                backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
                backToast.show();
            }
            backPressedTime = System.currentTimeMillis();
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_ftp) {
            Toast.makeText(getApplicationContext(), "FTP Selected", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_categories) {
            Toast.makeText(getApplicationContext(), "category Selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Toast.makeText(getApplicationContext(), "settings Selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.commit();
            Toast.makeText(getApplicationContext(), "Logout Success", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

//        DrawerLayout drawer = findViewById(R.id.reporter_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case (R.id.image):
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item);

                Intent imageIntent = new Intent(Intent.ACTION_PICK);
                imageIntent.setType("image/*");
                activityResultLauncher.launch(imageIntent);

                break;
            case (R.id.video):

                Intent videoIntent = new Intent(Intent.ACTION_PICK);
                videoIntent.setType("video/*");
                activityResultLauncher.launch(videoIntent);

                break;
            case (R.id.audio):

                Intent audioIntent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                audioIntent.setType("audio/*");
                activityResultLauncher.launch(audioIntent);
                break;
            case (R.id.pdf):

                Intent pdfIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pdfIntent.setType("*/pdf");
                activityResultLauncher.launch(pdfIntent);
                break;
            case (R.id.submitStory):
                storyTitle_Str = storyTitle.getText().toString();
                storyDescription_Str = storyDescription.getText().toString();
                CheckEditText = !TextUtils.isEmpty(storyTitle_Str) && !TextUtils.isEmpty(storyDescription_Str);

                if (CheckEditText) {
                    if (filesNamesList.isEmpty()) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Story Upload")
                                .setMessage("Upload without media?")

                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                // The dialog is automatically dismissed when a dialog button is clicked.
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        new AsyncSubmitStory().execute();
                                    }
                                })

                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setNegativeButton(android.R.string.cancel, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    } else new AsyncSubmitStory().execute();
                } else {

                    // If EditText is empty then this block will execute .
                    Toast.makeText(MainActivity.this, "Please fill all fields.", Toast.LENGTH_LONG).show();

                }
                break;

            case (R.id.sync_btn):


                break;
        }

    }

    private void syncFiles(int storyID) {
        ArrayAdapter<String> simpleAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                uploaded_files_arraylist);
        uploaded_files_listview.setAdapter(simpleAdapter);
        simpleAdapter.clear();

        try {
            ConnectionClass connectionClass = new ConnectionClass();
            Connection con = connectionClass.CONN();
            if (con != null) {
                String qu = "Select files from mediaTable where userid='" + UserIDfromSF + "' AND storyid='" + storyID + "'";
                Statement statement = con.createStatement();
                ResultSet resultSet = statement.executeQuery(qu);
                while (resultSet.next()) {
                    simpleAdapter.add(resultSet.getString("files"));
                }
            } else
                Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    private void GetData() {
//        ListView filesListView = findViewById(R.id.uploaded_files_list_view);
//
//        List<Map<String,String>> MyDataList = null;
//        ListItem MyData = new ListItem();
//        MyDataList = MyData.getList();
//
//        String[] Fromw = {"Files"};
//        int[] Tow ={R.id.files_name};
//        ad = new SimpleAdapter(MainActivity.this,MyDataList, R.layout.listlayouttemplate, Fromw, Tow);
//        filesListView.setAdapter(ad);
//    }


    //Final Submit Story Function

    public class AsyncSubmitStory extends AsyncTask<String, String, String> {
        String z = "";
        Boolean isSuccess = false;
        ProgressDialog loading = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            loading.setMessage("\tPosting the Story");
            loading.setCancelable(false);
            loading.show();
        }

        @Override
        protected void onPostExecute(String r) {
            loading.dismiss();
            Toast.makeText(MainActivity.this, r, Toast.LENGTH_SHORT).show();
            if (r.equals("Story Posted successfully")) {
                storyTitle.setText("");
                storyDescription.setText("");
                if (filesPathList != null) {

                    if (filesPathList.size() > 0 && filesNamesList.size() > 0) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    FTPActivity.client.createDirectory(storyTitle_Str);
                                    FTPActivity.client.changeDirectory(storyTitle_Str);

//                                    handleHhowToast("Created new folder successfully");

                                } catch (Exception e) {
                                    e.printStackTrace();
//                                    handleHhowToast("Failed to create new folder");
                                }
                            }
                        }).start();
                        asyncUpload();
                    }

                }
            }
        }

        @Override
        protected String doInBackground(String... params) {

            if (storyTitle_Str.trim().equals("") || storyDescription_Str.trim().equals(""))
                z = "Please enter Story Title and Story Description";
            else {
                try {
                    FTPActivity.client.changeDirectoryUp();
                    Connection con = connectionClass.CONN();
                    if (con == null) {
                        z = "Error in connection with SQL server";
                    } else {
                        String query = "select * from stories where storytitle='" + storyTitle_Str + "'";

                        Statement stmt2 = con.createStatement();
                        ResultSet rs = stmt2.executeQuery(query);

                        if (rs.next()) {
                            z = "Story Already Exists";
                            isSuccess = false;
                        } else {

                            String sql = "INSERT INTO stories (storytitle, storydescription, reporterid, username) VALUES ('" + storyTitle_Str + "','" + storyDescription_Str + "','" + UserIDfromSF + "', '" + UserNamefromSF + "')";
                            Statement stmt = con.createStatement();
                            stmt.executeUpdate(sql);
                            z = "Story Posted successfully";
                        }
                    }

                } catch (Exception ex) {
                    isSuccess = false;
                    z = ex.getMessage();
                }
            }
            return z;
        }
    }

//    /**
//     * Here we store the file url as it will be null after returning from camera
//     * app
//     */
//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        // save file url in bundle as it will be null on screen orientation
//        // changes
//        outState.putParcelable("file_uri", fileUri);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//
//        // get the file url
//        fileUri = savedInstanceState.getParcelable("file_uri");
//    }

    public void asyncUpload() {
        counter = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < filesPathList.size(); i++) {
                    try {
                        selectedFiename = filesPathList.get(i);
                        packageFile = new File(selectedFiename);
                        new UploadThread(i).start();
//                    packageFile = new File(localHome + File.separator  + selectedFiename);

                        needUploadSize = packageFile.length();

                        needContinue = false;
                        FTPFile[] ftpFiles = FTPActivity.client.list();
                        for (FTPFile ftpFile : ftpFiles) {
                            if (ftpFile.getName().equals(selectedFiename)) {
                                needContinue = true;
                                uploadSize = ftpFile.getSize();

                                //When resuming. The size of the local record is inconsistent with the server.
                                // It should be because of the removal of the header file. So you need to get the server when resuming
                                // The size of the file that has been transferred is used as the resuming point instead of the transferred size of the local record.
                                // So the file size here cannot be read directly from sharedpreferences.

                                if (needUploadSize <= uploadSize) {
                                    handleHhowToast("File already exists");
                                } else {
                                    Message msg = new Message();
                                    msg.what = 1111111;
                                    mHandler.sendMessage(msg);

                                    new ContinueUploadThread(i).start();
                                }

                                return;
                            } else if (ftpFiles[ftpFiles.length - 1] == ftpFile) {
                                uploadSize = 0L;
                                new UploadThread(i).start();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    private void handleHhowToast(String message) {
        Message msg = new Message();
        msg.what = Helperclass.SHOW_TOAST;
        msg.obj = message;
        mHandler.sendMessage(msg);
    }

    private void handleHideLoadView() {
        Message msg = new Message();
        msg.what = Helperclass.HIDE_LOAD_VIEW;
        mHandler.sendMessage(msg);
    }

    private void handleNotifyDataChanged() {
        Message msg = new Message();
        msg.what = Helperclass.NOTIFY_DATA_CHANGED;
        mHandler.sendMessage(msg);
    }

    private void handleUploadShow(String filename) {
        Message msg = new Message();
        msg.what = Helperclass.UPLOAD_SHOW;
        msg.obj = filename;
        mHandler.sendMessage(msg);
    }

    private void handleNotifyUpdateChanged(int percent) {
        Message msg = new Message();
        msg.what = Helperclass.UPLOAD_CHANGE;
        msg.obj = percent;
        mHandler.sendMessage(msg);
    }

    private void handleUploadHide() {
        Message msg = new Message();
        msg.what = Helperclass.UPLOAD_HIDE;
        mHandler.sendMessage(msg);
    }

    private void clearArray() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                files_array_list);

        files_list_view.setAdapter(arrayAdapter);
        arrayAdapter.clear();
        filesPathList.clear();
        filesNamesList.clear();
//        GetData();
    }

    private void handleDownloadShow(String fileName) {
        Message msg = new Message();
        msg.what = Helperclass.DOWNLOAD_SHOW;
        msg.obj = fileName;
        mHandler.sendMessage(msg);
    }

    private void handleNotifyDownChanged(int percent) {
        Message msg = new Message();
        msg.what = Helperclass.DOWNLOAD_CHANGE;
        msg.obj = percent;
        mHandler.sendMessage(msg);
    }

    private void handleDownloadHide() {
        Message msg = new Message();
        msg.what = Helperclass.DOWNLOAD_HIDE;
        mHandler.sendMessage(msg);
    }

    //MARK: - Interactive message processing
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Helperclass.SHOW_TOAST:
                    Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case Helperclass.SET_TITLE:
                    toolbar.setSubtitle((String) msg.obj);
                    break;

                case Helperclass.NOTIFY_DATA_CHANGED:
                    simpleAdapter.notifyDataSetChanged();
                    break;

                case Helperclass.HIDE_LOAD_VIEW:
                    loadText.setText("Current user: " + FTPActivity.currentUser);
                    progressBar.setVisibility(View.INVISIBLE);
                    break;

                case Helperclass.DOWNLOAD_SHOW:
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(0);
                    loadText.setText("Downloading: " + (String) msg.obj + " ...");
                    loadText.setVisibility(View.VISIBLE);
                    break;
                case Helperclass.DOWNLOAD_CHANGE:
                    progressBar.setProgress((int) msg.obj);
                    break;
                case Helperclass.DOWNLOAD_HIDE:
                    loadText.setText("Current user: " + FTPActivity.currentUser);
                    progressBar.setVisibility(View.INVISIBLE);
                    break;

                case Helperclass.UPLOAD_SHOW:
                    progressBar.setVisibility(View.VISIBLE);
                    loadText.setVisibility(View.VISIBLE);
                    loadText.setText("Uploading: " + (String) msg.obj + " ...");
                    break;
                case Helperclass.UPLOAD_CHANGE:
                    if (msg.obj != null) {
                        progressBar.setProgress((int) msg.obj);
                    }
                    break;
                case Helperclass.UPLOAD_HIDE:
                    loadText.setText("Attach Files");
                    progressBar.setVisibility(View.INVISIBLE);
                    break;

                case 1111111:
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setTitle("Breakpoint upload").setMessage("\n" +
                            "Part of the file has been uploaded，Will upload from the breakpoint");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setCancelable(true).create().show();
                    break;
                case 2222222:
                    AlertDialog.Builder alert2 = new AlertDialog.Builder(MainActivity.this);
                    alert2.setTitle("File download complete").setMessage("The file format does not support viewing or the file is too large，Please use another App to open the file");
                    alert2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setCancelable(true).create().show();
                    break;
            }
        }
    };

    //MARK: - Breakpoint upload thread
    public class ContinueUploadThread extends Thread {
        int i;

        public ContinueUploadThread(int i) {
            this.i = i;
        }

        @Override
        public void run() {
            try {
                if (FTPActivity.client.isResumeSupported()) {
                    FTPActivity.client.upload(packageFile, uploadSize, new MyUploadTransferListener(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    //MARK: - ftp file upload thread
    class UploadThread extends Thread {
        int i;

        public UploadThread(int i) {
            this.i = i;
        }

        @Override
        public void run() {
            try {
                FTPActivity.client.upload(packageFile, new MyUploadTransferListener(i));
            } catch (FileNotFoundException e) {
                Log.e(LOGTAG, "UploadThread FileNotFoundException");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }


    //MARK: - FTP file upload listener
    public class MyUploadTransferListener implements FTPDataTransferListener {
        int i;

        public MyUploadTransferListener(int i) {
            this.i = i;
        }

        public void started() {
            registerReceiver(connctionChangeReceiver, filter);

            handleUploadShow(selectedFiename);
        }

        public void transferred(int arg0) {
            uploadSize += arg0;
            int percent = (int) (uploadSize * 100 / (needUploadSize * 1.0));

            handleNotifyUpdateChanged(percent);
        }

        public void completed() {
            unregisterReceiver(connctionChangeReceiver);

            uploadSize = 0L;

            handleUploadHide();
//            handleHhowToast("Upload completed");
            counter++;
            if (counter == filesPathList.size()) {
                new AsyncUpdateMediaTable().execute();
            }

        }

        public void aborted() {
            unregisterReceiver(connctionChangeReceiver);

            handleUploadHide();
            handleHhowToast("Upload stop");
            reConnect();
        }

        public void failed() {
            unregisterReceiver(connctionChangeReceiver);
            handleUploadHide();
            handleHhowToast("Upload failed");
            reConnect();
        }
    }


    //MARK: -
    //Receive broadcast of network information changes
    class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            @SuppressLint("MissingPermission") NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

            if (!(activeNetInfo != null && activeNetInfo.isConnected())) {
                try {
                    FTPActivity.client.abortCurrentDataTransfer(true);
                    reConnect();

                    Intent intent2 = new Intent();
                    intent2.setAction("android.intent.action.MAIN");
                    intent2.addCategory("android.intent.category.LAUNCHER");
                    startActivity(intent2);

                    handleHhowToast("Network error");
                    handleUploadHide();
                } catch (IOException | FTPIllegalReplyException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void reConnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FTPActivity.login();
//TODO
                    FTPActivity.client.changeDirectory(storyTitle_Str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //TODO
    public class AsyncUpdateMediaTable extends AsyncTask<String, String, String> {
        String z = "";
        Boolean isSuccess = false;
//        ProgressDialog loading = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
//            loading.setMessage("\tUpdating Media Table");
//            loading.setCancelable(false);
//            loading.show();
        }

        @Override
        protected void onPostExecute(String r) {
//            loading.dismiss();
//            Toast.makeText(MainActivity.this, r, Toast.LENGTH_SHORT).show();
            if (isSuccess) {
                clearArray();
                syncFiles(Integer.parseInt(r));
//                Toast.makeText(MainActivity.this, "Media Table Updated", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
//            if (selectedFiename.equals(""))
//                z = "No Media";
//            else {
            try {
                Connection con = connectionClass.CONN();
                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {
                    String storyidsql = "SELECT storyid from stories WHERE reporterid='" + UserIDfromSF + "' AND storytitle='" + storyTitle_Str + "'";
//                    String storyidsql2 = "SELECT storyid from stories WHERE storytime=(select max(storytime) from stories";
                    Statement stmt2 = con.createStatement();
                    ResultSet resultSet = stmt2.executeQuery(storyidsql);
                    if (resultSet.next()) {
                        String storyID = resultSet.getString("storyid");

                        for (int i = 0; i < filesNamesList.size(); i++) {
                            String filename = filesNamesList.get(i);
                            String sql = "INSERT INTO mediaTable (userid, storyid, files) VALUES ('" + UserIDfromSF + "','" + storyID + "','" + filename + "')";
                            Statement stmt = con.createStatement();
                            stmt.executeUpdate(sql);
                        }
                        isSuccess = true;
                        z = storyID;
                    } else
                        z = "Media Table not updated";
                }
                con.close();
            } catch (Exception ex) {
                isSuccess = false;
                z = ex.getMessage();
            }
            return z;
        }
    }

}