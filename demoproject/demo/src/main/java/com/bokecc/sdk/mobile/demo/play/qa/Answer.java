package com.bokecc.sdk.mobile.demo.play.qa;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cc on 2018/2/11.
 */

public class Answer {

    //{"id":1001,"content":"A、阿斯顿发送到发","right":false}

    private int id;
    private String content;
    private boolean right;

    public Answer(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getInt("id");
        content = jsonObject.getString("content");
        right = jsonObject.getBoolean("right");
    }

    @Override
    public String toString() {

        return "Answer{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", right=" + right +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }
}
