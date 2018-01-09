package cn.hugo.android.scanner;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;

import com.google.zxing.Result;

import cn.hugo.android.scanner.camera.CameraManager;
import cn.hugo.android.scanner.view.ViewfinderView;

public interface CaptureActivityBase {
    void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor);

    void restartPreviewAfterDelay(long delayMS);

    ViewfinderView getViewfinderView();

    Handler getHandler();

    CameraManager getCameraManager();

    void drawViewfinder();

    Activity getActivityContext();
}
