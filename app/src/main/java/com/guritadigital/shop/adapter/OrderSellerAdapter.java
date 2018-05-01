package com.guritadigital.shop.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.guritadigital.shop.R;
import com.guritadigital.shop.model.ItemOrder;

import java.text.DecimalFormat;
import java.util.List;

public class OrderSellerAdapter extends BaseAdapter {
    private Context activity;
    private LayoutInflater inflater;
    private List<ItemOrder> itemList;

    public OrderSellerAdapter(Activity activity, List<ItemOrder> itemList) {
        this.activity = activity;
        this.itemList = itemList;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int location) {
        return itemList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_history, null);
        DecimalFormat formatduit = new DecimalFormat();
        TextView name = (TextView) convertView.findViewById(R.id.textTitle);
        TextView status = (TextView) convertView.findViewById(R.id.txtStatus);
        TextView txtTotal = (TextView) convertView.findViewById(R.id.txtTotal);
        TextView txtTime = (TextView) convertView.findViewById(R.id.txtTime);
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        final ItemOrder item = itemList.get(position);
        if (item.isOnTheSpot()) {
            icon.setBackgroundResource(R.drawable.checklist);
        } else {
            icon.setBackgroundResource(R.drawable.delivery);
        }
        name.setText(item.getNama() + "");
        if (item.getStatus().equals("PENDING")) {
            status.setText("NEW ORDER");
            status.setTextColor(Color.parseColor("#7bb241"));
        } else if (item.getStatus().equals("DONE")) {
            status.setText(item.getStatus());
            status.setTextColor(Color.parseColor("#7bb241"));
        } else if (item.getStatus().equals("PROCESSED")) {
            status.setText(item.getStatus());
            status.setTextColor(Color.parseColor("#3F51B5"));
        } else {
            status.setText(item.getStatus());
        }
        txtTotal.setText("Rp " + formatduit.format(item.getTotal()));
        txtTime.setText(item.getTanggal().replace("-","/"));
        return convertView;
    }

}
