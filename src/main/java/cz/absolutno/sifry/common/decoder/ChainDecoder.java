package cz.absolutno.sifry.common.decoder;

import java.util.ArrayList;

public abstract class ChainDecoder extends Decoder {
    protected abstract boolean isTemp();

    protected abstract EncodeResult encodeSingle(String s, int ix, boolean prefix);

    public static final class EncodeResult {
        final int code;
        final int len;
        int prefix;

        EncodeResult(int code, int len) {
            this.code = code;
            this.prefix = -1;
            this.len = len;
        }

        EncodeResult(EncodeResult er) {
            code = er.code;
            prefix = er.prefix;
            len = er.len;
        }

        EncodeResult(EncodeResult er, int prefix) {
            this(er);
            this.prefix = prefix;
        }
    }

    @Override
    protected boolean encodeInternal(String s, ArrayList<Integer> list) {
        final int len = s.length();
        boolean err = false;
        int ix;
        for (ix = 0; ix < len; ix++) {
            EncodeResult er = encodeSingle(s, ix, false);
            if (er == null) {
                err = true;
                continue;
            }
            if (er.prefix >= 0)
                list.add(er.prefix);
            list.add(er.code);
            ix += er.len - 1;
        }
        return err;
    }
}
