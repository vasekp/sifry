package cz.absolutno.sifry.tabulky.mobil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.tabulky.AidView;

public final class MobilView extends AidView {

    private int w, h, sx, sy;
    private final int wd;
    private final int priColor, secColor;
    private final Paint pLine, pText, pTextSmall;
    private final MobilDecoder md;

    private int lastAidIx = -1;
    private int hl = 0;

    private final Handler hnd;

    public MobilView(Context ctx, AttributeSet as) {
        super(ctx, as);

        wd = Utils.dpToPix(3);
        priColor = ContextCompat.getColor(ctx, R.color.priColor);
        secColor = ContextCompat.getColor(ctx, R.color.secColor);

        pLine = new Paint();
        pLine.setColor(ContextCompat.getColor(ctx, R.color.mainColor));
        pLine.setStrokeWidth(wd);
        pLine.setStrokeCap(Cap.ROUND);
        pLine.setAntiAlias(true);

        pText = new Paint();
        pText.setAntiAlias(true);
        pText.setTypeface(Typeface.DEFAULT);
        pText.setColor(priColor);
        pText.setTextAlign(Align.CENTER);

        pTextSmall = new Paint(pText);
        pTextSmall.setColor(secColor);

        md = new MobilDecoder();
        hnd = new Handler();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        sx = (w - 2 * wd) / 3;
        sy = (h - 2 * wd) / 3;
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        for (int x = 0; x <= 3; x++)
            c.drawLine(w / 2 - 1.5f * sx + sx * x, h / 2 - 1.5f * sy, w / 2 - 1.5f * sx + sx * x, h / 2 + 1.5f * sy, pLine);
        for (int y = 0; y <= 3; y++)
            c.drawLine(w / 2 - 1.5f * sx, h / 2 - 1.5f * sy + y * sy, w / 2 + 1.5f * sx, h / 2 - 1.5f * sy + y * sy, pLine);

        pText.setTextSize(sy - 3 * wd);
        int cis;
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                cis = 3 * y + x;
                c.drawText(String.valueOf(cis + 1), w / 2 - 1.5f * sx + x * sx + sx / 6, h / 2 - sy + y * sy - (pText.ascent() + pText.descent()) / 2, pText);
            }
        }

        pTextSmall.setTextSize(Math.min(sy - 3 * wd, getResources().getDimensionPixelSize(R.dimen.defTextSize)));
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                cis = 3 * y + x;
                final String[] st = md.getLetters(cis);
                int n = st.length;
                for (int i = 0; i < n; i++) {
                    if (!isAidActive())
                        if (cis == lastAidIx && i == hl) pTextSmall.setColor(priColor);
                    c.drawText(st[i], w / 2 - 1.5f * sx + x * sx + sx / 3 + sx * 2 / 3 * (i + 1) / (n + 1), h / 2 - sy + y * sy - (pText.ascent() + pText.descent()) / 2, pTextSmall);
                    if (!isAidActive())
                        pTextSmall.setColor(secColor);
                }
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
                hnd.postDelayed(enterRunnable, 500);
                break;
            case MotionEvent.ACTION_CANCEL:
                hnd.removeCallbacks(enterRunnable);
                hideAid();
                lastAidIx = -1;
                if (!isAidActive()) invalidate();
                return true;
            case MotionEvent.ACTION_DOWN:
                if (ix > 0 && ix == lastAidIx) {
                    hl++;
                    hl %= md.getNumLetters(ix);
                    playSoundEffect(SoundEffectConstants.CLICK);
                    setAidHighlight(hl);
                    if (!isAidActive()) invalidate();
                } else if (lastAidIx > 0 && oil != null)
                    oil.onInput(lastAidIx * 4 + hl, md.getLetter(lastAidIx, hl));
            case MotionEvent.ACTION_MOVE:
                hnd.removeCallbacks(enterRunnable);
                if (ix == lastAidIx) return true;
                if (ix <= 0) {
                    hideAid();
                    lastAidIx = ix;
                    if (!isAidActive()) invalidate();
                    return true;
                }
                final int xc = w / 2 - sx + sx * (ix % 3);
                final int yc = h / 2 - sy + sy * (ix / 3);
                hideAid();
                setAidEntries(md.getLetters(ix));
                setAidHighlight(0);
                if (isAidActive()) showAid(xc, yc);
                playSoundEffect(SoundEffectConstants.CLICK);
                hl = 0;
                lastAidIx = ix;
                if (!isAidActive()) invalidate();
                return true;
        }
        return false;
    }

    private final Runnable enterRunnable = new Runnable() {
        public void run() {
            if (lastAidIx > 0) {
                if (oil != null)
                    oil.onInput(lastAidIx * 4 + hl, md.getLetter(lastAidIx, hl));
                lastAidIx = -1;
                if (!isAidActive()) invalidate();
                hideAid();
            }
        }
    };

    private int index(float xc, float yc) {
        int xi = Utils.floor((xc - w / 2 + 1.5f * sx) / sx);
        int yi = Utils.floor((yc - h / 2 + 1.5f * sy) / sy);
        if (xi >= 0 && xi < 3 && yi >= 0 && yi < 3)
            return yi * 3 + xi;
        else
            return -1;
    }

}
