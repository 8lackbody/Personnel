package com.zht.personnel.adapter;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Objects;

public class EPCTag {

    private String date;

    private String epc;

    private String name;

    private String status;

    private Integer alert;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setAlert(Integer alert) {
        this.alert = alert;
    }

    public Integer getAlert() {
        return alert;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public EPCTag(String date, String epc, String name, String status, Integer alert) {
        this.date = date;
        this.epc = epc;
        this.name = name;
        this.status = status;
        this.alert = alert;
    }

    public EPCTag() {
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EPCTag)) return false;
        EPCTag epcTag = (EPCTag) o;
        return Objects.equals(getEpc(), epcTag.getEpc());
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(getEpc());
    }
}
