package com.fosung.barcodescanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import cn.hugo.android.scanner.view.BaseQrCodeScannerView;

public class QrCodeScanActivity extends AppCompatActivity {

    private BaseQrCodeScannerView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = (BaseQrCodeScannerView) findViewById(R.id.scan_view);
        view.onCreate();
        view.setOnScanResultListener(new BaseQrCodeScannerView.OnScanResultListener() {
            @Override
            public boolean scanResult(String result) {
                Toast.makeText(QrCodeScanActivity.this, result, Toast.LENGTH_LONG)
                     .show();
                return false;//true则结束页面，false则不结束
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        view.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        view.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        view.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return view.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
