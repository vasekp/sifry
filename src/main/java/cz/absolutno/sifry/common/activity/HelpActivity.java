package cz.absolutno.sifry.common.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import cz.absolutno.sifry.App;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;

public final class HelpActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.updateLocale();
        setContentView(R.layout.help_layout);

        Intent intent = getIntent();
        int resID = intent.getIntExtra(App.SPEC, 0);

        ((TextView) findViewById(R.id.tvREHelp)).setText(Utils.fromHtml(getString(resID)));
    }

}