package cn.hugo.android.scanner.encode;

import java.util.Hashtable;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import cn.hugo.android.scanner.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class BitmapEncode
{
	/**
	 * 生成二维码图片，在编码时需要将com.google.zxing.qrcode.encoder.Encoder.java中的
	 *  static final String DEFAULT_BYTE_MODE_ENCODING = "ISO8859-1";修改为UTF-8，否则中文编译后解析不了
	 */
	public static Bitmap encode(String content, int width, int height)
	{
		Bitmap bitmap = null;
		try
		{
			MultiFormatWriter writer = new MultiFormatWriter();
			Hashtable<EncodeHintType,Object> hst = new Hashtable<EncodeHintType,Object>();
			hst.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
			hst.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hst);
			int[] pixels = new int[width * height];
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					if (matrix.get(x, y))
					{
						pixels[y * width + x] = 0xff000000;
					} else
					//无信息设置像素点为白色 不设置会导致保存图片的时候一团黑
					{
						pixels[y * width + x] = 0xffffffff;
					}
				}
			}
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		} catch (Exception e)
		{
		}
		return bitmap;
	}

//	/**
//	 * 带有logo的二维码图片 
//	 */
//	public static Bitmap encodeWithLogo(Context context, String content, int width, int height)
//	{
//		Bitmap bitmap = encode(content, width, height);
//		Bitmap bitmapLogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_qrcode);
//		Bitmap bitmapScaleLogo = Bitmap.createScaledBitmap(bitmapLogo, width / 6, height / 6, true);
//		int leftLogo = bitmap.getWidth() / 2 - bitmapScaleLogo.getWidth() / 2;
//		int topLogo = bitmap.getHeight() / 2 - bitmapScaleLogo.getHeight() / 2;
//		Paint paint = new Paint();
//		Canvas canvas = new Canvas(bitmap);
//		canvas.drawBitmap(bitmapScaleLogo, leftLogo, topLogo, paint);
//		canvas.save();
//		canvas.restore();
//		paint.reset();
//		bitmapLogo.recycle();
//		bitmapScaleLogo.recycle();
//		return bitmap;
//	}
}
