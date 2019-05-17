package com.bokecc.sdk.mobile.demo.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.PlayDemoApplication;
import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.model.VisitorInfo;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.ParamsUtil;
import com.bokecc.sdk.mobile.play.InfoCollector;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class LandscapeVisitorInfoDialog extends Dialog {
    private Context context;
    private String visitorImageUrl,visitorJumpUrl,visitorTitle,visitorInfoId,videoId;
    private int visitorIsJump;
    private List<VisitorInfo> visitorInfos;
    private int size;
    private String message;
    private List<String> inputInfos;
    private CommitOrJumpVisitorInfo commitOrJumpVisitorInfo;

    public LandscapeVisitorInfoDialog(@NonNull Context context, String videoId, String visitorImageUrl, String visitorJumpUrl, String visitorTitle, String visitorInfoId, int visitorIsJump, List<VisitorInfo> visitorInfos, CommitOrJumpVisitorInfo commitOrJumpVisitorInfo) {
        super(context, R.style.CustomDialogInputVerifyCode);
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
        View view = inflater.inflate(R.layout.dialog_visitor_info, null);
        setContentView(view);

        ImageView iv_head_img = view.findViewById(R.id.iv_head_img);
        LinearLayout ll_info_one = view.findViewById(R.id.ll_info_one);
        LinearLayout ll_info_two = view.findViewById(R.id.ll_info_two);
        LinearLayout ll_info_three = view.findViewById(R.id.ll_info_three);
        LinearLayout ll_info_four = view.findViewById(R.id.ll_info_four);
        LinearLayout ll_info_five = view.findViewById(R.id.ll_info_five);
        TextView tv_title_one = view.findViewById(R.id.tv_title_one);
        TextView tv_title_two = view.findViewById(R.id.tv_title_two);
        TextView tv_title_three = view.findViewById(R.id.tv_title_three);
        TextView tv_title_four = view.findViewById(R.id.tv_title_four);
        TextView tv_title_five = view.findViewById(R.id.tv_title_five);
        final EditText et_tip_one = view.findViewById(R.id.et_tip_one);
        final EditText et_tip_two = view.findViewById(R.id.et_tip_two);
        final EditText et_tip_three = view.findViewById(R.id.et_tip_three);
        final EditText et_tip_four = view.findViewById(R.id.et_tip_four);
        final EditText et_tip_five = view.findViewById(R.id.et_tip_five);
        TextView tv_commit = view.findViewById(R.id.tv_commit);
        TextView tv_jump = view.findViewById(R.id.tv_jump);
        if (visitorIsJump==1){
            tv_jump.setVisibility(View.VISIBLE);
        }

        iv_head_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(visitorImageUrl)){
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri uri = Uri.parse(visitorImageUrl);
                    intent.setData(uri);
                    context.startActivity(intent);
                }
            }
        });
        if (visitorInfos!=null && visitorInfos.size()>0){
            size = visitorInfos.size();
            if (size ==1){
                showInfo(ll_info_one, tv_title_one, et_tip_one,0);
            }else if (size ==2){
                showInfo(ll_info_one, tv_title_one, et_tip_one,0);
                showInfo(ll_info_two, tv_title_two, et_tip_two,1);
            }else if (size ==3){
                showInfo(ll_info_one, tv_title_one, et_tip_one,0);
                showInfo(ll_info_two, tv_title_two, et_tip_two,1);
                showInfo(ll_info_three, tv_title_three, et_tip_three,2);
            }else if (size ==4){
                showInfo(ll_info_one, tv_title_one, et_tip_one,0);
                showInfo(ll_info_two, tv_title_two, et_tip_two,1);
                showInfo(ll_info_three, tv_title_three, et_tip_three,2);
                showInfo(ll_info_four, tv_title_four, et_tip_four,3);
            }else if (size ==5){
                showInfo(ll_info_one, tv_title_one, et_tip_one,0);
                showInfo(ll_info_two, tv_title_two, et_tip_two,1);
                showInfo(ll_info_three, tv_title_three, et_tip_three,2);
                showInfo(ll_info_four, tv_title_four, et_tip_four,3);
                showInfo(ll_info_five, tv_title_five, et_tip_five,4);
            }
        }
        if (TextUtils.isEmpty(visitorImageUrl)){
            iv_head_img.setImageResource(R.drawable.iv_visitor_info_head_img);
        }else {
            Glide.with(PlayDemoApplication.getContext()).load(visitorImageUrl).into(iv_head_img);
        }

        tv_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputOne = getInput(et_tip_one);
                String inputTwo = getInput(et_tip_two);
                String inputThree = getInput(et_tip_three);
                String inputFour = getInput(et_tip_four);
                String inputFive = getInput(et_tip_five);
                if (size==1){
                    if (TextUtils.isEmpty(inputOne)){
                        showToast(visitorInfos.get(0));
                        return;
                    }
                }else if (size==2){
                    if (TextUtils.isEmpty(inputOne)){
                        showToast(visitorInfos.get(0));
                        return;
                    }
                    if (TextUtils.isEmpty(inputTwo)){
                        showToast(visitorInfos.get(1));
                        return;
                    }
                }else if (size==3){
                    if (TextUtils.isEmpty(inputOne)){
                        showToast(visitorInfos.get(0));
                        return;
                    }
                    if (TextUtils.isEmpty(inputTwo)){
                        showToast(visitorInfos.get(1));
                        return;
                    }
                    if (TextUtils.isEmpty(inputThree)){
                        showToast(visitorInfos.get(2));
                        return;
                    }
                }else if (size==4){
                    if (TextUtils.isEmpty(inputOne)){
                        showToast(visitorInfos.get(0));
                        return;
                    }
                    if (TextUtils.isEmpty(inputTwo)){
                        showToast(visitorInfos.get(1));
                        return;
                    }
                    if (TextUtils.isEmpty(inputThree)){
                        showToast(visitorInfos.get(2));
                        return;
                    }
                    if (TextUtils.isEmpty(inputFour)){
                        showToast(visitorInfos.get(3));
                        return;
                    }
                }else if (size==5){
                    if (TextUtils.isEmpty(inputOne)){
                        showToast(visitorInfos.get(0));
                        return;
                    }
                    if (TextUtils.isEmpty(inputTwo)){
                        showToast(visitorInfos.get(1));
                        return;
                    }
                    if (TextUtils.isEmpty(inputThree)){
                        showToast(visitorInfos.get(2));
                        return;
                    }
                    if (TextUtils.isEmpty(inputFour)){
                        showToast(visitorInfos.get(3));
                        return;
                    }
                    if (TextUtils.isEmpty(inputFive)){
                        showToast(visitorInfos.get(4));
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
        lp.width = ParamsUtil.dpToPx(context,320);
        dialogWindow.setAttributes(lp);
    }

    private void showToast(VisitorInfo visitorInfo) {
        Toast.makeText(context,visitorInfo.getVisitorTip(),Toast.LENGTH_SHORT).show();
    }

    private void showInfo(LinearLayout ll_info_one, TextView tv_title_one, EditText et_tip_one,int position) {
        ll_info_one.setVisibility(View.VISIBLE);
        tv_title_one.setText(visitorInfos.get(position).getVisitorMes());
        et_tip_one.setHint(visitorInfos.get(position).getVisitorTip());
    }

    private String getInput(EditText editText){
        return editText.getText().toString().trim().replace(" ", "");
    }

}
