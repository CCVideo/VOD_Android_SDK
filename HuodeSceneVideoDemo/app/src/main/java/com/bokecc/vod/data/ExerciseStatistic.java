package com.bokecc.vod.data;

import org.json.JSONException;
import org.json.JSONObject;


public class ExerciseStatistic {
    private int questionId;
    private int accuracy;
    private boolean isAnswerRight;

    public ExerciseStatistic(JSONObject jsonObject) throws JSONException {
        questionId = jsonObject.getInt("questionId");
        accuracy = jsonObject.getInt("accuracy");
    }

    @Override
    public String toString() {
        return "ExerciseStatistic{" +
                "questionId=" + questionId +
                ", accuracy=" + accuracy +
                ", isAnswerRight=" + isAnswerRight +
                '}';
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public boolean isAnswerRight() {
        return isAnswerRight;
    }

    public void setAnswerRight(boolean answerRight) {
        isAnswerRight = answerRight;
    }
}
