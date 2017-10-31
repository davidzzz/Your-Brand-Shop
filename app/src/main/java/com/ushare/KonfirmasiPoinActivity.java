package com.ushare;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ushare.app.myapp;
import com.ushare.model.ItemDetail;
import com.ushare.model.ItemOrder;
import com.ushare.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class KonfirmasiPoinActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView teksNomorResi, teksTotalOrder, teksPoin, teksNomorUser, teksNama, teksNoHP;
    Button konfirmasi;
    String URL, URLPOIN, nomorResi, totalOrder, poin, nomorUser, id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konfirmasi_poin);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("KONFIRMASI");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        teksNomorResi = (TextView) findViewById(R.id.nomor_resi);
        teksTotalOrder = (TextView) findViewById(R.id.total_order);
        teksPoin = (TextView) findViewById(R.id.poin);
        teksNomorUser = (TextView) findViewById(R.id.nomor_user);
        teksNama = (TextView) findViewById(R.id.nama);
        teksNoHP = (TextView) findViewById(R.id.nomor_handphone);
        konfirmasi = (Button) findViewById(R.id.konfirmasi);
        konfirmasi.setEnabled(false);
        nomorResi = getIntent().getStringExtra("nomor");
        totalOrder = getIntent().getStringExtra("total");
        poin = getIntent().getStringExtra("poin");
        nomorUser = getIntent().getStringExtra("nomorUser");
        teksNomorResi.setText(nomorResi);
        teksTotalOrder.setText("Rp. " + totalOrder);
        teksPoin.setText(poin + " poin");
        teksNomorUser.setText(nomorUser);
        URL = Constant.URLAPI + "key=" + Constant.KEY + "&tag=user&nomorUser=" + nomorUser;
        URLPOIN = Constant.URLAPI + "key=" + Constant.KEY + "&tag=updatePoin";
        getDataUser();
        konfirmasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(KonfirmasiPoinActivity.this);
                alert.setTitle("KONFIRMASI");
                alert.setMessage("Apakah data transaksi sudah sesuai?");
                alert.setPositiveButton("YA", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        updatePoin();
                    }
                });

                alert.setNegativeButton("TIDAK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        });
    }

    public void updatePoin(){
        final ProgressDialog loading = ProgressDialog.show(this, "Loading", "Mengupdate poin user", false, false);
        JsonObjectRequest jsonKate = new JsonObjectRequest(URLPOIN + "&id=" + id + "&poin=" + poin, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loading.dismiss();
                Toast.makeText(KonfirmasiPoinActivity.this, "Poin berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.dismiss();
                Toast.makeText(KonfirmasiPoinActivity.this, "Poin tidak berhasil ditambahkan", Toast.LENGTH_SHORT).show();
            }
        });
        jsonKate.setRetryPolicy(new DefaultRetryPolicy(5000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        myapp.getInstance().addToRequestQueue(jsonKate);
    }

    public void getDataUser(){
        final ProgressDialog loading = ProgressDialog.show(this, "Loading", "Sedang mengambil data user", false, false);
        JsonObjectRequest jsonKate = new JsonObjectRequest(URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loading.dismiss();
                parseJsonKategory(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.dismiss();
            }
        });
        jsonKate.setRetryPolicy(new DefaultRetryPolicy(5000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        myapp.getInstance().addToRequestQueue(jsonKate);
    }

    private void parseJsonKategory(JSONObject response) {
        try {
            JSONObject feedObj = response.getJSONObject("data");
            teksNama.setText(feedObj.getString("nama"));
            teksNoHP.setText(feedObj.getString("telp"));
            id = feedObj.getString("user_id");
            konfirmasi.setEnabled(true);
        } catch (JSONException e) {
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
}
