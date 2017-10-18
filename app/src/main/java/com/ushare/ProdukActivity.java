package com.ushare;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import com.ushare.app.myapp;
import com.ushare.model.Cart;
import com.ushare.model.ItemMenu;
import com.ushare.util.Constant;
import com.ushare.util.EasyPermission;
import com.ushare.util.GPSTracker;
import com.ushare.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProdukActivity extends AppCompatActivity implements EasyPermission.OnPermissionResult, LocationListener {
    private static final String TAG = ProdukActivity.class.getSimpleName();
    private Toolbar toolbar;
    private ListView listview;
    private AdapterSinz adapter = null;
    private List<ItemMenu> arraylist;
    private ArrayList<Cart> cartList;
    private EasyPermission easyPermission;
    private LinearLayout estimasi;
    String URL, URL_MENU, id;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int PRODUK_DETAIL = 100;
    private int poin = 0, total_item = 0, totalBarang = 0, currentPosition;

    TextView txtTotal, total_notif;
    DecimalFormat formatduit = new DecimalFormat();
    ProgressBar loading;
    GPSTracker gps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.produk_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.Produk_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtTotal = (TextView) findViewById(R.id.txtTotal);
        arraylist = new ArrayList<ItemMenu>();
        cartList = new ArrayList<>();
        total_notif = (TextView) findViewById(R.id.total_notif);
        estimasi = (LinearLayout) findViewById(R.id.lytOrder);
        loading = (ProgressBar) findViewById(R.id.prgLoading);
        estimasi.setVisibility(View.GONE);
        gps = new GPSTracker(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            easyPermission = new EasyPermission();
            easyPermission.requestPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (gps.canGetLocation()) {
            startLocationUpdates();
        }
        estimasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gps.mCurrentLocation == null) {
                    startLocationUpdates();
                    Toast.makeText(ProdukActivity.this, "Aktifkan fitur GPS untuk mengambil lokasi anda", Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(ProdukActivity.this, CartActivity.class);
                    i.putExtra("latitude", gps.getLatitude());
                    i.putExtra("longitude", gps.getLongitude());
                    i.putExtra("poin", poin);
                    i.putParcelableArrayListExtra("cartList", cartList);
                    startActivity(i);
                }
            }
        });
        id = getIntent().getStringExtra("id");
        URL_MENU = Constant.URLAPI + "key=" + Constant.KEY + "&tag=produk&id=" + id;
        listview = (ListView) findViewById(R.id.list_view);
        adapter = new AdapterSinz(this, arraylist);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                currentPosition = position;
                ItemMenu item = (ItemMenu) parent.getItemAtPosition(position);
                Intent i = new Intent(ProdukActivity.this, ProdukDetailActivity.class);
                i.putExtra("id", item.getIdMenu());
                i.putExtra("nama", item.getNamaMenu());
                i.putExtra("porsi", item.getQuantity());
                startActivityForResult(i, PRODUK_DETAIL);
            }
        });
        getData();
    }

    public boolean isLogin() {
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        return sessionManager.isLoggedIn();
    }

    private void getData() {
        JsonObjectRequest jsonKate = new JsonObjectRequest(URL_MENU, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseJsonKategory(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.setVisibility(View.GONE);
            }
        });
        jsonKate.setRetryPolicy(new DefaultRetryPolicy(5000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        myapp.getInstance().addToRequestQueue(jsonKate);
    }

    private void parseJsonKategory(JSONObject response) {
        try {
            JSONArray produkArray = response.getJSONArray("produk");
            for (int i = 0; i < produkArray.length(); i++) {
                JSONObject feedObj = (JSONObject) produkArray.get(i);
                ItemMenu item = new ItemMenu();
                item.setIdMenu(feedObj.getString("id"));
                item.setNamaMenu(feedObj.getString("nama"));
                item.setHarga(feedObj.getInt("harga"));
                item.setHalal(feedObj.getString("halal"));
                item.setPoin(feedObj.getInt("poin"));
                item.setDeskripsi(feedObj.getString("deskripsi"));
                item.setGambar(feedObj.getString("gambar"));
                arraylist.add(item);
            }
        } catch (JSONException e) {
        }
        adapter.notifyDataSetChanged();
        loading.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        gps.getLatitude();
                        gps.getLongitude();
                        break;
                }
                break;
            case PRODUK_DETAIL:
                if (data != null) {
                    Cart c = data.getParcelableExtra("cart");
                    Cart cartLama = adapter.findCart(c.getIdMenu());
                    if (cartLama != null) {
                        cartList.remove(cartLama);
                        totalBarang -= (cartLama.getQuantity() * cartLama.getHarga());
                        total_item -= cartLama.getQuantity();
                        poin -= (cartLama.getQuantity() * cartLama.getPoin());
                    }
                    cartList.add(c);
                    ItemMenu item = (ItemMenu) adapter.getItem(currentPosition);
                    item.setQuantity(c.getQuantity());
                    adapter.notifyDataSetChanged();
                    totalBarang += (c.getQuantity() * c.getHarga());
                    total_item += c.getQuantity();
                    poin += (c.getQuantity() * c.getPoin());
                    if (totalBarang == 0) {
                        estimasi.setVisibility(View.GONE);
                    } else {
                        estimasi.setVisibility(View.VISIBLE);
                        txtTotal.setText("Rp " + formatduit.format(totalBarang));
                        total_notif.setText(total_item + "");
                    }
                }
                break;
        }
    }

    @Override
    public void onPermissionResult(String permission, boolean isGranted) {
        switch (permission) {
            case android.Manifest.permission.ACCESS_COARSE_LOCATION:
                if (!isGranted) {
                    easyPermission.requestPermission(ProdukActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
                }
                break;
        }
    }

    public void startLocationUpdates() {
        final ProgressDialog loading = ProgressDialog.show(ProdukActivity.this, "Cari Lokasi", "Sedang mencari lokasi pengguna", false, true);
        LocationServices.SettingsApi.checkLocationSettings(
                gps.mGoogleApiClient,
                gps.mLocationSettingsRequest
        ).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        try {
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    gps.mGoogleApiClient, gps.mLocationRequest, ProdukActivity.this);
                        } catch (SecurityException e) {
                            Toast.makeText(ProdukActivity.this, "Lokasi tidak terdeteksi", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade location settings");
                        try {
                            status.startResolutionForResult(ProdukActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                        Toast.makeText(ProdukActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
                if (gps.mCurrentLocation != null) {
                    gps.getLatitude();
                    gps.getLongitude();
                }
                loading.dismiss();
            }
        });
    }

    public class AdapterSinz extends BaseAdapter {
        private Context context;
        private List<ItemMenu> list;
        private LayoutInflater inflater;
        DecimalFormat formatData = new DecimalFormat();

        public AdapterSinz(Context context, List<ItemMenu> list) {
            this.context = context;
            this.list = list;
        }

        public Cart findCart(String id) {
            for (int i = 0; i < cartList.size(); i++) {
                Cart cart = cartList.get(i);
                if (cart.getIdMenu().equals(id)) {
                    return cart;
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int location) {
            return list.get(location);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ItemMenu item = list.get(position);
            if (inflater == null)
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null)
                convertView = inflater.inflate(R.layout.produk_item, null);
            final TextView qty = (TextView) convertView.findViewById(R.id.edtQty);
            qty.setText("" + item.getQuantity());
            TextView nama = (TextView) convertView.findViewById(R.id.textproduk);
            nama.setText(item.getNamaMenu());

            TextView txtHalal = (TextView) convertView.findViewById(R.id.txtHalal);
            txtHalal.setText(item.getHalal());
            String text = item.getHalal();
            if (text.equals("HALAL")) {
                txtHalal.setTextColor(Color.parseColor("#7bb241"));
            } else {
                txtHalal.setTextColor(Color.parseColor("#F44336"));
            }
            ImageView gambar = (ImageView) convertView.findViewById(R.id.gambar);
            if (!item.getGambar().equals("null")) {
                Glide.with(ProdukActivity.this).load(Constant.URLADMIN + item.getGambar())
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(gambar);
            } else {
                Glide.with(ProdukActivity.this).load(R.drawable.shop)
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(gambar);
            }

            TextView harga = (TextView) convertView.findViewById(R.id.textharga);
            String format = formatData.format(item.getHarga());
            harga.setText("Rp " + format);

            Button plus = (Button) convertView.findViewById(R.id.btnplus);
            Button min = (Button) convertView.findViewById(R.id.btnmin);

            // bagian button plus di tekan
            plus.setOnClickListener(new View.OnClickListener() {
                ItemMenu qty = list.get(position);

                @Override
                public void onClick(View v) {
                    if (isLogin()) {
                        qty.setQuantity(qty.getQuantity() + 1);
                        int SubTotal = (qty.getQuantity() * qty.getHarga());
                        qty.setTotal(SubTotal);
                        poin += qty.getPoin();
                        Cart cart = findCart(qty.getIdMenu());
                        if (cart != null) {
                            cart.setQuantity(qty.getQuantity());
                            cart.setTotal(SubTotal);
                        } else {
                            cart = new Cart();
                            cart.setIdMenu(qty.getIdMenu());
                            cart.setNamaMenu(qty.getNamaMenu());
                            cart.setQuantity(qty.getQuantity());
                            cart.setHarga(qty.getHarga());
                            cart.setPoin(qty.getPoin());
                            cart.setTotal(SubTotal);
                            cart.setGambar(qty.getGambar());
                            cartList.add(cart);
                        }
                        notifyDataSetChanged();
                        TotalAmount(qty.getHarga());
                    }else{
                        Intent login = new Intent(context, LoginActivity.class);
                        startActivity(login);
                        finish();
                    }
                }
            });

            // bagian button min di tekan
            min.setOnClickListener(new View.OnClickListener() {
                ItemMenu qty = list.get(position);

                @Override
                public void onClick(View v) {
                    Cart cart = findCart(qty.getIdMenu());
                    if (qty.getQuantity() == 0) {
                        Toast.makeText(context, "Kamu belum order produk ini.", Toast.LENGTH_SHORT).show();
                        cartList.remove(cart);
                    } else if (qty.getQuantity() == 1) {
                        qty.setQuantity(0);
                        qty.setTotal(0);
                        poin -= qty.getPoin();
                        cartList.remove(cart);
                        notifyDataSetChanged();
                        TotalAmount(-qty.getHarga());
                    } else {
                        qty.setQuantity(qty.getQuantity() - 1);
                        int SubTotal = (qty.getQuantity() * qty.getHarga());
                        qty.setTotal(SubTotal);
                        poin -= qty.getPoin();
                        if (cart != null) {
                            cart.setQuantity(qty.getQuantity());
                            cart.setTotal(SubTotal);
                        } else {
                            cart = new Cart();
                            cart.setIdMenu(qty.getIdMenu());
                            cart.setNamaMenu(qty.getNamaMenu());
                            cart.setQuantity(qty.getQuantity());
                            cart.setHarga(qty.getHarga());
                            cart.setPoin(qty.getPoin());
                            cart.setTotal(SubTotal);
                            cart.setGambar(qty.getGambar());
                            cartList.add(cart);
                        }
                        notifyDataSetChanged();
                        TotalAmount(-qty.getHarga());
                    }
                }
            });

            return convertView;
        }
    }

    public void TotalAmount(int harga) {
        totalBarang += harga;
        if (harga < 0) {
            total_item--;
        } else {
            total_item++;
        }
        if (totalBarang == 0) {
            estimasi.setVisibility(View.GONE);
        } else {
            estimasi.setVisibility(View.VISIBLE);
            txtTotal.setText("Rp " + formatduit.format(totalBarang));
            total_notif.setText(total_item + "");
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
            cekData();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        if (gps.isConnected()) {
            startLocationUpdates();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (arraylist != null) {
            arraylist.clear();
            listview.setAdapter(null);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        cekData();
    }

    private void cekData() {
        if (cartList.size() > 0) {
            ShowDialog();
        } else {
            gps.stopLocationUpdates();
            this.finish();
            overridePendingTransition(R.anim.open_main, R.anim.close_next);
        }
    }

    private void ShowDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.delete_confirm);
        builder.setMessage(getString(R.string.delete_msg));
        builder.setPositiveButton(getString(R.string.ya), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                cartList.clear();
                gps.stopLocationUpdates();
                finish();
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

    @Override
    protected void onStart() {
        super.onStart();
        gps.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gps.isConnected()) {
            gps.stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gps.isConnected()) {
            gps.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        gps.onLocationChanged(location);
    }
}
