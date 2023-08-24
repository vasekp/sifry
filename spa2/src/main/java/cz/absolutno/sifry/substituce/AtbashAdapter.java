package cz.absolutno.sifry.substituce;

import cz.absolutno.sifry.common.alphabet.Alphabet;

final class AtbashAdapter extends PosunyAdapter {

    public AtbashAdapter(Alphabet abc) {
        super(abc);
    }

    @Override
    public int encode(int ord, int position) {
        return (cnt - 1 - ord + cnt - position) % cnt;
    }

}
