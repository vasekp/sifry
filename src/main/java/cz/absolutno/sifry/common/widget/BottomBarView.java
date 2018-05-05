package cz.absolutno.sifry.common.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class BottomBarView extends View {

    private float w, h;
    private final float wd, wg;
    private final GradientDrawable grad;
    private final Paint pLine, pText;

    private VelocityTracker vel = null;
    private BottomBarFlinger flinger;
    private String[] entries = null;
    private int index;

    private float lastX;
    private boolean tap;

    private OnChangeListener ocl = null;


    public BottomBarView(Context ctx, AttributeSet as) {
        super(ctx, as);

        wd = Utils.dpToPix(2);
        wg = getResources().getDimension(R.dimen.gradSize);

        pLine = new Paint();
        pLine.setColor(ContextCompat.getColor(ctx, R.color.mainColor));
        pLine.setAntiAlias(true);
        pLine.setStrokeWidth(wd);
        pLine.setStrokeCap(Cap.ROUND);
        pLine.setStyle(Style.STROKE);

        pText = new Paint();
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setTypeface(Typeface.DEFAULT);
        pText.setTextSize(getResources().getDimensionPixelSize(R.dimen.mainTextSize));
        pText.setColor(isInEditMode() ? Color.WHITE : ContextCompat.getColor(ctx, R.color.priColor));
        pText.setAntiAlias(true);

        grad = new GradientDrawable(Orientation.LEFT_RIGHT, new int[]{Color.BLACK, Color.TRANSPARENT});
        flinger = new BottomBarFlinger(0, 0, 0);

        setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.butSize));
    }

    public void setEntries(String[] entries, int def) {
        this.entries = entries;
        flinger = new BottomBarFlinger(0, (entries == null ? 0 : entries.length - 1), def);
        index = def;
        setMinimumHeight(entries == null ? 0 : getResources().getDimensionPixelSize(R.dimen.butSize));
        requestLayout();
        invalidate();
    }

    public String[] getEntries() {
        return entries;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        int index = e.getActionIndex();
        if (index != 0) return false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (vel == null)
                    vel = VelocityTracker.obtain();
                else
                    vel.clear();
                lastX = e.getX();
                vel.addMovement(e);
                tap = true;
                break;
            case MotionEvent.ACTION_MOVE:
                vel.addMovement(e);
                flinger.drag(-(e.getX() - lastX) / (w / 2));
                lastX = e.getX();
                ViewCompat.postInvalidateOnAnimation(this);
                tap = false;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                vel.computeCurrentVelocity(1);
                if (action == MotionEvent.ACTION_UP)
                    if (tap && flinger.isFinished()) {
                        float ixraw = this.index + (e.getX() - w / 2) / (w / 2 - wg / 2);
                        if (Math.abs(ixraw - Math.round(ixraw)) < 1.5f * wg / w) {
                            int ix = Math.round(ixraw);
                            if (ix >= 0 && ix < entries.length)
                                flinger.goTo(ix);
                        }
                    } else
                        flinger.fling(-vel.getXVelocity() / (w / 2));
                else
                    flinger.goTo(this.index);
                vel.recycle();
                vel = null;
                ViewCompat.postInvalidateOnAnimation(this);
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
    }

    @Override
    public void onDraw(Canvas c) {
        if (entries == null)
            return;
        float tr = flinger.compute() * (w / 2 - wg / 2);
        c.translate(0, wd / 2);
        c.drawLine(0, 0, w, 0, pLine);
        c.translate(-tr, 0);
        for (int i = 0; i < entries.length; i++)
            c.drawText(entries[i], w / 2 + i * (w / 2 - wg / 2), h / 2 - (pText.ascent() + pText.descent()) / 2, pText);
        c.translate(tr, 0);
        grad.setBounds(0, (int) wd / 2, (int) wg, (int) (h - wd / 2));
        grad.draw(c);
        c.scale(-1, 1, w / 2, 0);
        grad.draw(c);
        if (!flinger.isFinished())
            ViewCompat.postInvalidateOnAnimation(this);
        else {
            int oldIx = index;
            index = Math.round(flinger.compute());
            if (index != oldIx & ocl != null)
                ocl.onChange(index, oldIx);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        width = (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(widthMeasureSpec) : getSuggestedMinimumWidth();
        height = (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(heightMeasureSpec) : getSuggestedMinimumHeight();
        setMeasuredDimension(width, height);
    }

    public void goTo(int which) {
        flinger.goTo(which);
        invalidate();
    }

    public interface OnChangeListener {
        void onChange(int curIx, int lastIx);
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

        private String[] entries;
        private int index;

        SavedState(Parcelable in) {
            super(in);
        }

        void read(BottomBarView bv) {
            entries = bv.entries;
            index = bv.index;
        }

        SavedState(Parcel in) {
            super(in);
            entries = in.createStringArray();
            index = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeStringArray(entries);
            dest.writeInt(index);
        }

        void apply(BottomBarView bv) {
            bv.setEntries(entries, index);
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
