package cz.absolutno.sifry.morse;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.widget.ButtonView;

public final class MorseView extends ButtonView {

    private static String[] znaky;

    public MorseView(Context ctx, AttributeSet as) {
        super(ctx, as);

        if (isInEditMode())
            znaky = new String[]{".", "-", "/"};
        else
            znaky = getResources().getStringArray(R.array.saMDZnaky);

        setSize(2, 2, 3);
    }

    @Override
    protected RectF getRectF(int ix) {
        RectF rf = super.getRectF(ix);
        if (ix == 2) {
            rf.left = 0.3f;
            rf.right = 1.7f;
        }
        return rf;
    }

    @Override
    protected String getIxText(int ix) {
        return znaky[ix];
    }

}