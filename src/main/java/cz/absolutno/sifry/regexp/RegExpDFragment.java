package cz.absolutno.sifry.regexp;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.common.activity.AbstractDFragment;

public final class RegExpDFragment extends AbstractDFragment {

	private static final int progressDelay = 30;
	private static final int matchesDelay = 100;
	
	private RegExpNative re;
	private TextView tvProgress;
	private ProgressBar pbProgress;
	private RegExpExpListAdapter adapter;
	 
	@Override
	protected int getMenuCaps() {
		return HAS_CLEAR;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.regexp_layout, null);
		tvProgress = (TextView)v.findViewById(R.id.tvRDPocet);
		pbProgress = (ProgressBar)v.findViewById(R.id.pbRDProgress);
		((ExpandableListView)v.findViewById(R.id.elRDResults)).setOnChildClickListener(Utils.copyChildClickListener);
		v.findViewById(R.id.btRDGo).setOnClickListener(goListener);
		return v;
	}
	
    public void loadRE(RegExpNative re) {
    	this.re = re;
    	adapter = new RegExpExpListAdapter(re);
		((ExpandableListView)getView().findViewById(R.id.elRDResults)).setAdapter(adapter);
		getView().findViewById(R.id.btRDGo).setEnabled(re != null);
		if(re != null && re.isRunning())
			launchRefresh();
    }

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		tvProgress = null;
		pbProgress = null;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(re == null)
			return;
		final int[] progress = re.getProgress();
		if(progress[1] > 0) {
			int matches = (progress[1] <= 10000)?progress[1]:10000;
			tvProgress.setText(String.valueOf(progress[1]));
			adapter.update(matches);
		}
	}
	
	private OnClickListener goListener = new OnClickListener() {
		public void onClick(View v) {
			if(re.isRunning()) {
				re.stopThread();
				updateGoButton();
				return;
			}
			String zad[] = new String[3];
			for(int i = 0; i < 3; i++) {
				zad[i] = (((ToggleButton)getView().findViewById(idCB[i])).isChecked()?"":"!") +
					((EditText)getView().findViewById(idET[i])).getText().toString();
			}
	    	adapter.clear();
			re.startThread(zad);
			launchRefresh();
		}
	};
	
	public void updateGoButton() {
		((Button)getView().findViewById(R.id.btRDGo)).setText(re.isRunning()?R.string.tRDStop:R.string.tRDHledej);
	}
	
	private void launchRefresh() {
		final Handler h = new Handler();
		Runnable rMain = new Runnable() {
			public void run() {
				if(!isVisible()) {
					h.postDelayed(this, matchesDelay);
					return;
				}
				final int[] progress = re.getProgress();
				if(progress[5] != 0) {
					Utils.toast(re.getError());
					tvProgress.setText("");
					re.stopThread();
					updateGoButton();
					return;
				}
				int matches = (progress[1] <= 10000)?progress[1]:10000;
				if(progress[4] != 0 || matches > 0 || tvProgress.length() > 0)
					tvProgress.setText(String.valueOf(progress[1]));
				if(progress[4] != 0)
					h.postDelayed(this, matchesDelay);
				adapter.update(matches);
				updateGoButton();
			}
		};
		Runnable rProgress = new Runnable() {
			public void run() {
				if(!isVisible()) {
					h.postDelayed(this, matchesDelay);
					return;
				}
				final int[] progress = re.getProgress();
				pbProgress.setProgress((int)((float)progress[3]/progress[2]*1000));
				if(progress[4] != 0)
					h.postDelayed(this, progressDelay);
				else
					pbProgress.setVisibility(View.GONE);
			}
		};
		h.postDelayed(rMain, matchesDelay);
		h.post(rProgress);
		pbProgress.setProgress(0);
		pbProgress.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onClear() {
    	if(re != null)
    		re.free();
    	adapter.clear();
    	for(int i = 0; i < 3; i++) {
    		((EditText)getView().findViewById(idET[i])).setText("");
    		((ToggleButton)getView().findViewById(idCB[i])).setChecked(true);
    	}
    	tvProgress.setText("");
    	updateGoButton();
    }
    
	final int[] idET = { R.id.etRDFiltr1, R.id.etRDFiltr2, R.id.etRDFiltr3 };
    final int[] idCB = { R.id.cbRDFiltr1, R.id.cbRDFiltr2, R.id.cbRDFiltr3 };
}