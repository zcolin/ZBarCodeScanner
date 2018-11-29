# BarcodeScannerLib
##二维码扫描库.

####基于 zxing 的二维码扫描库，封装了View视图

## Gradle
app的build.gradle中添加
```
dependencies {
    implementation "com.android.support:appcompat-v7:你的依赖版本号"
    implementation 'com.github.zcolin:ZBarCodeScanner:latest.release'
}
```
工程的build.gradle中添加
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Usage
==
layout
```
<cn.hugo.android.scanner.view.BaseQrCodeScannerView
android:id="@+id/scan_view"
android:layout_width="match_parent"
android:layout_height="match_parent"/>
```
code
```
    private BaseQrCodeScannerView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcodescan);

        view = (BaseQrCodeScannerView) findViewById(R.id.scan_view);
        view.onCreate();
        view.setOnScanResultListener(new BaseQrCodeScannerView.OnScanResultListener() {
            @Override
            public boolean scanResult(String result) {
                ToastUtil.toastShort(result);
                QrCodeScanActivity.this.finish();
                return true;//false会提示扫描错误，并重新开始扫描
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

//    @Override
//    protected void onToolBarRightBtnClick() {
//        view.toggleFlashLight();
//    }
```
