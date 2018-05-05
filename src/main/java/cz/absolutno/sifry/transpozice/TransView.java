package cz.absolutno.sifry.transpozice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.BitSet;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;

public abstract class TransView extends View {

    private String s = "";
    Alphabet abc;
    boolean ignoreNP;
    private OnInputListener oil = null;
    private int len = 0;

    private final int priColor, selColor, usedColor, bothColor;
    private BitSet bsUsed = new BitSet();
    private String mark = null;

    private final Paint p;

    public TransView(Context ctx, AttributeSet as) {
        super(ctx, as);

        priColor = isInEditMode() ? Color.WHITE : ContextCompat.getColor(ctx, R.color.priColor);
        selColor = ContextCompat.getColor(ctx, R.color.transSelColor);
        usedColor = ContextCompat.getColor(ctx, R.color.transUsedColor);
        bothColor = ContextCompat.getColor(ctx, R.color.transBothColor);

        p = new Paint();
        p.setTextAlign(Paint.Align.CENTER);
        p.setColor(priColor);
        p.setAntiAlias(true);

        loadPreferences();
        setMinimumHeight(3 * getResources().getDimensionPixelSize(R.dimen.butSize));
    }

    private void loadPreferences() {
        abc = Alphabet.getPreferentialFullInstance();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        ignoreNP = (sp != null && sp.getBoolean("pref_parse_np", false));
    }

    public final void reloadPreferences() {
        loadPreferences();
        setText(s);
    }

    @SuppressLint("DefaultLocale")
    public void setText(String s) {
        if (s == null) this.s = "";
        else this.s = s;
        StringParser sp = abc.getStringParser(this.s);
        len = 0;
        int ord;
        while ((ord = sp.getNextOrd()) != StringParser.EOF)
            if (ord >= 0 || !ignoreNP)
                len++;
        bsUsed = new BitSet(len);
        clearMarks();
    }

    final void registerInput(int ix) {
        mark = null;
        if (ix < 0) return;
        bsUsed.set(ix);
        StringParser sp = abc.getStringParser(s);
        if (ignoreNP) {
            int ord = -1, i = 0;
            while (i <= ix && (ord = sp.getNextOrd()) != StringParser.EOF)
                if (ord >= 0) i++;
            mark = abc.chr(ord);
        } else {
            for (int i = 0; i < ix; i++)
                sp.getNextOrd();
            int ord = sp.getNextOrd();
            if (ord >= 0)
                mark = abc.chr(ord);
            else
                mark = String.valueOf(sp.getLastChar());
        }
        invalidate();
        if (oil != null)
            oil.onInput(mark);
    }

    final int getLength() {
        return len;
    }

    public void clearTrf() {
    }

    public void clearMarks() {
        mark = null;
        bsUsed.clear();
        invalidate();
    }

    public abstract void onRevX();

    public abstract void onRevY();

    public abstract void onRevDiag();

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        StringParser sp = abc.getStringParser(s);
        int ord, i = 0;
        String chr;
        PointF pt;

        p.setTextSize(Math.min(getSuggestedTextSize(), getResources().getDimensionPixelSize(R.dimen.defTextSize)));
        float yoff = (p.ascent() + p.descent()) / 2;

        while ((ord = sp.getNextOrd()) != StringParser.EOF) {
            if (ord == StringParser.ERR) {
                if (ignoreNP)
                    continue;
                else
                    chr = String.valueOf(sp.getLastChar());
            } else
                chr = abc.chr(ord);
            pt = getCoords(i);
            p.setColor(getColor(i, chr));
            c.drawText(chr, pt.x, pt.y - yoff, p);
            i++;
        }
    }

    final int getColor(int i, String chr) {
        if (chr.equals(mark))
            return bsUsed.get(i) ? bothColor : selColor;
        else
            return bsUsed.get(i) ? usedColor : priColor;
    }

    protected abstract PointF getCoords(int i);

    protected abstract float getSuggestedTextSize();

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        width = (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(widthMeasureSpec) : getSuggestedMinimumWidth();
        height = (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(heightMeasureSpec) : getSuggestedMinimumHeight();
        setMeasuredDimension(width, height);
    }

    public interface OnInputListener {
        void onInput(String in);
    }

    public final void setOnInputListener(OnInputListener oil) {
        this.oil = oil;
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

        private String s, mark;
        private BitSet bsUsed;

        SavedState(Parcelable in) {
            super(in);
        }

        void read(TransView tv) {
            s = tv.s;
            mark = tv.mark;
            bsUsed = tv.bsUsed;
        }

        SavedState(Parcel in) {
            super(in);
            s = in.readString();
            mark = in.readString();
            bsUsed = (BitSet) in.readSerializable();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(s);
            dest.writeString(mark);
            dest.writeSerializable(bsUsed);
        }

        void apply(TransView tv) {
            tv.setText(s);
            tv.mark = mark;
            tv.bsUsed = bsUsed;
            tv.invalidate();
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