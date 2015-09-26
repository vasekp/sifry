package cz.absolutno.sifry.common.alphabet;

public class FullAlphabet extends Alphabet {
	
	private final String full;
	
	public FullAlphabet(String full) {
		this.full = full;
	}

	@Override
	public int count() {
		return full.length();
	}
	
	@Override
	public String chr(int i) {
		if(i < 0 || i >= count()) return "?";
		return String.valueOf(full.charAt(i));
	}
	
	@Override
	public int ord(String c) {
		if(c.length() != 1) return -1;
		else return full.indexOf(c);
	}
	
	protected int ord(char c) {
		return full.indexOf(c);
	}
	
	@Override
	public StringParser getStringParser(String s) {
		return new FullStringParser(s);
	}
	
	
	public class FullStringParser extends StringParser {
		
		public FullStringParser(String s) {
			super(s);
		}
		
		@Override
		protected Ret next() {
			char c = s.charAt(ix++);
			int o = full.indexOf(c);
			return new Ret(o >= 0 ? o : ERR, c);
		}
	}
}