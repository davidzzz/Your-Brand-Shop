package com.ushare.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Reservasi implements Parcelable {
    private String nama, noHP, keterangan, waktu;
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

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public String getWaktu() {
        return waktu;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(nama);
        out.writeInt(jumlah);
        out.writeString(noHP);
        out.writeString(keterangan);
        out.writeString(waktu);
    }

    public static final Parcelable.Creator<Reservasi> CREATOR = new Parcelable.Creator<Reservasi>() {
        public Reservasi createFromParcel(Parcel in) {
            return new Reservasi(in);
        }

        public Reservasi[] newArray(int size) {
            return new Reservasi[size];
        }
    };

    private Reservasi(Parcel in) {
        nama = in.readString();
        jumlah = in.readInt();
        noHP = in.readString();
        keterangan = in.readString();
        waktu = in.readString();
    }
}
