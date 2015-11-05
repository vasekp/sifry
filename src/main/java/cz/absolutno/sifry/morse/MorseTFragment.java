package cz.absolutno.sifry.morse;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;

@SuppressWarnings("deprecation")
public final class MorseTFragment extends AbstractDFragment implements SurfaceHolder.Callback {

    private String in;
    private ArrayList<String> bin;
    private ModItem[] mody;
    private int selMod;

    private final Handler handler = new Handler();
    private String binCurr;
    private int activeColor;
    private int delay, freq, vol;
    private int ix1, ix2;

    private Camera cam;
    private Camera.Parameters params;

    private final int rate = 40000;
    private AudioTrack track;
    private short sample[];


    @Override
    protected int getMenuCaps() {
        return 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String entries[] = getResources().getStringArray(R.array.saMTMod);
        int id[] = Utils.getIdArray(R.array.iaMTMod);
        boolean hasFlash = App.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (hasFlash)
            mody = new ModItem[entries.length];
        else
            mody = new ModItem[entries.length - 1];
        int j = 0;
        for (int i = 0; i < entries.length; i++) {
            if (id[i] == R.id.idMTSvetlo && !hasFlash) continue;
            mody[j] = new ModItem(id[i], entries[i]);
            j++;
        }

        Bundle args = getArguments();
        if (args != null) {
            in = args.getString(App.VSTUP1);
            bin = args.getStringArrayList(App.DATA);
        }

        activeColor = ContextCompat.getColor(getContext(), R.color.morseActiveColor);

        setRetainInstance(true);
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.morset_layout, container, false);

