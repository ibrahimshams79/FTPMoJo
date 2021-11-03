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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    boolean isconnected = true;
    private long backPressedTime;
    private Toast backToast;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer;
    ProgressDialog pDialog;
    private EditText storyTitle, storyDescription;
    SharedPreferences sharedPreferences;
    boolean CheckEditText = false;
    String uid, username;
    String UserIDfromSF, UserNamefromSF;
    private String storyTitle_Str;
    public String storyDescription_Str;
    ConnectionClass connectionClass;

    private ArrayList<String> filesPathList;
    private ArrayList<String> filesNamesList;
    private ArrayList<String> files_array_list, uploaded_files_arraylist;
    private ListView files_list_view;

    private List<HashMap<String, Object>> ftpSimpleAdaptList;
    private SimpleAdapter ftpSimpleAdapter;

    //    private ArraySet<String> imagePathList;
//    private ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
//    ArrayList<filesNameList> fileArrayList = new ArrayList<>();
    private File packageFile;
    private boolean needContinue = false;
    //    private boolean filesUploaded = false;
    private long needUploadSize = 0L;
    private long uploadSize = 0L;
    private static final String LOGTAG = "FTPClient";
    public BroadcastReceiver connctionChangeReceiver;
    public IntentFilter filter;
    private String selectedFiename;
    //    private String localHome;
    Toolbar toolbar;
    //    private ArrayAdapter<String> simpleAdapter;
    private ProgressBar progressBar;
    private TextView loadText;
    private static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private ListView uploaded_files_listview;
    //    SimpleAdapter ad;
    int counter;
    ActivityResultLauncher<Intent> activityResultLauncher;
    private ArrayAdapter<String> arrayAdapter;
    ProgressDialog loading;
    private ActivityResultLauncher<String[]> pdfResultLauncher;
    Boolean isUploading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initDialog();
        verifyStoragePermissions(this);
    }

    private void addToArray(ActivityResult result) throws URISyntaxException {
        Intent intent = result.getData();

        Uri fileUri = null;
        if (intent != null) {
            fileUri = intent.getData();
        }

//        String[] filePathColumn = {MediaStore.Files.FileColumns.DATA};
//        Cursor cursor = getContentResolver().query(fileUri,
//                filePathColumn, null, null, null, null);
//
//        cursor.moveToFirst();
//        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//        //    int noOfFiles;
//        String filepath = cursor.getString(columnIndex);

//            filesPathList.add(filepath);


        String filepath = getFilePath(fileUri);
        arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                files_array_list);

        files_list_view.setAdapter(arrayAdapter);

        if (filepath != null) {
            File file = new File(filepath);
            filesPathList.add(filepath);
            arrayAdapter.add(file.getName());
            filesNamesList.add(file.getName());
        }
    }

    private void addToArray(Uri result) throws URISyntaxException {
        File myFile = new File(result.getPath());
        String filepathfullname = myFile.getAbsolutePath();


        final String[] split = filepathfullname.split(":");
        String filepath = split[1];


        arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                files_array_list);

        files_list_view.setAdapter(arrayAdapter);

        if (filepath != null) {
//            File file = new File(filepath);
            filesPathList.add(filepath);
            arrayAdapter.add(myFile.getName());
            filesNamesList.add(myFile.getName());
        }
    }


    @SuppressLint("Recycle")
    public String getFilePath(Uri uri) throws URISyntaxException {
        String filepath = null;

        String[] filePathColumn = {MediaStore.Files.FileColumns.DATA};

        Cursor cursor = null;

        cursor = getContentResolver().query(uri,
                filePathColumn, null, null, null);


        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filepath = cursor.getString(columnIndex);
        //    int noOfFiles;
        return filepath;
    }

