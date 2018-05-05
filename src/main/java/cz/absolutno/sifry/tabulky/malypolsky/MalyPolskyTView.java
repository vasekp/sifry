package cz.absolutno.sifry.tabulky.malypolsky;

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

public final class MalyPolskyTView extends View implements DrawOnWhiteView {

    private final boolean[] steny = new boolean[4];
    private boolean rot;
    private boolean tecka;
    private final Paint pLine, pFill;

    public MalyPolskyTView(Context ctx, AttributeSet as) {
        super(ctx, as);

        pLine = new Paint();
        pLine.setColor(Color.WHITE);
        pLine.setStrokeWidth(Utils.dpToPix(1));
        pLine.setAntiAlias(true);
        pLine.setStyle(Style.STROKE);
        pLine.setStrokeCap(Cap.ROUND);

        pFill = new Paint(pLine);
        pFill.setStyle(Style.FILL);
    }

    private void setIn(int x, int y, int xq, int yq) {
        if (yq == 0) {
            steny[0] = (x >= 0);
            steny[1] = (x <= 0);
            steny[2] = (y >= 0);
            steny[3] = (y <= 0);
        } else {
            steny[0] = (x == 1);
            steny[1] = (x == -1);
            steny[2] = (y == 1);
            steny[3] = (y == -1);
        }
        tecka = (xq == 1);
        rot = (yq == 1);
        invalidate();
    }

    public void setIn(int[] s) {
        setIn(s[0], s[1], s[2], s[3]);
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        float w = getWidth() - getPaddingLeft() - getPaddingRight();
        float h = getHeight() - getPaddingTop() - getPaddingBottom();
        c.translate(getPaddingLeft(), getPaddingTop());
        if (rot) {
            if (steny[1] && steny[2])
                c.rotate(90, w / 2, h / 2);
            else if (steny[0] && steny[2])
                c.rotate(180, w / 2, h / 2);
            else if (steny[0] && steny[3])
                c.rotate(270, w / 2, h / 2);
            if (tecka)
                c.drawCircle(w / 2, h * 3 / 8, h / 16, pFill);
            c.drawLine(w / 2, h * 3 / 4, w * 3 / 16, h / 4, pLine);
            c.drawLine(w / 2, h * 3 / 4, w * 13 / 16, h / 4, pLine);
        } else {
            if (tecka)
                c.drawCircle(w / 2, h / 2, h / 16, pFill);
            if (steny[0])
                c.drawLine(w / 4, h / 4, w / 4, h * 3 / 4, pLine);
            if (steny[1])
                c.drawLine(w * 3 / 4, h / 4, w * 3 / 4, h * 3 / 4, pLine);
            if (steny[2])
                c.drawLine(w / 4, h / 4, w * 3 / 4, h / 4, pLine);
            if (steny[3])
                c.drawLine(w / 4, h * 3 / 4, w * 3 / 4, h * 3 / 4, pLine);
        }
    }

    public void drawOnWhite(Canvas c) {
        pLine.setColor(Color.BLACK);
        pFill.setColor(Color.BLACK);
        draw(c);
        pLine.setColor(Color.WHITE);
        pFill.setColor(Color.WHITE);
    }
}
