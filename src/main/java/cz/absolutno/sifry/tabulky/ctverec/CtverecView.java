package cz.absolutno.sifry.tabulky.ctverec;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.decoder.AlphabetDecoder;
import cz.absolutno.sifry.common.decoder.Decoder;
import cz.absolutno.sifry.tabulky.AidView;

public final class CtverecView extends AidView {

    private int w, h;
    private final int wd;
    private float sx, sy;
    private final Paint pLine, pText, pTextSmall;
    private Alphabet abc;
    private PathEffect dashEffect;

    private int lastAidIx = -1;

    public CtverecView(Context ctx, AttributeSet as) {
        super(ctx, as);

        wd = Utils.dpToPix(3);

        pLine = new Paint();
        pLine.setColor(ContextCompat.getColor(ctx, R.color.mainColor));
        pLine.setStrokeWidth(wd);
        pLine.setStrokeCap(Cap.ROUND);
        pLine.setStyle(Style.STROKE);
        pLine.setAntiAlias(true);

        pText = new Paint();
        pText.setAntiAlias(true);
        pText.setTypeface(Typeface.DEFAULT);
        pText.setColor(ContextCompat.getColor(ctx, R.color.priColor));
        pText.setTextAlign(Align.CENTER);

        pTextSmall = new Paint(pText);
        pTextSmall.setColor(ContextCompat.getColor(ctx, R.color.secColor));

        abc = Alphabet.getVariantInstance(25, "");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        sx = w / 7f;
        sy = w / 7f;
        dashEffect = new DashPathEffect(new float[]{w * 3 / 70f, w * 3 / 70f}, w * 3 / 140f);
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        c.drawRect(w / 2 - 2.5f * sx, h / 2 - 2.5f * sy, w / 2 + 2.5f * sx, h / 2 + 2.5f * sy, pLine);
        pLine.setPathEffect(dashEffect);
        c.drawRect(w / 2 - 1.5f * sx, h / 2 - 1.5f * sy, w / 2 + 1.5f * sx, h / 2 + 1.5f * sy, pLine);
        pLine.setPathEffect(null);

        pText.setTextSize(Math.min(sy - 3 * wd, getResources().getDimensionPixelSize(R.dimen.defTextSize)));
        pTextSmall.setTextSize(Math.min(sy - 5 * wd, 0.8f * getResources().getDimensionPixelSize(R.dimen.defTextSize)));

        int ix;
        for (int x = 0; x < 5; x++)
            for (int y = 0; y < 5; y++) {
                ix = y * 5 + x;
                c.drawText(abc.chr(ix), w / 2 - 2 * sx + x * sx, h / 2 - 2 * sy + y * sy - (pText.ascent() + pText.descent()) / 2, pText);
            }

        for (int x = 0; x < 5; x++) {
            c.drawText(String.valueOf(x + 1), w / 2 - 2 * sx + x * sx, h / 2 - 3 * sy - (pText.ascent() + pText.descent()) / 2, pTextSmall);
            c.drawText(String.valueOf(x + 1), w / 2 - 3 * sx, h / 2 - 2 * sy + x * sy - (pText.ascent() + pText.descent()) / 2, pTextSmall);
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
                    if (oil != null) oil.onInput(ix, abc.chr(ix));
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
                    final int xc = (int) (w / 2 - 2 * sx + sx * (ix % 5));
                    final int yc = (int) (h / 2 - 2 * sy + sy * (ix / 5));
                    setAid(abc.chr(ix));
                    showAid(xc, yc);
                    playSoundEffect(SoundEffectConstants.CLICK);
                }
                lastAidIx = ix;
                return true;
        }
        return false;
    }

    private int index(float xc, float yc) {
        int xi = Utils.floor((xc - w / 2 + 2.5f * sx) / sx);
        int yi = Utils.floor((yc - h / 2 + 2.5f * sy) / sy);
        if (xi >= 0 && xi < 5 && yi >= 0 && yi < 5)
            return yi * 5 + xi;
        else
            return -1;
    }

    public void setVar(String var) {
        abc = Alphabet.getVariantInstance(25, var);
        invalidate();
    }

    public String chr(int i) {
        return abc.chr(i);
    }

    @SuppressWarnings("unused")
    public Decoder getDecoder() {
        return new AlphabetDecoder(abc);
    }

}
