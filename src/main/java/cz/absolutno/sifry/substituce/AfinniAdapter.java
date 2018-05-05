package cz.absolutno.sifry.substituce;

import cz.absolutno.sifry.common.alphabet.Alphabet;

final class AfinniAdapter extends PosunyAdapter {

    private int coeff = 1;

    public AfinniAdapter(Alphabet abc) {
        super(abc);
    }

    public void setCoeff(int coeff) {
        this.coeff = coeff;
        notifyDataSetChanged();
    }

    @Override
    public int encode(int ord, int position) {
        return (ord * coeff + position) % cnt;
    }

}
