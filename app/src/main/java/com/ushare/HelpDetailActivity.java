package com.ushare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ushare.app.myapp;
import com.ushare.util.Constant;

import org.json.JSONException;
import org.json.JSONObject;

public class HelpDetailActivity extends AppCompatActivity {
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_detail);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("HELP");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        id = getIntent().getIntExtra("id", 0);
        ambilData();
    }

    private void ambilData() {
        String URL = Constant.URLAPI + "key=" + Constant.KEY + "&tag=helpdetail&id=" + id;
        JsonObjectRequest jsonKate = new JsonObjectRequest(URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject object = response.getJSONObject("data");
                    TextView judul = (TextView) findViewById(R.id.judul);
                    TextView isi = (TextView) findViewById(R.id.isi);
                    judul.setText(object.getString("judul"));
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        isi.setText(Html.fromHtml(object.getString("isi"), Html.FROM_HTML_MODE_COMPACT), TextView.BufferType.SPANNABLE);
                    } else {
                        isi.setText(Html.fromHtml(object.getString("isi")), TextView.BufferType.SPANNABLE);
                    }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // app icon in action bar clicked; go home
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
