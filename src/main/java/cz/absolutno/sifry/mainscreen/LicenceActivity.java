package cz.absolutno.sifry.mainscreen;

import android.app.ExpandableListActivity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class LicenceActivity extends ExpandableListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.updateLocale();
        setListAdapter(new LicenceELA());
    }


    static final class LicenceELA extends BaseExpandableListAdapter {

        private final String[] groups;
        private final int[] groupIDs;

        LicenceELA() {
            groups = App.getContext().getResources().getStringArray(R.array.saLicence);
            groupIDs = Utils.getIdArray(R.array.iaLicence);
        }

        public int getGroupCount() {
            return groups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupIDs[groupPosition];
        }

        public String getGroup(int groupPosition) {
            if (getGroupId(groupPosition) == R.raw.lic_sifry) {
                try {
                    String thisVersion = App.getContext().getPackageManager().getPackageInfo(App.getContext().getPackageName(), 0).versionName;
                    return String.format(groups[groupPosition], App.getContext().getString(R.string.app_name), thisVersion);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                    return "";
                }
            } else if (getGroupId(groupPosition) == R.raw.lic_dict)
                return String.format(groups[groupPosition], App.getContext().getString(R.string.tLicDict));
            else
                return groups[groupPosition];
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.gen_group_item, parent, false);
            ((TextView) convertView).setText(getGroup(groupPosition));
            return convertView;
        }

        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return 1;
        }

        public String getChild(int groupPosition, int childPosition) {
            InputStream is = App.getContext().getResources().openRawResource((int) getGroupId(groupPosition));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int size;
            try {
                while ((size = is.read(buffer)) >= 0)
                    os.write(buffer, 0, size);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return os.toString();
        }

        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = App.getInflater().inflate(R.layout.licence_item, parent, false);
            ((TextView) convertView).setText(getChild(groupPosition, childPosition));
            return convertView;
        }

        public boolean hasStableIds() {
            return true;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

    }
}
