package cz.absolutno.sifry.transpozice;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;

public abstract class Trans2DView extends TransView {

    private float[] mx = new float[6];
    private float ow, oh, sz;
    private final float[] lastX = new float[3], lastY = new float[3];
    private boolean motion;

    public Trans2DView(Context ctx, AttributeSet as) {
        super(ctx, as);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int cnt = e.getPointerCount();
        if (cnt >= 4) return false;
        int action = e.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                motion = false;
            case MotionEvent.ACTION_POINTER_DOWN:
                for (int i = 0; i < cnt; i++) {
                    lastX[i] = e.getX(i);
                    lastY[i] = e.getY(i);
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (motion) return true;
                registerInput(index(e.getX(), e.getY()));
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                int j = 0;
                for (int i = 0; i < cnt; i++) {
                    if (i == e.getActionIndex()) continue;
                    lastX[j] = e.getX(i);
                    lastY[j] = e.getY(i);
                    j++;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                float[] curX = new float[3], curY = new float[3];
                for (int i = 0; i < cnt; i++) {
                    curX[i] = e.getX(i);
                    curY[i] = e.getY(i);
                }
                switch (cnt) {
                    case 1:
                        if (Math.abs(curX[0] - lastX[0]) < 5 || Math.abs(curY[0] - lastY[0]) < 5)
                            return false;
                        mx[4] += curX[0] - lastX[0];
                        mx[5] += curY[0] - lastY[0];
                        break;
                    case 2:
                        float d2 = (lastX[0] - lastX[1]) * (lastX[0] - lastX[1]) + (lastY[0] - lastY[1]) * (lastY[0] - lastY[1]);
                        float u = ((lastX[0] - lastX[1]) * (curX[0] - curX[1]) + (lastY[0] - lastY[1]) * (curY[0] - curY[1])) / d2;
                        float v = ((lastY[0] - lastY[1]) * (curX[0] - curX[1]) - (lastX[0] - lastX[1]) * (curY[0] - curY[1])) / d2;
                        float tmp;
                        mx[4] = curX[0] - u * (lastX[0] - mx[4]) - v * (lastY[0] - mx[5]);
                        mx[5] = curY[0] - u * (lastY[0] - mx[5]) + v * (lastX[0] - mx[4]);
                        tmp = u * mx[0] + v * mx[1];
                        mx[1] = u * mx[1] - v * mx[0];
                        mx[0] = tmp;
                        tmp = u * mx[2] + v * mx[3];
                        mx[3] = u * mx[3] - v * mx[2];
                        mx[2] = tmp;
                        break;
                    case 3:
                        float det = mx[0] * mx[3] - mx[1] * mx[2];
                        float dx1 = lastX[1] - lastX[0];
                        float dy1 = lastY[1] - lastY[0];
                        float dx2 = lastX[2] - lastX[0];
                        float dy2 = lastY[2] - lastY[0];
                        float dett = (mx[3] * dx1 - mx[2] * dy1) * (mx[0] * dy2 - mx[1] * dx2) - (mx[0] * dy1 - mx[1] * dx1) * (mx[3] * dx2 - mx[2] * dy2);
                        float[] tmx = new float[6];
                        tmx[0] = det / dett * (mx[0] * (dy2 * (curX[1] - curX[0]) - dy1 * (curX[2] - curX[0])) + mx[1] * (dx1 * (curX[2] - curX[0]) - dx2 * (curX[1] - curX[0])));
                        tmx[1] = det / dett * (mx[0] * (dy2 * (curY[1] - curY[0]) - dy1 * (curY[2] - curY[0])) + mx[1] * (dx1 * (curY[2] - curY[0]) - dx2 * (curY[1] - curY[0])));
                        tmx[2] = det / dett * (mx[2] * (dy2 * (curX[1] - curX[0]) - dy1 * (curX[2] - curX[0])) + mx[3] * (dx1 * (curX[2] - curX[0]) - dx2 * (curX[1] - curX[0])));
                        tmx[3] = det / dett * (mx[2] * (dy2 * (curY[1] - curY[0]) - dy1 * (curY[2] - curY[0])) + mx[3] * (dx1 * (curY[2] - curY[0]) - dx2 * (curY[1] - curY[0])));
                        tmx[4] = curX[0] - ((tmx[0] * mx[3] - tmx[2] * mx[1]) * (lastX[0] - mx[4]) + (tmx[2] * mx[0] - tmx[0] * mx[2]) * (lastY[0] - mx[5])) / det;
                        tmx[5] = curY[0] - ((tmx[3] * mx[0] - tmx[1] * mx[2]) * (lastY[0] - mx[5]) + (tmx[1] * mx[3] - tmx[3] * mx[1]) * (lastX[0] - mx[4])) / det;
                        System.arraycopy(tmx, 0, mx, 0, 6);
                        break;
                }
                for (int i = 0; i < cnt; i++) {
                    lastX[i] = curX[i];
                    lastY[i] = curY[i];
                }
                invalidate();
                motion = true;
                return true;
        }
        return false;
    }

    private int index(float xc, float yc) {
        float det = mx[0] * mx[3] - mx[1] * mx[2];
        float xt = (mx[3] * (xc - mx[4]) - mx[2] * (yc - mx[5])) / det;
        float yt = (mx[0] * (yc - mx[5]) - mx[1] * (xc - mx[4])) / det;
        int ixMin = -1;
        float disMin = 0;
        final int len = getLength();
        for (int i = 0; i < len; i++) {
            PointF orig = get2DCoords(i);
            float dis = (xt - orig.x) * (xt - orig.x) + (yt - orig.y) * (yt - orig.y);
            if (ixMin < 0 || dis < disMin) {
                disMin = dis;
                ixMin = i;
            }
        }
        if (ixMin < 0 || disMin > 0.5f * 0.5f)
            return -1;
        else
            return ixMin;
    }

    final float untransX(float xc, float yc) {
        float det = mx[0] * mx[3] - mx[1] * mx[2];
        return (mx[3] * (xc - mx[4]) - mx[2] * (yc - mx[5])) / det;
    }

    final float untransY(float xc, float yc) {
        float det = mx[0] * mx[3] - mx[1] * mx[2];
        return (mx[0] * (yc - mx[5]) - mx[1] * (xc - mx[4])) / det;
    }

    @Override
    public final void clearTrf() {
        RectF b = getBounds();
        ow = getWidth();
        oh = getHeight();
        sz = Math.min(ow / (b.right - b.left), oh / (b.bottom - b.top));
        mx[0] = mx[3] = sz;
        mx[1] = mx[2] = 0;
        mx[4] = (ow - (b.right + b.left) * sz) / 2;
        mx[5] = (oh - (b.bottom + b.top) * sz) / 2;
        invalidate();
    }

    @Override
    protected final void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (sz <= 0) {
            clearTrf();
            return;
        }

        RectF b = getBounds();
        float nsz = Math.min(w / (b.right - b.left), h / (b.bottom - b.top));
        float x0 = untransX(ow / 2, oh / 2);
        float y0 = untransY(ow / 2, oh / 2);
        mx[0] *= nsz / sz;
        mx[1] *= nsz / sz;
        mx[2] *= nsz / sz;
        mx[3] *= nsz / sz;
        mx[4] = w / 2 - mx[0] * x0 - mx[2] * y0;
        mx[5] = h / 2 - mx[1] * x0 - mx[3] * y0;

        ow = w;
        oh = h;
        sz = nsz;
    }

