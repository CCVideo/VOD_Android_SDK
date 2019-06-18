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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.inter.SelectCompressLevel;
import com.bokecc.vod.inter.SelectPlayer;

public class SelectPlayerDialog extends Dialog {

    private Context context;
    private SelectPlayer selectPlayer;
    public SelectPlayerDialog(Context context, SelectPlayer selectPlayer) {
        super(context, R.style.CheckNetworkDialog);
        this.context = context;
        this.selectPlayer = selectPlayer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_select_player, null);
        setContentView(view);
        LinearLayout ll_ijk =  view.findViewById(R.id.ll_ijk);
        LinearLayout ll_media = view.findViewById(R.id.ll_media);
        LinearLayout ll_vr = view.findViewById(R.id.ll_vr);

        ll_ijk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectPlayer != null) {
                    selectPlayer.selectDWIjkMediaPlayer();
                    dismiss();
                }
            }
        });

        ll_media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectPlayer != null) {
                    selectPlayer.selectDWMediaPlayer();
                    dismiss();
                }
            }
        });

        ll_vr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectPlayer != null) {
                    selectPlayer.selectVrPlay();
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
