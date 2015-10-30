package cz.absolutno.sifry.braille;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public final class BrailleTView extends View {

    private final boolean[] in = {false, false, false, false, false, false};
    private final Paint p;

    public BrailleTView(Context ctx, AttributeSet as) {
        super(ctx, as);

        p = new Paint();
        p.setColor(Color.BLACK);
        p.setStrokeWidth(0);
        p.setAntiAlias(true);
    }

    public void setIn(int x) {
        for (int i = 0; i < 6; i++)
            in[i] = ((x & (1 << i)) != 0);
        invalidate();
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        float w = getWidth() - getPaddingLeft() - getPaddingRight();
        float h = getHeight() - getPaddingTop() - getPaddingBottom();
        c.translate(0, getPaddingTop());
        c.clipRect(0, 0, getWidth(), h);
        c.drawColor(Color.WHITE);
        c.translate(getPaddingLeft(), 0);
        for (int ix = 0; ix < 6; ix++) {
            float x, y;
            x = ((ix / 3) * 2 - 1) * h / 6 + w / 2;
            y = (ix % 3) * h / 3 + h / 6;
            p.setStyle(in[ix] ? Style.FILL_AND_STROKE : Style.STROKE);
            c.drawCircle(x, y, h / 10, p);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        width = (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(widthMeasureSpec) : getSuggestedMinimumWidth();
        height = (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(heightMeasureSpec) : getSuggestedMinimumHeight();
        setMeasuredDimension(width, height);
    }

}