package cz.absolutno.sifry.vlajky;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.larvalabs.svgandroid.SVG;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.activity.AbstractCFragment;
import cz.absolutno.sifry.common.alphabet.DigitsExtendedAlphabet;
import cz.absolutno.sifry.common.alphabet.PlainEnglishAlphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;
import cz.absolutno.sifry.common.widget.FixedGridLayout;

public final class VlajkyCFragment extends AbstractCFragment {

    private VlajkySVGs svgs;
    private FixedGridLayout fgl;
    private String vstup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vlajkyc_layout, container, false);
        fgl = v.findViewById(R.id.fglVCVystup);
        svgs = VlajkySVGs.getInstance();
        fgl.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                copyImage(fgl);
            }
        });
        return v;
    }

    @Override
    public boolean encode(String input) {
        StringBuilder sb = new StringBuilder();
        fgl.removeAllViews();
        LayoutInflater inflater = App.getInflater();
        DigitsExtendedAlphabet<?> abc = new DigitsExtendedAlphabet<>(new PlainEnglishAlphabet());
        StringParser sp = abc.getStringParser(input);
        int ord;
        boolean err = false;
        while ((ord = sp.getNextOrd()) != StringParser.EOF) {
            if (ord == StringParser.ERR) {
                err = true;
                continue;
            }
            SVG svg = svgs.getChar(abc.chr(ord).charAt(0));
            ImageView v = (ImageView) inflater.inflate(R.layout.vlajkyc_item, fgl, false);
            v.setImageDrawable(svg.getDrawable());
            fgl.addView(v);
            sb.append(abc.chr(ord));
        }
        vstup = sb.toString();
        return err;
    }

    @Override
    public void packData(Bundle data) {
        data.putString(App.VSTUP, vstup);
    }

}
