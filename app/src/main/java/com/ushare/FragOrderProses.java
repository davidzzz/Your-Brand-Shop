package com.ushare;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import com.ushare.adapter.OrderAdapter;
import com.ushare.app.myapp;
import com.ushare.model.ItemOrder;
import com.ushare.util.Constant;
import com.ushare.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class FragOrderProses extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public FragOrderProses(){

    }
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView list;
    private List<ItemOrder> itemList;
    private ItemOrder object;
    private OrderAdapter adapter;
    SessionManager session;
    HashMap<String, String> user;
    String URL_ORDER,userid,URL_CANCEL,order_id,URL_ACCEPT;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        list =(ListView)rootView.findViewById(R.id.ListView1);
        session = new SessionManager(getActivity().getApplicationContext());
        itemList = new ArrayList<ItemOrder>();
        user = session.getUserDetails();
        userid = user.get(session.KEY_PASSENGER_ID);
        adapter = new OrderAdapter(getActivity(), itemList);
        URL_CANCEL = Constant.URLADMIN+"api/cancel.php";
        URL_ACCEPT = Constant.URLADMIN+"api/accept_user.php";
        URL_ORDER = Constant.URLADMIN+"api/history.php?key=" + Constant.KEY + "&tag=order&user_id="+userid;
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
                object= itemList.get(position);
                order_id = object.getId();
                if (object.getStatus().equals("PROCESSED")) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle(R.string.app_name);
                    alert.setIcon(R.drawable.ic_launcher);
                    alert.setMessage("Accept Delivery");
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Accept();
                        }
                    });
                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();

                        }
                    });

                    alert.show();
                } else {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle(R.string.app_name);
                    alert.setIcon(R.drawable.ic_launcher);
                    alert.setMessage("Cancel This Order ??");
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Cancel();
                        }
                    });
                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();

                        }
                    });

                    alert.show();
                }
            }
        });

        return rootView;
    }

    private void Accept() {
        final ProgressDialog loading = ProgressDialog.show(getActivity(),"Send Data...","Please wait...",false,true);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_ACCEPT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        Toast.makeText(getActivity(), s , Toast.LENGTH_LONG).show();
                        ambilData();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Toast.makeText(getActivity(), "Server error, Please Try again.." , Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();
                //Adding parameters
                params.put("order_id", order_id);
                params.put("key", Constant.KEY);
                //returning parameters
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        myapp.getInstance().addToRequestQueue(stringRequest);
    }

    private void Cancel() {
        final ProgressDialog loading = ProgressDialog.show(getActivity(),"Send Data...","Please wait...",false,true);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CANCEL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        Toast.makeText(getActivity(), s , Toast.LENGTH_LONG).show();
                        ambilData();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Toast.makeText(getActivity(), "Server error, Please Try again.." , Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();
                //Adding parameters
                params.put("order_id", order_id);
                params.put("key", Constant.KEY);
                params.put("user_id", userid);
                //returning parameters
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        myapp.getInstance().addToRequestQueue(stringRequest);
    }

    private void ambilData() {
        JsonObjectRequest jsonKate = new JsonObjectRequest(URL_ORDER, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Dismissing progress dialog\
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
                item.setStatus(feedObj.getString("status_order"));
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
        ambilData();
        super.onResume();

    }

    @Override
    public void onDestroy() {
        list.setAdapter(null);
        Glide.get(getActivity()).clearMemory();
        super.onDestroy();
    }
}
