package com.ushare.model;

public class Reservasi {
    private String nama, noHP, waktu;
    private int jumlah;

    public Reservasi() {}

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getNama() {
        return nama;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setNoHP(String noHP) {
        this.noHP = noHP;
    }

    public String getNoHP() {
        return noHP;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public String getWaktu() {
        return waktu;
    }
}