        ArrayAdapter<ModItem> adapter = new ArrayAdapter<ModItem>(getActivity(), android.R.layout.simple_spinner_item, mody);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) v.findViewById(R.id.spMTMod)).setAdapter(adapter);

        ((SeekBar) v.findViewById(R.id.sbMTSpeed)).setProgress(getResources().getInteger(R.integer.mtSpeedDef) - getResources().getInteger(R.integer.mtSpeedMin));
        ((SeekBar) v.findViewById(R.id.sbMTFreq)).setProgress(getResources().getInteger(R.integer.mtFreqDef) - getResources().getInteger(R.integer.mtFreqMin));
        ((SeekBar) v.findViewById(R.id.sbMTVol)).setProgress(getResources().getInteger(R.integer.mtVolDef) - getResources().getInteger(R.integer.mtVolMin));

        v.findViewById(R.id.btMTGo).setOnClickListener(goListener);

        ((SurfaceView) v.findViewById(R.id.sfMTPreview)).getHolder().addCallback(this);
        ((SurfaceView) v.findViewById(R.id.sfMTPreview)).getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        ((Spinner) v.findViewById(R.id.spMTMod)).setOnItemSelectedListener(modListener);

        ((TextView) v.findViewById(R.id.tvMTVstup)).setText(in);

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        stop();
        if (cam != null) {
            cam.stopPreview();
            cam.release();
        }
        cam = null;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume() {
        super.onResume();
        View v = getView();
        ((SeekBar) v.findViewById(R.id.sbMTSpeed)).setOnSeekBarChangeListener(speedListener);
        ((SeekBar) v.findViewById(R.id.sbMTFreq)).setOnSeekBarChangeListener(freqListener);
        ((SeekBar) v.findViewById(R.id.sbMTVol)).setOnSeekBarChangeListener(volListener);
        speedListener.onProgressChanged(null, ((SeekBar) v.findViewById(R.id.sbMTSpeed)).getProgress(), false);
        freqListener.onProgressChanged(null, ((SeekBar) v.findViewById(R.id.sbMTFreq)).getProgress(), false);
        volListener.onProgressChanged(null, ((SeekBar) v.findViewById(R.id.sbMTVol)).getProgress(), false);
        try {
            cam = Camera.open();
            if (cam != null)
                params = cam.getParameters();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        if (cam != null) {
            try {
                cam.setPreviewDisplay(holder);
                cam.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (cam != null) {
            try {
                cam.setPreviewDisplay(holder);
                cam.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (cam != null)
            cam.stopPreview();
    }


    private final OnSeekBarChangeListener speedListener = new OnSeekBarChangeListener() {

        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @SuppressWarnings("ConstantConditions")
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int x = progress + getResources().getInteger(R.integer.mtSpeedMin);
            ((TextView) getView().findViewById(R.id.tvMTSpeed)).setText(String.format(getString(R.string.patMTSpeed), x));
            delay = 1200 / x;
        }
    };

    private final OnSeekBarChangeListener freqListener = new OnSeekBarChangeListener() {

        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @SuppressWarnings("ConstantConditions")
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            freq = (progress + getResources().getInteger(R.integer.mtFreqMin)) * 10;
            ((TextView) getView().findViewById(R.id.tvMTFreq)).setText(String.format(getString(R.string.patMTFreq), freq));
        }
    };

    private final OnSeekBarChangeListener volListener = new OnSeekBarChangeListener() {

        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @SuppressWarnings("ConstantConditions")
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            vol = (progress + getResources().getInteger(R.integer.mtVolMin));
            ((TextView) getView().findViewById(R.id.tvMTVol)).setText(String.format(getString(R.string.patMTVol), vol));
        }
    };

    private final OnItemSelectedListener modListener = new OnItemSelectedListener() {
        @SuppressWarnings("ConstantConditions")
        public void onItemSelected(AdapterView<?> parentView, View childView, int position, long id) {
            selMod = ((ModItem) parentView.getAdapter().getItem(position)).id;
            getView().findViewById(R.id.llMTFreq).setVisibility(selMod == R.id.idMTZvuk ? View.VISIBLE : View.GONE);
            getView().findViewById(R.id.btMTGo).setEnabled(!(selMod == R.id.idMTSvetlo && cam == null));
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    private final OnClickListener goListener = new OnClickListener() {
        public void onClick(View v) {
            ix1 = 0;
            setRunning(true);
            if (selMod == R.id.idMTZvuk) {
                genSample();
                android.util.Log.d("sampleSize", Integer.toString(sample.length / 2));
                track = new AudioTrack(AudioManager.STREAM_MUSIC, rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 4 * Utils.ceil(sample.length / 4f), AudioTrack.MODE_STREAM);
                track.play();
            }
            handler.postDelayed(newChar, delay);
        }
    };

    private final OnClickListener stopListener = new OnClickListener() {
        public void onClick(View v) {
            stop();
        }
    };


    @SuppressWarnings("ConstantConditions")
    private void setRunning(boolean running) {
        View v = getView();
        v.findViewById(R.id.spMTMod).setEnabled(!running);
        v.findViewById(R.id.sbMTFreq).setEnabled(!running);
        v.findViewById(R.id.sbMTSpeed).setEnabled(!running);
        v.findViewById(R.id.sbMTVol).setEnabled(!running);
        ((Button) v.findViewById(R.id.btMTGo)).setText(running ? R.string.tStop : R.string.tStart);
        v.findViewById(R.id.btMTGo).setOnClickListener(running ? stopListener : goListener);
    }

    @SuppressWarnings("ConstantConditions")
    private void stop() {
        handler.removeCallbacks(newChar);
        handler.removeCallbacks(newBit);
        if (selMod == R.id.idMTSvetlo) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            cam.setParameters(params);
        } else if (selMod == R.id.idMTZvuk) {
            if (track != null)
                track.release();
            track = null;
        }
        ((TextView) getView().findViewById(R.id.tvMTVstup)).setText(in);
        setRunning(false);
    }

    private void genSample() {
        int len = (int) Math.floor(freq * delay / 1000 * 1.05f);
        len = (int) Math.ceil((float) rate / freq * len);
        float t;
        sample = new short[len];
        for (int i = 0; i < len; i++) {
            t = (float) i / rate * freq;
            t = t - (float) Math.floor(t);
            sample[i] = (short) ((t < 0.5f) ? 65535 * (t - 0.25f) * vol / 100 : 65535 * (0.75f - t) * vol / 100);
        }
    }


    private final Runnable newChar = new Runnable() {
        @SuppressWarnings("ConstantConditions")
        public void run() {
            SpannableString ss = new SpannableString(in);
            ss.setSpan(new ForegroundColorSpan(activeColor), ix1, ix1 + 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((TextView) getView().findViewById(R.id.tvMTVstup)).setText(ss);
            String s = bin.get(ix1);
            StringBuilder sb = new StringBuilder();
            int len = s.length();
            if (len > 0)
                for (int i = 0; i < len; i++)
                    sb.append(s.charAt(i) == '1' ? "1110" : "10");
            else
                sb.append("0");
            binCurr = sb.toString();
            ix2 = 0;
            handler.post(newBit);
        }
    };

    private final Runnable newBit = new Runnable() {
        public void run() {
            if (ix2 < binCurr.length() - 1)
                handler.postDelayed(newBit, delay);
            else {
                ix1++;
                if (ix1 < in.length())
                    handler.postDelayed(newChar, 3 * delay);
                else
                    handler.post(finish);
            }
            if (selMod == R.id.idMTSvetlo) {
                params.setFlashMode(binCurr.charAt(ix2) == '1' ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
                cam.setParameters(params);
            } else if (binCurr.charAt(ix2) == '1') track.write(sample, 0, sample.length);
            ix2++;
        }
    };

    private final Runnable finish = new Runnable() {
        public void run() {
            stop();
        }
    };


    private static final class ModItem {
        private final int id;
        private final String s;

        public ModItem(int id, String s) {
            this.id = id;
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }
    }

}
