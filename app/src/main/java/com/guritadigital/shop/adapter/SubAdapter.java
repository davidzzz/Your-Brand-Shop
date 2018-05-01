package com.guritadigital.shop.adapter;

import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.guritadigital.shop.R;
import com.guritadigital.shop.SubKategoriActivity;
import com.guritadigital.shop.model.ItemSub;
import com.guritadigital.shop.util.Constant;
import com.guritadigital.shop.view.RoundedCornerTransformation;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SubAdapter extends BaseAdapter {
    private Context activity;
    private LayoutInflater inflater;
    private List<ItemSub> itemList;
    private int colorValue;

    public SubAdapter(Activity activity, List<ItemSub> itemList, int colorValue) {
        this.activity = activity;
        this.itemList = itemList;
        this.colorValue = colorValue;
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
        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.sub_item, null);

        ImageView thumbNail = (ImageView) convertView.findViewById(R.id.imgThumb);
        TextView name = (TextView) convertView.findViewById(R.id.txtText);
        ItemSub itemlatest = itemList.get(position);
        if (activity.getClass().getSimpleName().equals(SubKategoriActivity.class.getSimpleName())) {
            name.setText(itemlatest.getName());
        }
        RelativeLayout layout = (RelativeLayout) convertView.findViewById(R.id.layout_list);
        layout.setBackgroundColor(colorValue);
        Glide.with(activity)
                .load(Constant.URLADMIN + itemlatest.getImage())
                .transform(new RoundedCornerTransformation(activity))
                .placeholder(R.drawable.loading)
                .centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).into(thumbNail);

        return convertView;
    }

}
