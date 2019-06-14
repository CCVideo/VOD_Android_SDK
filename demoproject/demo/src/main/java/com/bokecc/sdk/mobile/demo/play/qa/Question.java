package com.bokecc.sdk.mobile.demo.play.qa;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Question {

    public Question(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getInt("id");
        content = jsonObject.getString("content");
        showTime = jsonObject.getInt("showTime");
        explainInfo = jsonObject.getString("explainInfo");
        jump = jsonObject.getBoolean("jump");
        backSecond = jsonObject.getInt("backSecond");
        if (jsonObject.has("keepPlay")){
            keepPlay = jsonObject.getBoolean("keepPlay");
        }

        JSONArray answerArray = jsonObject.getJSONArray("answers");

        int rightAnswerCount = 0;

        for (int i=0; i<answerArray.length(); i++) {
            Answer answer = new Answer(answerArray.getJSONObject(i));
            if (answer.isRight()) {
                rightAnswerCount++;
            }
            answers.add(answer);
        }

        if (rightAnswerCount > 1) {
            isMultiAnswer = true;
        }

    }

    /**
     * id : 99
     * content : 阿大是打发斯蒂芬
     * showTime : 2
     * explainInfo : 阿斯顿发送到
     * jump : false
     * backSecond : -1
     * answers : [{"id":1001,"content":"A、阿斯顿发送到发","right":false},{"id":1002,
     * "content":"B、阿斯顿发送到发三","right":true},{"id":1003,"content":"C、发的发送到","right":false},
     * {"id":1004,"content":"D、防辐射","right":false}]
     */

    private int id;
    private String content;
    private int showTime;
    private String explainInfo;
    private boolean jump;
    private boolean keepPlay;
    private int backSecond;

    private boolean isAnsweredFlag;
    public boolean isMultiAnswer;

    public void setAnsweredFlag(boolean answeredFlag) {
        isAnsweredFlag = answeredFlag;
    }

    @Override
    public String toString() {

        return "Question{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", showTime=" + showTime +
                ", explainInfo='" + explainInfo + '\'' +
                ", jump=" + jump +
                ", keepPlay=" + keepPlay +
                ", backSecond=" + backSecond +
                ", answers=" + answers +
                '}';
    }

    private List<Answer> answers = new ArrayList<>();

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

    public int getShowTime() {
        return showTime;
    }

    public void setShowTime(int showTime) {
        this.showTime = showTime;
    }

    public String getExplainInfo() {
        return explainInfo;
    }

    public void setExplainInfo(String explainInfo) {
        this.explainInfo = explainInfo;
    }

    public boolean isJump() {
        return jump;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public boolean isKeepPlay() {
        return keepPlay;
    }

    public void setKeepPlay(boolean keepPlay) {
        this.keepPlay = keepPlay;
    }

    public int getBackSecond() {
        return backSecond;
    }

    public void setBackSecond(int backSecond) {
        this.backSecond = backSecond;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public boolean isAnsweredFlag() {
        return isAnsweredFlag;
    }

    public boolean isMultiAnswer() {
        return isMultiAnswer;
    }
}
