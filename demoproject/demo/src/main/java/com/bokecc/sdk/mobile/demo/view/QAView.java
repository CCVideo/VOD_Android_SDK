package com.bokecc.sdk.mobile.demo.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.play.qa.Answer;
import com.bokecc.sdk.mobile.demo.play.qa.Question;
import com.bokecc.sdk.mobile.demo.play.qa.UrlImageSpan;
import com.bokecc.sdk.mobile.demo.util.ParamsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cc on 2018/2/24.
 * <p>
 * 问答展示类
 */

public class QAView {

    Context mContext;
    View qa_question_view;

    public QAView(Context context) {
        this.mContext = context;
        initPopupWindow();
    }

    PopupWindow mPopupWindow;
    private View mRootView;

    private void initPopupWindow() {
        mPopupWindow = new PopupWindow(mContext);
        mPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPopupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        mRootView = LayoutInflater.from(mContext).inflate(R.layout.qa_layout, null);
        qa_question_view = LayoutInflater.from(mContext).inflate(R.layout.qa_question_view, null);
        initView();

        mPopupWindow.setContentView(mRootView);
//        mPopupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.indicator_bg));
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mPopupWindow.setOutsideTouchable(true);
//        mPopupWindow.setFocusable(false);
    }

    Question question;
    private SpannableString spannableString;

