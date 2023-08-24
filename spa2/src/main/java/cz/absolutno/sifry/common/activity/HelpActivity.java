package cz.absolutno.sifry.common.activity;

import static android.content.pm.PackageManager.GET_META_DATA;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class HelpActivity extends Activity {

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(App.localizedContext(newBase));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.updateLocale();
        setContentView(R.layout.help_layout);

        Intent intent = getIntent();
        int resID = intent.getIntExtra(App.SPEC, 0);

        try {
            setTitle(getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA).labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            // leave title as it is
        }

        ((TextView) findViewById(R.id.tvREHelp)).setText(Utils.fromHtml(getString(resID)));
    }

}