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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.bokecc.vod.R;
import com.bokecc.vod.adapter.DefinitionAdapter;
import com.bokecc.vod.data.DefinitionInfo;
import com.bokecc.vod.inter.DeleteFile;
import com.bokecc.vod.inter.SelectDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeleteFileDialog extends Dialog {
    private Context context;
    private DeleteFile deleteFile;

    public DeleteFileDialog(Context context,DeleteFile deleteFile) {
        super(context, R.style.DeleteFileDialog);
        this.context = context;
        this.deleteFile = deleteFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_delete_file, null);
        setContentView(view);

        TextView tv_delete_file = view.findViewById(R.id.tv_delete_file);
        tv_delete_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteFile!=null){
                    deleteFile.deleteFile();
                }
                dismiss();
            }
        });

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.8);
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.CENTER);
    }

}
