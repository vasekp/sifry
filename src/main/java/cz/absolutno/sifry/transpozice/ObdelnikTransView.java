package cz.absolutno.sifry.transpozice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class ObdelnikTransView extends Trans2DView {

    private int col = 0, row = 0;
    private int tmpCol = 0, tmpRow = 0;
    private final Paint p;
    private float downX, downY;
    private boolean resCallback = false;
    private boolean resize = false;
    private boolean alignRows = false, tmpAlign = false;
    private float resScale;
    private final Path pth = new Path();
    private final Handler h = new Handler();

    public ObdelnikTransView(Context ctx, AttributeSet as) {
        super(ctx, as);

        p = new Paint();
        p.setColor(ContextCompat.getColor(ctx, R.color.mainColor));
        p.setStrokeWidth(0);
        p.setStyle(Style.STROKE);
        p.setAntiAlias(true);
        p.setStrokeCap(Cap.BUTT);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float xo, yo;
        if (e.getPointerCount() > 1)
            return super.onTouchEvent(e);
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                xo = untransX(e.getX(), e.getY());
                yo = untransY(e.getX(), e.getY());
                RectF b = getBounds();
                downX = e.getX();
                downY = e.getY();
                if (yo >= b.top && yo <= b.bottom) {
                    if (xo <= b.left + .5f || xo >= b.right - .5f) {
                        resScale = Math.max(Math.abs(xo / (col + .5f) * 2), 1);
                        h.postDelayed(resizeRunnable, ViewConfiguration.getLongPressTimeout());
                        tmpAlign = false;
                        resCallback = true;
                    }
                } else if (xo >= b.left && xo <= b.right)
                    if (yo <= b.top + .5f || yo >= b.bottom - .5f) {
                        resScale = Math.max(Math.abs(yo / (row + .5f) * 2), 1);
                        h.postDelayed(resizeRunnable, ViewConfiguration.getLongPressTimeout());
                        tmpAlign = true;
                        resCallback = true;
                    }
                break;
            case MotionEvent.ACTION_MOVE:
                if (resCallback && Math.max(Math.abs(e.getX() - downX), Math.abs(e.getY() - downY)) > ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    h.removeCallbacks(resizeRunnable);
                    resCallback = false;
                }
                if (!resize) break;
                if (tmpAlign) {
                    yo = untransY(e.getX(), e.getY());
                    int tmpRow = Math.max(Utils.floor(Math.abs(2 * yo / resScale)), 1);
                    if(tmpRow > getLength())
                        tmpRow = getLength();
                    if (tmpRow != this.tmpRow) {
                        this.tmpRow = tmpRow;
                        invalidate();
                    }
                } else {
                    xo = untransX(e.getX(), e.getY());
                    int tmpCol = Math.max(Utils.floor(Math.abs(2 * xo / resScale)), 1);
                    if(tmpCol > getLength())
                        tmpCol = getLength();
                    if (tmpCol != this.tmpCol) {
                        this.tmpCol = tmpCol;
                        invalidate();
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                h.removeCallbacks(resizeRunnable);
                resCallback = false;
                if (!resize) break;
                if (tmpAlign)
                    setRow(this.tmpRow);
                else
                    setCol(this.tmpCol);
                resize = false;
                invalidate();
                return true;
            case MotionEvent.ACTION_CANCEL:
                h.removeCallbacks(resizeRunnable);
                resCallback = false;
                resize = false;
                invalidate();
                break;
        }
        return super.onTouchEvent(e);
    }

    private final Runnable resizeRunnable = new Runnable() {
        public void run() {
            tmpRow = row;
            tmpCol = col;
            resize = true;
            invalidate();
            resCallback = false;
        }
    };

    @Override
    public void setText(String s) {
        super.setText(s);
        int len = getLength();
        if (alignRows) {
            if (row == 0) row = Math.min(8, len);
            col = Utils.ceil((float) getLength() / row);
        } else {
            if (col == 0) col = Math.min(8, len);
            row = Utils.ceil((float) getLength() / col);
        }
        clearTrf();
    }

    @Override
    protected RectF getBounds() {
        return new RectF(-col / 2f - 0.5f, -row / 2f - 0.1f, col / 2f + 0.5f, row / 2f + 0.1f);
    }

    @Override
    protected PointF get2DCoords(int i) {
        if (alignRows) {
            int len = getLength();
            int z = len % row;
            if (z == 0) z = row;
            if (i < z * col)
                return new PointF((i % col) - (col - 1) / 2f, (i / col) - (row - 1) / 2f);
            else {
                i -= z * col;
                return new PointF((i % (col - 1)) - (col - 1) / 2f, (i / (col - 1)) + z - (row - 1) / 2f);
            }
        } else
            return new PointF((i % col) - (col - 1) / 2f, (i / col) - (row - 1) / 2f);
    }

    private void setCol(int col) {
        if (col <= 0) col = 1;
        this.col = col;
        row = Utils.ceil((float) getLength() / col);
        alignRows = false;
    }

    private void setRow(int row) {
        if (col <= 0) col = 1;
        this.row = row;
        col = Utils.ceil((float) getLength() / row);
        alignRows = true;
    }

    @Override
    protected float getSuggestedTextSize() {
        return Math.min(Math.min(getLineLength(1, 0), getLineLength(0, 1)), Math.min(getLineLength(1, 1), getLineLength(1, -1)));
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        if (resize) {
            if (tmpAlign) {
                tmpCol = Utils.ceil((float) getLength() / tmpRow);
                c.concat(getTrfMatrix());
                c.translate(-tmpCol / 2f, -tmpRow / 2f);
                pth.rewind();
                pth.moveTo(0, 0);
                pth.lineTo(tmpCol, 0);
                int len = getLength();
                if (tmpCol > 0 && len % tmpCol != 0 && tmpRow > 1) {
                    int ry = len % tmpRow;
                    pth.lineTo(tmpCol, ry);
                    pth.lineTo(tmpCol - 1, ry);
                    pth.lineTo(tmpCol - 1, tmpRow);
                } else
                    pth.lineTo(tmpCol, tmpRow);
                pth.lineTo(0, tmpRow);
                pth.close();
                c.drawPath(pth, p);
            } else {
                tmpRow = Utils.ceil((float) getLength() / tmpCol);
                c.concat(getTrfMatrix());
                c.translate(-tmpCol / 2f, -tmpRow / 2f);
                pth.rewind();
                pth.moveTo(0, 0);
                pth.lineTo(tmpCol, 0);
                int len = getLength();
                if (tmpCol > 0 && len % tmpCol != 0 && tmpRow > 1) {
                    int rx = len % tmpCol;
                    pth.lineTo(tmpCol, tmpRow - 1);
                    pth.lineTo(rx, tmpRow - 1);
                    pth.lineTo(rx, tmpRow);
                } else
                    pth.lineTo(tmpCol, tmpRow);
                pth.lineTo(0, tmpRow);
                pth.close();
                c.drawPath(pth, p);
            }
        }
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

        int col;

        SavedState(Parcelable in) {
            super(in);
        }

        void read(ObdelnikTransView tv) {
            col = tv.col;
        }

        SavedState(Parcel in) {
            super(in);
            col = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(col);
        }

        void apply(ObdelnikTransView tv) {
            tv.setCol(col);
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
