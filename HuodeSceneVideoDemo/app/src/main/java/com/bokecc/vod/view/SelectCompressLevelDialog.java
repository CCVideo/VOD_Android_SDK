package com.bokecc.vod.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.inter.SelectCompressLevel;
import com.bokecc.vod.inter.SelectSpeed;

public class SelectCompressLevelDialog extends Dialog {

    private Context context;
    private SelectCompressLevel selectCompressLevel;
    public SelectCompressLevelDialog(Context context, SelectCompressLevel selectCompressLevel) {
        super(context, R.style.CheckNetworkDialog);
        this.context = context;
        this.selectCompressLevel = selectCompressLevel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_select_compress_level, null);
        setContentView(view);
        TextView tv_not_compress = (TextView) view.findViewById(R.id.tv_not_compress);
        TextView tv_high_compress = (TextView) view.findViewById(R.id.tv_high_compress);
        TextView tv_medium_compress = (TextView) view.findViewById(R.id.tv_medium_compress);
        TextView tv_low_compress = (TextView) view.findViewById(R.id.tv_low_compress);

        tv_not_compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectCompressLevel != null) {
                    selectCompressLevel.selectedCompressLevel(0);
                    dismiss();
                }
            }
        });

        tv_high_compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectCompressLevel != null) {
                    selectCompressLevel.selectedCompressLevel(1);
                    dismiss();
                }
            }
        });

        tv_medium_compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectCompressLevel != null) {
                    selectCompressLevel.selectedCompressLevel(2);
                    dismiss();
                }
            }
        });

        tv_low_compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectCompressLevel != null) {
                    selectCompressLevel.selectedCompressLevel(3);
                    dismiss();
                }
            }
        });


        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 1.0);
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.BOTTOM);
    }

}
