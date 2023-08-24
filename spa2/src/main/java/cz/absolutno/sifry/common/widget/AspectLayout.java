package cz.absolutno.sifry.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import cz.absolutno.sifry.R;

public final class AspectLayout extends ViewGroup {

    private final float relWidth;
    private final float relHeight;

    public AspectLayout(Context ctx) {
        this(ctx, null);
    }

    public AspectLayout(Context ctx, AttributeSet as) {
        this(ctx, as, 0);
    }

    public AspectLayout(Context ctx, AttributeSet as, int defStyle) {
        super(ctx, as, defStyle);

        TypedArray ta = getContext().obtainStyledAttributes(as, R.styleable.AspectLayout);
        relWidth = ta.getFloat(R.styleable.AspectLayout_relWidth, 1);
        relHeight = ta.getFloat(R.styleable.AspectLayout_relHeight, 1);
        ta.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() != 1)
            return;
        View child = getChildAt(0);
        float unit = Math.min((r - l) / relWidth, (b - t) / relHeight);
        int w = (int) (relWidth * unit), h = (int) (relHeight * unit);
        child.layout((r - l - w) / 2, (b - t - h) / 2, (r - l + w) / 2, (b - t + h) / 2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int modeX, modeY;
        int reqWidth, reqHeight;
        float unit;

        modeX = MeasureSpec.getMode(widthMeasureSpec);
        modeY = MeasureSpec.getMode(heightMeasureSpec);

        reqWidth = getSuggestedMinimumWidth();
        reqHeight = getSuggestedMinimumHeight();

        if (getChildCount() != 0) {
            int widthMeasureSpecChild, heightMeasureSpecChild;

            if (modeX == MeasureSpec.UNSPECIFIED || modeY == MeasureSpec.UNSPECIFIED) {
                widthMeasureSpecChild = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), modeX == MeasureSpec.UNSPECIFIED ? modeX : MeasureSpec.AT_MOST);
                heightMeasureSpecChild = MeasureSpec.makeMeasureSpec(reqHeight = MeasureSpec.getSize(heightMeasureSpec), modeY == MeasureSpec.UNSPECIFIED ? modeY : MeasureSpec.AT_MOST);
            } else {
                unit = Math.min(reqWidth / relWidth, reqHeight / relHeight);
                widthMeasureSpecChild = MeasureSpec.makeMeasureSpec((int) (relWidth * unit), MeasureSpec.AT_MOST);
                heightMeasureSpecChild = MeasureSpec.makeMeasureSpec((int) (relHeight * unit), MeasureSpec.AT_MOST);
            }

            View child = getChildAt(0);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpecChild, heightMeasureSpecChild);
                reqWidth = Math.max(reqWidth, child.getMeasuredWidth());
                reqHeight = Math.max(reqHeight, child.getMeasuredHeight());
            }
        }

        unit = Math.max(reqWidth / relWidth, reqHeight / relHeight);
        reqWidth = resolveSize((int) (unit * relWidth), widthMeasureSpec);
        reqHeight = resolveSize((int) (unit * relHeight), heightMeasureSpec);

        unit = Math.min(reqWidth / relWidth, reqHeight / relHeight);
        if (getChildCount() != 0) {
            int widthMeasureSpecChild, heightMeasureSpecChild;
            widthMeasureSpecChild = MeasureSpec.makeMeasureSpec((int) (relWidth * unit), MeasureSpec.EXACTLY);
            heightMeasureSpecChild = MeasureSpec.makeMeasureSpec((int) (relHeight * unit), MeasureSpec.EXACTLY);
            View child = getChildAt(0);
            if (child.getVisibility() != GONE)
                measureChild(child, widthMeasureSpecChild, heightMeasureSpecChild);
        }

        setMeasuredDimension(resolveSize(reqWidth, widthMeasureSpec), resolveSize(reqHeight, heightMeasureSpec));
    }

}
