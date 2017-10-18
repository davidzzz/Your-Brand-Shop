package com.ushare;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ushare.model.Cart;
import com.ushare.util.Constant;
import com.ushare.util.SessionManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;

public class ReservasiActivity extends AppCompatActivity {
    Toolbar mToolbar;
    EditText teksNama, teksJumlah, teksHP;
    TextView teksTanggal, teksWaktu;
    Button send;
    String URL_SEND, userid, nama, jumlah, noHP, tanggal, waktu;
    SessionManager session;
    HashMap<String, String> user;
    ProgressDialog loading;
    int colorValue;
    Calendar c;

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
        c = Calendar.getInstance();
        pilihTanggal();
        teksNama = (EditText) findViewById(R.id.nama);
        teksJumlah = (EditText) findViewById(R.id.jumlah);
        teksHP = (EditText) findViewById(R.id.nomor_handphone);
        teksTanggal = (TextView) findViewById(R.id.teksTanggal);
        teksWaktu = (TextView) findViewById(R.id.teksWaktu);
        send = (Button) findViewById(R.id.send);
        URL_SEND = Constant.URLADMIN + "api/reservasi.php";
        session = new SessionManager(getApplicationContext());
        user = session.getUserDetails();
        userid = user.get(session.KEY_PASSENGER_ID);
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
