package com.bokecc.vod.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ExeQuestion {

    private String explainInfo;
    private int id;
    private int type;
    private String content;
    private String content2;
    private boolean isMultiAnswer;
    private List<ExerciseAnswer> answers = new ArrayList<>();


    public ExeQuestion(JSONObject jsonObject) throws JSONException {
        explainInfo = jsonObject.getString("explainInfo");
        id = jsonObject.getInt("id");
        type = jsonObject.getInt("type");
        content = jsonObject.getString("content");
        if (jsonObject.has("content2")){
            content2 = jsonObject.getString("content2");
        }
        JSONArray answerArray = jsonObject.getJSONArray("answers");

        int rightAnswerCount = 0;

        for (int i=0; i<answerArray.length(); i++) {
            ExerciseAnswer answer = new ExerciseAnswer(answerArray.getJSONObject(i));
            if (answer.isRight()) {
                rightAnswerCount++;
            }
            answers.add(answer);
        }

        if (rightAnswerCount > 1) {
            isMultiAnswer = true;
        }

    }

    public String getExplainInfo() {
        return explainInfo;
    }

    public void setExplainInfo(String explainInfo) {
        this.explainInfo = explainInfo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent2() {
        return content2;
    }

    public void setContent2(String content2) {
        this.content2 = content2;
    }

    public boolean isMultiAnswer() {
        return isMultiAnswer;
    }

    public void setMultiAnswer(boolean multiAnswer) {
        isMultiAnswer = multiAnswer;
    }

    public List<ExerciseAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ExerciseAnswer> answers) {
        this.answers = answers;
    }

    @Override
    public String toString() {
        return "ExeQuestion{" +
                "explainInfo='" + explainInfo + '\'' +
                ", id=" + id +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", content2='" + content2 + '\'' +
                ", isMultiAnswer=" + isMultiAnswer +
                ", answers=" + answers +
                '}';
    }
}
