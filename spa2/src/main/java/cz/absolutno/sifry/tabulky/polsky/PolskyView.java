package cz.absolutno.sifry.tabulky.polsky;

import android.content.Context;
import android.util.AttributeSet;

import cz.absolutno.sifry.common.decoder.Decoder;
import cz.absolutno.sifry.tabulky.AidView;

public abstract class PolskyView extends AidView {

    final PolskyKrizDecoder pk;

    public PolskyView(Context ctx, AttributeSet as) {
        super(ctx, as);
        pk = new PolskyKrizDecoder();
    }

    public final void setVar(String var) {
        pk.setVar(var);
        invalidate();
    }

    public final String chr(int i) {
        return pk.decode(i);
    }

    @SuppressWarnings("unused")
    public final Decoder getDecoder() {
        return pk;
    }

}