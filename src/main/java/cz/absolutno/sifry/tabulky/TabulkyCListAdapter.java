package cz.absolutno.sifry.tabulky;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

public abstract class TabulkyCListAdapter extends BaseAdapter {

    protected static final int TYPE_TEXT = 0;
    protected static final int TYPE_GRID = 1;

    private final ArrayList<Integer> raw;

    protected TabulkyCListAdapter() {
        raw = new ArrayList<>();
    }

    public final boolean load(String input) {
        notifyDataSetChanged();
        return encode(input, raw);
    }

    protected abstract boolean encode(String input, ArrayList<Integer> raw);

    public final ArrayList<Integer> getData() {
        return raw;
    }

    protected void setVar(int var, String abcVar) {
    }

    protected final boolean anyData() {
        return (raw.size() > 0);
    }


    protected abstract String getItemDesc(int position);

    public abstract String getItem(int position);

    @Override
    public abstract int getItemViewType(int position);

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    protected final View getViewHelper(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = App.getInflater();
        if (getItemViewType(position) == TYPE_GRID) {
            convertView = inflater.inflate(R.layout.grid_list_item, parent, false);
            ((TextView) convertView.findViewById(R.id.desc)).setText(getItemDesc(position));
        } else {
            if (convertView == null || convertView.getId() == R.id.itemGrid)
                convertView = inflater.inflate(R.layout.gen_list_item, parent, false);
            ((TextView) convertView.findViewById(R.id.desc)).setText(getItemDesc(position));
            ((TextView) convertView.findViewById(R.id.cont)).setText(getItem(position));
        }
        return convertView;
    }

}
