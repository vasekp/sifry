package cz.absolutno.sifry.tabulky.polsky;

import cz.absolutno.sifry.common.decoder.AlphabetDecoder;


public final class PolskyKrizDecoder extends AlphabetDecoder {
	public PolskyKrizDecoder() {
		super(27, "");
	}
	
	public PolskyKrizDecoder(String var) {
		super(27, var);
	}
	
	public void setVar(String var) {
		setVar(27, var);
	}
	
	public String decode(int i, int j, int k) {
		return decode(i*9+j*3+k);
	}

	public String decode(int[] a) {
		return decode(a[0]*9+a[1]*3+a[2]);
	}
	
}
