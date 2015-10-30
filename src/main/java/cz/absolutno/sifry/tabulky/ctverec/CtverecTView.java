package cz.absolutno.sifry.tabulky.ctverec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.widget.DrawOnWhiteView;

public final class CtverecTView extends View implements DrawOnWhiteView {

    private final float wd;
    private int x, y;
    private boolean alt;
    private final Paint pLine, pFill;

    public CtverecTView(Context ctx, AttributeSet as) {
        super(ctx, as);

        wd = Utils.dpToPix(1);
        pLine = new Paint();
        pLine.setColor(Color.WHITE);
        pLine.setStrokeWidth(wd);
        pLine.setAntiAlias(true);
        pLine.setStyle(Style.STROKE);

        pFill = new Paint(pLine);
        pFill.setStyle(Style.FILL);
    }

    public void setIn(int x, int y, boolean alt) {
        this.x = x;
        this.y = y;
        this.alt = alt;
        invalidate();
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        float w = getWidth() - getPaddingLeft() - getPaddingRight();
        float h = getHeight() - getPaddingTop() - getPaddingBottom();
        c.translate(getPaddingLeft(), getPaddingTop());
        if (alt)
            c.drawRect(w / 5, h / 5, w * 4 / 5, h * 4 / 5, pLine);
        else
            c.drawRect(wd / 2, wd / 2, w - wd / 2, h - wd / 2, pLine);
        c.drawRect(w * x / 5, h * y / 5, w * (x + 1) / 5, h * (y + 1) / 5, pFill);
    }

    public void drawOnWhite(Canvas c) {
        pLine.setColor(Color.BLACK);
        pFill.setColor(Color.BLACK);
        draw(c);
        pLine.setColor(Color.WHITE);
        pFill.setColor(Color.WHITE);
    }
}