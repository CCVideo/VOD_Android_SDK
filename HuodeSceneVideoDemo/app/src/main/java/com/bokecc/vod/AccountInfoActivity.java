package com.bokecc.vod;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.vod.utils.MultiUtils;


public class AccountInfoActivity extends AppCompatActivity {
    private Activity activity;
    private TextView tv_user_id,tv_api_key;
    private EditText et_verification_code;
    private ImageView iv_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        MultiUtils.setStatusBarColor(this, R.color.transparent, true);
        activity = this;
        initView();
    }

    private void initView() {
        tv_user_id = findViewById(R.id.tv_user_id);
        tv_api_key = findViewById(R.id.tv_api_key);
        et_verification_code = findViewById(R.id.et_verification_code);

        iv_back = findViewById(R.id.iv_back);

        if (!TextUtils.isEmpty(MultiUtils.getVerificationCode())){
            et_verification_code.setText(MultiUtils.getVerificationCode());
        }

        tv_user_id.setText(ConfigUtil.USERID);
        tv_api_key.setText(ConfigUtil.API_KEY);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String code = et_verification_code.getText().toString().trim();
        MultiUtils.setVerificationCode(code);

    }
}
