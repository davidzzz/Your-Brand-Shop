package com.ushare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ushare.model.Feedback;

public class FeedbackDetailActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView teksNama, teksKomentar, teksWaktu;
    RatingBar rating1, rating2, rating3, rating4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("RESERVASI");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        teksNama = (TextView) findViewById(R.id.nama);
        teksKomentar = (TextView) findViewById(R.id.komentar);
        teksWaktu = (TextView) findViewById(R.id.waktu);
        rating1 = (RatingBar) findViewById(R.id.kelayakan_harga);
        rating2 = (RatingBar) findViewById(R.id.kebersihan);
        rating3 = (RatingBar) findViewById(R.id.pelayanan);
        rating4 = (RatingBar) findViewById(R.id.rasa);
        Feedback feedback = getIntent().getParcelableExtra("feedback");
        teksNama.setText(feedback.getNama());
        teksKomentar.setText(feedback.getKomentar());
        teksWaktu.setText(feedback.getWaktu());
        rating1.setRating(feedback.getRating1());
        rating2.setRating(feedback.getRating2());
        rating3.setRating(feedback.getRating3());
        rating4.setRating(feedback.getRating4());
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
