package com.bokecc.vod.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cc on 2018/2/11.
 */

public class ExerciseAnswer {

    //{"id":1001,"content":"A、阿斯顿发送到发","right":false}

    private int id;
    private String content;
    private boolean right;
    private boolean isSelected;
    private boolean isMultiSelect;
    private boolean isCommit;

    public ExerciseAnswer(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getInt("id");
        content = jsonObject.getString("content");
        right = jsonObject.getBoolean("right");
    }

    @Override
    public String toString() {
        return "ExerciseAnswer{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", right=" + right +
                ", isSelected=" + isSelected +
                '}';
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
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

    public boolean isMultiSelect() {
        return isMultiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        isMultiSelect = multiSelect;
    }

    public boolean isCommit() {
        return isCommit;
    }

    public void setCommit(boolean commit) {
        isCommit = commit;
    }
}
