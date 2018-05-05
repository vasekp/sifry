package cz.absolutno.sifry.tabulky.polsky;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class PolskyKlasView extends PolskyView {

    private int w, h, sx, sy;
    private final int wd;
    private final Paint pLine, pText;

    private int lastAidIx = -1;

    public PolskyKlasView(Context ctx, AttributeSet as) {
        super(ctx, as);

        wd = Utils.dpToPix(3);

        pLine = new Paint();
        pLine.setColor(ContextCompat.getColor(ctx, R.color.mainColor));
        pLine.setStrokeWidth(wd);
        pLine.setStrokeCap(Cap.ROUND);
        pLine.setAntiAlias(true);

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
        sx = w / 3;
        sy = h / 3;
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        for (int x = -1; x <= 1; x++)
            for (int y = -1; y <= 1; y++) {
                if (x >= 0)
                    c.drawLine(w / 2 + sx * x - .5f * sx + 1.5f * wd, h / 2 + sy * y - .5f * sy + 1.5f * wd,
                            w / 2 + sx * x - .5f * sx + 1.5f * wd, h / 2 + sy * y + .5f * sy - 1.5f * wd, pLine);
                if (x <= 0)
                    c.drawLine(w / 2 + sx * x + .5f * sx - 1.5f * wd, h / 2 + sy * y - .5f * sy + 1.5f * wd,
                            w / 2 + sx * x + .5f * sx - 1.5f * wd, h / 2 + sy * y + .5f * sy - 1.5f * wd, pLine);
                if (y >= 0)
                    c.drawLine(w / 2 + sx * x - .5f * sx + 1.5f * wd, h / 2 + sy * y - .5f * sy + 1.5f * wd,
                            w / 2 + sx * x + .5f * sx - 1.5f * wd, h / 2 + sy * y - .5f * sy + 1.5f * wd, pLine);
                if (y <= 0)
                    c.drawLine(w / 2 + sx * x - .5f * sx + 1.5f * wd, h / 2 + sy * y + .5f * sy - 1.5f * wd,
                            w / 2 + sx * x + .5f * sx - 1.5f * wd, h / 2 + sy * y + .5f * sy - 1.5f * wd, pLine);
            }

        pText.setTextSize(Math.min(sy - 5 * wd, getResources().getDimensionPixelSize(R.dimen.defTextSize)));
        int x;
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                for (int k = -1; k <= 1; k++) {
                    x = (i + 1) * 9 + (j + 1) * 3 + (k + 1);
                    c.drawText(pk.decode(x), w / 2 + j * sx + k * 0.3f * sx, h / 2 + i * sy - (pText.ascent() + pText.descent()) / 2, pText);
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
                    final int ix3 = ix / 3;
                    final int xc = w / 2 - sx + sx * (ix3 % 3);
                    final int yc = h / 2 - sy + sy * (ix / 9);
                    if (ix3 != lastAidIx / 3) {
                        hideAid();
                        setAidEntries(new String[]{pk.decode(3 * ix3), pk.decode(3 * ix3 + 1), pk.decode(3 * ix3 + 2)});
                    }
                    if (ix != lastAidIx) {
                        playSoundEffect(SoundEffectConstants.CLICK);
                        setAidHighlight(ix % 3);
                    }
                    showAid(xc, yc);
                }
                lastAidIx = ix;
                return true;
        }
        return false;
    }

    private int index(float xc, float yc) {
        int xi = Utils.floor((xc - w / 2 + 1.5f * sx) / sx * 3);
        int yi = Utils.floor((yc - h / 2 + 1.5f * sy) / sy);
        if (xi >= 0 && xi < 9 && yi >= 0 && yi < 3)
            return yi * 9 + xi;
        else
            return -1;
    }

}
