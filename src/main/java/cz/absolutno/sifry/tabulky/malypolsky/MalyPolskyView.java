package cz.absolutno.sifry.tabulky.malypolsky;

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
import cz.absolutno.sifry.common.decoder.Decoder;
import cz.absolutno.sifry.tabulky.AidView;

public final class MalyPolskyView extends AidView {

    private int w, h, sx, sy;
    private final int wd;
    private final Paint pLine, pFill, pText;
    private MalyPolskyKrizDecoder mpk = null;

    private int lastAidIx = -1;

    public MalyPolskyView(Context ctx, AttributeSet as) {
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
        sx = w / 7;
        sy = h / 7;
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        pText.setTextSize(Math.min(sy - 5 * wd, getResources().getDimensionPixelSize(R.dimen.defTextSize)));

        for (int x = -1; x <= 1; x++)
            for (int y = -1; y <= 1; y++) {
                if (x >= 0)
                    c.drawLine(w / 4 + sx * x - .5f * sx + 1.5f * wd, h / 4 + sy * y - .5f * sy + 1.5f * wd,
                            w / 4 + sx * x - .5f * sx + 1.5f * wd, h / 4 + sy * y + .5f * sy - 1.5f * wd, pLine);
                if (x <= 0)
                    c.drawLine(w / 4 + sx * x + .5f * sx - 1.5f * wd, h / 4 + sy * y - .5f * sy + 1.5f * wd,
                            w / 4 + sx * x + .5f * sx - 1.5f * wd, h / 4 + sy * y + .5f * sy - 1.5f * wd, pLine);
                if (y >= 0)
                    c.drawLine(w / 4 + sx * x - .5f * sx + 1.5f * wd, h / 4 + sy * y - .5f * sy + 1.5f * wd,
                            w / 4 + sx * x + .5f * sx - 1.5f * wd, h / 4 + sy * y - .5f * sy + 1.5f * wd, pLine);
                if (y <= 0)
                    c.drawLine(w / 4 + sx * x - .5f * sx + 1.5f * wd, h / 4 + sy * y + .5f * sy - 1.5f * wd,
                            w / 4 + sx * x + .5f * sx - 1.5f * wd, h / 4 + sy * y + .5f * sy - 1.5f * wd, pLine);
            }

        for (int x = -1; x <= 1; x++)
            for (int y = -1; y <= 1; y++) {
                if (x >= 0)
                    c.drawLine(3 * w / 4 + sx * x - .5f * sx + 1.5f * wd, h / 4 + sy * y - .5f * sy + 1.5f * wd,
                            3 * w / 4 + sx * x - .5f * sx + 1.5f * wd, h / 4 + sy * y + .5f * sy - 1.5f * wd, pLine);
                if (x <= 0)
                    c.drawLine(3 * w / 4 + sx * x + .5f * sx - 1.5f * wd, h / 4 + sy * y - .5f * sy + 1.5f * wd,
                            3 * w / 4 + sx * x + .5f * sx - 1.5f * wd, h / 4 + sy * y + .5f * sy - 1.5f * wd, pLine);
                if (y >= 0)
                    c.drawLine(3 * w / 4 + sx * x - .5f * sx + 1.5f * wd, h / 4 + sy * y - .5f * sy + 1.5f * wd,
                            3 * w / 4 + sx * x + .5f * sx - 1.5f * wd, h / 4 + sy * y - .5f * sy + 1.5f * wd, pLine);
                if (y <= 0)
                    c.drawLine(3 * w / 4 + sx * x - .5f * sx + 1.5f * wd, h / 4 + sy * y + .5f * sy - 1.5f * wd,
                            3 * w / 4 + sx * x + .5f * sx - 1.5f * wd, h / 4 + sy * y + .5f * sy - 1.5f * wd, pLine);
                c.drawCircle(3 * w / 4 + (sx + wd / 2) * x, h / 4 + (sy + wd / 2) * y, sy / 4, pFill);
            }

        c.save();
        c.rotate(45.0f, w / 4, 3 * h / 4);
        for (int x = -1; x <= 1; x += 2)
            for (int y = -1; y <= 1; y += 2) {
                c.drawLine(w / 4 + 1.5f * wd * x, 3 * h / 4 + 1.5f * wd * y, w / 4 + sx * x, 3 * h / 4 + 1.5f * wd * y, pLine);
                c.drawLine(w / 4 + 1.5f * wd * x, 3 * h / 4 + 1.5f * wd * y, w / 4 + 1.5f * wd * x, 3 * h / 4 + sy * y, pLine);
            }
        c.restore();

        c.save();
        c.rotate(45.0f, 3 * w / 4, 3 * h / 4);
        for (int x = -1; x <= 1; x += 2)
            for (int y = -1; y <= 1; y += 2) {
                c.drawLine(3 * w / 4 + 1.5f * wd * x, 3 * h / 4 + 1.5f * wd * y, 3 * w / 4 + sx * x, 3 * h / 4 + 1.5f * wd * y, pLine);
                c.drawLine(3 * w / 4 + 1.5f * wd * x, 3 * h / 4 + 1.5f * wd * y, 3 * w / 4 + 1.5f * wd * x, 3 * h / 4 + sy * y, pLine);
                c.drawCircle(3 * w / 4 + (sx / 2 + wd / 2) * x, 3 * h / 4 + (sy / 2 + wd / 2) * y, sy / 4, pFill);
            }
        c.restore();

        for (int xq = 0; xq <= 1; xq++) {
            for (int y = -1; y <= 1; y++)
                for (int x = -1; x <= 1; x++) {
                    PointF pf = getPoint(x, y, xq, 0);
                    c.drawText(mpk.decode(x, y, xq, 0), pf.x, pf.y - (pText.ascent() + pText.descent()) / 2, pText);
                }
        }
        for (int xq = 0; xq <= 1; xq++) {
            for (int y = -1; y <= 1; y += 2)
                for (int x = -1; x <= 1; x += 2) {
                    PointF pf = getPoint(x, y, xq, 1);
                    c.drawText(mpk.decode(x, y, xq, 1), pf.x, pf.y - (pText.ascent() + pText.descent()) / 2, pText);
                }
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
                    if (oil != null) oil.onInput(ix, mpk.decode(ix));
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
                    setAid(mpk.decode(ix));
                    showAid(pf.x, pf.y);
                    playSoundEffect(SoundEffectConstants.CLICK);
                }
                lastAidIx = ix;
                return true;
        }
        return false;
    }

    private int index(float xc, float yc) {
        int xq = Utils.floor(xc * 2 / w), yq = Utils.floor(yc * 2 / h);
        float xt = (xc - xq * w / 2) - w / 4, yt = (yc - yq * h / 2) - h / 4;
        if (yq == 0) {
            int xi = Utils.floor((xt + 1.5f * sx) / sx);
            int yi = Utils.floor((yt + 1.5f * sy) / sy);
            if (xi >= 0 && xi < 3 && yi >= 0 && yi < 3)
                return MalyPolskyKrizDecoder.buildInt(xi - 1, yi - 1, xq, yq);
            else
                return -1;
        } else {
            float xr = (yt - xt) / (float) (Math.sqrt(2)) / 1.5f; /* Little larger space */
            float yr = (yt + xt) / (float) (Math.sqrt(2)) / 1.5f;
            int xi = Utils.floor((xr + sx) / sx);
            int yi = Utils.floor((yr + sy) / sy);
            if (xi >= 0 && xi < 2 && yi >= 0 && yi < 2)
                return MalyPolskyKrizDecoder.buildInt(xi * 2 - 1, yi * 2 - 1, xq, yq);
            else
                return -1;
        }
    }

    private PointF getPoint(int x, int y, int xq, int yq) {
        if (yq == 0) {
            return new PointF((xq == 0 ? 1 : 3) * w / 4 + x * (sx + wd / 2), h / 4 + y * (sy + wd / 2));
        } else {
            float tx, ty;
            tx = (-x + y) / (float) (Math.sqrt(2)) / 2;
            ty = (x + y) / (float) (Math.sqrt(2)) / 2;
            return new PointF((xq == 0 ? 1 : 3) * w / 4 + tx * (sx + wd), 3 * h / 4 + ty * (sy + wd));
        }
    }

    private PointF getPoint(int x) {
        int[] parse = MalyPolskyKrizDecoder.parseInt(x);
        return getPoint(parse[0], parse[1], parse[2], parse[3]);
    }

    public void setVar(int var, String abcVar) {
        mpk = new MalyPolskyKrizDecoder(var, abcVar);
        invalidate();
    }

    public String chr(int i) {
        return mpk.decode(i);
    }

    @SuppressWarnings("unused")
    public Decoder getDecoder() {
        return mpk;
    }
}
