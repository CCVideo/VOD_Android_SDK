package com.bokecc.sdk.mobile.demo.model;

import org.json.JSONException;
import org.json.JSONObject;

public class VisitorInfo {
    private String visitorMes;
    private String visitorTip;

    public VisitorInfo(JSONObject jsonObject) throws JSONException{
        visitorMes = jsonObject.getString("visitorMes");
        visitorTip = jsonObject.getString("visitorTip");
    }


    public String getVisitorMes() {
        return visitorMes;
    }

    public void setVisitorMes(String visitorMes) {
        this.visitorMes = visitorMes;
    }

    public String getVisitorTip() {
        return visitorTip;
    }

    public void setVisitorTip(String visitorTip) {
        this.visitorTip = visitorTip;
    }

    @Override
    public String toString() {
        return "VisitorInfo{" +
                "visitorMes='" + visitorMes + '\'' +
                ", visitorTip='" + visitorTip + '\'' +
                '}';
    }
}
