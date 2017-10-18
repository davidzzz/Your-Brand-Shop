package com.ushare;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import com.ushare.adapter.HistoryAdapter;
import com.ushare.app.myapp;
import com.ushare.model.ItemOrder;
import com.ushare.util.Constant;
import com.ushare.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FragmentHistory extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public FragmentHistory(){

    }
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView list;
    private List<ItemOrder> itemList;
    private HistoryAdapter adapter;
    SessionManager session;
    HashMap<String, String> user;
    String URL_ORDER,userid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        list =(ListView)rootView.findViewById(R.id.ListView1);
        session = new SessionManager(getActivity().getApplicationContext());
        itemList = new ArrayList<ItemOrder>();
        user = session.getUserDetails();
        userid = user.get(session.KEY_PASSENGER_ID);
        adapter = new HistoryAdapter(getActivity(), itemList);
        URL_ORDER = Constant.URLADMIN+"api/history.php?key="+ Constant.KEY +"&tag=history&user_id="+userid;
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                ambilData();
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
        try {
            JSONArray feedArray = response.getJSONArray("data");
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);
                ItemOrder item = new ItemOrder();
                item.setId(feedObj.getString("order_id"));
                item.setNama(feedObj.getString("nama"));
                item.setTotal(feedObj.getInt("total_order"));
                item.setStatus(feedObj.getString("status_deliver"));
                item.setTanggal(feedObj.getString("time"));

                itemList.add(item);
            }

            // notify data changes to list adapater
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
