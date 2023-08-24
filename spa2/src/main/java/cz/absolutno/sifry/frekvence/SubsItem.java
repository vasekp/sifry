package cz.absolutno.sifry.frekvence;

import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;

public final class SubsItem implements Serializable {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 3996457042461256515L;
    public final int ord;
    public final String orig;
    public int cnt;
    public String repl;

    public SubsItem(int ord, String chr, int cnt) {
        super();
        this.ord = ord;
        this.orig = chr;
        this.cnt = cnt;
        this.repl = null;
    }


    static class AlphComparator implements Comparator<SubsItem> {
        final Collator c = Collator.getInstance();

        public int compare(SubsItem lhs, SubsItem rhs) {
            if (lhs.ord >= 0 && rhs.ord >= 0)
                return lhs.ord - rhs.ord;
            else if (lhs.ord >= 0 || rhs.ord >= 0)
                return (lhs.ord >= 0) ? -1 : 1;
            else return c.compare(lhs.orig, rhs.orig);
        }
    }

    protected static class FreqComparator extends AlphComparator {
        @Override
        public int compare(SubsItem lhs, SubsItem rhs) {
            if (lhs.cnt != rhs.cnt)
                return rhs.cnt - lhs.cnt;
            else
                return super.compare(lhs, rhs);
        }
    }
}