package com.ushare;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ushare.model.Reservasi;
import com.ushare.util.Constant;

public class ReservasiDetailActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView teksNama, teksJumlah, teksNoHP, teksKeterangan, teksWaktu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservasi_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("RESERVASI");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        teksNama = (TextView) findViewById(R.id.nama);
        teksJumlah = (TextView) findViewById(R.id.jumlah);
        teksNoHP = (TextView) findViewById(R.id.noHP);
        teksKeterangan = (TextView) findViewById(R.id.keterangan);
        teksWaktu = (TextView) findViewById(R.id.waktu);
        Reservasi reservasi = getIntent().getParcelableExtra("reservasi");
        teksNama.setText(reservasi.getNama());
        teksJumlah.setText(reservasi.getJumlah() + " ORANG");
        teksNoHP.setText(reservasi.getNoHP());
        teksKeterangan.setText(reservasi.getKeterangan());
        teksWaktu.setText(reservasi.getWaktu());
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
