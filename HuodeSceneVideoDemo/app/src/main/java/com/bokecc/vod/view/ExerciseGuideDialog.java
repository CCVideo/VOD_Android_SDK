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
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.inter.DeleteFile;
import com.bokecc.vod.utils.MultiUtils;

public class ExerciseGuideDialog extends Dialog {
    private Context context;

    public ExerciseGuideDialog(Context context) {
        super(context, R.style.ExerciseGuideDialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_exercise_guide, null);
        setContentView(view);

        ImageView iv_exercise_guide = view.findViewById(R.id.iv_exercise_guide);
        iv_exercise_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiUtils.setIsReadExerciseGuide(true);
                dismiss();
            }
        });

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 1.0);
        lp.height = (int) (d.heightPixels * 1.0);
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.CENTER);
        setCanceledOnTouchOutside(false);
    }

}
