package com.ushare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import com.ushare.adapter.OrderSellerAdapter;
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

public class OrderList extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private Toolbar toolbar;
    private ListView list;
    private List<ItemOrder> itemList;
    private ItemOrder object;
    private OrderSellerAdapter adapter;
    String id_user, tipe, URL;
    boolean isFlashDeal;
    SessionManager session;
    HashMap<String, String> user;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static OrderList newInstance(String tipe, boolean isFlashDeal) {
        Bundle args = new Bundle();
        args.putString("tipe", tipe);
        args.putBoolean("isFlashDeal", isFlashDeal);

        OrderList fragment = new OrderList();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        session = new SessionManager(getActivity());
        user = session.getUserDetails();
        id_user = user.get(SessionManager.KEY_PASSENGER_ID);
        list = (ListView) rootView.findViewById(R.id.ListView1);
        itemList = new ArrayList<ItemOrder>();
        adapter = new OrderSellerAdapter(getActivity(), itemList);
        tipe = getArguments().getString("tipe");
        isFlashDeal = getArguments().getBoolean("isFlashDeal");
        URL = Constant.URLADMIN + "api/order_list.php?key=" + Constant.KEY + "&tag=list&tipe=" + tipe + "&isFlashDeal=" + isFlashDeal;
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                ambilData();
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                object = itemList.get(position);
                Intent detail = new Intent(getActivity(), OrderDetail.class);
                detail.putExtra("item_order", object);
                startActivity(detail);
            }
        });

        return rootView;
    }

    public List<ItemOrder> getListItem() {
        return itemList;
    }

    public BaseAdapter getAdapter() {
        return adapter;
    }

    private void ambilData() {
        JsonObjectRequest jsonKate = new JsonObjectRequest(URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (itemList != null) {
                    itemList.clear();
                }
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
                    item.setIdUser(feedObj.getString("user_id"));
                    item.setNama(feedObj.getString("nama"));
                    item.setTotal(feedObj.getInt("total_order"));
                    item.setStatus(feedObj.getString("status_order"));
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
                itemDetail.setPoin(feedObj.getInt("poin") * feedObj.getInt("qty"));
                item.setItemDetail(itemDetail);
            }
            itemList.add(item);
            itemList.remove(0);
            list.setAdapter(adapter);
        } catch (JSONException e) {
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
        ambilData();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (list != null) {
            list.setAdapter(null);
        }
    }
}
