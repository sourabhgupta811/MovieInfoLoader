package com.example.android.movieinfoloader;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
    public static ArrayList<String[]> myDataList;
    private Context context;
    MyRecyclerViewAdapter(Context context){
        this.context=context;
    }
    public void setMyDataList(ArrayList<String[]> myDataList){
        MyRecyclerViewAdapter.myDataList=myDataList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater= LayoutInflater.from(parent.getContext());
        View v=inflater.inflate(R.layout.recyclerview_item,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.nameTextView.setText(myDataList.get(position)[2]);
        Glide.with(context).load(myDataList.get(position)[1]).into(holder.movieImageView);
    }

    @Override
    public int getItemCount() {
        return myDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView movieImageView;
        TextView nameTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            movieImageView=itemView.findViewById(R.id.recyclerview_image);
            nameTextView=itemView.findViewById(R.id.recyclerview_movie_name);
            itemView.setOnClickListener(v -> {
                Intent intent=new Intent(context,DetailsActivity.class);
                intent.putExtra("position",getAdapterPosition());
                context.startActivity(intent);
            });
        }
    }
}