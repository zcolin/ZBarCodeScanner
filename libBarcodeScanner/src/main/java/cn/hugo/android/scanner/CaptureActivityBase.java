package cn.hugo.android.scanner;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;

import com.google.zxing.Result;

import cn.hugo.android.scanner.camera.CameraManager;
import cn.hugo.android.scanner.view.ViewfinderView;

public interface CaptureActivityBase
{
	public abstract void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor);

	public abstract void restartPreviewAfterDelay(long delayMS);

	public abstract ViewfinderView getViewfinderView();

	public abstract Handler getHandler();

	public abstract CameraManager getCameraManager();

	public abstract void drawViewfinder();
	
	public abstract Activity getActivityContext();
}
