package com.example.ftpmojo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class HistoryFragment extends Fragment implements HistoryViewAdapter.ItemClickListener {

    private SharedPreferences sharedPreferences;
    private String uid, UserIDfromSF;
    RecyclerView recyclerView;
    ArrayList<Datamodel> dataModelArrayList;
    Connection connect;
    String ConnectionResult = "";
    Boolean isSuccess = false;
    Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        recyclerView = v.findViewById(R.id.storyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dataModelArrayList = new ArrayList<>();
        //        toolbar = v.findViewById(R.id.toolbar);
//        toolbar.setTitle("My Stories");
        return v;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

//        Objects.requireNonNull(((FTPActivity) requireActivity()).getSupportActionBar()).setTitle("My Stories");
        sharedPreferences = requireActivity().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
        UserIDfromSF = sharedPreferences.getString("UID", uid);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("My Stories");

        inflater.inflate(R.menu.filter_stories_menu, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) item.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_list_item_array, R.layout.spinner_text);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                switch (i) {
                    case 0:
                        dataModelArrayList.clear();
                        syncData(1);
                        break;

                    case 1:
                        dataModelArrayList.clear();
                        syncData(0);
                        break;

                    case 2:
                        dataModelArrayList.clear();
                        syncData(7);
                        break;

                    case 3:
                        dataModelArrayList.clear();
                        syncData(30);
                        break;
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void syncData(int i) {

        try {
            ConnectionClass connectionClass = new ConnectionClass();
            connect = connectionClass.CONN();
            ResultSet rs;
            if (connect != null) {
                if (i == 0) {
                    PreparedStatement preparedStatement = connect.prepareStatement("Select * from stories where userid= ? ORDER BY storytime DESC");
                    preparedStatement.setString(1, UserIDfromSF);
                    rs = preparedStatement.executeQuery();
                } else {
                    String currentdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    String formattedDate =
                            getCalculatedDate("", "yyyy-MM-dd HH:mm:ss", -i);

                    PreparedStatement preparedStatement = connect.prepareStatement("Select * from stories where userid= ? and storytime between ? and ? ORDER BY storytime DESC");
                    preparedStatement.setString(1, UserIDfromSF);
                    preparedStatement.setString(3, currentdate);
                    preparedStatement.setString(2, formattedDate);
                    rs = preparedStatement.executeQuery();
                }
                if (rs != null) {
                    while (rs.next()) {
                        try {
                            dataModelArrayList.add(new Datamodel(rs.getString("no_of_files"),
                                    rs.getString("storytitle"),
                                    rs.getString("storydetail"),
                                    rs.getString("storytime")));
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }

                    }
                }
                ConnectionResult = "Success";
                isSuccess = true;
                connect.close();
            } else {
                Toast.makeText(getContext(), "Error with server connection", Toast.LENGTH_SHORT).show();
                ConnectionResult = "Failed";
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        HistoryViewAdapter historyViewAdapter = new HistoryViewAdapter(dataModelArrayList, this);
        recyclerView.setAdapter(historyViewAdapter);
    }

    public static String getCalculatedDate(String date, String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);

        if (!date.isEmpty()) {
            try {
                cal.setTime(s.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Connect to FTP Server");

//        Objects.requireNonNull(((FTPActivity) requireActivity()).getSupportActionBar()).setTitle("Connect to FTP Server");
    }

    @Override
    public void onItemClick(Datamodel dataModel) {

        Fragment myFragment = StoryDetailFragment.newInstance(dataModel.getStoryTitle(), dataModel.getStoryDesc());
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.history_fragment_container3, myFragment).addToBackStack(null).commit();
    }
}



