package com.example.meng.zxingscan;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 得到请求结果
 */
public class ResultScanActivity extends AppCompatActivity {

    String result = "";
    TextView mTextView;
    ImageView mBackIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_scan);
        mTextView = (TextView) findViewById(R.id.textView);
        mBackIv = (ImageView) findViewById(R.id.back);
        Intent intent = this.getIntent();
        if (intent != null) {
            result = intent.getStringExtra("result");
        }
        if (!TextUtils.isEmpty(result)) {
            mTextView.setText(result);
        }
        mBackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResultScanActivity.this.finish();
            }
        });
    }
}
