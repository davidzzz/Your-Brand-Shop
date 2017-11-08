package com.ushare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import com.ushare.adapter.HistoryAdapter;
import com.ushare.app.myapp;
import com.ushare.model.ItemDetail;
import com.ushare.model.ItemOrder;
import com.ushare.util.Constant;
import com.ushare.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FragmentHistory extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView list;
    private List<ItemOrder> itemList;
    private HistoryAdapter adapter;
    SessionManager session;
    HashMap<String, String> user;
    String URL_ORDER,userid;
    boolean isFlashDeal;

    public static FragmentHistory newInstance(boolean isFlashDeal){
        Bundle args = new Bundle();
        args.putBoolean("isFlashDeal", isFlashDeal);

        FragmentHistory fragment = new FragmentHistory();
        fragment.setArguments(args);
        return fragment;
    }

    public List<ItemOrder> getListItem() {
        return itemList;
    }

    public BaseAdapter getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        list =(ListView)rootView.findViewById(R.id.ListView1);
        session = new SessionManager(getActivity().getApplicationContext());
        itemList = new ArrayList<ItemOrder>();
        user = session.getUserDetails();
        userid = user.get(session.KEY_PASSENGER_ID);
        isFlashDeal = getArguments().getBoolean("isFlashDeal");
        adapter = new HistoryAdapter(getActivity(), itemList);
        URL_ORDER = Constant.URLADMIN+"api/history.php?key="+ Constant.KEY +"&tag=history&user_id="+userid + "&isFlashDeal=" + isFlashDeal;
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                ambilData();
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                ItemOrder object = itemList.get(position);
                Intent detail = new Intent(getActivity(), OrderDetail.class);
                detail.putExtra("item_order", object);
                startActivity(detail);
            }
        });
        return rootView;
    }

    private void ambilData() {
        JsonObjectRequest jsonKate = new JsonObjectRequest(URL_ORDER, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                itemList.clear();
                parseJsonKategory(response);
            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        jsonKate.setRetryPolicy(new DefaultRetryPolicy(5000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        myapp.getInstance().addToRequestQueue(jsonKate);
    }

    private void parseJsonKategory(JSONObject response) {
        String id = "";
        ItemOrder item = new ItemOrder();
        try {
            JSONArray feedArray = response.getJSONArray("data");
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);
                if (!feedObj.getString("order_id").equals(id)) {
                    itemList.add(item);
                    id = feedObj.getString("order_id");
                    item = new ItemOrder();
                    item.setId(feedObj.getString("order_id"));
                    item.setNama(feedObj.getString("nama"));
                    item.setTotal(feedObj.getInt("total_order"));
                    item.setStatus(feedObj.getString("status_deliver"));
                    item.setTanggal(feedObj.getString("time"));
                    item.setCatatan(feedObj.getString("notes"));
                    item.setAddress(feedObj.getString("address"));
                    item.setTelp(feedObj.getString("telp_order"));
                    item.setGcm_id(feedObj.getString("gcm"));
                    item.setTtlongkir(feedObj.getInt("ttl_ongkir"));
                    item.setOnTheSpot(feedObj.getString("on_the_spot").equals("1"));
                }
                ItemDetail itemDetail = new ItemDetail();
                itemDetail.setNama(feedObj.getString("nama_produk"));
                itemDetail.setQty(feedObj.getInt("qty"));
                itemDetail.setSubtotal(feedObj.getInt("subtotal"));
                item.setItemDetail(itemDetail);
            }
            itemList.add(item);
            itemList.remove(0);
            list.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        ambilData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(list == null){
            ambilData();
        }
    }

    @Override
    public void onDestroy() {
        list.setAdapter(null);
        Glide.get(getActivity()).clearMemory();
        super.onDestroy();
    }
}
