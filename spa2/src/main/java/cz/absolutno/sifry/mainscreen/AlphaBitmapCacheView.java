package cz.absolutno.sifry.mainscreen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AlphaBitmapCacheView extends ImageView {

    private static Bitmap bmpTemp;
    private Bitmap bmp;
    private final Paint pAlpha;
    private final Paint paint;

    public AlphaBitmapCacheView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        pAlpha = new Paint();
        pAlpha.setColorFilter(new ColorMatrixColorFilter(new float[]{
                0, 0, 0, 0, 1,
                0, 0, 0, 0, 1,
                0, 0, 0, 0, 1,
                1 / 3f, 1 / 3f, 1 / 3f, 0, 0
        }));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bmp == null)
            createBitmap();
        canvas.drawBitmap(bmp, 0, 0, paint);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        bmp = null;
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bmp = null;
        if (w == 0 || h == 0)
            return;
        if (bmpTemp == null)
            return;
        if (w != bmpTemp.getWidth() || h != bmpTemp.getHeight()) {
            bmpTemp.recycle();
            bmpTemp = null;
        }
    }

    @SuppressLint("WrongCall") /* draw() would result in an infinite loop as the overriden onDraw() calls createBitmap() */
    private void createBitmap() {
        if (bmpTemp == null)
            bmpTemp = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
        Canvas c = new Canvas(bmpTemp);
        c.drawColor(Color.BLACK);
        super.onDraw(c);
        bmp = Bitmap.createBitmap(getWidth(), getHeight(), Config.ALPHA_8);
        c = new Canvas(bmp);
        c.drawBitmap(bmpTemp, 0, 0, pAlpha);
    }

}
