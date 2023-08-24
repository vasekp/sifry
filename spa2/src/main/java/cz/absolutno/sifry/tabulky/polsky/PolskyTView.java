package cz.absolutno.sifry.tabulky.polsky;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.widget.DrawOnWhiteView;

public final class PolskyTView extends View implements DrawOnWhiteView {

    private final boolean[] steny = new boolean[4];
    private int tecky;
    private boolean alt;
    private final Paint pLine, pFill;

    public PolskyTView(Context ctx, AttributeSet as) {
        super(ctx, as);

        pLine = new Paint();
        pLine.setColor(Color.WHITE);
        pLine.setStrokeWidth(Utils.dpToPix(1));
        pLine.setStrokeCap(Cap.ROUND);
        pLine.setAntiAlias(true);
        pLine.setStyle(Style.STROKE);

        pFill = new Paint(pLine);
        pFill.setStyle(Style.FILL);
    }

    public void setIn(int x, int y, int z, boolean alt) {
        steny[0] = (x >= 1);
        steny[1] = (x <= 1);
        steny[2] = (y >= 1);
        steny[3] = (y <= 1);
        tecky = z;
        this.alt = alt;
        invalidate();
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        float w = getWidth() - getPaddingLeft() - getPaddingRight();
        float h = getHeight() - getPaddingTop() - getPaddingBottom();
        c.translate(getPaddingLeft(), getPaddingTop());
        if (steny[0])
            c.drawLine(w / 6, h / 4, w / 6, h * 3 / 4, pLine);
        if (steny[1])
            c.drawLine(w * 5 / 6, h / 4, w * 5 / 6, h * 3 / 4, pLine);
        if (steny[2])
            c.drawLine(w / 6, h / 4, w * 5 / 6, h / 4, pLine);
        if (steny[3])
            c.drawLine(w / 6, h * 3 / 4, w * 5 / 6, h * 3 / 4, pLine);
        if (alt)
            for (int i = 0; i < tecky; i++)
                c.drawCircle(w / 2 + w / 4 * (i + (1 - tecky) / 2f), h / 2, w / 18, pFill);
        else
            c.drawCircle(w / 2 + w / 6 * (tecky - 1), h / 2, w / 18, pFill);
    }

    public void drawOnWhite(Canvas c) {
        pLine.setColor(Color.BLACK);
        pFill.setColor(Color.BLACK);
        draw(c);
        pLine.setColor(Color.WHITE);
        pFill.setColor(Color.WHITE);
    }

}