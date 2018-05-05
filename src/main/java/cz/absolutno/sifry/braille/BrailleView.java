package cz.absolutno.sifry.braille;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class BrailleView extends View {

    private float w, h, r;
    private final float wd;
    private final Paint pDash, pFill, pText;
    private boolean[] in = new boolean[6];
    private OnChangeListener ocl = null;
    private boolean tah = false;
    private int lastVal = 0;

    public BrailleView(Context ctx, AttributeSet as) {
        super(ctx, as);

        wd = Utils.dpToPix(3);

        pDash = new Paint();
        pDash.setColor(ContextCompat.getColor(ctx, R.color.mainColor));
        pDash.setAntiAlias(true);
        pDash.setStrokeWidth(wd);
        pDash.setStrokeCap(Cap.ROUND);
        pDash.setStyle(Style.STROKE);

        pFill = new Paint(pDash);
        pFill.setStyle(Style.FILL);

        pText = new Paint();
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setTypeface(Typeface.DEFAULT_BOLD);
        pText.setColor(isInEditMode() ? Color.WHITE : ContextCompat.getColor(ctx, R.color.priColor));
        pText.setAntiAlias(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int cnt = e.getPointerCount();
        int ix;
        int action = e.getActionMasked();
        if (tah && cnt == 1) {
            if (action == MotionEvent.ACTION_UP) {
                if (ocl != null) ocl.onChange(getVal(), false);
                getParent().requestDisallowInterceptTouchEvent(false);
                clear();
            } else {
                ix = index(e.getX(), e.getY());
                if (action == MotionEvent.ACTION_DOWN) {
                    if (ix >= 0) {
                        clear();
                        if (ocl != null) ocl.onChange(0, true);
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else
                        return false;
                }
                if (ix >= 0) in[ix] = true;
                if (getVal() != lastVal) {
                    playSoundEffect(SoundEffectConstants.CLICK);
                    lastVal = getVal();
                    if (ocl != null) ocl.onChange(lastVal, true);
                }
            }
            invalidate();
        } else {
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                int index = e.getActionIndex();
                ix = index(e.getX(index), e.getY(index));
                if (ix >= 0) {
                    in[ix] = !in[ix];
                    playSoundEffect(SoundEffectConstants.CLICK);
                }
                lastVal = getVal();
                if (ocl != null) ocl.onChange(lastVal, true);
                invalidate();
            }
        }
        return true;
    }

    private int index(float xc, float yc) {
        float x, y;
        for (int ix = 0; ix < 6; ix++) {
            x = ((ix / 3) * 2 - 1) * h / 6 + w / 2;
            y = (ix % 3) * h / 3 + h / 6;
            if ((x - xc) * (x - xc) + (y - yc) * (y - yc) < r * r) return ix;
        }
        return -1;
    }

    public int getVal() {
        int x = 0;
        for (int i = 0; i < 6; i++) {
            if (in[i]) x += 1 << i;
        }
        return x;
    }

    @SuppressWarnings("unused")
    public void setVal(int x) {
        for (int i = 0; i < 6; i++) {
            in[i] = ((x & (1 << i)) != 0);
        }
        if (ocl != null) ocl.onChange(x, true);
        lastVal = x;
        invalidate();
    }

    public void clear() {
        for (int i = 0; i < 6; i++)
            in[i] = false;
        ocl.onChange(0, false);
        lastVal = 0;
        invalidate();
    }

    public void setTah(boolean tah) {
        if (this.tah == tah)
            return;
        this.tah = tah;
        clear();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        r = h / 12;
        float o = 2 * (float) Math.PI * r;
        pDash.setPathEffect(new DashPathEffect(new float[]{o / 48, o / 16}, 0));
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        for (int i = 0; i < 6; i++)
            drawOne(c, i, in[i]);
    }

    private void drawOne(Canvas c, int ix, boolean b) {
        float x, y;
        x = ((ix / 3) * 2 - 1) * h / 6 + w / 2;
        y = (ix % 3) * h / 3 + h / 6;
        c.drawCircle(x, y, r, pDash);
        if (b) c.drawCircle(x, y, r - 2 * wd, pFill);

        pText.setTextSize(Math.min(2 * r - 5 * wd, getResources().getDimensionPixelSize(R.dimen.defTextSize)));
        c.drawText(String.valueOf(ix + 1), x, y - (pText.ascent() + pText.descent()) / 2, pText);
    }

    public interface OnChangeListener {
        void onChange(int x, boolean down);
    }

    public void setOnChangeListener(OnChangeListener ocl) {
        this.ocl = ocl;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable in) {
        if (!(in instanceof SavedState)) {
            super.onRestoreInstanceState(in);
            return;
        }

        SavedState state = (SavedState) in;
        super.onRestoreInstanceState(state.getSuperState());
        state.apply(this);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.read(this);
        return state;
    }


    private static final class SavedState extends BaseSavedState {

        private boolean tah;
        private boolean[] in;

        SavedState(Parcelable in) {
            super(in);
        }

        void read(BrailleView bv) {
            tah = bv.tah;
            in = bv.in;
        }

        SavedState(Parcel in) {
            super(in);
            tah = (in.readInt() != 0);
            this.in = in.createBooleanArray();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(tah ? 1 : 0);
            dest.writeBooleanArray(in);
        }

        void apply(BrailleView bv) {
            bv.tah = tah;
            bv.in = in;
            bv.invalidate();
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

    }

}
