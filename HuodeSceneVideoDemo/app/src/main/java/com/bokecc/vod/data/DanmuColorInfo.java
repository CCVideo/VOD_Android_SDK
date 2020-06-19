package com.bokecc.vod.data;

public class DanmuColorInfo {
    private int normalImgRes;
    private int selectdImgRes;
    private boolean isSelected = false;
    private String color;

    public int getNormalImgRes() {
        return normalImgRes;
    }

    public void setNormalImgRes(int normalImgRes) {
        this.normalImgRes = normalImgRes;
    }

    public int getSelectdImgRes() {
        return selectdImgRes;
    }

    public void setSelectdImgRes(int selectdImgRes) {
        this.selectdImgRes = selectdImgRes;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
