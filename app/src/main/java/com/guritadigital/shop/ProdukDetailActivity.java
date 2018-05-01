package com.guritadigital.shop;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.guritadigital.shop.app.myapp;
import com.guritadigital.shop.model.Cart;
import com.guritadigital.shop.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProdukDetailActivity extends AppCompatActivity {
    private Toolbar toolbar;
    String id, URL, nama, gambar;
    private SliderLayout mDemoSlider;
    private TextView teksNama, teksHarga, teksPoin, teksQty, deskripsi, total;
    private Button order;
    int harga, qty, poin;
    Cart cart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produk_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra("nama"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Constant.COLOR));
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Constant.COLOR);
        } else {
            window.setTitleColor(Constant.COLOR);
        }
        teksNama = (TextView) findViewById(R.id.nama);
        teksHarga = (TextView) findViewById(R.id.harga);
        teksPoin = (TextView) findViewById(R.id.poin);
        teksQty = (TextView) findViewById(R.id.qty);
        deskripsi = (TextView) findViewById(R.id.deskripsi);
        total = (TextView) findViewById(R.id.total);
        order = (Button) findViewById(R.id.order);
        mDemoSlider = (SliderLayout) findViewById(R.id.slider);
        id = getIntent().getStringExtra("id");
        qty = getIntent().getIntExtra("porsi", 0);
        URL = Constant.URLAPI + "key=" + Constant.KEY + "&tag=produkdetail&id=" + id;
        getData();
        Button plus = (Button) findViewById(R.id.btnplus);
        Button min = (Button) findViewById(R.id.btnmin);

        // bagian button plus di tekan
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qty++;
                int SubTotal = qty * harga;
                if (cart != null) {
                    cart.setQuantity(qty);
                    cart.setTotal(SubTotal);
                } else {
                    cart = new Cart();
                    cart.setIdMenu(id);
                    cart.setNamaMenu(nama);
                    cart.setQuantity(qty);
                    cart.setHarga(harga);
                    cart.setPoin(poin);
                    cart.setGambar(gambar);
                    cart.setTotal(SubTotal);
                }
                teksQty.setText(qty + "");
                total.setText("Total: Rp. " + SubTotal);
            }
        });

        // bagian button min di tekan
        min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (qty == 1) {
                    qty = 0;
                    teksQty.setText("0");
                    total.setText("Total: Rp. 0");
                } else if (qty > 1) {
                    qty--;
                    int SubTotal = qty * harga;
                    if (cart != null) {
                        cart.setQuantity(qty);
                        cart.setTotal(SubTotal);
                    } else {
                        cart = new Cart();
                        cart.setIdMenu(id);
                        cart.setNamaMenu(nama);
                        cart.setQuantity(qty);
                        cart.setHarga(harga);
                        cart.setPoin(poin);
                        cart.setGambar(gambar);
                        cart.setTotal(SubTotal);
                    }
                    teksQty.setText(qty + "");
                    total.setText("Total: Rp. " + SubTotal);
                }
            }
        });

        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(100, getIntent().putExtra("cart", cart));
                finish();
            }
        });
    }

    private void getData() {
        JsonObjectRequest jsonKate = new JsonObjectRequest(URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseJsonKategory(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        jsonKate.setRetryPolicy(new DefaultRetryPolicy(5000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        myapp.getInstance().addToRequestQueue(jsonKate);

        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(5000);
    }

    private void parseJsonKategory(JSONObject response) {
        try {
            JSONArray data = response.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject feedObj = (JSONObject) data.get(i);
                String judul = feedObj.getString("judul");
                gambar = feedObj.getString("gambar");
                nama = feedObj.getString("nama_produk");
                harga = Integer.parseInt(feedObj.getString("harga"));
                poin = Integer.parseInt(feedObj.getString("poin"));
                teksNama.setText(nama);
                teksHarga.setText("Rp. " + harga);
                teksPoin.setText("Poin : " + poin);
                teksQty.setText(qty + "");
                total.setText("Total: Rp. " + (qty * harga));
                if (qty > 0) {
                    cart = new Cart();
                    cart.setIdMenu(id);
                    cart.setNamaMenu(nama);
                    cart.setQuantity(qty);
                    cart.setHarga(harga);
                    cart.setPoin(poin);
                    cart.setGambar(gambar);
                    cart.setTotal(qty * harga);
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    deskripsi.setText(Html.fromHtml(feedObj.getString("deskripsi"), Html.FROM_HTML_MODE_COMPACT), TextView.BufferType.SPANNABLE);
                } else {
                    deskripsi.setText(Html.fromHtml(feedObj.getString("deskripsi")), TextView.BufferType.SPANNABLE);
                }
                TextSliderView textSliderView = new TextSliderView(ProdukDetailActivity.this);
                textSliderView
                        .image(Constant.URLADMIN + gambar)
                        .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                        .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                            @Override
                            public void onSliderClick(BaseSliderView slider) {
                                Intent i = new Intent(ProdukDetailActivity.this, GambarActivity.class);
                                i.putExtra("url", slider.getUrl());
                                startActivity(i);
                            }
                        });

                mDemoSlider.addSlider(textSliderView);
            }
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
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
