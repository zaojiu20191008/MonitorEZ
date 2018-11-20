package com.easygo.tv.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easygo.monitor.R;
import com.easygo.tv.widget.TipMessageBean;

import java.util.ArrayList;
import java.util.List;

public class TipMessageAdapter extends RecyclerView.Adapter {

    private List<TipMessageBean> data = new ArrayList<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if(viewType == TipMessageBean.TYPE_PAY_SUCCESS) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tip_msg_pay_success, parent, false);
            return new PaySuccessViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tip_msg_black_list_in, parent, false);
            return new BlackListViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(data == null || data.size() == 0) {
            return super.getItemViewType(position);
        } else {
            TipMessageBean tipMessageBean = data.get(position);
            return tipMessageBean.type;
        }
    }

    private int no_focus;
    private int has_focus;
    public void setColor(int no_focus, int has_focus) {
        this.no_focus = no_focus;
        this.has_focus = has_focus;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        TipMessageBean tipMessageBean = data.get(position);

        switch (tipMessageBean.type) {
            case TipMessageBean.TYPE_PAY_SUCCESS:
                final PaySuccessViewHolder paySuccessViewHolder = (PaySuccessViewHolder) holder;
                paySuccessViewHolder.title.setText("有" + tipMessageBean.paySuccessCount + "个商品支付成功");
                paySuccessViewHolder.root.setTag(position);
                paySuccessViewHolder.root.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus) {
                            paySuccessViewHolder.title.setTextColor(has_focus);
                        } else {
                            paySuccessViewHolder.title.setTextColor(no_focus);
                        }
                    }
                });
                break;
            case TipMessageBean.TYPE_BLACK_LIST:
                final BlackListViewHolder blackListViewHolder = (BlackListViewHolder) holder;
                blackListViewHolder.title.setText("可疑用户：" + tipMessageBean.blackListName + " 进店");
                blackListViewHolder.root.setTag(position);
                blackListViewHolder.root.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus) {
                            blackListViewHolder.title.setTextColor(has_focus);
                            blackListViewHolder.content.setTextColor(has_focus);
                        } else {
                            blackListViewHolder.title.setTextColor(no_focus);
                            blackListViewHolder.content.setTextColor(no_focus);
                        }
                    }
                });
                break;
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return data != null? data.size(): 0;
    }

    public void setData(List<TipMessageBean> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void addData(TipMessageBean tipMessageBean) {
        data.add(0, tipMessageBean);
//        notifyDataSetChanged();
        notifyItemInserted(0);
    }

    public void clearData() {
        this.data.clear();
        notifyDataSetChanged();
    }

    public void remove(String deviceSerial) {
        if(data != null) {
            ArrayList<Integer> remove = new ArrayList<>();
            int size = data.size();
            TipMessageBean tipMessageBean;
            for (int i = 0; i < size; i++) {
                tipMessageBean = data.get(i);
                if(deviceSerial.equals(tipMessageBean.deviceSerial)) {
                    remove.add(i);
                    break;
                }
            }

            int removeCount = remove.size();
            for (int i = 0; i < removeCount; i++) {
                Integer integer = remove.get(i);
                data.remove(integer.intValue());
            }
//            notifyItemRemoved(removeIndex);
            notifyDataSetChanged();
        }
    }

    public void remove(int position) {
        if(data != null) {
            data.remove(position);
//            notifyItemRemoved(position);
            notifyDataSetChanged();
        }
    }


    static class BlackListViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout root;
        private final TextView title;
        private final TextView content;

        BlackListViewHolder(View itemView) {
            super(itemView);

            root = ((LinearLayout) itemView.findViewById(R.id.root));
            title = ((TextView) itemView.findViewById(R.id.title));
            content = ((TextView) itemView.findViewById(R.id.content));

        }
    }

    static class PaySuccessViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout root;
        private final TextView title;

        PaySuccessViewHolder(View itemView) {
            super(itemView);

            root = ((LinearLayout) itemView.findViewById(R.id.root));
            title = ((TextView) itemView.findViewById(R.id.title));

        }
    }
}
