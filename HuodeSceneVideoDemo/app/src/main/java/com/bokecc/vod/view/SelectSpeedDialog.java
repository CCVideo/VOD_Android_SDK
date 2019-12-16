package com.bokecc.vod.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.inter.SelectSpeed;

import java.util.Map;

public class SelectSpeedDialog extends Dialog {

    private Context context;
    private SelectSpeed selectSpeed;
    private float currentSpeed;

    public SelectSpeedDialog(Context context, float currentSpeed, SelectSpeed selectSpeed) {
        super(context, R.style.SetVideoDialog);
        this.context = context;
        this.selectSpeed = selectSpeed;
        this.currentSpeed = currentSpeed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_select_speed, null);
        setContentView(view);
        TextView tv_zero_point_five = (TextView) view.findViewById(R.id.tv_zero_point_five);
        TextView tv_one_point_zero = (TextView) view.findViewById(R.id.tv_one_point_zero);
        TextView tv_one_point_five = (TextView) view.findViewById(R.id.tv_one_point_five);
        TextView tv_two_point_zero = (TextView) view.findViewById(R.id.tv_two_point_zero);

        if (currentSpeed==0.5f){
            tv_zero_point_five.setTextColor(context.getResources().getColor(R.color.orange));
        }else if (currentSpeed==1.0f){
            tv_one_point_zero.setTextColor(context.getResources().getColor(R.color.orange));
        }else if (currentSpeed==1.5f){
            tv_one_point_five.setTextColor(context.getResources().getColor(R.color.orange));
        }else if (currentSpeed==2.0f){
            tv_two_point_zero.setTextColor(context.getResources().getColor(R.color.orange));
        }

        tv_zero_point_five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectSpeed != null) {
                    selectSpeed.selectedSpeed(0.5f);
                    dismiss();
                }
            }
        });

        tv_one_point_zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectSpeed != null) {
                    selectSpeed.selectedSpeed(1.0f);
                    dismiss();
                }
            }
        });

        tv_one_point_five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectSpeed != null) {
                    selectSpeed.selectedSpeed(1.5f);
                    dismiss();
                }
            }
        });

        tv_two_point_zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectSpeed != null) {
                    selectSpeed.selectedSpeed(2.0f);
                    dismiss();
                }
            }
        });


        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.35);
        lp.height = (int) (d.heightPixels * 1.0);
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.RIGHT);
    }

}
