package cz.absolutno.sifry.regexp;

import android.content.res.AssetManager;

public final class RegExpNative {

    static {
        System.loadLibrary("regrep");
    }

    public static final int MaxListResults = 201;

    private long nativeContext;

    private native void init();

    public native void free();

    private native void nativeFinalize();

    public native void startThread(AssetManager mgr, String fn, String re[]);

    public native void stopThread();

    public native boolean isRunning();

    public native Report getProgress();

    public native String getError();

    public native String getResult(int ix);

    RegExpNative() {
        init();
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        nativeFinalize();
    }

    static class Report {
        public boolean running;
        public boolean error;
        public int matches;
        public float progress;
    }

}
