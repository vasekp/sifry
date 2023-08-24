package cz.absolutno.sifry.common.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class ColorChunkTextView extends View {

    private OnChunkClickListener onChunkClickListener = null;
    private final Paint p;

    private int downIx;
    private boolean trackingScroll;
    private boolean trackingZoom;
    private float lastY;
    private float lastD;
    private int lines;
    private boolean widthsValid = false;

    private ChunkInternal[] data = new ChunkInternal[0];
    private float sz;
    private final float defSz, maxSz;
    private float dy = 0;

    public ColorChunkTextView(Context ctx, AttributeSet as) {
        super(ctx, as);

        defSz = sz = getResources().getDimensionPixelSize(R.dimen.defTextSize);
        maxSz = getResources().getDimensionPixelSize(R.dimen.maxTextSize);
        p = new Paint();
        p.setAntiAlias(true);
        p.setTextSize(sz);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int cnt = e.getPointerCount();
        if (cnt >= 3) return false;
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                trackingScroll = false;
                trackingZoom = false;
                downIx = index(e.getX(), e.getY());
                lastY = e.getY();
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (cnt == 2) {
                    float dx = e.getX(1) - e.getX(0);
                    float dy = e.getY(1) - e.getY(0);
                    lastD = (float) Math.sqrt(dx * dx + dy * dy);
                    trackingZoom = true;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (cnt == 1) {
                    if (trackingZoom)
                        return true;
                    if (!trackingScroll && Math.abs(e.getY() - lastY) < 5)
                        return true;
                    trackingScroll = true;
                    setScroll(dy - e.getY() + lastY);
                    lastY = e.getY();
                } else if (cnt == 2) {
                    if (!trackingZoom) return true;
                    float dx = e.getX(1) - e.getX(0);
                    float dy = e.getY(1) - e.getY(0);
                    float curD = (float) Math.sqrt(dx * dx + dy * dy);
                    setTextSize(sz * curD / lastD);
                    lastD = curD;
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (trackingZoom || trackingScroll)
                    return true;
                int ix = index(e.getX(), e.getY());
                if (ix < 0 || ix != downIx)
                    return true;
                onChunkClick(ix);
                return true;
        }
        return super.onTouchEvent(e);
    }

    private void setData(ChunkInternal[] data) {
        this.data = data;
        invalidateWidths();
    }

    public void setData(ArrayList<ColorChunk> input) {
        int sz = input.size();
        data = new ChunkInternal[sz];
        for (int i = 0; i < sz; i++)
            data[i] = new ChunkInternal(input.get(i));
        invalidateWidths();
    }

    public ColorChunk getChunk(int i) {
        return new ColorChunk(data[i]);
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (ChunkInternal chunk : data)
            sb.append(chunk.chr);
        return sb.toString();
    }

    @SuppressWarnings("unused")
    public void replaceChunk(int i, ColorChunk newChunk) {
        data[i] = new ChunkInternal(newChunk);
        invalidateWidths();
    }

    public void setChunkColor(int i, int newColor) {
        data[i].color = newColor;
        invalidate();
    }

    public void setTextSize(float sz) {
        float newSz = (sz <= 0 ? defSz : Math.min(sz, maxSz));
        setScroll(dy * newSz / sz);
        this.sz = newSz;
        p.setTextSize(this.sz);
        invalidateWidths();
    }

    public void setScroll(float dy) {
        float maxDy = lines * (p.descent() - p.ascent()) - getHeight();
        this.dy = Math.max(Math.min(dy, maxDy), 0);
        invalidate();
    }

    private void invalidateWidths() {
        widthsValid = false;
        invalidate();
    }

    private void calculateWidths() {
        StringBuilder sb = new StringBuilder();
        for (ChunkInternal chunk : data) {
            sb.append(chunk.chr);
        }

        String s = sb.toString();
        float[] widths = new float[s.length()];
        p.getTextWidths(s, widths);
        int ix = 0;
        for (ChunkInternal chunk : data) {
            chunk.w = 0;
            int len = chunk.chr.length();
            if (len == 1)
                chunk.w = widths[ix++];
            else {
                for (int ixto = ix + len; ix < ixto; ix++)
                    chunk.w += widths[ix];
            }
        }
        widthsValid = true;
        calculatePositions();
    }

    private void calculatePositions() {
        int ln = 0;
        int ixFrom = 0;
        int len = data.length;
        float w = getWidth();
        if (w <= 0) return;
        while (ixFrom < len) {
            float x = 0;
            int lastSpace = -1;
            int ixTo;
            for (ixTo = ixFrom; ixTo < len; ixTo++) {
                if (data[ixTo].goodbreak)
                    lastSpace = ixTo;
                x += data[ixTo].w;
                if (x > w) break;
            }
            if (ixTo < len && lastSpace >= 0)
                ixTo = lastSpace + 1;
            x = 0;
            for (int ix = ixFrom; ix < ixTo; ix++) {
                data[ix].x = x;
                data[ix].ln = ln;
                x += data[ix].w;
            }
            ixFrom = ixTo;
            ln++;
        }
        lines = ln;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculatePositions();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (!widthsValid) calculateWidths();
        float y0 = -p.ascent() - dy;
        float yl = p.descent() - p.ascent();
        for (ChunkInternal chunk : data) {
            p.setColor(chunk.color);
            c.drawText(chunk.chr, chunk.x, y0 + chunk.ln * yl, p);
        }
    }

    private int index(float xc, float yc) {
        int ln = Utils.floor((yc + dy) / (p.descent() - p.ascent()));
        int len = data.length;
        for (int i = 0; i < len; i++) {
            ChunkInternal chunk = data[i];
            if (chunk.ln == ln && chunk.x <= xc && chunk.x + chunk.w > xc)
                return i;
        }
        return -1;
    }

    private void onChunkClick(int ix) {
        if (onChunkClickListener != null)
            onChunkClickListener.onChunkClick(ix);
    }

    public void setOnChunkClickListener(OnChunkClickListener onChunkClickListener) {
        this.onChunkClickListener = onChunkClickListener;
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

        private ChunkInternal[] data;
        private float sz;
        private float dy;

        SavedState(Parcelable in) {
            super(in);
        }

        void read(ColorChunkTextView ctv) {
            sz = ctv.sz;
            dy = ctv.dy;
            data = ctv.data;
        }

        SavedState(Parcel in) {
            super(in);
            sz = in.readFloat();
            dy = in.readFloat();
            data = (ChunkInternal[]) in.readSerializable();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeFloat(sz);
            dest.writeFloat(dy);
            dest.writeSerializable(data);
        }

        void apply(ColorChunkTextView ctv) {
            ctv.setData(data);
            ctv.setTextSize(sz);
            ctv.setScroll(dy);
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


    public interface OnChunkClickListener {
        void onChunkClick(int ix);
    }


    public static class ColorChunk implements Serializable {
        @SuppressWarnings("unused")
        private static final long serialVersionUID = -5907611771010408650L;

        public final String chr;
        int color;

        public ColorChunk(String chr, int color) {
            this.chr = chr;
            this.color = color;
        }

        ColorChunk(ColorChunk chunk) {
            chr = chunk.chr;
            color = chunk.color;
        }

        @Override
        public final String toString() {
            return chr;
        }
    }

    private static final class ChunkInternal extends ColorChunk {
        @SuppressWarnings("unused")
        private static final long serialVersionUID = 7320543464412705968L;

        final boolean goodbreak;
        float w;
        float x;
        int ln;

        ChunkInternal(ColorChunk chunk) {
            super(chunk);
            goodbreak = chr.equals(" ");
        }
    }

}
