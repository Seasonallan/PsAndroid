package com.season.example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.season.btc.R;
import com.season.lib.util.ToastUtil;
import com.season.mvp.ui.BaseTLEActivity;


public class SelectActivity extends BaseTLEActivity {

    RadioGroup themeRadioGroup;
    RadioGroup envRadioGroup;
    RadioGroup colorRadioGroup;
    RadioGroup languageRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        getTitleBar().enableLeftButton();
        getTitleBar().setTopTile("K线图模块");

        themeRadioGroup = findViewById(R.id.rg_theme);
        envRadioGroup = findViewById(R.id.rg_environment);
        colorRadioGroup = findViewById(R.id.rg_color);
        languageRadioGroup = findViewById(R.id.rg_language);

        findViewById(R.id.tv_start).setOnClickListener(view -> onButtonClicked(view));
        findViewById(R.id.tv_start1).setOnClickListener(view -> onButtonClicked(view));
        findViewById(R.id.tv_start2).setOnClickListener(view -> onButtonClicked(view));
    }

    private void onButtonClicked(View view) {
        Intent intent = new Intent(SelectActivity.this, KLineChartActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("coinCode", view.getTag().toString());
        bundle.putString("language", findViewById(languageRadioGroup.getCheckedRadioButtonId()).getTag().toString());
        bundle.putBoolean("riseGreen", colorRadioGroup.getCheckedRadioButtonId() == R.id.rb_color1);
        bundle.putBoolean("nightMode", themeRadioGroup.getCheckedRadioButtonId() == R.id.rb_theme1);

        bundle.putString("webSocketUrl", envRadioGroup.getCheckedRadioButtonId() == R.id.rb_environment1 ? Key.socketUrl
                : envRadioGroup.getCheckedRadioButtonId() == R.id.rb_environment2 ? Key.socketUrlPre : Key.socketUrlTest);
        bundle.putString("briefUrl", envRadioGroup.getCheckedRadioButtonId() == R.id.rb_environment1 ? Key.briefUrl
                : envRadioGroup.getCheckedRadioButtonId() == R.id.rb_environment2 ? Key.briefUrlPre : Key.briefUrlTest);

        intent.putExtra("bundle", bundle);

        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            String backType = data.getStringExtra("backType");
            switch (backType) {
                case "买入":
                    ToastUtil.showToast("买入按钮点击");
                    break;
                case "卖出":
                    ToastUtil.showToast("卖出按钮点击");
                    break;
            }
        }
    }
}