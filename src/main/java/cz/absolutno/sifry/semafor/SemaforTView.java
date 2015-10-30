package cz.absolutno.sifry.semafor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.widget.DrawOnWhiteView;

public final class SemaforTView extends View implements DrawOnWhiteView {

    private int colB1, colB2;
    private final int colV1, colV2;
    private int s1 = -1, s2 = -1;
    private boolean valid = false;
    private final Paint pStroke, pFill;
    private final Path[] path = new Path[4];

    public SemaforTView(Context ctx, AttributeSet as) {
        super(ctx, as);

        colB1 = ContextCompat.getColor(ctx, R.color.semaforB1Color);
        colB2 = ContextCompat.getColor(ctx, R.color.semaforB2Color);
        colV1 = ContextCompat.getColor(ctx, R.color.semaforV1Color);
        colV2 = ContextCompat.getColor(ctx, R.color.semaforV2Color);

        pStroke = new Paint();
        pStroke.setColor(colB1);
        pStroke.setAntiAlias(true);
        pStroke.setStrokeCap(Cap.ROUND);

        pFill = new Paint();
        pFill.setStyle(Style.FILL);
        pFill.setAntiAlias(true);

        path[0] = new Path();
        path[0].moveTo(0, 2);
        path[0].lineTo(1, 3);
        path[0].lineTo(0, 3);
        path[0].close();
        path[1] = new Path();
        path[1].moveTo(0, 2);
        path[1].lineTo(1, 3);
        path[1].lineTo(1, 2);
        path[1].close();
        path[2] = new Path();
        path[2].moveTo(0, 2);
        path[2].lineTo(-1, 3);
        path[2].lineTo(0, 3);
        path[2].close();
        path[3] = new Path();
        path[3].moveTo(0, 2);
        path[3].lineTo(-1, 3);
        path[3].lineTo(-1, 2);
        path[3].close();
    }

    public void setIn(int x) {
        int i;
        s1 = -1;
        s2 = -1;
        for (i = 0; i < 8; i++)
            if ((x & (1 << i)) != 0) {
                s1 = i;
                break;
            }
        for (i++; i < 8; i++)
            if ((x & (1 << i)) != 0) {
                s2 = i;
                break;
            }
        for (i++; i < 8; i++)
            if ((x & (1 << i)) != 0) {
                s2 = -1; // More than two bits: invalid
                break;
            }
        valid = (s1 >= 0) && (s2 >= 0);
        if (s1 == 0 && s2 <= 4) {
            s1 = s2;
            s2 = 8;
        }
        if (s2 == 2) {
            s2 = s1;
            s1 = 2;
        }
        if (s1 == 6) {
            s1 = s2;
            s2 = 6;
        }
        invalidate();
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        if (!valid) return;

        float w = getWidth() - getPaddingLeft() - getPaddingRight();
        float h = getHeight() - getPaddingTop() - getPaddingBottom();
        float u = w / 7;
        float wd1, wd2;
        wd1 = w / 10;
        wd2 = Math.max(Utils.dpToPix(3), w / 32);
        c.translate(getPaddingLeft(), getPaddingTop());

        pStroke.setColor(colB1);
        pStroke.setStrokeWidth(wd1);
        c.drawLine(w / 2, h / 2 + 2 * u - wd1 / 2, w / 2, h / 2, pStroke);
        pFill.setColor(colB1);
        c.drawCircle(w / 2, h / 2 - u, wd1 / 2, pFill);

        pStroke.setColor(colB2);
        pStroke.setStrokeWidth(wd2);
        c.save();
        c.translate(w / 2 - u / 2, h / 2);
        c.rotate(45 * s1);
        c.drawLine(0, 0, 0, 3 * u - wd2 / 2, pStroke);
        c.scale(u, u);
        pFill.setColor(colV1);
        c.drawPath(path[(s1 <= 4) ? 0 : 2], pFill);
        pFill.setColor(colV2);
        c.drawPath(path[(s1 <= 4) ? 1 : 3], pFill);

        c.restore();
        c.translate(w / 2 + u / 2, h / 2);
        c.scale(-1, 1);
        c.rotate(45 * (8 - s2));
        c.drawLine(0, 0, 0, 3 * u - wd2 / 2, pStroke);
        c.scale(u, u);
        pFill.setColor(colV1);
        c.drawPath(path[((8 - s2) <= 4) ? 0 : 2], pFill);
        pFill.setColor(colV2);
        c.drawPath(path[((8 - s2) <= 4) ? 1 : 3], pFill);
    }

    public void drawOnWhite(Canvas c) {
        colB1 = Color.BLACK;
        colB2 = Color.BLACK;
        draw(c);
        colB1 = ContextCompat.getColor(getContext(), R.color.semaforB1Color);
        colB2 = ContextCompat.getColor(getContext(), R.color.semaforB2Color);
    }
}
