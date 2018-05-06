package cz.absolutno.sifry;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cz.absolutno.sifry.common.activity.CopyFragment;

public final class Utils {

    public static int dpToPix(float dp) {
        return (int) (App.getScale() * dp + 0.5);
    }

    @SuppressWarnings("unused")
    public static float pixToDp(float pix) {
        return pix / App.getScale();
    }

    public static int floor(float f) {
        return (int) Math.floor(f);
    }

    public static int ceil(float f) {
        return (int) Math.ceil(f);
    }


    public static void toast(int resid) {
        Toast.makeText(App.getContext(), resid, Toast.LENGTH_SHORT).show();
    }

    public static void toast(CharSequence txt) {
        Toast.makeText(App.getContext(), txt, Toast.LENGTH_SHORT).show();
    }

    private static void copy(FragmentActivity activity, String str) {
        copyExact(activity, str.replace('·', ' '));
    }

    public static void copyExact(FragmentActivity activity, String str) {
        if (str.length() == 0) return;
        Bundle args = new Bundle();
        args.putString(App.VSTUP, str);
        CopyFragment dialog = new CopyFragment();
        dialog.setArguments(args);

        dialog.show(activity.getSupportFragmentManager(), "copy");
    }

    public static final OnClickListener copyClickListener = new OnClickListener() {
        public void onClick(View view) {
            copy((FragmentActivity) view.getContext(), ((TextView) view).getText().toString());
        }
    };

    public static final OnItemClickListener copyItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
            if(parentView.getContext() instanceof FragmentActivity)
                copy((FragmentActivity) parentView.getContext(), parentView.getAdapter().getItem(position).toString());
        }
    };

    public static final OnChildClickListener copyChildClickListener = new OnChildClickListener() {
        public boolean onChildClick(ExpandableListView parentView, View childView, int groupPosition, int childPosition, long id) {
            copy((FragmentActivity) parentView.getContext(), parentView.getExpandableListAdapter().getChild(groupPosition, childPosition).toString());
            return true;
        }
    };


    public static int[] getIdArray(int resId) {
        TypedArray ta = App.getContext().getResources().obtainTypedArray(resId);
        int[] arr = new int[ta.length()];
        for (int i = 0; i < arr.length; i++)
            arr[i] = ta.getResourceId(i, 0);
        ta.recycle();
        return arr;
    }

    @SuppressWarnings("unused")
    public static ArrayList<Integer> getIdList(int resId) {
        TypedArray ta = App.getContext().getResources().obtainTypedArray(resId);
        final int len = ta.length();
        ArrayList<Integer> arr = new ArrayList<>(len);
        for (int i = 0; i < len; i++)
            arr.add(ta.getResourceId(i, 0));
        ta.recycle();
        return arr;
    }

    public static CharSequence[][] load2DStringArray(int arrID) {
        TypedArray ta = App.getContext().getResources().obtainTypedArray(arrID);
        CharSequence[][] arr = new CharSequence[ta.length()][];
        for (int i = 0; i < arr.length; i++)
            arr[i] = ta.getTextArray(i);
        ta.recycle();
        return arr;
    }


    public static CharSequence fromHtml(String s) {
        CharSequence cs = Html.fromHtml(s);
        int i = cs.length() - 1;
        while(i > 0 && Character.isWhitespace(cs.charAt(i - 1))) i--;
        return cs.subSequence(0, i);
    }


    public static String getCharDesc(char c, String mezera) {
        if (Character.isLetterOrDigit(c))
            return String.valueOf(c);
        else if (c == '\n')
            return "↵";
        else if (c == ' ')
            return mezera;
        else
            return "\"" + String.valueOf(c) + "\"";
    }

}
