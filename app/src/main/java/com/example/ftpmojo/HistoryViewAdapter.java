package com.example.ftpmojo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryViewAdapter extends RecyclerView.Adapter<HistoryViewAdapter.myviewHolder> {
    ArrayList<Datamodel> dataholder;
    private final ItemClickListener itemClickListener;

    public HistoryViewAdapter(ArrayList<Datamodel> dataholder, ItemClickListener itemClickListener) {
        this.dataholder = dataholder;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public myviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_design, parent, false);
        return new myviewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onBindViewHolder(@NonNull myviewHolder holder, int position) {
        holder.storyTitle.setText(dataholder.get(position).getStoryTitle());
        holder.noOfFiles.setText(dataholder.get(position).getNoOfFiles());
        holder.storyTime.setText(dataholder.get(position).getStoryTime());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onItemClick(dataholder.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataholder.size();
    }

    class myviewHolder extends RecyclerView.ViewHolder {

        TextView storyTitle, noOfFiles, storyTime;

        public myviewHolder(@NonNull View itemView) {
            super(itemView);
            storyTitle = itemView.findViewById(R.id.rvstorytitle);
            storyTime = itemView.findViewById(R.id.rvstoryTime);
            noOfFiles = itemView.findViewById(R.id.rvfileno);

        }

    }

    public interface ItemClickListener{
        public void onItemClick(Datamodel dataModel);
        
    }
}
