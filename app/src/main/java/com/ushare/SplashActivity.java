package com.ushare;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;



/**
 * Created by liau on 5/12/16.
 */
public class SplashActivity extends Activity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private ImageView mLogo;
    private TextView welcomeText;
    private static final int SPLASH_DURATION = 5000; // 2 detik

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        mLogo = (ImageView) findViewById(R.id.logo);
        welcomeText = (TextView) findViewById(R.id.welcome_text);

        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        final boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

        if (isFirstStart) {
            //  Launch app intro
            Intent i = new Intent(SplashActivity.this, Intro.class);
            startActivity(i);
            finish();

            //  Make a new preferences editor
            SharedPreferences.Editor e = getPrefs.edit();

            //  Edit preference to make it false because we don't want this to run again
            e.putBoolean("firstStart", false);

            //  Apply changes
            e.apply();


        }else{

            start();
        }
    }

    private void start() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        },SPLASH_DURATION );

    }
}