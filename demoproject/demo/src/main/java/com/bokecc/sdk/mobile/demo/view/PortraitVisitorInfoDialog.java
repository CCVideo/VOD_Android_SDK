package com.bokecc.sdk.mobile.demo.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.model.VisitorInfo;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.play.InfoCollector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class PortraitVisitorInfoDialog extends Dialog {
    private Context context;
    private String visitorImageUrl,visitorJumpUrl,visitorTitle,visitorInfoId,videoId;
    private int visitorIsJump;
    private List<VisitorInfo> visitorInfos;
    private int size,currentPos=0;
    private String inputOne,inputTwo,inputThree,inputFour,inputFive,message;
    private List<String> inputInfos;
    private CommitOrJumpVisitorInfo commitOrJumpVisitorInfo;
    private TextView tv_info_title,tv_commit,tv_next_step,tv_last_step,tv_jump;
    private EditText et_input;

    public PortraitVisitorInfoDialog(@NonNull Context context, String videoId, String visitorImageUrl, String visitorJumpUrl, String visitorTitle, String visitorInfoId, int visitorIsJump, List<VisitorInfo> visitorInfos, CommitOrJumpVisitorInfo commitOrJumpVisitorInfo) {
        super(context, R.style.PortraitVisitorInfoDialogStyle);
        this.context = context;
        this.videoId = videoId;
        this.visitorImageUrl = visitorImageUrl;
        this.visitorJumpUrl = visitorJumpUrl;
        this.visitorTitle = visitorTitle;
        this.visitorInfoId = visitorInfoId;
        this.visitorIsJump = visitorIsJump;
        this.visitorInfos = visitorInfos;
        this.commitOrJumpVisitorInfo = commitOrJumpVisitorInfo;
        inputInfos = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_portrait_visitor_info, null);
        setContentView(view);

        final TextView tv_title = view.findViewById(R.id.tv_title);
        tv_info_title = view.findViewById(R.id.tv_info_title);
        tv_commit = view.findViewById(R.id.tv_commit);
        tv_next_step = view.findViewById(R.id.tv_next_step);
        tv_last_step = view.findViewById(R.id.tv_last_step);
        tv_jump = view.findViewById(R.id.tv_jump);
        et_input = view.findViewById(R.id.et_input);

        tv_title.setText(visitorTitle);
        size = visitorInfos.size();
        if (visitorIsJump==1){
            tv_jump.setVisibility(View.VISIBLE);
        }
        if (size==1){
            tv_commit.setVisibility(View.VISIBLE);
        }else {
            tv_next_step.setVisibility(View.VISIBLE);
        }
        if (size>0){
            tv_info_title.setText(visitorInfos.get(0).getVisitorMes());
            et_input.setHint(visitorInfos.get(0).getVisitorTip());
        }

        tv_next_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentPos==0){
                    inputOne = getInput(et_input);
                    if (TextUtils.isEmpty(inputOne)){
                        showToast(visitorInfos.get(currentPos));
                        return;
                    }
                    tv_jump.setVisibility(View.GONE);
                    tv_last_step.setVisibility(View.VISIBLE);
                    controlNext();
                }else if (currentPos==1){
                    inputTwo = getInput(et_input);
                    if (TextUtils.isEmpty(inputTwo)){
                        showToast(visitorInfos.get(currentPos));
                        return;
                    }
                    controlNext();
                }else if (currentPos==2){
                    inputThree = getInput(et_input);
                    if (TextUtils.isEmpty(inputThree)){
                        showToast(visitorInfos.get(currentPos));
                        return;
                    }
                    controlNext();
                }else if (currentPos==3){
                    inputFour = getInput(et_input);
                    if (TextUtils.isEmpty(inputFour)){
                        showToast(visitorInfos.get(currentPos));
                        return;
                    }
                    controlNext();
                }else if (currentPos==4){
                    inputFive = getInput(et_input);
                    if (TextUtils.isEmpty(inputFive)){
                        showToast(visitorInfos.get(currentPos));
                        return;
                    }
                    controlNext();
                }

            }
        });

        tv_last_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEndInput();
                if (currentPos==4){
                    et_input.setText(inputFour);
                    controlLast();
                }else if (currentPos==3){
                    et_input.setText(inputThree);
                    controlLast();
                }else if (currentPos==2){
                    et_input.setText(inputTwo);
                    controlLast();
                }else if (currentPos==1){
                    et_input.setText(inputOne);
                    tv_last_step.setVisibility(View.GONE);
                    controlLast();
                }
            }
        });

        tv_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPos==0){
                    inputOne = getInput(et_input);
                    if (TextUtils.isEmpty(inputOne)){
                        showToast(visitorInfos.get(currentPos));
                        return;
                    }
                }else if (currentPos==1){
                    inputTwo = getInput(et_input);
                    if (TextUtils.isEmpty(inputTwo)){
                        showToast(visitorInfos.get(currentPos));
                        return;
                    }
                }else if (currentPos==2){
                    inputThree = getInput(et_input);
                    if (TextUtils.isEmpty(inputThree)){
                        showToast(visitorInfos.get(currentPos));
                        return;
                    }
                }else if (currentPos==3){
                    inputFour = getInput(et_input);
                    if (TextUtils.isEmpty(inputFour)){
                        showToast(visitorInfos.get(currentPos));
                        return;
                    }
                }else if (currentPos==4){
                    inputFive = getInput(et_input);
                    if (TextUtils.isEmpty(inputFive)){
                        showToast(visitorInfos.get(currentPos));
                        return;
                    }
                }
                inputInfos.add(inputOne);
                inputInfos.add(inputTwo);
                inputInfos.add(inputThree);
                inputInfos.add(inputFour);
                inputInfos.add(inputFive);

                try {
                    JSONArray jsonArray = new JSONArray();
                    for (int i=0;i<visitorInfos.size();i++){
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("collector", visitorInfos.get(i).getVisitorMes());
                        jsonObject.put("collectorMes", inputInfos.get(i));
                        jsonArray.put(jsonObject);
                    }
                    message = jsonArray.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                InfoCollector.reportVisitInfo(visitorInfoId,videoId,ConfigUtil.USERID,message);
                if (commitOrJumpVisitorInfo!=null){
                    dismiss();
                    commitOrJumpVisitorInfo.commit();
                }

            }
        });

        tv_jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (commitOrJumpVisitorInfo!=null){
                    dismiss();
                    commitOrJumpVisitorInfo.jump();
                }
            }
        });


        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 1.0);
        lp.height = (int) (d.heightPixels * 0.4);
        dialogWindow.setGravity(Gravity.TOP);
        dialogWindow.setAttributes(lp);
    }

    private void getEndInput() {
        if (currentPos==0){
            inputOne = getInput(et_input);
        }else if (currentPos==1){
            inputTwo = getInput(et_input);
        }else if (currentPos==2){
            inputThree = getInput(et_input);
        }else if (currentPos==3){
            inputFour = getInput(et_input);
        }else if (currentPos==4){
            inputFive = getInput(et_input);
        }
    }

    private void controlLast() {
        currentPos = currentPos -1;
        tv_info_title.setText(visitorInfos.get(currentPos).getVisitorMes());
        tv_next_step.setVisibility(View.VISIBLE);
        tv_commit.setVisibility(View.GONE);
    }

    private void controlNext() {
        currentPos = currentPos + 1;
        tv_info_title.setText(visitorInfos.get(currentPos).getVisitorMes());
        if (currentPos==1 && !TextUtils.isEmpty(inputTwo)){
            et_input.setText(inputTwo);
        }else if (currentPos==2 && !TextUtils.isEmpty(inputThree)){
            et_input.setText(inputThree);
        }else if (currentPos==3 && !TextUtils.isEmpty(inputFour)){
            et_input.setText(inputFour);
        }else if (currentPos==4 && !TextUtils.isEmpty(inputFive)){
            et_input.setText(inputFive);
        }else {
            et_input.setText("");
            et_input.setHint(visitorInfos.get(currentPos).getVisitorTip());
        }

        Log.i("visitorinfo", "当前位置：" +currentPos+"  问题个数"+ size);
        if (currentPos==(size-1)){
            tv_next_step.setVisibility(View.GONE);
            tv_commit.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(VisitorInfo visitorInfo) {
        Toast.makeText(context,visitorInfo.getVisitorTip(),Toast.LENGTH_SHORT).show();
    }


    private String getInput(EditText editText){
        return editText.getText().toString().trim().replace(" ", "");
    }

}
