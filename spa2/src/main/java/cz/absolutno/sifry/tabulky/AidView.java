package cz.absolutno.sifry.tabulky;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

public abstract class AidView extends View {

    private final int yOff;

    private final LinearLayout llAid;
    private final PopupWindow pwAid;
    private int pwWd, pwHt;
    private boolean active;

    protected OnInputListener oil = null;

    @SuppressLint("InflateParams")
    public AidView(Context ctx, AttributeSet as) {
        super(ctx, as);

        yOff = getResources().getDimensionPixelSize(R.dimen.aidOffset);

        llAid = (LinearLayout) App.getInflater().inflate(R.layout.aid_popup, null);
        pwAid = new PopupWindow(this);
        pwAid.setWidth(LayoutParams.WRAP_CONTENT);
        pwAid.setHeight(LayoutParams.WRAP_CONTENT);
        pwAid.setContentView(llAid);
        pwAid.setTouchable(false);
        pwAid.setBackgroundDrawable(null);

        updateActiveFlag();
    }

    public final void updateActiveFlag() {
        active = getResources().getBoolean(R.bool.pref_tbl_aid_default);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (sp != null)
            active = sp.getBoolean("pref_tab_aid", active);
    }

    protected final boolean isAidActive() { /* Kontrola nad zobrazováním nápovědek je na volajícím */
        return active;
    }

    protected final void setAidEntries(String[] list) {
        final int numEntries = llAid.getChildCount();
        if (list.length > numEntries) {
            LayoutInflater inflater = App.getInflater();
            for (int i = numEntries; i < list.length; i++)
                inflater.inflate(R.layout.aid_entry, llAid);
        } else if (list.length < numEntries) {
            for (int i = list.length; i < numEntries; i++)
                llAid.removeViewAt(i);
        }
        for (int i = 0; i < list.length; i++)
            ((TextView) llAid.getChildAt(i)).setText(list[i]);
        llAid.measure(0, 0);
        pwWd = pwAid.getContentView().getMeasuredWidth();
        pwHt = pwAid.getContentView().getMeasuredHeight();
        pwAid.setWidth(pwWd);
        pwAid.setHeight(pwHt);
        setAidHighlight(-1);
    }

    protected final void setAid(String aid) {
        setAidEntries(new String[]{aid});
        setAidHighlight(-1);
    }

    protected final void setAidHighlight(int highlight) {
        final int numEntries = llAid.getChildCount();
        for (int i = 0; i < numEntries; i++)
            ((TextView) (llAid.getChildAt(i))).setTypeface(i == highlight ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        if (pwAid.isShowing())
            llAid.invalidate();
    }

    protected final void showAid(int x, int y) {
        int[] loc = new int[2];
        getLocationOnScreen(loc);
        if (pwAid.isShowing())
            pwAid.update(x - pwWd / 2 + loc[0], y - pwHt - yOff + loc[1], -1, -1);
        else
            pwAid.showAtLocation(this, Gravity.LEFT | Gravity.TOP, x - pwWd / 2 + loc[0], y - pwHt - yOff + loc[1]);
        getParent().requestDisallowInterceptTouchEvent(true);
    }

    protected final void showAid(float x, float y) {
        showAid((int) x, (int) y);
    }

    protected final void hideAid() {
        if (pwAid.isShowing())
            pwAid.dismiss();
    }


    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        width = (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(widthMeasureSpec) : getSuggestedMinimumWidth();
        height = (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(heightMeasureSpec) : getSuggestedMinimumHeight();
        setMeasuredDimension(width, height);
    }


    public final void setOnInputListener(OnInputListener oil) {
        this.oil = oil;
    }

    public interface OnInputListener {
        void onInput(int x, String s);
    }

}
