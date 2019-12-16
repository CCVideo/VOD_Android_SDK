package com.bokecc.vod.view;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.inter.BlankContentListener;

import java.util.ArrayList;
import java.util.List;


public class CompleteBlankTextView extends RelativeLayout {

    private TextView tvContent;
    private EditText et_input;
    private Context context;

    private boolean isCanClick = true;

    private List<String> answerList;

    private List<AnswerScope> rangeList;

    private SpannableStringBuilder content;

    public CompleteBlankTextView(Context context) {
        this(context, null);
    }

    public CompleteBlankTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CompleteBlankTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_fill_blank, this);

        tvContent = findViewById(R.id.tv_content);
        et_input = findViewById(R.id.et_input);
    }


    public void setData(String originContent, List<AnswerScope> answerRangeList) {
        if (TextUtils.isEmpty(originContent) || answerRangeList == null
                || answerRangeList.isEmpty()) {
            return;
        }


        content = new SpannableStringBuilder(originContent);

        rangeList = answerRangeList;


        for (AnswerScope range : rangeList) {
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#55B1FF"));
            content.setSpan(colorSpan, range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        answerList = new ArrayList<>();
        for (int i = 0; i < rangeList.size(); i++) {
            answerList.add("");
        }


        for (int i = 0; i < rangeList.size(); i++) {
            AnswerScope range = rangeList.get(i);
            BlankClickableSpan blankClickableSpan = new BlankClickableSpan(i);
            content.setSpan(blankClickableSpan, range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        tvContent.setMovementMethod(LinkMovementMethod.getInstance());
        tvContent.setText(content);
    }

    public boolean isCanClick() {
        return isCanClick;
    }

    public void setCanClick(boolean canClick) {
        isCanClick = canClick;
    }

    /**
     * 点击事件
     */
    class BlankClickableSpan extends ClickableSpan {

        private int position;

        public BlankClickableSpan(int position) {
            this.position = position;
        }

        @Override
        public void onClick(final View widget) {
            if (!isCanClick){
                return;
            }
            et_input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // 填写答案
                    String answer = et_input.getText().toString();
                    fillAnswer(answer, position);
                    if (blankContentListener!=null){
                        blankContentListener.blankContent(answer);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            et_input.requestFocus();

            InputMethodManager inputMethodManager =
                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }

        @Override
        public void updateDrawState(TextPaint ds) {

            ds.setUnderlineText(false);
        }
    }


    private void fillAnswer(String answer, int position) {
        answer = " " + answer + " ";


        AnswerScope range = rangeList.get(position);
        content.replace(range.start, range.end, answer);


        AnswerScope currentRange = new AnswerScope(range.start, range.start + answer.length());
        rangeList.set(position, currentRange);


        content.setSpan(new UnderlineSpan(),
                currentRange.start, currentRange.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        answerList.set(position, answer.replace(" ", ""));


        tvContent.setText(content);

        for (int i = 0; i < rangeList.size(); i++) {
            if (i > position) {

                AnswerScope oldNextRange = rangeList.get(i);
                int oldNextAmount = oldNextRange.end - oldNextRange.start;

                int difference = currentRange.end - range.end;


                AnswerScope nextRange = new AnswerScope(oldNextRange.start + difference,
                        oldNextRange.start + difference + oldNextAmount);
                rangeList.set(i, nextRange);
            }
        }
    }


    public List<String> getAnswerList() {
        return answerList;
    }

    BlankContentListener blankContentListener;

    public BlankContentListener getBlankContentListener() {
        return blankContentListener;
    }

    public void setBlankContentListener(BlankContentListener blankContentListener) {
        this.blankContentListener = blankContentListener;
    }

    public void setAnswerRightColor(){
        for (AnswerScope range : rangeList) {
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#8BC04B"));
            content.setSpan(colorSpan, range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvContent.setText(content);
        }
    }

    public void setAnswerErrorColor(){
        for (AnswerScope range : rangeList) {
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#E44F5A"));
            content.setSpan(colorSpan, range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
            content.setSpan(strikethroughSpan, range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvContent.setText(content);
        }
    }
}
