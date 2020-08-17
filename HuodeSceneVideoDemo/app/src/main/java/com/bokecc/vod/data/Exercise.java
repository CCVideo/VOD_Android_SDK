package com.bokecc.vod.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Exercise {
    private int id;
    private String title;
    private int showTime;
    private List<ExeQuestion> exeQuestions = new ArrayList<>();

    public Exercise(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getInt("id");
        title = jsonObject.getString("title");
        showTime = jsonObject.getInt("showTime");

        JSONArray exeQuestionArray = jsonObject.getJSONArray("questions");

        for (int i=0; i<exeQuestionArray.length(); i++) {
            ExeQuestion exeQuestion = new ExeQuestion(exeQuestionArray.getJSONObject(i));
            exeQuestions.add(exeQuestion);
        }

    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getShowTime() {
        return showTime;
    }

    public void setShowTime(int showTime) {
        this.showTime = showTime;
    }

    public List<ExeQuestion> getExeQuestions() {
        return exeQuestions;
    }

    public void setExeQuestions(List<ExeQuestion> exeQuestions) {
        this.exeQuestions = exeQuestions;
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", showTime=" + showTime +
                ", exeQuestions=" + exeQuestions +
                '}';
    }
}
