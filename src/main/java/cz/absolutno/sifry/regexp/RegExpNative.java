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

    @SuppressWarnings("FieldCanBeLocal")
    private final AssetManager mgr; // We need to keep an explicit reference as long as re is running.

    private native void init(AssetManager mgr, String fn);

    private native void swtch(String fn);

    public native void free();

    private native void nativeFinalize();

    public native void startThread(String re[]);

    public native void stopThread();

    public native boolean isRunning();

    public native int[] getProgress();

    public native String getResult(int ix);

    public native String getError();

    public RegExpNative(String filename) {
        mgr = App.getContext().getAssets();
        init(mgr, "raw/" + filename);
    }

    public void switchFilename(String filename) {
        swtch("raw/" + filename);
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        nativeFinalize();
    }

}
