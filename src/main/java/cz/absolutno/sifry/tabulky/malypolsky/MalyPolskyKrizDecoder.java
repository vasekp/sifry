package cz.absolutno.sifry.tabulky.malypolsky;

import java.util.ArrayList;

import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.alphabet.Alphabet;
import cz.absolutno.sifry.common.alphabet.StringParser;
import cz.absolutno.sifry.common.decoder.Decoder;

@SuppressWarnings("PointlessBitwiseExpression")
public final class MalyPolskyKrizDecoder extends Decoder {
    private int var;
    private final Alphabet abc;

    private static final int YQ_SHIFT = 5;
    private static final int XQ_SHIFT = 4;
    private static final int Y_SHIFT = 2;
    private static final int X_SHIFT = 0;
    private static final int YQ_MASK = 0x20;
    private static final int XQ_MASK = 0x10;
    private static final int Y_MASK = 0xC;
    private static final int X_MASK = 0x3;

    public MalyPolskyKrizDecoder(int var, String abcVar) {
        this.var = var;
        abc = Alphabet.getVariantInstance(26, abcVar);
    }

    @SuppressWarnings("unused")
    public void setVar(int var) {
        this.var = var;
    }

	/* Souřadnice v MPK:
	 * (3x3)
	 * -1,-1   0,-1  +1,-1
	 * -1, 0   0, 0  +1, 0
	 * -1,+1   0,+1  +1,+1
	 * 
	 * (2x2)
	 *     -1,-1
	 * +1,-1   -1,+1
	 *     +1,+1
	 */

    public String decode(int x, int y, int xq, int yq) {
        int ixc;
        x++;
        y++;
        if (yq == 1) {
            x /= 2;
            y /= 2;
        }
        switch (var) {
            case R.id.idTDMP9T4:
                ixc = (yq == 0 ? xq * 9 + y * 3 + x : 18 + xq * 4 + y * 2 + x);
                break;
            case R.id.idTDMP94T:
                ixc = (yq == 0 ? xq * 13 + y * 3 + x : xq * 13 + 9 + y * 2 + x);
                break;
            case R.id.idTDMPT94:
                ixc = (yq == 0 ? y * 6 + x * 2 + xq : 18 + y * 4 + x * 2 + xq);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return abc.chr(ixc);
    }

    public String decode(int[] a) {
        return decode(a[0], a[1], a[2], a[3]);
    }

    @Override
    public String decode(int x) {
        return decode(parseInt(x));
    }


    public static int buildInt(int x, int y, int xq, int yq) {
        return (yq << YQ_SHIFT) + (xq << XQ_SHIFT) + ((y + 1) << Y_SHIFT) + ((x + 1) << X_SHIFT);
    }

    public static int[] parseInt(int x) {
        return new int[]{((x & X_MASK) >> X_SHIFT) - 1, ((x & Y_MASK) >> Y_SHIFT) - 1, (x & XQ_MASK) >> XQ_SHIFT, (x & YQ_MASK) >> YQ_SHIFT};
    }

    @Override
    protected boolean encodeInternal(String s, ArrayList<Integer> list) {
        StringParser sp = abc.getStringParser(s);
        boolean err = false;
        int ord, x, y, xq, yq;
        while ((ord = sp.getNextOrd()) != StringParser.EOF) {
            if (ord == StringParser.ERR) {
                err = true;
                continue;
            }
            switch (var) {
                case R.id.idTDMP9T4:
                    if (ord >= 18) {
                        yq = 1;
                        ord -= 18;
                        xq = (ord & 4) >> 2;
                        y = ord & 2;
                        x = (ord & 1) << 1;
                    } else {
                        yq = 0;
                        xq = ord / 9;
                        y = (ord / 3) % 3;
                        x = ord % 3;
                    }
                    break;
                case R.id.idTDMP94T:
                    xq = (ord >= 13 ? 1 : 0);
                    ord -= xq * 13;
                    if (ord >= 9) {
                        yq = 1;
                        ord -= 9;
                        y = ord & 2;
                        x = (ord & 1) << 1;
                    } else {
                        yq = 0;
                        y = (ord / 3) % 3;
                        x = ord % 3;
                    }
                    break;
                case R.id.idTDMPT94:
                    xq = ord & 1;
                    ord /= 2;
                    if (ord >= 9) {
                        yq = 1;
                        ord -= 9;
                        y = ord & 2;
                        x = (ord & 1) << 1;
                    } else {
                        yq = 0;
                        y = (ord / 3) % 3;
                        x = ord % 3;
                    }
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            list.add(buildInt(x - 1, y - 1, xq, yq));
        }
        return err;
    }

}
