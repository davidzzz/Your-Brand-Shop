package com.guritadigital.shop;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.guritadigital.shop.adapter.TabAdapter;
import com.guritadigital.shop.model.ItemOrder;
import com.guritadigital.shop.util.Constant;
import com.guritadigital.shop.util.SessionManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class TabActivity extends AppCompatActivity {
    ViewPager pager;
    TabAdapter adapter;
    TabLayout tabs;
    Toolbar mToolbar;
    HashMap<String, String> user;
    String tipe, akses;
    OrderList orderList, historyList;
    FragOrderProses fragOrder;
    FragmentHistory fragHistory;
    List<ItemOrder> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
        tipe = getIntent().getStringExtra("tipe");
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(tipe.equals("history") ? "ORDER" : "VOUCHER");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Constant.COLOR));
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Constant.COLOR);
        } else {
            window.setTitleColor(Constant.COLOR);
        }
        LinearLayout layoutPoin = (LinearLayout) findViewById(R.id.layout_poin);
        SessionManager session = new SessionManager(getApplicationContext());
        user = session.getUserDetails();
        akses = user.get(SessionManager.KEY_AKSES);

        adapter = new TabAdapter(getSupportFragmentManager());
        if (tipe.equals("history")) {
            boolean isFlashDeal = getIntent().getBooleanExtra("isFlashDeal", false);
            layoutPoin.setVisibility(View.GONE);
            if (akses.equals("2")) {
                orderList = OrderList.newInstance("order", isFlashDeal);
                historyList = OrderList.newInstance("history", isFlashDeal);
                adapter.setFragment(orderList, "ORDER");
                adapter.setFragment(historyList, "HISTORY");
            } else {
                fragOrder = FragOrderProses.newInstance(isFlashDeal);
                fragHistory = FragmentHistory.newInstance(isFlashDeal);
                adapter.setFragment(fragOrder, "ORDER");
                adapter.setFragment(fragHistory, "HISTORY");
            }
        } else if (tipe.equals("voucher")) {
            adapter.setFragment(new VoucherList(), "VOUCHER");
            if (akses.equals("2")) {
                layoutPoin.setVisibility(View.GONE);
                adapter.setFragment(new OrderVoucher(), "ORDER VOUCHER");
            } else {
                layoutPoin.setVisibility(View.VISIBLE);
                TextView poin = (TextView) findViewById(R.id.poin);
                poin.setText("" + user.get(SessionManager.KEY_POIN));
                Button spin = (Button) findViewById(R.id.spin);
                spin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(TabActivity.this, SpinActivity.class));
                    }
                });
                adapter.setFragment(new MyVoucher(), "MY VOUCHER");
            }
        }
        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);

        tabs = (TabLayout)findViewById(R.id.tabs);
        tabs.setupWithViewPager(pager);
    }

    public class CompareNama implements Comparator<ItemOrder> {
        @Override
        public int compare(ItemOrder i1, ItemOrder i2) {
            return i1.getNama().compareToIgnoreCase(i2.getNama());
        }
    }

    public class CompareTanggal implements Comparator<ItemOrder> {
        @Override
        public int compare(ItemOrder i1, ItemOrder i2) {
            return i1.getTanggal().compareToIgnoreCase(i2.getTanggal());
        }
    }

    public class CompareStatus implements Comparator<ItemOrder> {
        @Override
        public int compare(ItemOrder i1, ItemOrder i2) {
            return i1.getStatus().compareToIgnoreCase(i2.getStatus());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (tipe.equals("history")) {
            getMenuInflater().inflate(R.menu.menu_order, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.nama || id == R.id.tanggal || id == R.id.status) {
            Comparator<ItemOrder> c;
            if (id == R.id.nama) {
                c = new CompareNama();
            } else if (id == R.id.tanggal) {
                c = new CompareTanggal();
            } else {
                c = new CompareStatus();
            }
            if (akses.equals("2")) {
                list = orderList.getListItem();
                Collections.sort(list, c);
                orderList.getAdapter().notifyDataSetChanged();
                list = historyList.getListItem();
                Collections.sort(list, c);
                historyList.getAdapter().notifyDataSetChanged();
            } else {
                list = fragOrder.getListItem();
                Collections.sort(list, c);
                fragOrder.getAdapter().notifyDataSetChanged();
                list = fragHistory.getListItem();
                Collections.sort(list, c);
                fragHistory.getAdapter().notifyDataSetChanged();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (tipe.equals("voucher")) {
            TextView poin = (TextView) findViewById(R.id.poin);
            poin.setText("" + user.get(SessionManager.KEY_POIN));
        }
    }
}
