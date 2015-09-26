package cz.absolutno.sifry.morse;

import java.util.ArrayList;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;
import cz.absolutno.sifry.common.widget.ButtonView.OnInputListener;

public final class MorseDFragment extends AbstractDFragment {

	private TextView vstup;
	private MorseLA adapter;

	@Override
	protected int getMenuCaps() {
		return HAS_CLEAR;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(adapter == null)
			adapter = new MorseLA();
		if(savedInstanceState != null)
	    	adapter.load(savedInstanceState.getIntegerArrayList(App.VSTUP2));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.morsed_layout, null);
		FrameLayout fl = new FrameLayout(getActivity());
		
		vstup = (TextView)v.findViewById(R.id.tvMDVstup);
		ImageView ivBsp = (ImageView)v.findViewById(R.id.ivBsp);
		ivBsp.setOnClickListener(bspListener);
		ivBsp.setOnLongClickListener(clearListener);
		
		((MorseView)v.findViewById(R.id.mvMDVstup)).setOnInputListener(new OnInputListener() {
			public void onInput(int tag, String text) {
				vstup.append(text);
				adapter.append(tag);
			}
		});
		
		((ListView)v.findViewById(R.id.lvMDReseni)).setAdapter(adapter);
		((ListView)v.findViewById(R.id.lvMDReseni)).setOnItemClickListener(Utils.copyItemClickListener);
		
		fl.addView(v);
		return fl;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		ViewGroup vp = (ViewGroup)getView();
		View v = onCreateView(App.getInflater(), null, null);
		vp.removeAllViews();
		vp.addView(v);
	}
	
	private final OnClickListener bspListener = new OnClickListener() {
		public void onClick(View v) {
			if(vstup.length() > 0)
				vstup.setText(vstup.getText().subSequence(0, vstup.length()-1));
			adapter.removeLast();
		}
	};

    @Override
    public void loadData(Bundle data) {
    	adapter.load(data.getIntegerArrayList(App.VSTUP2));
    }
    
    @Override
    public boolean saveData(Bundle data) {
    	if(adapter.getCount() == 0)
    		return false;
    	data.putString(App.VSTUP, adapter.getItem(0));
    	return true;
    }

	@Override
    protected void onClear() {
    	vstup.setText("");
    	adapter.clear();
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putIntegerArrayList(App.VSTUP2, adapter.getData());
	}
	
	@Override
	public void onResume() {
		super.onResume();
    	String[] znaky = getResources().getStringArray(R.array.saMDZnaky);
    	StringBuilder sb = new StringBuilder();
    	ArrayList<Integer> raw = adapter.getData();
    	for(Integer i : raw)
    		sb.append(znaky[i]);
    	vstup.setText(sb.toString());
	}
	
    
	private static final class MorseLA extends BaseAdapter {
		
		private String[] varianty;
		private ArrayList<Integer> raw = new ArrayList<Integer>();
		private MorseDecoder md = new MorseDecoder();
		
		public MorseLA() {
			varianty = App.getContext().getResources().getStringArray(R.array.saMDVarianty);
		}
		
		public void load(ArrayList<Integer> raw) {
			this.raw = raw;
			notifyDataSetChanged();
		}
		
		public void clear() {
			raw.clear();
			notifyDataSetChanged();
		}
		
		public void append(int x) {
			raw.add(x);
			notifyDataSetChanged();
		}
		
		public void removeLast() {
			if(raw == null) return;
			int sz = raw.size();
			if(sz == 0) return;
			raw.remove(sz-1);
			notifyDataSetChanged();
		}
		
		public ArrayList<Integer> getData() {
			return raw;
		}


		public int getCount() {
			if(raw != null)
				return varianty.length;
			else
				return 0;
		}
	
		private String getItemDesc(int position) {
			return varianty[position];
		}
	
		public String getItem(int position) {
			return decodeRaw((position&1)!=0, (position&2)!=0);
		}
	
		public long getItemId(int position) {
			return position;
		}
	
		public View getView(int position, View convertView, ViewGroup parent) {
	        if(convertView == null)
	        	convertView = App.getInflater().inflate(R.layout.gen_list_item, null);
	        TextView tvDesc = (TextView)convertView.findViewById(R.id.desc);
	        TextView tvCont = (TextView)convertView.findViewById(R.id.cont);
	        tvDesc.setText(getItemDesc(position));
	        tvCont.setText(getItem(position));
	        return convertView;
		}
		
		
		
		private String decodeRaw(boolean invert, boolean reverse) {
			final int sz = raw.size();
			int l = 0, v = 0;
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < sz; i++) {
				int t = raw.get(reverse?sz-i-1:i);
				if(t < 2) {
					if(invert) t = 1 - t;
					v = 2*v+t;
					l++;
				} else {
					if(i > 1 || l > 0) /* ignorovat lomítko na začátku */
						sb.append(md.decode((1<<l)+v));
					l = 0;
					v = 0;
				}
			}
			if(l > 0)
				sb.append(md.decode((1<<l)+v));
			return sb.toString();
		}
		
	}

}