    /**
     * 设置需要展示的question
     *
     * @param question
     */
    public void setQuestion(Question question) {
        this.question = question;
        isRight = false;
        isOneChecked = false;

        rlQAResult.setVisibility(View.GONE);
        String content = question.getContent();
        if (!TextUtils.isEmpty(content)) {
            spannableString = new SpannableString(content);
            Pattern pattern = Pattern.compile("\\{[^\\}]+\\}");
            final Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                UrlImageSpan imageSpan;
                String group = matcher.group();
                if (group.contains("http")) {
                    String url = group.substring(1, group.length() - 1);
                    imageSpan = new UrlImageSpan(mContext, url, tvQAContent);
                    spannableString.setSpan(imageSpan, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            tvQAContent.setText(spannableString);
        } else {
            tvQAContent.setText(content);
        }

        ll_container.removeAllViews();
        if (question.isMultiAnswer()) {
            ll_container.addView(qa_question_view);
            ll_container.addView(getMultiAnswerLayout(question.getAnswers()));
        } else {
            ll_container.addView(qa_question_view);
            ll_container.addView(getSingleAnswerLayout(question.getAnswers()));
        }

        if (question.isJump()) {
            tvQAJump.setBackgroundColor(0xe2419bf9);
            tvQAJump.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isRight = true;
                    dismiss();
                }
            });
        } else {
            tvQAJump.setBackgroundColor(0xff9198a3);
            tvQAJump.setOnClickListener(null);
        }

    }

    // 针对多选，记录当前答案是否是正确的，如果不选择同时答案是false，那么认为不选择就是正确答案
    class AnswerWrapper {
        public boolean isRight;
        public Answer answer;

        public AnswerWrapper(Answer answer) {
            this.answer = answer;
            isRight = (false == answer.isRight());
        }
    }

    List<AnswerWrapper> answerWrappers = new ArrayList<>();

    private boolean isRight; // 当前选择的答案是否正确
    private boolean isOneChecked; // 是否有一个选项被选择

    //生成多选的界面布局
    final int choiceDrawableWidth = 16;

    private LinearLayout getMultiAnswerLayout(List<Answer> answerList) {
        answerWrappers.clear();

        LinearLayout rootLayout = new LinearLayout(mContext); // container使用LinearLayout
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(layoutParams);

        int topMargin = ParamsUtil.dpToPx(mContext, 1);
        int width = ParamsUtil.dpToPx(mContext, choiceDrawableWidth);

        for (Answer answer : answerList) {

            final AnswerWrapper answerWrapper = new AnswerWrapper(answer);
            answerWrappers.add(answerWrapper);

            final RadioButton radioButton = new RadioButton(mContext);

            Drawable mDrawable = mContext.getResources().getDrawable(R.drawable.qa_multi_select_drawable);
            mDrawable.setBounds(0, 0, width, width);
            radioButton.setButtonDrawable(null);

            LinearLayout.LayoutParams radioButtonLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            radioButtonLayoutParams.setMargins(0, topMargin, 0, 0);
            radioButton.setLayoutParams(radioButtonLayoutParams);

            radioButton.setCompoundDrawables(mDrawable, null, null, null);
            //            展示答案内容
            String content = answer.getContent();
            if (!TextUtils.isEmpty(content)) {
                SpannableString spannableString = new SpannableString(content);
                Pattern pattern = Pattern.compile("\\{[^\\}]+\\}");
                final Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    UrlImageSpan imageSpan;
                    String group = matcher.group();
                    if (group.contains("http")) {
                        String url = group.substring(1, group.length() - 1);
                        imageSpan = new UrlImageSpan(mContext, url, radioButton);
                        spannableString.setSpan(imageSpan, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                radioButton.setText(spannableString);
            } else {
                radioButton.setText(answer.getContent());
            }
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            radioButton.setTextColor(0xff666666);
            radioButton.setCompoundDrawablePadding(ParamsUtil.dpToPx(mContext, 5));
            radioButton.setOnClickListener(new View.OnClickListener() {
                GlobalValue globalValue = new GlobalValue();

                @Override
                public void onClick(View v) {
                    isOneChecked = true;

                    boolean isCheck = globalValue.isCheck();
                    if (isCheck) {
                        radioButton.setChecked(false);
                        answerWrapper.isRight = (answerWrapper.answer.isRight() == false);
                    } else {
                        radioButton.setChecked(true);
                        answerWrapper.isRight = (answerWrapper.answer.isRight() == true);
                    }

                    globalValue.setCheck(!isCheck);
                }
            });

            rootLayout.addView(radioButton);
        }

        return rootLayout;
    }

    // 由于多选需要单击取消选择且RadioButton不支持此操作，故额外添加一个类来记录状态
    private class GlobalValue {
        public boolean isCheck() {
            return isCheck;
        }

        public void setCheck(boolean check) {
            isCheck = check;
        }

        private boolean isCheck;
    }

    // 获取单选布局
    private RadioGroup getSingleAnswerLayout(List<Answer> answerList) {
        RadioGroup rootLayout = new RadioGroup(mContext);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.MATCH_PARENT);
        rootLayout.setOrientation(RadioGroup.VERTICAL);
        rootLayout.setLayoutParams(layoutParams);

        int topMargin = ParamsUtil.dpToPx(mContext, 1);
        int width = ParamsUtil.dpToPx(mContext, choiceDrawableWidth);

        for (int i = 0; i < answerList.size(); i++) {
            final Answer answer = answerList.get(i);
            RadioButton radioButton = new RadioButton(mContext);
            Drawable mDrawable = mContext.getResources().getDrawable(R.drawable.qa_single_select_drawable);
            mDrawable.setBounds(0, 0, width, width);
            radioButton.setButtonDrawable(null);

            RadioGroup.LayoutParams radioButtonLayoutParams = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);

            radioButtonLayoutParams.setMargins(0, topMargin, 0, 0);
            radioButton.setLayoutParams(radioButtonLayoutParams);

            radioButton.setCompoundDrawables(mDrawable, null, null, null);
//            展示答案内容
            String content = answer.getContent();
            if (!TextUtils.isEmpty(content)) {
                SpannableString spannableString = new SpannableString(content);
                Pattern pattern = Pattern.compile("\\{[^\\}]+\\}");
                final Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    UrlImageSpan imageSpan;
                    String group = matcher.group();
                    if (group.contains("http")) {
                        String url = group.substring(1, group.length() - 1);
                        imageSpan = new UrlImageSpan(mContext, url, radioButton);
                        spannableString.setSpan(imageSpan, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                radioButton.setText(spannableString);
            } else {
                radioButton.setText(answer.getContent());
            }


            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            radioButton.setTextColor(0xff666666);
            radioButton.setCompoundDrawablePadding(ParamsUtil.dpToPx(mContext, 5));

            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isOneChecked = true;

                    if (isChecked) {
                        if (answer.isRight()) {
                            isRight = true;
                        } else {
                            isRight = false;
                        }
                    }
                }
            });

            rootLayout.addView(radioButton);
        }


        return rootLayout;
    }

    /**
     * 展示问答界面
     *
     * @param view
     */
    public void show(View view) {
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    /**
     * 隐藏问答界面
     */
    public void dismiss() {
        mPopupWindow.dismiss();
    }

    /**
     * 界面是否显示
     *
     * @return
     */
    public boolean isPopupWindowShown() {
        return mPopupWindow.isShowing();
    }

    /**
     * 设置隐藏监听器
     *
     * @param qaViewDismissListener
     */
    public void setQAViewDismissListener(final QAViewDismissListener qaViewDismissListener) {
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (qaViewDismissListener != null) {
                    qaViewDismissListener.onDismiss(isRight);
                }
            }
        });
    }

    TextView tvQAContent, tvQAJump, tvQASubmit;
    ScrollView svAnswers;

    RelativeLayout rlQAResult;
    TextView tvExplainInfo, tvResultTitle, tvResultReturn;
    ImageView ivResultBgFlag;
    LinearLayout ll_container;


    private void initView() {
        tvQAContent = qa_question_view.findViewById(R.id.qa_content_tv);
        tvQAJump = findViewById(R.id.qa_jump_tv);
        tvQASubmit = findViewById(R.id.qa_submit_tv);
        tvQASubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOneChecked) {
                    showResult();
                } else {
                    Toast.makeText(mContext, "请选择答案", Toast.LENGTH_SHORT).show();
                }
            }
        });

        svAnswers = findViewById(R.id.answers_layout_sv);
        ll_container = findViewById(R.id.ll_container);

        rlQAResult = findViewById(R.id.qa_result_rl);
        tvExplainInfo = findViewById(R.id.qa_explain_info_tv);
        tvResultTitle = findViewById(R.id.qa_result_title_tv);
        tvResultReturn = findViewById(R.id.qa_result_return_tv);
        ivResultBgFlag = findViewById(R.id.qa_result_bg_flag_iv);

        tvResultReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void showResult() {

        if (question.isMultiAnswer()) {
            isRight = getMultiRight();
        }

        rlQAResult.setVisibility(View.VISIBLE);
        tvResultReturn.setText("确认");
        tvExplainInfo.setText(question.getExplainInfo());

        if (isRight) {
            tvResultTitle.setText("回答正确");
            tvResultTitle.setTextColor(0xff17bc2f);
            ivResultBgFlag.setImageDrawable(mContext.getResources().getDrawable(R.drawable.qa_result_right));
        } else {
            tvResultTitle.setText("回答错误");
            tvResultTitle.setTextColor(0xffe03a3a);
            ivResultBgFlag.setImageDrawable(mContext.getResources().getDrawable(R.drawable.qa_result_wrong));

            if (question.getBackSecond() > 0) {
                tvResultReturn.setText("返回");
            }
        }
    }

    private boolean getMultiRight() {
        for (AnswerWrapper answerWrapper : answerWrappers) {
            if (!answerWrapper.isRight) {
                return false;
            }
        }

        return true;
    }

    private <T extends View> T findViewById(int id) {
        return mRootView.findViewById(id);
    }

    /**
     * QAView界面隐藏监听器
     */
    public interface QAViewDismissListener {
        /**
         * 隐藏回调
         *
         * @param isRight 用户是否选择了正确答案
         */
        void onDismiss(boolean isRight);
    }
}