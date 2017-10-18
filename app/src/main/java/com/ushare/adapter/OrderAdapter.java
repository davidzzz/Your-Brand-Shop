package com.ushare.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;


import com.ushare.R;
import com.ushare.model.ItemOrder;

import java.util.List;

public class OrderAdapter extends BaseAdapter {


    private Context activity;
    private LayoutInflater inflater;
    private List<ItemOrder> itemList;
    String order_id,URL;
    public OrderAdapter(Activity activity, List<ItemOrder> itemList) {
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
            convertView = inflater.inflate(R.layout.item_order, null);
        Button btnCancel =(Button)convertView.findViewById(R.id.btnCancel);
        TextView name = (TextView) convertView.findViewById(R.id.textTitle);
        TextView status = (TextView) convertView.findViewById(R.id.txtStatus);
        TextView txtTotal = (TextView) convertView.findViewById(R.id.txtTotal);
        TextView txtTime = (TextView) convertView.findViewById(R.id.txtTime);
        final ItemOrder item = itemList.get(position);
        name.setText(item.getNama()+" ");
        if(item.getStatus().equals("PROCESSED")){
            btnCancel.setText("Accept Delivery");
        }else{
            btnCancel.setText("");
        }
        status.setText(item.getStatus());
        txtTotal.setText("Rp "+item.getTotal());
        txtTime.setText(item.getTanggal().replace("-","/"));

        return convertView;
    }



}
