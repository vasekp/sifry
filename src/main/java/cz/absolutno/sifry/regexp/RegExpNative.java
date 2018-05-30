package cz.absolutno.sifry.regexp;

import android.content.res.AssetManager;

import cz.absolutno.sifry.App;

public final class RegExpNative {

    static {
        System.loadLibrary("regrep");
    }

    public static final class Progress {
        public static final int RUN = 0;
        public static final int ERR = 1;
        public static final int MATCHES = 2;
        public static final int POS = 3;
        public static final int SIZE = 4;
    }

    private long nativeContext;

    private native void init();

    public native void free();

    private native void nativeFinalize();

    public native void startThread(AssetManager mgr, String fn, String re[]);

    public native void stopThread();

    public native boolean isRunning();

    public native int[] getProgress();

    public native String getResult(int ix);

    public native String getError();

    public RegExpNative() {
        init();
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        nativeFinalize();
    }

}
