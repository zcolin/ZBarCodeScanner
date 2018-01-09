package cn.hugo.android.scanner.encode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

public class BitmapEncode {
    /**

     */
    public static Bitmap encode(String content, int width, int height) {
        return encode(content, width, height, 0);
    }

    /**
     * 生成二维码图片，在编码时需要将com.google.zxing.qrcode.encoder.Encoder.java中的
     * static final String DEFAULT_BYTE_MODE_ENCODING = "ISO8859-1";修改为UTF-8，否则中文编译后解析不了
     *
     * @param color 二维码颜色
     */
    public static Bitmap encode(String content, int width, int height, int color) {
        Bitmap bitmap = null;
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            Hashtable<EncodeHintType, Object> hst = new Hashtable<>();
            hst.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hst.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hst);
            int[] pixels = new int[width * height];
            color = color != 0 ? color : 0xff000000;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = color;
                    } else
                    //无信息设置像素点为白色 不设置会导致保存图片的时候一团黑
                    {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        } catch (Exception e) {
        }
        return bitmap;
    }

    /**
     * 带有logo的二维码图片
     */
    public static Bitmap encodeWithPic(Context context, int drawable, String content, int width, int height) {
        return encodeWithPic(context, drawable, content, width, height, 0);
    }

    public static Bitmap encodeWithPic(Context context, int drawable, String content, int width, int height, int color) {
        Bitmap bitmapPic = BitmapFactory.decodeResource(context.getResources(), drawable);
        return encodeWithPic(bitmapPic, content, width, height, color);
    }


    public static Bitmap encodeWithPic(Bitmap bitmapPic, String content, int width, int height) {
        return encodeWithPic(bitmapPic, content, width, height, 0);
    }

    /**
     * 带有logo的二维码图片
     */
    public static Bitmap encodeWithPic(Bitmap bitmapPic, String content, int width, int height, int color) {
        Bitmap bitmap = encode(content, width, height, color);
        int leftLogo = bitmap.getWidth() / 2 - bitmapPic.getWidth() / 2;
        int topLogo = bitmap.getHeight() / 2 - bitmapPic.getHeight() / 2;
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmapPic, leftLogo, topLogo, paint);
        canvas.save();
        canvas.restore();
        paint.reset();
        bitmapPic.recycle();
        return bitmap;
    }
}
