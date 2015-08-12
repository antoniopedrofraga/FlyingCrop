package com.flyingcrop;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.flyingcrop.util.IabHelper;
import com.flyingcrop.util.IabResult;
import com.flyingcrop.util.Inventory;
import com.flyingcrop.util.Purchase;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


import java.util.ArrayList;


public class MainMenu extends Activity {

    boolean bootRun;
    IabHelper mHelper;
    final String TAG = "IAB";
    public static final String ITEM_PURCHASED = "premium5_.";
    boolean premium = false;
    Toast error;
    Toast premium_toast;
    boolean set_up = false;
    



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main_menu);

        error = Toast.makeText(getBaseContext(), getResources().getString(R.string.error_log_in_play_store), Toast.LENGTH_SHORT);
        premium_toast = Toast.makeText(getBaseContext(), getResources().getString(R.string.you_are_premium), Toast.LENGTH_SHORT);

        final SharedPreferences settings = getSharedPreferences("data", 0);
        if(!settings.getBoolean("advertising",false)){
            final AdView mAdView = (AdView) findViewById(R.id.adView);
            final AdRequest adRequest = new AdRequest.Builder().addTestDevice("1EAF2D1A43D561A3571617E0F935F6ED").build();
            mAdView.loadAd(adRequest);
        }

        if(settings.getBoolean("first_help",true)){
            Intent fIntent = new Intent(getApplicationContext(), FirstRun.class);
            startService(fIntent);
        }
        //Premium Upgrade


        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzDlK+tBuHZ+RGEld/zp5YWU+Ep88AKhAOTgKqSDv6VlkuRS6bqlJYLQBSUtd8NLxZvUDIokmN+dDVbiaWFRiqBlpADMrEakqhxgiVshnDLbOfU47LQmF5sLIY3qNHcFpezv13D+DX8SshSo9+YYl+FXPdHKXIOWFNjf+uNTYnRK7kpHQXq3dPF3zX8+A1X5a2U1ZgIYmsb/OcresG1bbU9ltqtjXpaE1Et8eYELJkAusLNDdr+nmhEd5ZJXTdJHehSO7Sq4CkI0uiZdjWCsNskwL7L4JGA+ogNNq/iIDZTTBkhckjaRBz9yiqIb3TsqAjfcIW740uVYmrr4QREpJwQIDAQAB";



        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new
                                   IabHelper.OnIabSetupFinishedListener() {
                                       public void onIabSetupFinished(IabResult result)
                                       {
                                           if (!result.isSuccess()) {
                                               Log.d(TAG, "In-app Billing setup failed: " +
                                                       result);
                                           } else {
                                               Log.d(TAG, "In-app Billing is set up OK");
                                               mHelper.queryInventoryAsync(mGotInventoryListener);
                                               set_up = true;
                                           }
                                       }
                                   });




        //Get bootRun boolean

        bootRun = settings.getBoolean("boot", false);

        final SharedPreferences.Editor editor = settings.edit();

        //Customize Action Bar
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(Html.fromHtml("Flying<b>Crop</b>"));

        //actionBar.setDisplayShowHomeEnabled(false);



        MyAdapter adapter = new MyAdapter(MainMenu.this, generateData());
        final ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent serviceIntent = new Intent(getApplicationContext(), NotificationService.class);
                Intent bIntent = new Intent(getApplicationContext(), ButtonService.class);
                switch(position){
                    case 3: //quit
                        stopService(serviceIntent);
                        stopService(bIntent);
                        finish();
                        break;
                    case 6://boot run
                        CheckBox cb = (CheckBox)view.findViewById(R.id.checkBox1);
                        cb.setChecked(!cb.isChecked());

                        if(!bootRun) {
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.main_menu_boot_allowed), Toast.LENGTH_SHORT).show();
                        }

                        bootRun = !bootRun;

                        editor.putBoolean("boot", bootRun);
                        editor.commit();

                        break;

                    case 8: //contact through e-mail
                        Intent send = new Intent(Intent.ACTION_SENDTO);
                        String uriText = "mailto:" + Uri.encode("antoniopedrofraga@gmail.com") +
                                "?subject=" + Uri.encode("FlyingCrop");
                        Uri uri = Uri.parse(uriText);

                        send.setData(uri);
                        startActivity(Intent.createChooser(send, "Send e-mail.."));
                        break;
                    case 1: //minimize

                        if( !ServiceIsRunning() ){ // ver se o processo esta a correr
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.main_menu_initiate_service), Toast.LENGTH_SHORT).show();
                            startService(serviceIntent);
                        }
                        finish();
                        break;
                    case 9: // version
                        final SharedPreferences settings = getSharedPreferences("data", 0);
                        String versionName = BuildConfig.VERSION_NAME;
                        new AlertDialog.Builder(MainMenu.this)
                                .setTitle(settings.getBoolean("premium", false) ? "FlyingCrop Premium" : "FlyingCrop")
                                .setMessage("\n" + getResources().getString(R.string.main_menu_version)+ " " + versionName + "\n\n" + getResources().getString(R.string.main_menu_version_text) +
                                        "\n\n" + getResources().getString(R.string.main_menu_version_developer) + "\n\nCopyright © 2015 FlyingCrop")
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .show();

                        break;

                    case 2: //settings
                        //nao te esquecas de fazer com icon e sem icon na notificacao
                        //tamanho da imagem, diretorio do save
                        Intent myIntent = new Intent(MainMenu.this, Settings.class);
                        MainMenu.this.startActivity(myIntent);
                        break;
                    case 5:
                        SharedPreferences settings2 = getSharedPreferences("data", 0);

                        if(!settings2.getBoolean("premium", false)) {

                            if (set_up) {
                                mHelper.launchPurchaseFlow(MainMenu.this, ITEM_PURCHASED, 10001,
                                        mPurchaseFinishedListener, "mypurchasetoken");
                            } else {
                                if (error.getView().getWindowVisibility() != View.VISIBLE)
                                    error.show();
                            }
                            break;

                        }else{
                            if (premium_toast.getView().getWindowVisibility() != View.VISIBLE)
                                premium_toast.show();
                        }
                }

            }
        });

    }

    private ArrayList<Item> generateData(){
        ArrayList<Item> models = new ArrayList<Item>();
        models.add(new Item(getResources().getString(R.string.main_menu)));
        if(ServiceIsRunning()) {
            models.add(new Item(R.drawable.minimize, getResources().getString(R.string.main_menu_run_changed), getResources().getString(R.string.main_menu_run_secondary)));
        }else{
            models.add(new Item(R.drawable.minimize, getResources().getString(R.string.main_menu_run), getResources().getString(R.string.main_menu_run_secondary)));
        }
        models.add(new Item(R.drawable.settings,getResources().getString(R.string.main_menu_settings),getResources().getString(R.string.main_menu_settings_secondary)));
        if(!ServiceIsRunning()) {
            models.add(new Item(R.drawable.quit, getResources().getString(R.string.main_menu_quit), getResources().getString(R.string.main_menu_quit_secondary)));
        }else{
            models.add(new Item(R.drawable.quit, getResources().getString(R.string.main_menu_quit_changed), getResources().getString(R.string.main_menu_quit_changed_secondary)));
        }
        models.add(new Item(getResources().getString(R.string.main_menu_enable)));

        Item premium = new Item(getResources().getString(R.string.main_menu_premium),getResources().getString(R.string.main_menu_premium_secondary),false);
        final SharedPreferences settings = getSharedPreferences("data", 0);

        if(settings.getBoolean("premium",false))
            premium.setPremium();

        models.add(premium);

        models.add(new Item(getResources().getString(R.string.main_menu_boot),getResources().getString(R.string.main_menu_boot_secondary),true,bootRun));

        models.add(new Item(getResources().getString(R.string.main_menu_support)));

        models.add(new Item(getResources().getString(R.string.main_menu_contact),getResources().getString(R.string.main_menu_contact_secondary), false));

        String versionName = BuildConfig.VERSION_NAME;



        models.add(new Item(getResources().getString(R.string.main_menu_version),settings.getBoolean("premium", false) ? versionName + " Premium" : versionName ,false));

        return models;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        MenuItem item = menu.findItem(R.id.action_settings);
        item.setVisible(false);
        this.invalidateOptionsMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public boolean ServiceIsRunning(){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if ("com.flyingcrop.NotificationService"
                    .equals(service.service.getClassName()))
            {
                return true;
            }

            if ("com.flyingcrop.ButtonService"
                    .equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);
            return rootView;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase)
        {
            if (result.isFailure()) {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.error_premium_upgrade), Toast.LENGTH_LONG).show();
                return;
            }
            else if (purchase.getSku().equals(ITEM_PURCHASED)) {
                consumeItem();
            }

        }
    };

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.error_premium_upgrade), Toast.LENGTH_LONG).show();

            } else {
                mHelper.consumeAsync(inventory.getPurchase(ITEM_PURCHASED),
                        mConsumeFinishedListener);
            }
        }
    };


    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        final SharedPreferences settings = getSharedPreferences("data", 0);
                        final SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean("advertising", true);
                        editor.putBoolean("premium", true);
                        editor.commit();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    } else {
                        Toast.makeText(getBaseContext(), getResources().getString(R.string.error_premium_upgrade), Toast.LENGTH_LONG).show();
                    }
                }
            };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {

            }
            else {
                // does the user have the premium upgrade?
                boolean mIsPremium = inventory.hasPurchase(ITEM_PURCHASED);
                final SharedPreferences settings = getSharedPreferences("data", 0);
                final SharedPreferences.Editor editor = settings.edit();
                if(mIsPremium && !settings.getBoolean("premium",false)){
                    editor.putBoolean("advertising", true);
                    editor.putBoolean("premium", true);
                    editor.commit();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }

            }
        }
    };

    @Override
    protected void onStop()
    {
        super.onStop();
        setContentView(R.layout.activity_transparent);
        finish();
    }

}
