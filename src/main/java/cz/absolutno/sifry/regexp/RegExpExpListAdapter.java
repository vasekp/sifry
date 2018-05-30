package cz.absolutno.sifry.regexp;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;

final class RegExpExpListAdapter extends BaseExpandableListAdapter {

    private int matches = 0;
    private final RegExpNative re;

    RegExpExpListAdapter(RegExpNative re) {
        this.re = re;
    }

    public void update(int matches) {
        this.matches = matches;
        notifyDataSetChanged();
    }

    public void clear() {
        matches = 0;
        notifyDataSetChanged();
    }

    public int getGroupCount() {
        return (matches + 99) / 100;
    }

    public String getGroup(int groupPosition) {
        int ub = groupPosition * 100 + 99;
        if (ub >= matches) ub = matches - 1;
        return String.format("%s – %s", re.getResult(groupPosition * 100), re.getResult(ub));
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = App.getInflater().inflate(R.layout.gen_group_item, parent, false);
        ((TextView) convertView).setText(getGroup(groupPosition));
        return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        if (groupPosition >= getGroupCount()) return 0;
        return (groupPosition < getGroupCount() - 1) ? 100 : (((matches - 1) % 100) + 1);
    }

    public String getChild(int groupPosition, int childPosition) {
        return re.getResult(groupPosition * 100 + childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = App.getInflater().inflate(R.layout.simple_list_item, parent, false);
        ((TextView) convertView).setText(getChild(groupPosition, childPosition));
        return convertView;
    }


    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }

}