    @Override
    public final void onRevX() {
        float x0 = untransX(ow / 2, oh / 2);
        float y0 = untransY(ow / 2, oh / 2);
        mx[0] = -mx[0];
        mx[2] = -mx[2];
        mx[4] = ow / 2 - mx[0] * x0 - mx[2] * y0;
        mx[5] = oh / 2 - mx[1] * x0 - mx[3] * y0;
        invalidate();
    }

    @Override
    public final void onRevY() {
        float x0 = untransX(ow / 2, oh / 2);
        float y0 = untransY(ow / 2, oh / 2);
        mx[1] = -mx[1];
        mx[3] = -mx[3];
        mx[4] = ow / 2 - mx[0] * x0 - mx[2] * y0;
        mx[5] = oh / 2 - mx[1] * x0 - mx[3] * y0;
        invalidate();
    }

    @Override
    public final void onRevDiag() {
        float x0 = untransX(ow / 2, oh / 2);
        float y0 = untransY(ow / 2, oh / 2);
        float t = mx[0];
        mx[0] = mx[2];
        mx[2] = t;
        t = mx[1];
        mx[1] = mx[3];
        mx[3] = t;
        mx[4] = ow / 2 - mx[0] * x0 - mx[2] * y0;
        mx[5] = oh / 2 - mx[1] * x0 - mx[3] * y0;
        invalidate();
    }

    @Override
    protected final PointF getCoords(int i) {
        PointF orig = get2DCoords(i);
        return new PointF(mx[0] * orig.x + mx[2] * orig.y + mx[4], mx[1] * orig.x + mx[3] * orig.y + mx[5]);
    }

    protected abstract RectF getBounds();

    protected abstract PointF get2DCoords(int i);

    final Matrix getTrfMatrix() {
        Matrix m = new Matrix();
        float[] values = new float[]{mx[0], mx[2], mx[4], mx[1], mx[3], mx[5], 0, 0, 1};
        m.setValues(values);
        return m;
    }

    final float getLineLength(float dx, float dy) {
        return (float) Math.sqrt((mx[0] * dx + mx[2] * dy) * (mx[0] * dx + mx[2] * dy) + (mx[1] * dx + mx[3] * dy) * (mx[1] * dx + mx[3] * dy));
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

        float ow, oh, sz;
        float[] mx;

        SavedState(Parcelable in) {
            super(in);
        }

        void read(Trans2DView tv) {
            ow = tv.ow;
            oh = tv.oh;
            sz = tv.sz;
            mx = tv.mx;
        }

        SavedState(Parcel in) {
            super(in);
            ow = in.readFloat();
            oh = in.readFloat();
            sz = in.readFloat();
            mx = new float[6];
            in.readFloatArray(mx);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeFloat(ow);
            dest.writeFloat(oh);
            dest.writeFloat(sz);
            dest.writeFloatArray(mx);
        }

        void apply(Trans2DView tv) {
            tv.mx = mx;
            tv.ow = ow;
            tv.oh = oh;
            tv.sz = sz;
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
