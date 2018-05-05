package cz.absolutno.sifry.common.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import cz.absolutno.sifry.R;

public final class SeekBarPreference extends DialogPreference {

    private SeekBar input;
    private TextView tVal;
    private final String hi, lo, fmt;
    private final int min, max;
    private int value;
    private final boolean showHiLo, showVal;

    public SeekBarPreference(Context ctx, AttributeSet as) {
        super(ctx, as);

        setDialogLayoutResource(R.layout.seekbar_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        TypedArray ta = getContext().obtainStyledAttributes(as, R.styleable.SeekBarPreference);
        min = ta.getInt(R.styleable.SeekBarPreference_min, 1);
        max = ta.getInt(R.styleable.SeekBarPreference_max, 10);
        hi = ta.getString(R.styleable.SeekBarPreference_hi);
        lo = ta.getString(R.styleable.SeekBarPreference_lo);
        fmt = ta.getString(R.styleable.SeekBarPreference_format);
        showHiLo = ta.getBoolean(R.styleable.SeekBarPreference_showHiLo, false);
        showVal = ta.getBoolean(R.styleable.SeekBarPreference_showVal, true);
        ta.recycle();

        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        tVal = view.findViewById(R.id.tvSbDVal);
        if (!showVal)
            tVal.setVisibility(View.GONE);

        input = view.findViewById(R.id.sbSbDVstup);
        input.setMax(max - min);
        input.setOnSeekBarChangeListener(listener);
        input.setProgress(value - min);

        TextView hi = view.findViewById(R.id.tvSbDHi);
        TextView lo = view.findViewById(R.id.tvSbDLo);
        if (showHiLo) {
            hi.setText(this.hi);
            lo.setText(this.lo);
        } else {
            hi.setVisibility(View.GONE);
            lo.setVisibility(View.GONE);
        }
    }

    private final OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {

        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            value = progress + min;
            tVal.setText(fmt != null ? String.format(fmt, value) : Integer.toString(value));
        }
    };

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult)
            persistInt(value);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            value = getPersistedInt(0);
        } else {
            value = (Integer) defaultValue;
            persistInt(value);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, 0);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent())
            return superState;

        final SavedState myState = new SavedState(superState);
        myState.value = value;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        input.setProgress(myState.value);
    }


    private static final class SavedState extends BaseSavedState {
        int value;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(value);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

}
