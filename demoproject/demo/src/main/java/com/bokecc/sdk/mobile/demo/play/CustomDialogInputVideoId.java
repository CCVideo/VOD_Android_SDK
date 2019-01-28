package com.bokecc.sdk.mobile.demo.play;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bokecc.sdk.mobile.demo.R;


public class CustomDialogInputVideoId extends Dialog {
    private Context context;
    private BtnCancelOrSureListener btnCancelOrSureListener;

    public CustomDialogInputVideoId(@NonNull Context context, BtnCancelOrSureListener btnCancelOrSureListener) {
        super(context, R.style.CustomDialogInputVerifyCode);
        this.context = context;
        this.btnCancelOrSureListener = btnCancelOrSureListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_input_video_id, null);
        setContentView(view);
        TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
        TextView tv_sure = (TextView) view.findViewById(R.id.tv_sure);
        final EditText et_input_verify_code = view.findViewById(R.id.et_input_verify_code);

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnCancelOrSureListener != null) {
                    btnCancelOrSureListener.btnCancel();
                    dismiss();
                }
            }
        });

        tv_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnCancelOrSureListener != null) {
                    btnCancelOrSureListener.btnSure(et_input_verify_code.getText().toString());
                    dismiss();
                }
            }
        });

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.9);
//        lp.height = (int) (d.heightPixels * 0.75);
        dialogWindow.setAttributes(lp);


    }
}
