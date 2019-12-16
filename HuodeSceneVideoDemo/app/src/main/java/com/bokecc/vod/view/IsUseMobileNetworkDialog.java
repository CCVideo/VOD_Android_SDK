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
import android.widget.Button;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.inter.DeleteFile;
import com.bokecc.vod.inter.IsUseMobieNetwork;

public class IsUseMobileNetworkDialog extends Dialog {
    private Context context;
    private IsUseMobieNetwork isUseMobieNetwork;

    public IsUseMobileNetworkDialog(Context context, IsUseMobieNetwork isUseMobieNetwork) {
        super(context, R.style.DeleteFileDialog);
        this.context = context;
        this.isUseMobieNetwork = isUseMobieNetwork;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_is_use_mobile_network, null);
        setContentView(view);

        Button btn_exit = view.findViewById(R.id.btn_exit);
        Button btn_continue = view.findViewById(R.id.btn_continue);

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                isUseMobieNetwork.exit();
            }
        });

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                isUseMobieNetwork.continuePlay();
            }
        });

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.7);
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.CENTER);
    }

}
