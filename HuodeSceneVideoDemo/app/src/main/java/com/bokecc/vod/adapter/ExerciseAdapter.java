package com.bokecc.vod.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.data.ExeQuestion;
import com.bokecc.vod.data.ExerciseAnswer;
import com.bokecc.vod.inter.BlankContentListener;
import com.bokecc.vod.inter.DoExerciseResult;
import com.bokecc.vod.utils.MultiUtils;
import com.bokecc.vod.view.AnswerScope;
import com.bokecc.vod.view.CompleteBlankTextView;
import com.bokecc.vod.view.FullShowListView;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends PagerAdapter {
    private int isAnswerRight = 2;
    private List<ExeQuestion> datas;
    private Context context;
    private LayoutInflater layoutInflater;
    private DoExerciseResult doExerciseResult;
    private String rightAnswer;

    public void setDoExerciseResult(DoExerciseResult doExerciseResult) {
        this.doExerciseResult = doExerciseResult;
    }

    public ExerciseAdapter(Context context, List<ExeQuestion> datas) {
        this.datas = datas;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = layoutInflater.inflate(R.layout.item_exercise, null);
        TextView tv_question_type = view.findViewById(R.id.tv_question_type);
        TextView tv_question = view.findViewById(R.id.tv_question);
        final ScrollView sv_exercise = view.findViewById(R.id.sv_exercise);
        final FullShowListView lv_answers = view.findViewById(R.id.lv_answers);
        final LinearLayout ll_commit = view.findViewById(R.id.ll_commit);
        final LinearLayout ll_question_explain = view.findViewById(R.id.ll_question_explain);
        final TextView tv_your_answer = view.findViewById(R.id.tv_your_answer);
        final TextView tv_right_answer = view.findViewById(R.id.tv_right_answer);
        final TextView tv_question_explain = view.findViewById(R.id.tv_question_explain);
        final CompleteBlankTextView fbt_question = view.findViewById(R.id.fbt_question);
        final Button btn_commit = view.findViewById(R.id.btn_commit);
        final Button btn_commit_blank = view.findViewById(R.id.btn_commit_blank);

        final ExeQuestion exeQuestion = datas.get(position);
        if (exeQuestion != null) {
            final List<ExerciseAnswer> answers = exeQuestion.getAnswers();
            final int type = exeQuestion.getType();
            if (type == 0) {
                tv_question_type.setText("单选");
                ll_commit.setVisibility(View.GONE);
                tv_question.setText(exeQuestion.getContent());
            } else if (type == 1) {
                tv_question_type.setText("多选");
                ll_commit.setVisibility(View.VISIBLE);
                btn_commit.setVisibility(View.VISIBLE);
                tv_question.setText(exeQuestion.getContent());
                for (ExerciseAnswer exerciseAnswer:answers){
                    exerciseAnswer.setMultiSelect(true);
                }
            } else if (type == 2) {
                tv_question_type.setText("填空");
                ll_commit.setVisibility(View.VISIBLE);
                btn_commit_blank.setVisibility(View.VISIBLE);
                tv_question.setVisibility(View.GONE);
                fbt_question.setVisibility(View.VISIBLE);
                ExerciseAnswer blankAnswer = exeQuestion.getAnswers().get(0);
                if (blankAnswer != null) {
                    rightAnswer = blankAnswer.getContent();
                }
                String content = exeQuestion.getContent();
                int content2Length = 0;
                String content2 = exeQuestion.getContent2();
                if (!TextUtils.isEmpty(content2)) {
                    content2Length = content2.length();
                }
                String blankQuestion = content + "________" + exeQuestion.getContent2();
                if (blankAnswer != null && !TextUtils.isEmpty(blankAnswer.getContent())) {
                    List<AnswerScope> ranges = new ArrayList<>();
                    if (!TextUtils.isEmpty(content)) {
                        ranges.add(new AnswerScope(content.length(), blankQuestion.length() - content2Length));
                        fbt_question.setData(blankQuestion, ranges);
                    }
                }
                lv_answers.setVisibility(View.INVISIBLE);
            }

            final ExerciseAnswerAdapter exerciseAnswerAdapter = new ExerciseAnswerAdapter(context, answers);
            lv_answers.setAdapter(exerciseAnswerAdapter);

            lv_answers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (type == 0) {
                        if (ll_question_explain.isShown()) {
                            MultiUtils.showToast((Activity) context, "你已回答此题");
                            return;
                        }
                        ExerciseAnswer item = (ExerciseAnswer) exerciseAnswerAdapter.getItem(position);
                        if (item.isRight()) {
                            isAnswerRight = 1;
                        } else {
                            isAnswerRight = 0;
                        }

                        for (int i = 0; i < answers.size(); i++) {
                            answers.get(i).setCommit(true);
                            if (i == position || answers.get(i).isRight()) {
                                answers.get(i).setSelected(true);
                            } else {
                                answers.get(i).setSelected(false);
                            }
                        }
                        exerciseAnswerAdapter.notifyDataSetChanged();
                        if (doExerciseResult != null) {
                            doExerciseResult.answerResult(exeQuestion.getId(), isAnswerRight);
                        }

                        ll_question_explain.setVisibility(View.VISIBLE);
                        String content = item.getContent();
                        if (!TextUtils.isEmpty(content)) {
                            String yourAnswer = content.substring(0, 1);
                            tv_your_answer.setText(yourAnswer);
                        }

                        for (ExerciseAnswer exerciseAnswer : answers) {
                            if (exerciseAnswer.isRight()) {
                                String rightAnswerContent = exerciseAnswer.getContent();
                                if (!TextUtils.isEmpty(rightAnswerContent)) {
                                    String rightAnswer = rightAnswerContent.substring(0, 1);
                                    tv_right_answer.setText(rightAnswer);
                                }
                            }
                        }

                        tv_question_explain.setText(exeQuestion.getExplainInfo());

                    }else if (type==1){
                        if (ll_question_explain.isShown()) {
                            MultiUtils.showToast((Activity) context, "你已回答此题");
                            return;
                        }
                        ExerciseAnswer exerciseAnswer = answers.get(position);
                        if (exerciseAnswer.isSelected()){
                            exerciseAnswer.setSelected(false);
                        }else {
                            exerciseAnswer.setSelected(true);
                        }
                        exerciseAnswerAdapter.notifyDataSetChanged();

                        int selectedCount = 0;
                        for (ExerciseAnswer answer:answers){
                            if (answer.isSelected()){
                                selectedCount++;
                            }
                        }

                        if (selectedCount>0){
                            btn_commit.setTextColor(context.getResources().getColor(R.color.visitor_info));
                            btn_commit.setBackgroundResource(R.drawable.commit_blank_corner_bac);
                        }else {
                            btn_commit.setTextColor(context.getResources().getColor(R.color.gray));
                            btn_commit.setBackgroundResource(R.drawable.light_gray_corner_bac);
                        }
                    }

                }
            });

            fbt_question.setBlankContentListener(new BlankContentListener() {
                @Override
                public void blankContent(String content) {
                    if (TextUtils.isEmpty(content)){
                        btn_commit_blank.setTextColor(context.getResources().getColor(R.color.gray));
                        btn_commit_blank.setBackgroundResource(R.drawable.light_gray_corner_bac);
                    }else {
                        btn_commit_blank.setTextColor(context.getResources().getColor(R.color.visitor_info));
                        btn_commit_blank.setBackgroundResource(R.drawable.commit_blank_corner_bac);
                    }
                }
            });
            //提交填空题
            btn_commit_blank.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<String> answerList = fbt_question.getAnswerList();
                    String fillContent = answerList.get(0);
                    if (TextUtils.isEmpty(fillContent)){
                        MultiUtils.showToast((Activity) context, "请填空");
                        return;
                    }
                    if (rightAnswer.equals(fillContent)){
                        isAnswerRight=1;
                        fbt_question.setAnswerRightColor();
                    }else {
                        isAnswerRight = 0;
                        fbt_question.setAnswerErrorColor();
                    }
                    if (doExerciseResult != null) {
                        doExerciseResult.answerResult(exeQuestion.getId(), isAnswerRight);
                    }
                    ll_commit.setVisibility(View.GONE);
                    ll_question_explain.setVisibility(View.VISIBLE);
                    tv_your_answer.setText(fillContent);
                    tv_right_answer.setText(rightAnswer);
                    tv_question_explain.setText(exeQuestion.getExplainInfo());
                    fbt_question.setCanClick(false);

                }
            });

            //提交多选题
            btn_commit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String yourAnswer = "";
                    for (ExerciseAnswer exerciseAnswer:answers){
                        if (exerciseAnswer.isSelected()){
                            String content = exerciseAnswer.getContent();
                            if (!TextUtils.isEmpty(content)){
                                String option = content.substring(0, 1);
                                if (TextUtils.isEmpty(yourAnswer)){
                                    yourAnswer = option;
                                }else {
                                    yourAnswer = yourAnswer + " " + option;
                                }
                            }
                        }

                    }

                    if (TextUtils.isEmpty(yourAnswer)){
                        MultiUtils.showToast((Activity) context, "请选择答案");
                        return;
                    }

                    for (ExerciseAnswer exerciseAnswer:answers){
                        exerciseAnswer.setCommit(true);
                    }
                    exerciseAnswerAdapter.notifyDataSetChanged();

                    String rightAnswers = "";
                    for (ExerciseAnswer exerciseAnswer:answers){
                        if (exerciseAnswer.isRight()){
                            String content = exerciseAnswer.getContent();
                            if (!TextUtils.isEmpty(content)){
                                String option = content.substring(0, 1);
                                if (TextUtils.isEmpty(rightAnswers)){
                                    rightAnswers = option;
                                }else {
                                    rightAnswers = rightAnswers + " " + option;
                                }
                            }
                        }

                    }

                    ll_commit.setVisibility(View.GONE);
                    ll_question_explain.setVisibility(View.VISIBLE);
                    tv_your_answer.setText(yourAnswer);
                    tv_right_answer.setText(rightAnswers);
                    tv_question_explain.setText(exeQuestion.getExplainInfo());
                    if (yourAnswer.equals(rightAnswers)){
                        isAnswerRight = 1;
                    }else {
                        isAnswerRight = 0;
                    }
                    if (doExerciseResult != null) {
                        doExerciseResult.answerResult(exeQuestion.getId(), isAnswerRight);
                    }
                }
            });
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }


}
