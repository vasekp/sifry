package cz.absolutno.sifry.substituce;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.common.alphabet.Alphabet;

public abstract class AbstractSubstAdapter extends BaseAdapter {

    protected final Alphabet abc;
    protected final int cnt;
    protected String str;

    AbstractSubstAdapter(Alphabet abc) {
        this.abc = abc;
        cnt = abc.count();
    }

    public void setInput(String str) {
        this.str = str;
        notifyDataSetChanged();
    }

    public void clear() {
        this.str = null;
        notifyDataSetChanged();
    }

    public final int getCount() {
        if (isValid())
            return getCountValid();
        else
            return 0;
    }

    protected boolean isValid() {
        return (str != null && str.length() != 0);
    }

    protected abstract int getCountValid();

    public abstract String getItem(int position);

    public abstract String getItemDesc(int position);

    public final long getItemId(int position) {
        return position;
    }

    public final View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = App.getInflater().inflate(R.layout.gen_list_item, parent);
        TextView tvDesc = (TextView) convertView.findViewById(R.id.desc);
        TextView tvCont = (TextView) convertView.findViewById(R.id.cont);
        tvDesc.setText(getItemDesc(position));
        tvCont.setText(getItem(position));
        return convertView;
    }

}