//    public String getFileName(Uri uri) {
//        String result = null;
//        if (uri.getScheme().equals("content")) {
//            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
//                if (cursor != null && cursor.moveToFirst()) {
////                    File file = cursor.getExtras().getParcelable("application/pdf");
//                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//                }
//            }
//        }
//        if (result == null) {
//            result = uri.getLastPathSegment();
//        }
//        return result;
//    }


    private void initViews() {

//        Configuration config = new Configuration();
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config,
//                getBaseContext().getResources().getDisplayMetrics());

        //        FirebaseApp.initializeApp(this);
        ImageButton serversyncbtn = findViewById(R.id.sync_btn);
        progressBar = findViewById(R.id.avi2);
        toolbar = findViewById(R.id.reporter_drawer_toolbar);
        toolbar.setTitle("Send Stories");
        setSupportActionBar(toolbar);
        ImageButton image = findViewById(R.id.image);
        ImageButton video = findViewById(R.id.video);
        ImageButton audio = findViewById(R.id.audio);
        ImageButton pdf = findViewById(R.id.pdf);
        loadText = findViewById(R.id.loadText1);
        storyTitle = findViewById(R.id.storyTitle);

        storyDescription = findViewById(R.id.storyDesc);

        Button submitStory = findViewById(R.id.submitStory);
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

        files_array_list = new ArrayList<>();
        filesPathList = new ArrayList<>();
        filesNamesList = new ArrayList<>();

        connctionChangeReceiver = new ConnectionChangeReceiver();
        filter = new IntentFilter(CONNECTIVITY_CHANGE_ACTION);
        uploaded_files_listview = findViewById(R.id.uploaded_files_list_view);
        uploaded_files_arraylist = new ArrayList<>();

        loading = new ProgressDialog(MainActivity.this);

        files_list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete File")
                        .setMessage("Remove this file")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                filesPathList.remove(i);
                                files_array_list.remove(i);
                                filesNamesList.remove(i);
                                arrayAdapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                ;
                return true;
            }
        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        addToArray(result);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        pdfResultLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    try {
                        addToArray(result);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_DOCUMENTS
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

//    protected void showpDialog() {
//
//        if (!pDialog.isShowing()) pDialog.show();
//    }
//
//    protected void hidepDialog() {
//
//        if (pDialog.isShowing()) pDialog.dismiss();
//    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.history_fragment_container2);
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove(fragment);
            trans.commit();
            manager.popBackStack();
            toolbar.setTitle("Send Stories");
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

        if (id == R.id.nav_history) {
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.history_fragment_container2,
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
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

//        DrawerLayout drawer = findViewById(R.id.reporter_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @SuppressLint("IntentReset")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case (R.id.image):

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
                if (Build.VERSION.SDK_INT >= 30) {
                    Intent audioIntent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                    audioIntent.setType("*/*");
                    activityResultLauncher.launch(audioIntent);
                } else {
                    Intent audioIntent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
//                audioIntent.setType("*/*");
                    activityResultLauncher.launch(audioIntent);
                }
                break;
            case (R.id.pdf):
                if (Build.VERSION.SDK_INT >= 29) {
                    Intent pdfIntent = null;
                    pdfIntent = new Intent(Intent.ACTION_PICK);

//                    pdfIntent.setType("application/pdf");
                    activityResultLauncher.launch(pdfIntent);
                } else {
                    String[] arrayofdocs = new String[]{"application/pdf",
                            "application/msword",
                            "application/ms-doc",
                            "application/doc",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            "text/plain"};

                    pdfResultLauncher.launch(
                            arrayofdocs);
                }
                break;
            case (R.id.submitStory):
                storyTitle_Str = storyTitle.getText().toString();
                storyDescription_Str = storyDescription.getText().toString();

                CheckEditText = !TextUtils.isEmpty(storyTitle_Str) && !TextUtils.isEmpty(storyDescription_Str);
                registerReceiver(connctionChangeReceiver, filter);

                if (isconnected) {
                    if (CheckEditText) {
                        if (filesNamesList.isEmpty()) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Story Upload")
                                    .setMessage("Upload without media?")

                                    // Specifying a listener allows you to take an action before dismissing the dialog.
                                    // The dialog is automatically dismissed when a dialog button is clicked.
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            new SubmitStory().execute();
                                        }
                                    })

                                    // A null listener allows the button to dismiss the dialog and take no further action.
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        } else {

                            try {
                                Connection con = connectionClass.CONN();
                                if (con == null) {
                                    Toast.makeText(MainActivity.this, "Error in connection with SQL server", Toast.LENGTH_LONG).show();
                                } else {
                                    PreparedStatement query = con.prepareStatement("select * from stories where storytitle=?");
                                    query.setString(1, storyTitle_Str);
                                    ResultSet rs = query.executeQuery();


                                    if (rs.next()) {
                                        Toast.makeText(MainActivity.this, "Story Already Exists", Toast.LENGTH_LONG).show();
                                    } else {
                                        con.close();
                                        if (filesPathList != null) {
                                            filesNamesList.size();
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        if (isUploading) {
                                                            FTPActivity.client.changeDirectoryUp();
                                                            FTPActivity.client.createDirectory(storyTitle_Str);
                                                            FTPActivity.client.changeDirectory(storyTitle_Str);
                                                        }
                                                        asyncUpload();
                                                        isUploading = true;

                                                    } catch (Exception e) {
                                                        handleHhowToast(e.getMessage());
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).start();

                                        }
                                    }
                                }

                            } catch (Exception ex) {
                                Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
                            }
                        }

                    } else {

                        // If EditText is empty then this block will execute .
                        Toast.makeText(MainActivity.this, "Please fill all fields.", Toast.LENGTH_LONG).show();

                    }
                } else
                    Toast.makeText(MainActivity.this, "There's no network", Toast.LENGTH_LONG).show();

                break;

            case (R.id.sync_btn):


                break;
            default:
                throw new IllegalStateException("Unexpected value: " + id);
        }

    }

    private void syncFiles(int storyID) {
        ArrayAdapter<String> simpleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                uploaded_files_arraylist);
        uploaded_files_listview.setAdapter(simpleAdapter);
        simpleAdapter.clear();

        try {
            ConnectionClass connectionClass = new ConnectionClass();
            Connection con = connectionClass.CONN();
            if (con != null) {
//                String qu = "Select files from mediaTable where userid='" + UserIDfromSF + "' AND storyid='" + storyID + "'";
//                Statement statement = con.createStatement();
//                ResultSet resultSet = statement.executeQuery(qu);
                PreparedStatement preparedStatement = con.prepareStatement("Select files from mediaTable where userid= ? AND storyid= ?");
                preparedStatement.setString(1, UserIDfromSF);
                preparedStatement.setString(2, String.valueOf(storyID));
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    simpleAdapter.add(resultSet.getString("files"));
                }
                con.close();
            } else
                Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public class SubmitStory extends AsyncTasks {
        String z = "";
        Boolean isSuccess = false;

        @Override
        public void onPreExecute() {
//            loading.setMessage("\tPosting the Story");
//            loading.setCancelable(false);
//            loading.show();
        }

        @Override
        public String doInBackground() {
            if (storyTitle_Str.trim().equals("") || storyDescription_Str.trim().equals(""))
                z = "Please enter Story Title and Story Description";
            else {
                try {
                    Connection con = connectionClass.CONN();
                    if (con == null) {
                        z = "Error in connection with SQL server";
                    } else {
//                        String query = "select * from stories where storytitle='" + storyTitle_Str + "'";
                        PreparedStatement query = con.prepareStatement("select * from stories where storytitle=?");
                        query.setString(1, storyTitle_Str);
                        ResultSet rs = query.executeQuery();
//                        Statement stmt2 = con.createStatement();
//                        ResultSet rs = stmt2.executeQuery(query);

                        if (rs.next()) {
                            z = "Story Already Exists";   //TODO
                            isSuccess = false;
                        } else {
//                            String sql = "INSERT INTO stories (storytitle, storydescription, reporterid, username) VALUES ('" + storyTitle_Str + "','" + storyDescription_Str + "','" + UserIDfromSF + "', '" + UserNamefromSF + "')";

                            PreparedStatement pstmt = con.prepareStatement("INSERT INTO stories (storytitle, storydescription, reporterid, username, mediafiles) VALUES (?, ?, ?, ?, ?)");
                            pstmt.setString(1, storyTitle_Str);
                            pstmt.setString(2, storyDescription_Str);
                            pstmt.setString(3, UserIDfromSF);
                            pstmt.setString(4, UserNamefromSF);
                            pstmt.setString(5, String.valueOf(filesNamesList.size()));
                            pstmt.executeUpdate();
//                            Statement stmt = con.createStatement();
//                            stmt.executeUpdate(sql);
                            z = "Story Posted successfully";
                        }
                    }
                    if (con != null)
                        con.close();
                } catch (Exception ex) {
                    isSuccess = false;
                    z = ex.getMessage();
                }
            }

            return z;
        }

        @Override
        public void onPostExecute(String r) {
//            loading.dismiss();
            Toast.makeText(MainActivity.this, r, Toast.LENGTH_SHORT).show();
            if (r.equals("Story Posted successfully")) {
                storyTitle.setText("");
                storyDescription.setText("");
                new UpdateMediaTable().execute();
//                if (filesPathList != null) {
//
//                    if (filesPathList.size() > 0 && filesNamesList.size() > 0) {
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    FTPActivity.client.changeDirectoryUp();
//                                    FTPActivity.client.createDirectory(storyTitle_Str);
//                                    FTPActivity.client.changeDirectory(storyTitle_Str);
//
////                                    handleHhowToast("Created new folder successfully");
//
//                                } catch (Exception e) {
//                                    e.printStackTrace();
////                                    handleHhowToast("Failed to create new folder");
//                                }
//                            }
//                        }).start();
//
////                        asyncUpload();
//
//
//                        // Passing params
////                        for (int i = 0; i < filesPathList.size(); i++) {
////                            int success_counter=0;
////                            selectedFiename = filesPathList.get(i);
////                            packageFile = new File(selectedFiename);
////                            Constraints uploadDataConstraints = new Constraints.Builder()
////                                    .setRequiredNetworkType(NetworkType.CONNECTED)
////                                    .setRequiresDeviceIdle(true)
////                                    .build();
////
////                            Data.Builder data = new Data.Builder();
////                            data.putInt("i", i);
////                            data.putString("packageFile", String.valueOf(packageFile));
////
////
////                            workRequest = new OneTimeWorkRequest.Builder(UploadWorker.class)
////                                    .addTag("Upload")
////                                    .setInputData(data.build())
//////                                    .setConstraints(uploadDataConstraints)
////                                    .build();
////
////                            workManager.enqueue(workRequest);
////                            try {
////                                WorkInfo workInfo = WorkManager.getInstance(getApplicationContext()).getWorkInfoById(workRequest.getId()).get();
////                                boolean success = workInfo.getOutputData().getBoolean("isSuccess", false);
////                                if (success){
////                                    success_counter++;
////                                }
////                                if (success_counter == filesPathList.size()){
////                                    new UpdateMediaTable().execute();
////                                }
////                            } catch (ExecutionException | InterruptedException e) {
////                                e.printStackTrace();
////                            }
////                        }
//                    }
//                }
            }
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
                        if (isUploading)
                            new UploadThread(i).start();

                        needUploadSize = packageFile.length();

                        needContinue = false;
                        FTPFile[] ftpFiles = FTPActivity.client.list();

                        for (FTPFile ftpFile : ftpFiles) {
                            String[] fileSplit = selectedFiename.split("/");
                            String fileName = fileSplit[fileSplit.length - 1];

                            if (ftpFile.getName().equals(fileName)) {
                                needContinue = true;
                                uploadSize = ftpFile.getSize();

                                //When resuming. The size of the local record is inconsistent with the server.
                                // It should be because of the removal of the header file. So you need to get the server when resuming
                                // The size of the file that has been transferred is used as the resuming point instead of the transferred size of the local record.
                                // So the file size here cannot be read directly from sharedpreferences.

                                if (needUploadSize <= uploadSize) {
                                    handleHhowToast(fileName+ "File already exists");
                                } else {

                                    new ContinueUploadThread(i).start();

                                }

//                                return;
                            } else if (ftpFiles[ftpFiles.length - 1] == ftpFile) {
                                uploadSize = 0L;
//                                    new UploadThread(i).start();
//                                Constraints.Builder builder = new Constraints.Builder()
//                                        .setRequiredNetworkType(NetworkType.CONNECTED);
//
//                                // Passing params
//                                Data.Builder data = new Data.Builder();
//                                data.putInt("i", i);
//                                data.putString("packageFile", selectedFiename);
//
//                                OneTimeWorkRequest syncWorkRequest =
//                                        new OneTimeWorkRequest.Builder(UploadWorker.class)
//                                                .addTag("FileUpload")
//                                                .setInputData(data.build())
//                                                .setConstraints(builder.build())
//                                                .build();
//
//                                WorkManager.getInstance(getApplicationContext()).enqueue(syncWorkRequest);
                            }
                        }
                        if (!isUploading)
                            new UploadThread(i).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

    }


    //    MARK: - Breakpoint upload thread
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
                handleHhowToast(e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e(LOGTAG, e.getMessage());
//                handleHhowToast(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }


    //    //    MARK: - FTP file upload listener
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
            counter++;
            handleUploadHide();
            if (counter == filesPathList.size()) {
                handleHhowToast(counter + " Files Uploaded");
                new SubmitStory().execute();
            }


        }

        public void aborted() {
            unregisterReceiver(connctionChangeReceiver);
//            reConnect(i);
//            handleUploadHide();
            handleHhowToast("Upload paused");
            handleUploadContinue(i);
//            handleUploadAborted(i);
        }

        public void failed() {
            unregisterReceiver(connctionChangeReceiver);
            handleUploadHide();
            handleHhowToast("Upload failed");
//            handleUploadAborted(i);
        }
    }


    //MARK: -
    //Receive broadcast of network information changes
    class ConnectionChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

            if (!(activeNetInfo != null && activeNetInfo.isConnected())) {
                try {
                    FTPActivity.client.abortCurrentDataTransfer(true);

                    Intent i = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(i);

                    handleHhowToast("Network error");
                    handleUploadHide();
                    isconnected = false;

                } catch (IOException | FTPIllegalReplyException e) {
                    e.printStackTrace();
                }
            } else {
                isconnected = true;
            }
        }
    }

    void reConnect(int i) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FTPActivity.login();
