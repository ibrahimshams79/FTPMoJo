package com.example.ftpmojo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class listAdapter extends ArrayAdapter<filesNameList> {

    public listAdapter(Context context, ArrayList<filesNameList> filesNameListArrayList){
        super(context,R.layout.list_items, filesNameListArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        filesNameList filesNameList = getItem(position);
        if (convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_items,parent,false);
        }
        TextView filenames = convertView.findViewById(R.id.files_view);
//        ProgressBar progressBar = convertView.findViewById(R.id.progressBar2);

        filenames.setText(filesNameList.filename);
//        progressBar.setProgress((filesNameList.progressBarID[position]));

        return super.getView(position, convertView, parent);
    }
}
