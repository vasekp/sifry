package cz.absolutno.sifry.kalendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class KalendarView extends View {

    private float w, h, r;
    private final Paint pLine, pText;
    private Calendar sel, anchor;

    private final SimpleDateFormat dayName;
    private final GestureDetector gesture;
    private OnChangeListener ocl = null;
    private final int priColor, secColor, terColor, anchorColor, histColor, bothColor;
    private int firstDay, firstWeek, lastWeek;
    private int[] weekNos, days, daysPre, daysPost;
    private String[] dayNames;
    private int todayIx, anchorIx;
    private HashSet<HistEntry> hist = new HashSet<>(10);
    private final boolean[] isHist = new boolean[32];

    public KalendarView(Context ctx, AttributeSet as) {
        super(ctx, as);

        float wd = Utils.dpToPix(2);
        if (isInEditMode()) {
            priColor = 0xFFFFFFFF;
            secColor = 0xFF808080;
            terColor = 0xFFC0C0C0;
        } else {
            priColor = ContextCompat.getColor(ctx, R.color.priColor);
            secColor = ContextCompat.getColor(ctx, R.color.secColor);
            terColor = ContextCompat.getColor(ctx, R.color.terColor);
        }
        anchorColor = ContextCompat.getColor(ctx, R.color.kalendarAnchorColor);
        histColor = ContextCompat.getColor(ctx, R.color.kalendarHistColor);
        bothColor = ContextCompat.getColor(ctx, R.color.kalendarBothColor);

        pLine = new Paint();
        pLine.setColor(ContextCompat.getColor(ctx, R.color.mainColor));
        pLine.setAntiAlias(true);
        pLine.setStrokeWidth(wd);
        pLine.setStrokeCap(Cap.ROUND);
        pLine.setStyle(Style.STROKE);

        pText = new Paint();
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setTypeface(Typeface.DEFAULT);
        pText.setColor(isInEditMode() ? Color.WHITE : ContextCompat.getColor(ctx, R.color.priColor));
        pText.setAntiAlias(true);

        gesture = new GestureDetector(getContext(), gestureListener);

        sel = Calendar.getInstance();
        anchor = (Calendar) sel.clone();
        dayName = new SimpleDateFormat("E", Locale.getDefault());
        rebuild();

        hist.clear();

        setMinimumWidth(8 * getResources().getDimensionPixelSize(R.dimen.butSize));
        setMinimumHeight(7 * getResources().getDimensionPixelSize(R.dimen.butSize));
    }

    private static class HistEntry implements Serializable {
        int year;
        int dayOfYear;

        HistEntry() {
            year = 0;
            dayOfYear = 0;
        }

        HistEntry(int year, int dayOfYear) {
            super();
            this.year = year;
            this.dayOfYear = dayOfYear;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof HistEntry && ((HistEntry) o).year == year && ((HistEntry) o).dayOfYear == dayOfYear;
        }

        @Override
        public int hashCode() {
            return year * 400 + dayOfYear;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        gesture.onTouchEvent(e);
        return true;
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        public boolean onDown(MotionEvent e) {
            float x = e.getX(), y = e.getY();
            if (x > w / 2 - 2.8f * r && y > h / 2 - 2.3f * r) {
                x -= w / 2 - 2.8f * r;
                y -= h / 2 - 2.3f * r;
                int xc = (int) (x / r);
                int yc = (int) (y / r);
                if (xc < 7 && yc <= lastWeek - firstWeek)
                    getParent().requestDisallowInterceptTouchEvent(true);
            }
            return true;
        }

        public boolean onSingleTapUp(MotionEvent e) {
            float x = e.getX(), y = e.getY();
            if (x < w / 2 - 3.2f * r && x > w / 2 - 4.2f * r && y < h / 2 - 2.7f * r && y > h / 2 - 3.7f * r)
                reset();
            else if (x > w / 2 - 2.8f * r && y > h / 2 - 2.3f * r) {
                x -= w / 2 - 2.8f * r;
                y -= h / 2 - 2.3f * r;
                int xc = (int) (x / r);
                int yc = (int) (y / r);
                if (xc < 7 && yc <= lastWeek - firstWeek) {
                    sel.set(Calendar.WEEK_OF_MONTH, yc + firstWeek);
                    sel.set(Calendar.DAY_OF_WEEK, ((xc + firstDay + 6) % 7) + 1);
                    rebuild();
                }
            }
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityY) < Math.abs(velocityX)) return false;
            add(Calendar.MONTH, -(int) Math.signum(velocityY));
            return true;
        }

        public void onLongPress(MotionEvent e) {
            float x = e.getX(), y = e.getY();
            if (x > w / 2 - 2.8f * r && y > h / 2 - 2.3f * r) {
                x -= w / 2 - 2.8f * r;
                y -= h / 2 - 2.3f * r;
                int xc = (int) (x / r);
                int yc = (int) (y / r);
                if (xc < 7 && yc <= lastWeek - firstWeek) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(sel.getTime());
                    c.set(Calendar.WEEK_OF_MONTH, yc + firstWeek);
                    c.set(Calendar.DAY_OF_WEEK, ((xc + firstDay + 6) % 7) + 1);
                    HistEntry he = new HistEntry(c.get(Calendar.YEAR), c.get(Calendar.DAY_OF_YEAR));
                    if (hist.contains(he))
                        hist.remove(he);
                    else
                        hist.add(he);
                    android.util.Log.d("hist", Integer.toString(hist.size()));
                    rebuild();
                }
            }
        }

        public boolean onDoubleTap(MotionEvent e) {
            float x = e.getX(), y = e.getY();
            if (x > w / 2 - 2.8f * r && y > h / 2 - 2.3f * r) {
                x -= w / 2 - 2.8f * r;
                y -= h / 2 - 2.3f * r;
                int xc = (int) (x / r);
                int yc = (int) (y / r);
                if (xc < 7 && yc <= lastWeek - firstWeek) {
                    anchor.setTime(sel.getTime());
                    anchor.set(Calendar.WEEK_OF_MONTH, yc + firstWeek);
                    anchor.set(Calendar.DAY_OF_WEEK, ((xc + firstDay + 6) % 7) + 1);
                    rebuild();
                }
            }
            return true;
        }

    };

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        r = Math.min(w / 8.4f, h / 7.4f);
    }

    @SuppressLint("DefaultLocale")
    private void rebuild() {
        Calendar cal = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

		/* Weekday names */
        firstDay = cal.getFirstDayOfWeek();
        dayNames = new String[7];
        for (int i = 0; i < 7; i++) {
            cal.set(Calendar.DAY_OF_WEEK, ((firstDay + i + 6) % 7) + 1);
            dayNames[i] = dayName.format(cal.getTime()).substring(0, 1).toUpperCase();
        }
		
		/* Week numbers */
        cal.setTime(sel.getTime());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        firstWeek = cal.get(Calendar.WEEK_OF_MONTH);
        int count = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, count);
        lastWeek = cal.get(Calendar.WEEK_OF_MONTH);
        weekNos = new int[lastWeek - firstWeek + 1];
        for (int j = 1; j <= count; j += 7) {
            cal.set(Calendar.DAY_OF_MONTH, j);
            weekNos[cal.get(Calendar.WEEK_OF_MONTH) - firstWeek] = cal.get(Calendar.WEEK_OF_YEAR);
        }
        cal.set(Calendar.DAY_OF_MONTH, count);
        weekNos[cal.get(Calendar.WEEK_OF_MONTH) - firstWeek] = cal.get(Calendar.WEEK_OF_YEAR);
        cal.setTime(sel.getTime());
		
		/* Day numbers and coordinates */
        days = new int[count];
        todayIx = -1;
        anchorIx = -1;
        HistEntry he = new HistEntry();
        for (int d = 1; d <= count; d++) {
            cal.set(Calendar.DAY_OF_MONTH, d);
            int x = (cal.get(Calendar.DAY_OF_WEEK) + 7 - firstDay) % 7;
            int y = cal.get(Calendar.WEEK_OF_MONTH) - firstWeek;
            he.year = cal.get(Calendar.YEAR);
            he.dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
            days[d - 1] = (y << 8) + (x << 5) + d;
            if (he.year == today.get(Calendar.YEAR) && he.dayOfYear == today.get(Calendar.DAY_OF_YEAR))
                todayIx = d;
            if (he.year == anchor.get(Calendar.YEAR) && he.dayOfYear == anchor.get(Calendar.DAY_OF_YEAR))
                anchorIx = d;
            isHist[d] = hist.contains(he);
        }

		/* Extra days of preceding / following month */
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int x = (cal.get(Calendar.DAY_OF_WEEK) + 7 - firstDay) % 7;
        if (x > 0) {
            daysPre = new int[x];
            cal.add(Calendar.MONTH, -1);
            int last = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 0; i < x; i++)
                daysPre[i] = (i << 5) + (last - x + 1 + i);
            cal.add(Calendar.MONTH, 1);
        } else daysPre = null;

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        x = (cal.get(Calendar.DAY_OF_WEEK) + 7 - firstDay) % 7;
        if (x < 6) {
            daysPost = new int[6 - x];
            for (int i = x + 1; i < 7; i++)
                daysPost[i - x - 1] = ((lastWeek - firstWeek) << 8) + (i << 5) + (i - x);
        } else daysPost = null;

        invalidate();
        requestListener();
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        pText.setTextSize(Math.min(r * 2 / 3, getResources().getDimensionPixelSize(R.dimen.defTextSize)));
        pText.setColor(terColor);
		
		/* Today */
        c.translate(-r * 0.2f, -r * 0.2f);
        c.drawText("⊙", w / 2 - 3.5f * r, h / 2 - 3 * r - (pText.ascent() + pText.descent()) / 2, pText);
		
		/* Day names */
        c.translate(r * 0.4f, 0);
        for (int i = 0; i < 7; i++)
            c.drawText(dayNames[i], w / 2 - 2.5f * r + i * r, h / 2 - 3 * r - (pText.ascent() + pText.descent()) / 2, pText);
		
		/* Week numbers */
        c.translate(-r * 0.4f, r * 0.4f);
        for (int j = 0; j < weekNos.length; j++)
            c.drawText(Integer.toString(weekNos[j]), w / 2 - 3.5f * r, h / 2 - 2 * r + j * r - (pText.ascent() + pText.descent()) / 2, pText);
		
		/* Days */
        c.translate(r * 0.4f, 0);
        for (int d : days) {
            int y = (d & 0x700) >> 8;
            int x = (d & 0xE0) >> 5;
            int dn = (d & 31);
            if (dn == anchorIx)
                pText.setColor(isHist[dn] ? bothColor : anchorColor);
            else
                pText.setColor(isHist[dn] ? histColor : priColor);
            c.drawText(Integer.toString(dn), w / 2 - 2.5f * r + x * r, h / 2 - 2 * r + y * r - (pText.ascent() + pText.descent()) / 2, pText);
            if (dn == todayIx)
                c.drawCircle(w / 2 - 2.5f * r + x * r, h / 2 - 2 * r + y * r, r * 1 / 3, pLine);
        }

        pText.setColor(secColor);
		/* Days of preceding / following month */
        if (daysPre != null)
            for (int d : daysPre) {
                int y = (d & 0x700) >> 8;
                int x = (d & 0xE0) >> 5;
                int dn = (d & 31);
                c.drawText(Integer.toString(dn), w / 2 - 2.5f * r + x * r, h / 2 - 2 * r + y * r - (pText.ascent() + pText.descent()) / 2, pText);
            }
        if (daysPost != null)
            for (int d : daysPost) {
                int y = (d & 0x700) >> 8;
                int x = (d & 0xE0) >> 5;
                int dn = (d & 31);
                c.drawText(Integer.toString(dn), w / 2 - 2.5f * r + x * r, h / 2 - 2 * r + y * r - (pText.ascent() + pText.descent()) / 2, pText);
            }
		
		/* x and y aids of selected date */
        int x = (sel.get(Calendar.DAY_OF_WEEK) + 7 - firstDay) % 7;
        int y = sel.get(Calendar.WEEK_OF_MONTH) - firstWeek;
        c.drawLine(w / 2 - 4.4f * r, h / 2 - 2.5f * r + y * r, w / 2 + 4.0f * r, h / 2 - 2.5f * r + y * r, pLine);
        c.drawLine(w / 2 - 4.4f * r, h / 2 - 1.5f * r + y * r, w / 2 + 4.0f * r, h / 2 - 1.5f * r + y * r, pLine);
        c.drawLine(w / 2 - 3 * r + x * r, h / 2 - 3.9f * r, w / 2 - 3 * r + x * r, h / 2 - 1.5f * r + (lastWeek - firstWeek) * r, pLine);
        c.drawLine(w / 2 - 2 * r + x * r, h / 2 - 3.9f * r, w / 2 - 2 * r + x * r, h / 2 - 1.5f * r + (lastWeek - firstWeek) * r, pLine);
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

    public void set(int field, int value) {
        sel.set(field, value);
        rebuild();
    }

    public void add(int field, int value) {
        sel.add(field, value);
        rebuild();
    }

    public void setDate(int month, int day) {
        sel.set(Calendar.MONTH, month);
        sel.set(Calendar.DAY_OF_MONTH, day);
        rebuild();
    }

    public void setSince(int days) {
        sel.setTime(anchor.getTime());
        sel.add(Calendar.DAY_OF_YEAR, days);
        rebuild();
    }

    private int getSince() {
        return (int) Math.round((sel.getTimeInMillis() - anchor.getTimeInMillis()) / 86400000.);
    }

    private void reset() {
        sel = Calendar.getInstance();
        rebuild();
    }

    public void clear() {
        sel = Calendar.getInstance();
        anchor = (Calendar) sel.clone();
        hist.clear();
        rebuild();
    }

    public void requestListener() {
        if (ocl == null) return;
        ocl.onChange(sel.get(Calendar.YEAR), sel.get(Calendar.MONTH), sel.get(Calendar.DAY_OF_MONTH), sel.get(Calendar.DAY_OF_YEAR), getSince());
        ocl.onChangeAnchor(anchor.getTime(), getSince());
    }

    public interface OnChangeListener {
        void onChange(int year, int month, int day, int dayYear, int daysSince);

        void onChangeAnchor(Date anchor, int daysSince);
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

        private Calendar sel, anchor;
        private HashSet<HistEntry> hist;

        SavedState(Parcelable in) {
            super(in);
        }

        void read(KalendarView kv) {
            sel = kv.sel;
            anchor = kv.anchor;
            hist = kv.hist;
        }

        @SuppressWarnings("unchecked") /* přetypování na HashSet<HistEntry> */ SavedState(Parcel in) {
            super(in);
            sel = (Calendar) in.readSerializable();
            anchor = (Calendar) in.readSerializable();
            hist = (HashSet<HistEntry>) in.readSerializable();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeSerializable(sel);
            dest.writeSerializable(anchor);
            dest.writeSerializable(hist);
        }

        void apply(KalendarView kv) {
            kv.sel = sel;
            kv.anchor = anchor;
            kv.hist = hist;
            kv.rebuild();
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
