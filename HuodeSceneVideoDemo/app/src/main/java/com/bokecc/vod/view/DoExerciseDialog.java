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

import com.bokecc.vod.R;
import com.bokecc.vod.data.Question;

public class DoExerciseDialog extends Dialog {

    private Context context;
    private Question question;


    public DoExerciseDialog(Context context, Question question) {
        super(context, R.style.SetVideoDialog);
        this.context = context;
        this.question = question;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_do_exercise, null);
        setContentView(view);



        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.9);
        lp.height = (int) (d.heightPixels * 0.5);
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.RIGHT);
    }

}
