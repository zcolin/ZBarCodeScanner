/*
 * *********************************************************
 *  author   colin
 *  company  fosung
 *  email    wanglin2046@126.com
 *  date     16-9-30 下午4:05
 * ********************************************************
 */

package cn.hugo.android.scanner.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import cn.hugo.android.scanner.AmbientLightManager;
import cn.hugo.android.scanner.CaptureActivityBase;
import cn.hugo.android.scanner.FinishListener;
import cn.hugo.android.scanner.InactivityTimer;
import cn.hugo.android.scanner.IntentSource;
import cn.hugo.android.scanner.R;
import cn.hugo.android.scanner.camera.CameraManager;
import cn.hugo.android.scanner.decode.CaptureActivityHandler;


/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 * <p>
 * 1.此view只能放置到Activity中
 * 2.开启camera，在后台独立线程中完成扫描任务；
 * 3.绘制了一个扫描区（viewfinder）来帮助用户将条码置于其中以准确扫描； 3.扫描成功后会将扫描结果展示在界面上。
 */
public class BaseQrCodeScannerView extends RelativeLayout implements SurfaceHolder.Callback, CaptureActivityBase {

    private static final String TAG = BaseQrCodeScannerView.class.getSimpleName();
    /**
     * 是否有预览
     */
    private boolean             hasSurface;
    /**
     * 活动监控器。如果手机没有连接电源线，那么当相机开启后如果一直处于不被使用状态则该服务会将当前activity关闭。
     * 活动监控器全程监控扫描活跃状态，与CaptureActivity生命周期相同.每一次扫描过后都会重置该监控，即重新倒计时。
     */
    private InactivityTimer     inactivityTimer;
    /**
     * 闪光灯调节器。自动检测环境光线强弱并决定是否开启闪光灯
     */
    private AmbientLightManager ambientLightManager;
    private CameraManager       cameraManager;

    /**
     * 扫描结果回调
     */
    private OnScanResultListener onScanResultListener;

    /**
     * 扫描区域
     */
    private ViewfinderView            viewfinderView;
    private CaptureActivityHandler    handler;
    private Result                    lastResult;
    private boolean                   isFlashlightOpen;
    /**
     * 【辅助解码的参数(用作MultiFormatReader的参数)】 编码类型，该参数告诉扫描器采用何种编码方式解码，即EAN-13，QR
     * Code等等 对应于DecodeHintType.POSSIBLE_FORMATS类型
     * 参考DecodeThread构造函数中如下代码：hints.put(DecodeHintType.POSSIBLE_FORMATS,
     * decodeFormats);
     */
    private Collection<BarcodeFormat> decodeFormats;
    /**
     * 【辅助解码的参数(用作MultiFormatReader的参数)】 该参数最终会传入MultiFormatReader，
     * 上面的decodeFormats和characterSet最终会先加入到decodeHints中 最终被设置到MultiFormatReader中
     * 参考DecodeHandler构造器中如下代码：multiFormatReader.setHints(hints);
     */
    private Map<DecodeHintType, ?>    decodeHints;
    /**
     * 【辅助解码的参数(用作MultiFormatReader的参数)】 字符集，告诉扫描器该以何种字符集进行解码
     * 对应于DecodeHintType.CHARACTER_SET类型
     * 参考DecodeThread构造器如下代码：hints.put(DecodeHintType.CHARACTER_SET,
     * characterSet);
     */
    private String                    characterSet;
    private Result                    savedResultToShow;
    private IntentSource              source;
    private Activity                  activity;

    public BaseQrCodeScannerView(Context context) {
        this(context, null);
    }

