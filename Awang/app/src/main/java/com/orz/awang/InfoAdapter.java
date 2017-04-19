package com.orz.awang;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by scotg on 17/3/2
 */

public class InfoAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<LogMSG> data;
    //private int counter = 0;
    public InfoAdapter(Context c,List<LogMSG> msgs){
        this.context = c;
        this.data = msgs;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new MViewHolder(LayoutInflater.from(context).inflate(R.layout.card_main,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //Log.e("INFO", "text " + data.get(position).getMSG());
        ((MViewHolder)holder).info.setText(data.get(position).getMSG());
        if(data.get(position).getType()==0)
            ((MViewHolder)holder).img.setImageResource(R.mipmap.info);
        else
            ((MViewHolder)holder).img.setImageResource(R.mipmap.error);
    }

    @Override
    public int getItemCount() {
        //Log.e("INFO", "size " + data.size());
        return data.size();
    }

    private class MViewHolder extends RecyclerView.ViewHolder{
        TextView info;
        ImageView img;
        MViewHolder(View itemView) {
            super(itemView);
            info = (TextView)itemView.findViewById(R.id.noti_text);
            img = (ImageView)itemView.findViewById(R.id.noti_img);
        }
    }

    public void addMSG(LogMSG newMsg){
        data.add(0,newMsg);
        notifyItemInserted(0);
        //counter++;
        //Log.e("INFO", "notified data change");
    }
}
