package cz.absolutno.sifry.transpozice;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;

import java.util.ArrayList;

import cz.absolutno.sifry.common.alphabet.StringParser;
import cz.absolutno.sifry.common.widget.ColorChunkTextView;
import cz.absolutno.sifry.common.widget.ColorChunkTextView.ColorChunk;
import cz.absolutno.sifry.common.widget.ColorChunkTextView.OnChunkClickListener;

public final class LineTransView extends TransView {

    private ColorChunkTextView ctv = null;

    public LineTransView(Context ctx, AttributeSet as) {
        super(ctx, as);
    }

    public void setChunkTextView(final ColorChunkTextView ctv) {
        this.ctv = ctv;

        ctv.setOnChunkClickListener(new OnChunkClickListener() {
            public void onChunkClick(int ix) {
                registerInput(ix);
                refreshColors();
            }
        });
    }

    @Override
    public void setText(String s) {
        ArrayList<ColorChunk> data = new ArrayList<>();
        StringParser sp = abc.getStringParser(s);
        int ord;
        while ((ord = sp.getNextOrd()) != StringParser.EOF) {
            if (ord < 0 && ignoreNP) continue;
            String chr = (ord >= 0 ? abc.chr(ord) : String.valueOf(sp.getLastChar()));
            data.add(new ColorChunk(chr, 0));
        }
        ctv.setData(data);
        super.setText(s);
    }

    @Override
    public void clearMarks() {
        super.clearMarks();
        refreshColors();
    }

    @Override
    public void clearTrf() {
        super.clearTrf();
        ctv.setTextSize(0);
        ctv.setScroll(0);
    }

    private void refreshColors() {
        int len = getLength();
        for (int i = 0; i < len; i++)
            ctv.setChunkColor(i, getColor(i, ctv.getChunk(i).chr));
    }

    @Override
    public void onRevX() {
    }

    @Override
    public void onRevY() {
    }

    @Override
    public void onRevDiag() {
    }

    @Override
    protected PointF getCoords(int i) {
        return null;
    }

    @Override
    protected float getSuggestedTextSize() {
        return 0;
    }

}