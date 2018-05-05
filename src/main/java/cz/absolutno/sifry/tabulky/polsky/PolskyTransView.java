package cz.absolutno.sifry.tabulky.polsky;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class PolskyTransView extends PolskyView {

    private int w, h, sx, sy;
    private final int wd;
    private float r;
    private final Paint pLine, pFill, pText;

    private int lastAidIx = -1;

    public PolskyTransView(Context ctx, AttributeSet as) {
        super(ctx, as);

        wd = Utils.dpToPix(3);

        pLine = new Paint();
        pLine.setColor(ContextCompat.getColor(ctx, R.color.mainColor));
        pLine.setStrokeWidth(wd);
        pLine.setStrokeCap(Cap.ROUND);
        pLine.setStyle(Style.STROKE);
        pLine.setAntiAlias(true);

        pFill = new Paint(pLine);
        pFill.setStyle(Style.FILL);

        pText = new Paint();
        pText.setAntiAlias(true);
        pText.setTypeface(Typeface.DEFAULT);
        pText.setColor(ContextCompat.getColor(ctx, R.color.priColor));
        pText.setTextAlign(Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        sx = w / 10;
        sy = h / 4;
        r = sy / 4;
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        pText.setTextSize(Math.min(sy - 5 * wd, getResources().getDimensionPixelSize(R.dimen.defTextSize)));

        for (int z = 0; z < 3; z++)
            for (int x = -1; x <= 1; x++)
                for (int y = -1; y <= 1; y++) {
                    PointF pf = getPoint(z * 9 + (y + 1) * 3 + (x + 1));
                    if (x >= 0) c.drawLine(pf.x - .5f * sx + 1.5f * wd, pf.y - .5f * sy + 1.5f * wd,
                            pf.x - .5f * sx + 1.5f * wd, pf.y + .5f * sy - 1.5f * wd, pLine);
                    if (x <= 0) c.drawLine(pf.x + .5f * sx - 1.5f * wd, pf.y - .5f * sy + 1.5f * wd,
                            pf.x + .5f * sx - 1.5f * wd, pf.y + .5f * sy - 1.5f * wd, pLine);
                    if (y >= 0) c.drawLine(pf.x - .5f * sx + 1.5f * wd, pf.y - .5f * sy + 1.5f * wd,
                            pf.x + .5f * sx - 1.5f * wd, pf.y - .5f * sy + 1.5f * wd, pLine);
                    if (y <= 0) c.drawLine(pf.x - .5f * sx + 1.5f * wd, pf.y + .5f * sy - 1.5f * wd,
                            pf.x + .5f * sx - 1.5f * wd, pf.y + .5f * sy - 1.5f * wd, pLine);
                }

        for (int z = 1; z < 3; z++)
            for (int i = 0; i < z; i++)
                c.drawCircle(w / 2 + sx * 3.5f * (z - 1) + 1.5f * r * (2 * i - z + 1), h / 2 + 1.5f * sy, r, pFill);

        for (int ix = 0; ix < 27; ix++) {
            PointF pf = getPoint(ix);
            c.drawText(pk.decode(ix), pf.x, pf.y - (pText.ascent() + pText.descent()) / 2, pText);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int cnt = e.getPointerCount();
        if (cnt != 1) return false;

        final int ix = index(e.getX(), e.getY());

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                if (ix >= 0) {
                    if (oil != null) oil.onInput(ix, pk.decode(ix));
                    if (!isAidActive()) playSoundEffect(SoundEffectConstants.CLICK);
                }
            case MotionEvent.ACTION_CANCEL:
                hideAid();
                lastAidIx = -1;
                return true;
            case MotionEvent.ACTION_DOWN:
                hideAid();
                lastAidIx = -1;
            case MotionEvent.ACTION_MOVE:
                if (!isAidActive()) return true;
                if (ix == lastAidIx) return true;
                if (ix == -1)
                    hideAid();
                else {
                    PointF pf = getPoint(ix);
                    setAid(pk.decode(ix));
                    showAid(pf.x, pf.y);
                    playSoundEffect(SoundEffectConstants.CLICK);
                }
                lastAidIx = ix;
                return true;
        }
        return false;
    }

    private int index(float xc, float yc) {
        int z = Utils.floor(3 * xc / w);
        int xi = Utils.floor((xc - 3.5f * sx * (z - 1) - w / 2 + 1.5f * sx) / sx);
        int yi = Utils.floor(yc / sy);
        if (xi >= 0 && xi < 3 && yi >= 0 && yi < 3)
            return z * 9 + yi * 3 + xi;
        else
            return -1;
    }

    private PointF getPoint(int ix) {
        int x = ix % 3, y = (ix / 3) % 3, z = ix / 9;
        return new PointF(w / 2 + sx * 3.5f * (z - 1) + sx * (x - 1), h / 2 - 1.5f * sy + y * sy);
    }

}