//TODO
                    FTPActivity.client.changeDirectory(storyTitle_Str);
                    new ContinueUploadThread(i).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //TODO
    public class UpdateMediaTable extends AsyncTasks {
        String z = "";
        Boolean isSuccess = false;

        @Override
        public void onPreExecute() {

        }

        @Override
        public void onPostExecute(String r) {

            if (isSuccess) {
                clearArray();
                syncFiles(Integer.parseInt(r));
                handleAlertBox(r);
            }
        }

        @Override
        public String doInBackground() {

            try {
                Connection con = connectionClass.CONN();
                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {
//                    PreparedStatement storyidsql = con.prepareStatement("SELECT storyid from stories WHERE reporterid='" + UserIDfromSF + "' AND storytitle='" + storyTitle_Str + "'");
//                    Statement stmt2 = con.createStatement();
//                    ResultSet resultSet = stmt2.executeQuery(storyidsql);
                    PreparedStatement storyidsql = con.prepareStatement("SELECT storyid from stories WHERE reporterid=  ?  AND storytitle=  ?");

                    storyidsql.setString(1, UserIDfromSF);
                    storyidsql.setString(2, storyTitle_Str);
                    ResultSet resultSet = storyidsql.executeQuery();
                    if (resultSet.next()) {
                        String storyID = resultSet.getString("storyid");

                        for (int i = 0; i < filesNamesList.size(); i++) {
                            String filename = "\\\\192.168.9.67/ftp/" + storyTitle_Str + "/" + filesNamesList.get(i);//ToDo
//                            String sql = "INSERT INTO mediaTable (userid, storyid, files) VALUES ('" + UserIDfromSF + "','" + storyID + "','" + filename + "')";
//                            Statement stmt = con.createStatement();
//                            stmt.executeUpdate(sql);
                            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO mediaTable (userid, storyid, files) VALUES ( ?, ? , ?)");
                            preparedStatement.setString(1, UserIDfromSF);
                            preparedStatement.setString(2, storyID);
                            preparedStatement.setString(3, filename);
                            preparedStatement.executeUpdate();
                        }
                        isSuccess = true;
                        z = storyID;
                    } else
                        z = "Media Table not updated";
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                isSuccess = false;
                z = ex.getMessage();
            }
            return z;
        }
    }


    private void handleHhowToast(String message) {
        Message msg = new Message();
        msg.what = Helperclass.SHOW_TOAST;
        msg.obj = message;
        mHandler.sendMessage(msg);
    }

    private void handleAlertBox(String message) {
        Message msg = new Message();
        msg.what = Helperclass.STORY_POSTED;
        msg.obj = message;
        mHandler.sendMessage(msg);
    }

//    private void handleUploadAborted(int i) {
//        Message msg = new Message();
//        msg.what = Helperclass.UPLOAD_ABORTED;
//        msg.obj = i;
//        mHandler.sendMessage(msg);
//    }

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

    public void handleUploadHide() {
        Message msg = new Message();
        msg.what = Helperclass.UPLOAD_HIDE;
        mHandler.sendMessage(msg);
    }

    public void handleUploadContinue(int i) {
        Message msg = new Message();
        msg.obj = i;
        msg.what = Helperclass.CONTINUE_UPLOAD;
        mHandler.sendMessage(msg);
    }


    private void clearArray() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                files_array_list);

        files_list_view.setAdapter(arrayAdapter);
        arrayAdapter.clear();
        filesPathList.clear();
        filesNamesList.clear();
