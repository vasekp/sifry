package cz.absolutno.sifry.regexp;

import java.io.File;

public final class RegExpNative {

	static {
		System.loadLibrary("pcre");
		System.loadLibrary("regrep");
	}
	
	private String fname; 

	private native void init(String fn);
	public native int free();
	private native void nativeFinalize();

	public native void startThread(String re[]);
	public native void stopThread();
	public native boolean isRunning();

	public native int[] getProgress();
	public native String getResult(int ix);
	public native String getError();
  
	public RegExpNative(String path) {
		fname = new File(path).getName();
		init(path);
	}
	
	@Override
	public void finalize() {
		nativeFinalize();
	}
	
	public String getFileName() {
		return fname;
	}
	
}
