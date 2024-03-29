package com.easygo.tv.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easygo.monitor.R;

import java.util.ArrayList;
import java.util.List;

public class CommandAdapter extends RecyclerView.Adapter<CommandAdapter.CmdViewHolder> {

    private List<String> data = new ArrayList<>();

    @Override
    public CmdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_command_dialog, parent, false);
        return new CmdViewHolder(view);
    }

    private int noFocusTextSize;
    private int hasFocusTextSize;
    public void setTextFocusSize(int noFocus, int hasFocus) {
        this.noFocusTextSize = noFocus;
        this.hasFocusTextSize = hasFocus;
    }

    @Override
    public void onBindViewHolder(final CmdViewHolder holder, final int position) {
        String cmdText = data.get(position);
        holder.cmd.setText(cmdText);
        holder.root.setTag(cmdText);

        holder.root.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
//                    holder.cmd.setTextSize(hasFocusTextSize);
                    holder.cmd.setTextColor(Color.parseColor("#000000"));
                } else {
//                    holder.cmd.setTextSize(noFocusTextSize);
                    holder.cmd.setTextColor(Color.parseColor("#99000000"));
                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return data != null? data.size(): 0;
    }

    public void setData(List<String> data) {
        this.data = data;
        notifyDataSetChanged();
    }


    static class CmdViewHolder extends RecyclerView.ViewHolder {

        private final FrameLayout root;
        private final TextView cmd;

        CmdViewHolder(View itemView) {
            super(itemView);
            root = ((FrameLayout) itemView.findViewById(R.id.frame_layout));
            cmd = ((TextView) itemView.findViewById(R.id.cmd));
        }
    }
}
