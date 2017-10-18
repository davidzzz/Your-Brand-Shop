package com.ushare;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.ushare.app.myapp;
import com.ushare.model.Cart;
import com.ushare.util.Constant;
import com.ushare.util.RouteDraw;
import com.ushare.util.SessionManager;
import com.ushare.util.Utils;
import com.ushare.view.ExpandableHeightListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CartActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, RouteDraw.onDrawRoute {
    private static final String TAG = CartActivity.class.getSimpleName();
    private Toolbar toolbar;
    private ExpandableHeightListView list;
    private ArrayList<Cart> cartList;

    private GoogleMap mMap;

    private AdapterCart adapter = null;
    SessionManager session;
    HashMap<String, String> user;
    String alamat, notes, totalorder, userid,telp;
    String listorder = "";
    String URL_SEND, URL_CONF, URL_LOKASI;
    Button btnSend;
    double latitude, longitude;
    EditText edtkoment, edtAlamat, edt_telp;
    int Total = 0; //total harga menulist
    int ongkir = 0; //ini total ongkir
    int subTotal = 0; //ini subtotal
    int meter = 0;
    int perkm = 0;
    int poin = 0;
    int totalBarang = 0;
    double latToko = 0.0, lngToko = 0.0;
    String idFlashDeal = "";
    TextView textTotal, textPoin, txtOngkir, txtsubtotl,titleongkir;
    DecimalFormat formatduit = new DecimalFormat();
    ProgressDialog loading;
    LinearLayout lytAlert,lytOrder;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.checkout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lytOrder =(LinearLayout)findViewById(R.id.lytOrder);
        lytAlert =(LinearLayout)findViewById(R.id.lytAlert);
        list = (ExpandableHeightListView) findViewById(R.id.listview);
        edtkoment = (EditText) findViewById(R.id.edtkoment);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        if(!Utils.isConnectedToInternet(this)){
            lytAlert.setVisibility(View.VISIBLE);
        }else{
            lytOrder.setVisibility(View.VISIBLE);
            lytAlert.setVisibility(View.GONE);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        edtAlamat = (EditText) findViewById(R.id.edtAlamat);
        edt_telp = (EditText)findViewById(R.id.edt_telp);

        textPoin = (TextView) findViewById(R.id.textPoin);
        textTotal = (TextView) findViewById(R.id.textTotal);
        txtOngkir = (TextView) findViewById(R.id.txtOngkir);
        titleongkir =(TextView) findViewById(R.id.titleongkir);
        txtsubtotl = (TextView) findViewById(R.id.txtsubtotl);
        session = new SessionManager(getApplicationContext());
        user = session.getUserDetails();
        userid = user.get(session.KEY_PASSENGER_ID);
        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);
        poin = getIntent().getIntExtra("poin", 0);
        idFlashDeal = getIntent().getStringExtra("idFlashDeal");

        URL_SEND = Constant.URLADMIN + "api/send_order.php";
        URL_CONF = Constant.URLAPI + "key=" + Constant.KEY + "&tag=konfigurasi&id=4";
        URL_LOKASI = Constant.URLAPI + "key=" + Constant.KEY + "&tag=lokasiToko";
        cartList = getIntent().getParcelableArrayListExtra("cartList");
        konfigurasi();
        lokasiToko();
        ambilData();
    }

    public Boolean Check() {
        notes = edtkoment.getText().toString();
        alamat = edtAlamat.getText().toString();
        telp = edt_telp.getText().toString();
        String ttl = textTotal.getText().toString();
        totalorder = ttl.replace(",","");

        edtAlamat.setError(null);
        edt_telp.setError(null);

        if (alamat.length() == 0) {
            edtAlamat.setError("Alamat masih kosong.");
            return false;
        }
        if (telp.length() == 0) {
            edt_telp.setError("Telp masih kosong.");
            return false;
        }
        if (meter > 20) {
            Toast.makeText(CartActivity.this, "Maximum 20 km..", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void KirimOrder() {
        String repTotal = listorder.replace("<br>","\n");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("KONFIRMASI CHECKOUT");
        builder.setMessage("Total Order : Rp "+ totalorder +"\n"+repTotal+
                "\nOngkir : Rp "+ongkir+
                "\nSub Total :"+txtsubtotl.getText().toString()+
                "\n\nDi Kirim Ke :\n"+alamat+"\n");
        builder.setPositiveButton(getString(R.string.kirim), new DialogInterface.OnClickListener() {

            public void onClick(final DialogInterface dialog, int which) {
                loading = ProgressDialog.show(CartActivity.this, "Kirim Order", "Please wait...", false, true);
                new SendOrder().execute();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getString(R.string.tidak), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    class SendOrder extends AsyncTask<Void,Void,Boolean> {
        String response;
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL(URL_SEND);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.connect();

                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (Cart c : cartList)
                {
                    sb.append("{\"idMenu\":");
                    sb.append(c.getIdMenu());
                    sb.append(",\"quantity\":");
                    sb.append(c.getQuantity());
                    sb.append(",\"total\":");
                    sb.append(c.getTotal());
                    sb.append("},");
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append("]");
                String parameter = "idUser=" + userid + "&totalOrder=" + totalorder + "&ongkir=" + ongkir + "&alamat=" + alamat +
                        "&notes=" + notes + "&telepon=" + telp + "&poin=" + poin + "&key=" + Constant.KEY + "&cartArray=" + sb.toString();

                if (idFlashDeal != null) {
                    parameter += "&idFlashDeal=" + idFlashDeal;
                }
                OutputStreamWriter writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                writer.write(parameter);
                writer.flush();
                writer.close();
                cartList.clear();

                InputStream responseStream = new BufferedInputStream(httpURLConnection.getInputStream());
                BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                response = stringBuilder.toString();
                responseStreamReader.close();
                responseStream.close();
                loading.dismiss();
                finish();
                return true;
            } catch (Exception e) {
                loading.dismiss();
                return false;
            }
        }

        @Override
        public void onPostExecute(Boolean result) {
            if (result != null && result) {
                Toast.makeText(CartActivity.this, response, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(CartActivity.this, "Order gagal dikirim", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void konfigurasi() {
        JsonObjectRequest jsonKate = new JsonObjectRequest(URL_CONF, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject feedObj = response.getJSONObject("data");
                    perkm = feedObj.getInt("value");
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

    private void lokasiToko() {
        JsonObjectRequest jsonKate = new JsonObjectRequest(URL_LOKASI, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject feedObj = response.getJSONObject("data");
                    latToko = feedObj.getDouble("latitude");
                    lngToko = feedObj.getDouble("long_latitude");
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

    private void ambilData() {
        adapter = new AdapterCart(cartList);
        list.setAdapter(adapter);
        list.setExpanded(true);
        TotalCheckOut();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend:
                if (Check()){
                    KirimOrder();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        RouteDraw.getInstance(this,CartActivity.this).setFromLatLong(latitude, longitude)
                .setToLatLong(latToko, lngToko).setGmapAndKey("AIzaSyBUfY7r8eToolFdQcCTRh-HrhZ0fKdo01Y",mMap)
                .run();
        if (mMap != null) {
            int height = 70;
            int width = 70;
            BitmapDrawable bitmapdraw=(BitmapDrawable) ContextCompat.getDrawable(CartActivity.this, R.mipmap.ic_launcher);
            Bitmap b=bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                    .title("location"));
            mMap.addMarker(new MarkerOptions().position(new LatLng(latToko, lngToko))
                    .title("Ke tujuan"));
        }
    }

    @Override
    public void afterDraw(String result) {
        try {
            //Convert string to jsona and parse
            JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);

            //parse total jarak
            JSONArray arr = routes.getJSONArray("legs");
            JSONObject a = arr.getJSONObject(0);
            JSONObject b = a.getJSONObject("distance");
            alamat = a.getString("start_address");
            meter = b.getInt("value");
            ongkir = ((int) Math.ceil(meter/ 1000.0)* perkm);
            edtAlamat.setText(alamat);
            txtOngkir.setText(formatduit.format(ongkir)+"");
            titleongkir.setText("Jarak ("+(int) Math.ceil(meter/ 1000.0)+" km x "+ formatduit.format(perkm) +")");
            subTotal = ongkir + totalBarang;
            txtsubtotl.setText(formatduit.format(subTotal)+"");
        } catch (JSONException e) {

        }
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    public class AdapterCart extends BaseAdapter {
        private LayoutInflater inflater;
        private List<Cart> list;
        DecimalFormat formatData = new DecimalFormat();

        public AdapterCart(List<Cart> list) {
            this.list = list;
        }

        @Override
        public int getCount() { return list.size(); }

        @Override
        public Object getItem(int location) { return list.get(location); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Cart items = list.get(position);
            if (inflater == null)
                inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null)
                convertView = inflater.inflate(R.layout.produk_item, null);
            TextView nama = (TextView) convertView.findViewById(R.id.textproduk);
            nama.setText(items.getNamaMenu());

            TextView harga = (TextView) convertView.findViewById(R.id.textharga);
            String format = formatData.format(items.getHarga());
            harga.setText("Rp " + format);
            ImageView gambar = (ImageView) convertView.findViewById(R.id.gambar);
            if (!items.getGambar().equals("null")) {
                Glide.with(CartActivity.this).load(Constant.URLADMIN + items.getGambar())
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(gambar);
            } else {
                Glide.with(CartActivity.this).load(R.drawable.shop)
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(gambar);
            }

            TextView qty = (TextView) convertView.findViewById(R.id.edtQty);
            // qty.setTag(item);
            qty.setText("" + items.getQuantity());

            final TextView txttotal = (TextView) convertView.findViewById(R.id.texTotal1);
            txttotal.setText(items.getTotal() + "");

            Button plus = (Button) convertView.findViewById(R.id.btnplus);
            Button min = (Button) convertView.findViewById(R.id.btnmin);
            // bagian button plus di tekan
            plus.setOnClickListener(new View.OnClickListener() {
                Cart qty = list.get(position);

                @Override
                public void onClick(View v) {
                    qty.setQuantity(qty.getQuantity() + 1);
                    Total = (qty.getQuantity() * qty.getHarga());
                    qty.setTotal(Total);
                    poin += qty.getPoin();
                    notifyDataSetChanged();
                    TotalCheckOut();
                }
            });

            // bagian button min di tekan
            min.setOnClickListener(new View.OnClickListener() {
                Cart qty = list.get(position);

                @Override
                public void onClick(View v) {
                    poin -= qty.getPoin();
                    if (qty.getQuantity() == 1) {
                        list.remove(position);
                        notifyDataSetChanged();
                        TotalCheckOut();
                    } else {
                        qty.setQuantity(qty.getQuantity() - 1);
                        Total = (qty.getQuantity() * qty.getHarga());
                        qty.setTotal(Total);
                        notifyDataSetChanged();
                        TotalCheckOut();
                    }
                }
            });

            return convertView;
        }
    }

    public void TotalCheckOut() {
        if (listorder.equals("")){
            listorder = "";
        }

        int jumlahlist = list.getAdapter().getCount();
        totalBarang = 0;
        for (int i = 0; i < jumlahlist; i++) {
            View v = list.getAdapter().getView(i, null, null);
            TextView txt = (TextView) v.findViewById(R.id.texTotal1);
            TextView textproduk = (TextView) v.findViewById(R.id.textproduk);
            TextView quantity = (TextView) v.findViewById(R.id.edtQty);
            String stringItem = quantity.getText().toString();
            String namaproduk = textproduk.getText().toString();
            String stringTotal = txt.getText().toString();
            listorder += namaproduk + " x " + stringItem + " = "+ stringTotal + "<br><br>";
            totalBarang += Integer.parseInt(stringTotal);
        }
        textTotal.setText(formatduit.format(totalBarang)+"");
        subTotal = ongkir + totalBarang;
        txtsubtotl.setText(formatduit.format(subTotal)+"");
        textPoin.setText(poin + "");
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
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (list != null) {
            list.setAdapter(null);
            cartList.clear();
        }
    }
}
