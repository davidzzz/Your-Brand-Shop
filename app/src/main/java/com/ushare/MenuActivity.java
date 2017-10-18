package com.ushare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import com.ushare.app.myapp;
import com.ushare.util.Constant;
import com.ushare.util.SessionManager;
import com.ushare.view.RoundImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MenuActivity.class.getSimpleName();
    private Toolbar toolbar;
    private LinearLayout lytProfile, lytMenu, lytOrder;
    //ambil data dari session
    SessionManager session;
    HashMap<String, String> user;
    String akses, photo, nama_user,URL,id_user;
    ImageView imgProfile;
    TextView txtnama,txtcount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_user);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.setting_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        txtnama = (TextView) findViewById(R.id.txtnama);
        txtcount =(TextView) findViewById(R.id.txtcount);
        session = new SessionManager(getApplicationContext());
        user = session.getUserDetails();
        photo = user.get(SessionManager.KEY_PHOTO);
        akses = user.get(SessionManager.KEY_AKSES);
        id_user = user.get(SessionManager.KEY_PASSENGER_ID);
        nama_user = user.get(SessionManager.KEY_NAME);
        URL = Constant.URLADMIN + "api/order_list.php?key=" + Constant.KEY + "&tag=count";
        txtnama.setText(nama_user);

        Glide.with(this)
                .load(photo)
                .transform(new RoundImage(MenuActivity.this))
                .into(imgProfile);
        lytProfile = (LinearLayout) findViewById(R.id.lytprofile);
        lytMenu = (LinearLayout) findViewById(R.id.lytMenu);
        lytOrder = (LinearLayout) findViewById(R.id.lytOrder);
        if (akses.equals("2")) {
            lytMenu.setVisibility(View.VISIBLE);
            lytOrder.setVisibility(View.VISIBLE);
            ambilData();
        } else {
            lytOrder.setVisibility(View.GONE);
            lytMenu.setVisibility(View.GONE);
        }
        lytOrder.setOnClickListener(this);
        lytMenu.setOnClickListener(this);
        lytProfile.setOnClickListener(this);
    }

    private void ambilData() {
        JsonObjectRequest jsonKate = new JsonObjectRequest(URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int count = response.getInt("data");
                    txtcount.setText(count + "");
                } catch (JSONException e) {
                }
            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        jsonKate.setRetryPolicy(new DefaultRetryPolicy(5000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        myapp.getInstance().addToRequestQueue(jsonKate);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lytprofile:
                Intent profile = new Intent(this, ProfileActivity.class);
                startActivity(profile);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                break;
            case R.id.lytMenu:
                Intent menu = new Intent(this, ListSeller.class);
                startActivity(menu);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                break;
            case R.id.lytOrder:
                Intent order = new Intent(this, OrderList.class);
                startActivity(order);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // app icon in action bar clicked; go home
            this.finish();
            overridePendingTransition(R.anim.open_main, R.anim.close_next);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Glide.get(this).clearMemory();
        finish();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }
}
