package cz.absolutno.sifry.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class FixedGridLayout extends ViewGroup {

    private final int numColumns;
    private int nX, nY;
    @SuppressWarnings("FieldCanBeLocal")
    private int w1, h1;
    private final boolean alignLeft;

    public FixedGridLayout(Context ctx) {
        this(ctx, null);
    }

    public FixedGridLayout(Context ctx, AttributeSet as) {
        this(ctx, as, 0);
    }

    public FixedGridLayout(Context ctx, AttributeSet as, int defStyle) {
        super(ctx, as, defStyle);

        TypedArray ta = getContext().obtainStyledAttributes(as, R.styleable.FixedGridLayout);
        numColumns = ta.getInt(R.styleable.FixedGridLayout_numColumns, -1);
        alignLeft = ta.getBoolean(R.styleable.FixedGridLayout_alignLeft, false);
        ta.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        int w = r - l, h = b - t;

        if (nX == 0 || nY == 0) return; /* onLayout při re-kreaci skrytého fragmentu voláno bez onMeasure, tj. s nX == 0 */
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            int x = alignLeft ? w1 * (i % nX) : w * (i % nX) / nX + w / nX / 2 - v.getMeasuredWidth() / 2;
            int y = h * (i / nX) / nY + h / nY / 2 - v.getMeasuredHeight() / 2;
            v.layout(x, y, x + v.getMeasuredWidth(), y + v.getMeasuredHeight());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMeasureSpecChild, heightMeasureSpecChild;
        int modeX, modeY;
        int count;

        modeX = MeasureSpec.getMode(widthMeasureSpec);
        modeY = MeasureSpec.getMode(heightMeasureSpec);

        final int totalCount = getChildCount();
        count = 0;
        for (int i = 0; i < totalCount; i++)
            if (getChildAt(i).getVisibility() != GONE)
                count++;

        if (numColumns > 0) {
            nX = numColumns;
            nY = Utils.ceil((float) count / nX);
            if (nY == 0) nY = 1;
            widthMeasureSpecChild = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec) / nX, modeX == MeasureSpec.UNSPECIFIED ? modeX : MeasureSpec.AT_MOST);
            heightMeasureSpecChild = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) / nY, modeY == MeasureSpec.UNSPECIFIED ? modeY : MeasureSpec.AT_MOST);
        } else {
            widthMeasureSpecChild = 0;
            heightMeasureSpecChild = heightMeasureSpec;
        }

        w1 = 1;
        h1 = 1;
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != GONE) {
                    measureChild(child, widthMeasureSpecChild, heightMeasureSpecChild);
                    w1 = Math.max(w1, child.getMeasuredWidth());
                    h1 = Math.max(h1, child.getMeasuredHeight());
                }
            }

            if (numColumns <= 0) {
                if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
                    nX = Utils.floor(MeasureSpec.getSize(widthMeasureSpec) / w1);
                    if (nX == 0) nX = 1;
                    nY = Utils.ceil((float) count / nX);
                } else {
                    nX = count;
                    nY = 1;
                }

            }
        } else {
            nX = 1;
            nY = 1;
        }

        widthMeasureSpecChild = MeasureSpec.makeMeasureSpec(w1, MeasureSpec.AT_MOST);
        heightMeasureSpecChild = MeasureSpec.makeMeasureSpec(h1, MeasureSpec.AT_MOST);
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpecChild, heightMeasureSpecChild);
            }
        }

        setMeasuredDimension(resolveSize(Math.max(nX * w1, getSuggestedMinimumWidth()), widthMeasureSpec),
                resolveSize(Math.max(nY * h1, getSuggestedMinimumHeight()), heightMeasureSpec));
    }

}
