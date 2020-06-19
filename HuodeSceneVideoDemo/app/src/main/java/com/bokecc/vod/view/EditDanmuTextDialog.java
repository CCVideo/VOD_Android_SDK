package com.bokecc.vod.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.sdk.mobile.util.HttpUtil;
import com.bokecc.vod.R;
import com.bokecc.vod.adapter.DanmuColorAdapter;
import com.bokecc.vod.data.DanmuColorInfo;
import com.bokecc.vod.inter.OnEditDanmuText;
import com.bokecc.vod.utils.MultiUtils;

import java.util.List;


public class EditDanmuTextDialog extends Dialog {

    private Context context;
    private OnEditDanmuText onEditDanmuText;
    private CharSequence charSequence;
    private int start, end;
    private List<DanmuColorInfo> danmuColorInfoDatas;
    private DanmuColorAdapter danmuColorAdapter;
    private boolean isShowDanmuColor = false;
    private String danmuColor = "0xffffff";
    private boolean isPortrait = false;

    public EditDanmuTextDialog(Context context, boolean isPortrait, OnEditDanmuText onEditDanmuText) {
        super(context, R.style.CheckNetworkDialog);
        this.context = context;
        this.isPortrait = isPortrait;
        this.onEditDanmuText = onEditDanmuText;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_edit_danmu_text, null);
        setContentView(view);

        final EditText et_danmu_text = view.findViewById(R.id.et_danmu_text);

        final TextView tv_sure = view.findViewById(R.id.tv_sure);
        final TextView tv_remain_text_num = view.findViewById(R.id.tv_remain_text_num);
        final ImageView iv_select_danmu_color = view.findViewById(R.id.iv_select_danmu_color);
        final LinearLayout ll_landscape_danmu_color = view.findViewById(R.id.ll_landscape_danmu);
        RecyclerView rv_select_danmu_color = view.findViewById(R.id.rv_select_danmu_color);

        et_danmu_text.requestFocus();
        et_danmu_text.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) et_danmu_text.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }, 200);

        tv_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String danmuText = et_danmu_text.getText().toString();
                if (TextUtils.isEmpty(danmuText)) {
                    MultiUtils.showToast((Activity) context, "请输入内容");
                    return;
                }
                onEditDanmuText.getDanmuText(danmuText, danmuColor);
                MultiUtils.hideSoftKeyboard(et_danmu_text);
                dismiss();
            }
        });

        et_danmu_text.setFilters(new InputFilter[]{new EmoFilter()});
        et_danmu_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                charSequence = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                start = et_danmu_text.getSelectionStart();
                end = et_danmu_text.getSelectionEnd();
                int remainTextNum = 30 - charSequence.length();
                if (remainTextNum < 0) {
                    remainTextNum = 0;
                }
                tv_remain_text_num.setText(remainTextNum + "");
                if (charSequence.length() > 30) {
                    MultiUtils.showToast((Activity) context, "弹幕文字不能超过30个字");
                    s.delete(start - 1, end);
                    int currentSelection = end;
                    et_danmu_text.setText(s);
                    et_danmu_text.setSelection(currentSelection);
                }
            }
        });

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 1.0);
        dialogWindow.setAttributes(lp);
        dialogWindow.setDimAmount(0);
        dialogWindow.setGravity(Gravity.BOTTOM);

        iv_select_danmu_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShowDanmuColor) {
                    iv_select_danmu_color.setImageResource(R.mipmap.iv_select_danmu_color);
                    ll_landscape_danmu_color.setVisibility(View.GONE);
                    MultiUtils.showSoftKeyboard(et_danmu_text);
                    isShowDanmuColor = false;
                } else {
                    iv_select_danmu_color.setImageResource(R.mipmap.iv_select_danmu_color_selected);
                    MultiUtils.hideSoftKeyboard(et_danmu_text);
                    ll_landscape_danmu_color.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ll_landscape_danmu_color.setVisibility(View.VISIBLE);
                        }
                    }, 50);
                    isShowDanmuColor = true;
                }

            }
        });

        danmuColorInfoDatas = MultiUtils.getDanmuColorInfoDatas();
        if (isPortrait) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 7);
            rv_select_danmu_color.setLayoutManager(gridLayoutManager);
        } else {
            LinearLayoutManager layoutManagerEffect = new LinearLayoutManager(context);
            layoutManagerEffect.setOrientation(OrientationHelper.HORIZONTAL);
            rv_select_danmu_color.setLayoutManager(layoutManagerEffect);
        }

        danmuColorAdapter = new DanmuColorAdapter(danmuColorInfoDatas, isPortrait);
        rv_select_danmu_color.setAdapter(danmuColorAdapter);

        danmuColorAdapter.setOnItemClickListener(new DanmuColorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DanmuColorInfo item, int position) {
                for (DanmuColorInfo danmuColorInfo : danmuColorInfoDatas) {
                    danmuColorInfo.setSelected(false);
                }
                danmuColorInfoDatas.get(position).setSelected(true);
                danmuColorAdapter.notifyDataSetChanged();
                danmuColor = item.getColor();
            }
        });

        et_danmu_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShowDanmuColor) {
                    iv_select_danmu_color.setImageResource(R.mipmap.iv_select_danmu_color);
                    ll_landscape_danmu_color.setVisibility(View.GONE);
                    isShowDanmuColor = false;
                }
            }
        });

    }


}
