package cz.absolutno.sifry.semafor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class SemaforView extends View {

    private float w, h, r;
    private final float wd;

    private final Paint pStroke, pDash, pFill;
    private final boolean[] in = new boolean[8];
    private OnInputListener oil = null;

    private boolean tracking = false;
    private int lastDole = 0;

    private final Handler hnd = new Handler();

    public SemaforView(Context ctx, AttributeSet as) {
        super(ctx, as);

        wd = Utils.dpToPix(3);

        pDash = new Paint();
        pDash.setColor(ContextCompat.getColor(ctx, R.color.mainColor));
        pDash.setStrokeWidth(wd);
        pDash.setStrokeCap(Cap.ROUND);
        pDash.setStyle(Style.STROKE);
        pDash.setAntiAlias(true);

        pStroke = new Paint(pDash);
        pStroke.setStrokeWidth(2 * wd);

        pFill = new Paint(pDash);
        pFill.setStyle(Style.FILL);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int cnt = e.getPointerCount();
        int action = e.getActionMasked();
        if (cnt == 0) return false;
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
                clear();
                if (oil != null) oil.onInput(0, 0);
                return true;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (cnt == 1) tracking = true;
            case MotionEvent.ACTION_MOVE:
                if (!tracking) return true;
                hnd.removeCallbacks(clearRunnable);
                if (dole() >= 2) {
                    clear();
                    tracking = true;
                }
                int pointerIndex = e.getActionIndex();
                int ix = index(e.getX(pointerIndex), e.getY(pointerIndex));
                if (ix >= 0) {
                    if (action == MotionEvent.ACTION_DOWN && in[ix]) {
                        in[ix] = false;
                        tracking = false;
                    } else
                        in[ix] = true;
                }
                int d = dole();
                if (d != lastDole) {
                    playSoundEffect(SoundEffectConstants.CLICK);
                    if (oil != null) oil.onInput(getVal(), d);
                    lastDole = d;
                }
                if (d == 2) {
                    tracking = false;
                    hnd.postDelayed(clearRunnable, 500);
                }
                invalidate();
                return true;
        }
        return false;
    }

    private final Runnable clearRunnable = new Runnable() {
        public void run() {
            clear();
        }
    };

    private int dole() {
        int dole = 0;
        for (int i = 0; i < 8; i++)
            if (in[i])
                dole++;
        return dole;
    }

    public void clear() {
        for (int i = 0; i < 8; i++)
            in[i] = false;
        tracking = false;
        lastDole = 0;
        invalidate();
    }

    public boolean isActive() {
        return dole() != 0;
    }

    private int getVal() {
        int x = 0;
        for (int i = 0; i < 8; i++)
            if (in[i])
                x += 1 << i;
        return x;
    }

    public void setVal(int x) {
        clear();
        for (int i = 0; i < 8; i++)
            if (x == 1 << i) {
                in[i] = true;
                lastDole = 1;
                invalidate();
            }
        if (oil != null) oil.onInput(x, lastDole);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        r = h / 15;
        float o = 2 * (float) Math.PI * r;
        pDash.setPathEffect(new DashPathEffect(new float[]{o / 48, o / 16}, 0));
        pStroke.setShader(new RadialGradient(w / 2, h / 2, h * 0.35f - r, 0,
                ContextCompat.getColor(getContext(), R.color.mainColor), Shader.TileMode.CLAMP));
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        for (int i = 0; i < 8; i++)
            drawOne(c, i, in[i]);
    }

    private void drawOne(Canvas c, int ix, boolean b) {
        float x, y;
        x = w / 2 - 0.35f * h * (float) (Math.sin(Math.PI * ix / 4));
        y = h / 2 + 0.35f * h * (float) (Math.cos(Math.PI * ix / 4));
        c.drawCircle(x, y, r, pDash);
        if (b) {
            c.drawLine(w / 2, h / 2, x, y, pStroke);
            c.drawCircle(x, y, r - 2 * wd, pFill);
        }
    }

    private int index(float xc, float yc) {
        float x, y;
        for (int ix = 0; ix < 8; ix++) {
            x = w / 2 - 0.35f * h * (float) (Math.sin(Math.PI * ix / 4));
            y = h / 2 + 0.35f * h * (float) (Math.cos(Math.PI * ix / 4));
            if ((x - xc) * (x - xc) + (y - yc) * (y - yc) < 2 * r * r) return ix;
        }
        return -1;
    }

    public interface OnInputListener {
        void onInput(int x, int c);
    }

    public void setOnInputListener(OnInputListener oil) {
        this.oil = oil;
    }

}
