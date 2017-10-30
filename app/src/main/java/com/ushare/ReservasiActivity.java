package com.ushare;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ushare.app.myapp;
import com.ushare.model.Cart;
import com.ushare.model.Help;
import com.ushare.model.Reservasi;
import com.ushare.util.Constant;
import com.ushare.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ReservasiActivity extends AppCompatActivity {
    Toolbar mToolbar;
    EditText teksNama, teksJumlah, teksHP;
    TextView teksTanggal, teksWaktu;
    Button send;
    String URL_SEND, userid, nama, jumlah, noHP, tanggal, waktu, akses;
    SessionManager session;
    HashMap<String, String> user;
    ProgressDialog loading;
    int colorValue;
    Calendar c;
    Adapter adapter;
    ArrayList<Reservasi> list = new ArrayList<>();
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservasi);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("RESERVASI");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        colorValue = getIntent().getIntExtra("color", 0);
        LinearLayout layout = (LinearLayout)findViewById(R.id.activity_reservasi);
        layout.setBackgroundColor(colorValue);
        URL_SEND = Constant.URLADMIN + "api/reservasi.php";
        session = new SessionManager(getApplicationContext());
        user = session.getUserDetails();
        userid = user.get(session.KEY_PASSENGER_ID);
        akses = user.get(SessionManager.KEY_AKSES);
        LinearLayout layout_form = (LinearLayout) findViewById(R.id.form_reservasi);
        listView = (ListView) findViewById(R.id.list_reservasi);
        if (akses.equals("1")) {
            layout_form.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            c = Calendar.getInstance();
            pilihTanggal();
            teksNama = (EditText) findViewById(R.id.nama);
            teksJumlah = (EditText) findViewById(R.id.jumlah);
            teksHP = (EditText) findViewById(R.id.nomor_handphone);
            teksTanggal = (TextView) findViewById(R.id.teksTanggal);
            teksWaktu = (TextView) findViewById(R.id.teksWaktu);
            send = (Button) findViewById(R.id.send);
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nama = teksNama.getText().toString();
                    jumlah = teksJumlah.getText().toString();
                    noHP = teksHP.getText().toString();
                    if (nama.equals("")) {
                        Toast.makeText(ReservasiActivity.this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    } else if (jumlah.equals("")) {
                        Toast.makeText(ReservasiActivity.this, "Jumlah Pelanggan tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    } else if (noHP.equals("")) {
                        Toast.makeText(ReservasiActivity.this, "Nomor Handphone tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    } else {
                        loading = ProgressDialog.show(ReservasiActivity.this, "Kirim Reservasi", "Please wait...", false, true);
                        tanggal = teksTanggal.getText().toString();
                        waktu = teksWaktu.getText().toString();
                        new SendReservasi().execute();
                    }
                }
            });
        } else {
            layout_form.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter = new Adapter(list);
            ambilData();
        }
    }

    public void pilihTanggal() {
        Button icon_kalender = (Button) findViewById(R.id.tanggal);
        icon_kalender.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int y = c.get(Calendar.YEAR);
                    int m = c.get(Calendar.MONTH);
                    int d = c.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(ReservasiActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    String day, month;
                                    c.set(year, monthOfYear, dayOfMonth);
                                    if (dayOfMonth < 10) {
                                        day = '0' + String.valueOf(dayOfMonth);
                                    } else {
                                        day = String.valueOf(dayOfMonth);
                                    }
                                    monthOfYear++; // disini bulan dimulai dari 0
                                    if (monthOfYear < 10) {
                                        month = '0' + String.valueOf(monthOfYear);
                                    } else {
                                        month = String.valueOf(monthOfYear);
                                    }
                                    teksTanggal.setText(year + "-" + month + "-" + day);
                                }
                            }, y, m, d);
                    datePickerDialog.show();
                }
        });
        Button icon_jam = (Button) findViewById(R.id.waktu);
        icon_jam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int h = c.get(Calendar.HOUR_OF_DAY);
                    int m = c.get(Calendar.MINUTE);
                    TimePickerDialog timePickerDialog = new TimePickerDialog(ReservasiActivity.this,
                            new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hour, int minute) {
                                    String jam = (hour < 10 ? "0" : "") + String.valueOf(hour);
                                    String menit = (minute < 10 ? "0" : "") + String.valueOf(minute);
                                    c.set(Calendar.HOUR_OF_DAY, hour);
                                    c.set(Calendar.MINUTE, minute);
                                    c.set(Calendar.AM_PM, hour < 12 ? Calendar.AM : Calendar.PM);
                                    teksWaktu.setText(jam + ":" + menit);
                                }
                            }, h, m, false);
                    timePickerDialog.show();
                }
        });
    }

    class SendReservasi extends AsyncTask<Void,Void,Boolean> {
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

                String parameter = "idUser=" + userid + "&nama=" + nama + "&jumlah=" + jumlah + "&noHP=" + noHP
                        + "&tanggal=" + tanggal + "&waktu=" + waktu + "&key=" + Constant.KEY;

                OutputStreamWriter writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                writer.write(parameter);
                writer.flush();
                writer.close();

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
                return true;
            } catch (Exception e) {
                loading.dismiss();
                return false;
            }
        }

        @Override
        public void onPostExecute(Boolean result) {
            if (result != null && result) {
                Toast.makeText(ReservasiActivity.this, response, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ReservasiActivity.this, "Reservasi gagal dikirim", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void ambilData() {
        String URL = Constant.URLADMIN + "api/reservasi.php?key=" + Constant.KEY + "&tag=list";
        loading = ProgressDialog.show(this, "Loading", "Sedang mengambil data", false, true);
        JsonObjectRequest jsonKate = new JsonObjectRequest(URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                list.clear();
                parseJsonKategory(response);
                loading.dismiss();
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
            JSONArray feedArray = response.getJSONArray("data");
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);
                Reservasi reservasi = new Reservasi();
                reservasi.setNama(feedObj.getString("nama"));
                reservasi.setJumlah(feedObj.getInt("jumlah"));
                reservasi.setNoHP(feedObj.getString("nomor_handphone"));
                reservasi.setWaktu(feedObj.getString("waktu"));
                list.add(reservasi);
            }
            listView.setAdapter(adapter);
        } catch (JSONException e) {
        }
        adapter.notifyDataSetChanged();
    }

    public class Adapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<Reservasi> list;

        public Adapter(List<Reservasi> list) {
            this.list = list;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (inflater == null)
                inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null)
                convertView = inflater.inflate(R.layout.list_reservasi, null);
            Reservasi reservasi = list.get(position);
            TextView nama = (TextView) convertView.findViewById(R.id.nama);
            nama.setText(reservasi.getNama());
            TextView jumlah = (TextView) convertView.findViewById(R.id.jumlah);
            jumlah.setText("Jumlah : " + reservasi.getJumlah() + " orang");
            TextView noHP = (TextView) convertView.findViewById(R.id.noHP);
            noHP.setText("No. HP : " + reservasi.getNoHP());
            TextView waktu = (TextView) convertView.findViewById(R.id.waktu);
            waktu.setText(reservasi.getWaktu());
            return convertView;
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
}
