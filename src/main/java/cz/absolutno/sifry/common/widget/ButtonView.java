package cz.absolutno.sifry.common.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public class ButtonView extends View {

    private int w, h, sz, sep;
    private final Paint p;
    private OnInputListener oil = null;
    private int rd = 1, sl = 1, poc;
    private int pressed;
    private final boolean alignLeft;

    private static final int[] state_pressed = new int[]{android.R.attr.state_pressed}; // for onDraw()

    public ButtonView(Context ctx, AttributeSet as) {
        super(ctx, as);

        p = new Paint();
        p.setAntiAlias(true);
        p.setTypeface(Typeface.DEFAULT);
        int color = (isInEditMode() ? Color.WHITE : ContextCompat.getColor(ctx, R.color.priColor));
        try {
            TypedValue tv = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.buttonStyle, tv, true);
            TypedArray a = getContext().obtainStyledAttributes(tv.data, new int[]{android.R.attr.textColor});
            color = a.getColor(0, color);
            a.recycle();
        } catch (Resources.NotFoundException ignored) { }
        p.setColor(color);
        p.setTextAlign(Align.CENTER);

        alignLeft = as.getAttributeBooleanValue(App.XMLNS, "alignLeft", false);
    }

    @Override
    protected final void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
    }

    @Override
    public final void onDraw(Canvas c) {
        super.onDraw(c);

        sep = getResources().getDimensionPixelSize(R.dimen.cislaPadding);
        sz = Math.min((w - (sl - 1) * sep) / sl, (h - (rd - 1) * sep) / rd);
        if (sz < getResources().getDimensionPixelSize(R.dimen.cislaMinSize)) {
            sep = 0;
            sz = Math.min((w - (sl - 1) * sep) / sl, (h - (rd - 1) * sep) / rd);
        }

        StateListDrawable sld = (StateListDrawable) ContextCompat.getDrawable(getContext(), R.drawable.btn_mytheme);
        assert sld != null;
        Drawable dNormal = sld.getCurrent();
        sld.setState(state_pressed);
        Drawable dPressed = sld.getCurrent();

        p.setTextSize(Math.min(0.8f * sz, getResources().getDimensionPixelSize(R.dimen.defTextSize)));
        for (int ix = 0; ix < poc; ix++) {
            Drawable d = (ix == pressed) ? dPressed : dNormal;
            Rect r = getRect(ix);
            if (isInEditMode())
                d.setBounds(r.left, r.top, r.left + r.right, r.top + r.bottom);
            else
                d.setBounds(r);
            d.draw(c);
            c.drawText(getIxText(ix), r.centerX(), r.centerY() - (p.ascent() + p.descent()) / 2, p);
        }
    }

    private Rect getRect(int ix) {
        final Rect r = new Rect();
        final RectF rf = getRectF(ix);
        if (alignLeft) {
            r.left = (int) ((sz + sep) * rf.left) + sep / 2;
            r.right = (int) ((sz + sep) * rf.right) - sep / 2;
        } else {
            r.left = w / 2 - sl * (sz + sep) / 2 + (int) ((sz + sep) * rf.left) + sep / 2;
            r.right = w / 2 - sl * (sz + sep) / 2 + (int) ((sz + sep) * rf.right) - sep / 2;
        }
        r.top = h / 2 - rd * (sz + sep) / 2 + (int) ((sz + sep) * rf.top) + sep / 2;
        r.bottom = h / 2 - rd * (sz + sep) / 2 + (int) ((sz + sep) * rf.bottom) - sep / 2;
        return r;
    }

    protected RectF getRectF(int ix) {
        final RectF rf = new RectF();
        final int x = ix % sl, y = ix / sl;
        rf.set(x, y, x + 1, y + 1);
        return rf;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public final boolean onTouchEvent(MotionEvent e) {
        int cnt = e.getPointerCount();
        if (cnt != 1) return false;
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                pressed = -1;
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        invalidate();
                    }
                }, 100);
                return true;
            case MotionEvent.ACTION_CANCEL:
                pressed = -1;
                invalidate();
                return true;
            case MotionEvent.ACTION_DOWN:
                int ix = index(e.getX(), e.getY());
                if (ix != -1) {
                    playSoundEffect(SoundEffectConstants.CLICK);
                    pressed = ix;
                    if (oil != null) oil.onInput(getIxTag(ix), getIxText(ix));
                    invalidate();
                    return true;
                }
        }
        return false;
    }

    private int index(float xc, float yc) {
        for (int ix = 0; ix < poc; ix++)
            if (getRect(ix).contains((int) xc, (int) yc))
                return ix;
        return -1;
    }

    public final void setSize(int rd, int sl, int poc) {
        this.rd = rd;
        this.sl = sl;
        this.poc = poc;
        this.pressed = -1;
        setMinimumWidth(sl * Utils.dpToPix(50));
        setMinimumHeight(rd * Utils.dpToPix(50));
        requestLayout();
        invalidate();
    }

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        width = (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(widthMeasureSpec) : getSuggestedMinimumWidth();
        height = (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(heightMeasureSpec) : getSuggestedMinimumHeight();
        setMeasuredDimension(width, height);
    }

    protected int getIxTag(int ix) {
        return ix;
    }

    protected String getIxText(int ix) {
        return String.valueOf(ix + 1);
    }

    public final void setOnInputListener(OnInputListener oil) {
        this.oil = oil;
    }

    public interface OnInputListener {
        void onInput(int tag, String text);
    }

}
