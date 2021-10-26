package com.example.ftpmojo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

public class HistoryFragment extends Fragment {

    ListItem listItem = new ListItem();
    ArrayList<Map<String, String>> arrayList;
    private SharedPreferences sharedPreferences;
    private String uid, UserIDfromSF;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Objects.requireNonNull(((FTPActivity) requireActivity()).getSupportActionBar()).setTitle("My Stories");
        sharedPreferences = getActivity().getSharedPreferences("SHARED_PREF", MODE_PRIVATE);
        UserIDfromSF = sharedPreferences.getString("UID", uid);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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

                switch (i){
                    case 0:
                       arrayList= (ArrayList<Map<String, String>>) listItem.getList(UserIDfromSF);
                        Log.d(TAG, "onItemSelected:" +arrayList.size());
                        break;

                    case 1:
                       arrayList = (ArrayList<Map<String, String>>) listItem.getList(UserIDfromSF);
                        Log.d(TAG, "onItemSelected:" +arrayList.size());
                        break;

                    case 3:
                        arrayList = (ArrayList<Map<String, String>>) listItem.getList(UserIDfromSF);
                        Log.d(TAG, "onItemSelected:" +arrayList.size());
                        break;
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(((FTPActivity) requireActivity()).getSupportActionBar()).setTitle("Connect to FTP Server");
    }
}



