package com.flyingcrop;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ViewAnimator;

import com.flyingcrop.common.activities.SampleActivityBase;
import com.flyingcrop.common.logger.Log;
import com.flyingcrop.common.logger.LogFragment;
import com.flyingcrop.common.logger.LogWrapper;
import com.flyingcrop.common.logger.MessageOnlyLogFilter;


public class EasyCrop extends SampleActivityBase {

    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.flyingcrop.R.layout.transparent);

        if (savedInstanceState == null) {

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    ScreenCaptureFragment fragment = new ScreenCaptureFragment();
                    transaction.replace(com.flyingcrop.R.id.sample_content_fragment, fragment);
                    transaction.commit();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.flyingcrop.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(com.flyingcrop.R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(com.flyingcrop.R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? com.flyingcrop.R.string.sample_hide_log : com.flyingcrop.R.string.sample_show_log);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case com.flyingcrop.R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                ViewAnimator output = (ViewAnimator) findViewById(com.flyingcrop.R.id.sample_output);
                if (mLogShown) {
                    output.setDisplayedChild(1);
                } else {
                    output.setDisplayedChild(0);
                }
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Create a chain of targets that will receive log data */
    @Override
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(com.flyingcrop.R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());
        Log.i(TAG, "Ready");
    }
}
