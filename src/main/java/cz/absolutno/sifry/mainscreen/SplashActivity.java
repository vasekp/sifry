package cz.absolutno.sifry.mainscreen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import cz.absolutno.sifry.App;

public final class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		App.updateLocale();
    	Intent intent = new Intent(this, MainActivity.class);
    	startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}

}