    public BaseQrCodeScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.activity = (Activity) context;
    }

    /**
     * 在Activity的onCreate中调用
     */
    public void onCreate() {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LayoutInflater.from(activity)
                      .inflate(R.layout.scan_qrcodescan, this);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(activity);
        ambientLightManager = new AmbientLightManager(activity);
    }

    /**
     * 在Activity的onResume中调用
     */
    public void onResume() {
        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
        // 相机初始化的动作需要开启相机并测量屏幕大小，这些操作
        // 不建议放到onCreate中，因为如果在onCreate中加上首次启动展示帮助信息的代码的 话，
        // 会导致扫描窗口的尺寸计算有误的bug
        cameraManager = new CameraManager(activity);
        viewfinderView = (ViewfinderView) findViewById(R.id.actyscan_viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);
        handler = null;
        lastResult = null;
        // 摄像头预览功能必须借助SurfaceView，因此也需要在一开始对其进行初始化
        // 如果需要了解SurfaceView的原理
        // 参考:http://blog.csdn.net/luoshengyang/article/details/8661317
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.actyscan_preview_view); // 预览
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // 防止sdk8的设备初始化预览异常
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            surfaceHolder.addCallback(this);
        }
        // 启动闪光灯调节器
        ambientLightManager.start(cameraManager);
        // 恢复活动监控器
        inactivityTimer.onResume();
        source = IntentSource.NONE;
        decodeFormats = null;
        characterSet = null;
    }

    /**
     * 在Activity的onPause调用
     */
    public void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        ambientLightManager.stop();
        // 关闭摄像头
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.actyscan_preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }

    /**
     * 在Activity的onPause调用
     */
    public void onDestroy() {
        inactivityTimer.shutdown();
    }

    /**
     * 打开/关闭闪光灯
     */
    public void toggleFlashLight() {
        if (isFlashlightOpen) {
            cameraManager.setTorch(false); // 关闭闪光灯
            isFlashlightOpen = false;
        } else {
            cameraManager.setTorch(true); // 打开闪光灯
            isFlashlightOpen = true;
        }
    }

    /**
     * 添加结果监听
     */
    public void setOnScanResultListener(OnScanResultListener onScanResultListener) {
        this.onScanResultListener = onScanResultListener;
    }

    /**
     * 在Activity的onKeyDown调用
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if ((source == IntentSource.NONE) && lastResult != null) { // 重新进行扫描
                    restartPreviewAfterDelay(0L);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                cameraManager.zoomIn();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                cameraManager.zoomOut();
                return true;
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        hasSurface = false;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult   The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode     A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        //		// 重新计时
        //		inactivityTimer.onActivity();
        //		lastResult = rawResult;
        //		// 把图片画到扫描框
        //		viewfinderView.drawResultBitmap(barcode);
        //		beepManager.playBeepSoundAndVibrate();
        //		Toast.makeText(this, "识别结果:" + ResultParser.parseResult(rawResult).toString(), Toast.LENGTH_SHORT).show();
        //震动
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
        scanSuccess(ResultParser.parseResult(rawResult)
                                .toString());
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    private void resetStatusView() {
        viewfinderView.setVisibility(View.VISIBLE);
        lastResult = null;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    /**
     * 向CaptureActivityHandler中发送消息，并展示扫描到的图像
     */
    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.app_name));
        builder.setMessage(activity.getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(activity));
        builder.setOnCancelListener(new FinishListener(activity));
        builder.show();
    }

    //扫描成功
    private void scanSuccess(String result) {
        if (TextUtils.isEmpty(result)) {
            // 重新进行扫描
            restartPreviewAfterDelay(0L);
            Toast.makeText(activity, "扫描结果为空，请重新扫描！", Toast.LENGTH_SHORT)
                 .show();
        } else {
            //String resultStr = ResultParser.parseResult(rawResult).toString();
            if (onScanResultListener != null && !onScanResultListener.scanResult(result)) {
                // 重新进行扫描
                restartPreviewAfterDelay(0L);
                Toast.makeText(activity, "二维码数据错误，请重新扫描！", Toast.LENGTH_SHORT)
                     .show();
            }
        }
    }

    @Override
    public Activity getActivityContext() {
        return activity;
    }

    /**
     * 扫描结果接口
     */
    public interface OnScanResultListener {
        /**
         * @return 为true 则结束Activity
         */
        boolean scanResult(String result);
    }
}