//        GetData();
    }


    //MARK: - Interactive message processing
    public Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Helperclass.SHOW_TOAST:
                    Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case Helperclass.SET_TITLE:
                    toolbar.setSubtitle((String) msg.obj);
                    break;

                case Helperclass.HIDE_LOAD_VIEW:
                    loadText.setText("Current user: " + FTPActivity.currentUser);
                    progressBar.setVisibility(View.INVISIBLE);
                    break;

                case Helperclass.UPLOAD_SHOW:
                    progressBar.setVisibility(View.VISIBLE);
                    loadText.setVisibility(View.VISIBLE);
                    loadText.setText("Uploading: " + msg.obj + " ...");
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

                case Helperclass.STORY_POSTED:
                    AlertDialog.Builder alert3 = new AlertDialog.Builder(MainActivity.this);
                    alert3.setTitle("Story Uploaded").setMessage("\n" +
                            "All files uploaded");
                    alert3.setPositiveButton("Stories History?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.history_fragment_container2,
                                    new HistoryFragment()).commit();

                        }
                    }).setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).create().show();
                    break;

//                case Helperclass.UPLOAD_ABORTED:
//                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
//                    alert.setTitle("Network Interupted").setMessage("\n" +
//                            "Story will be re-uploaded");
//                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            if (isconnected){
//
//                            }
////                                asyncUpload();
////                                new UploadThread((int) msg.obj);
//                            else {
//                                toolbar.setSubtitle("NO NETWORK");
//                                toolbar.setTitleTextColor(3);
//
//                            }
//
//                        }
//                    }).create().show();
//                    break;
                case Helperclass.CONTINUE_UPLOAD:
                    isUploading = false;
                    registerReceiver(connctionChangeReceiver, filter);
                    AlertDialog.Builder alert2 = new AlertDialog.Builder(MainActivity.this);
                    alert2.setTitle("Network Error").setMessage("\n" +
                            "Switch on the network to continue upload.");
                    final AlertDialog mDialog = alert2.create();
                    mDialog.setCanceledOnTouchOutside(false);
                    alert2.setPositiveButton("Continue Upload", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

//                            if (isconnected && needContinue) {
//                                if (msg.obj != null)
//                                    reConnect((int) msg.obj);
//                            } else
                            if (isconnected) {
                                isUploading = false;
                                asyncUpload();
                            }else
                            {
                                Intent i = new Intent(Settings.ACTION_SETTINGS);
                                startActivity(i);
                            }


                        }
                    }).setNegativeButton("Cancel Upload", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            storyTitle.setText("");
                            storyDescription.setText("");
                            clearArray();
                        }
                    }).setCancelable(false).create().show();
                    if(isconnected)
                        alert2.show();
                    break;
            }
        }
    };
}