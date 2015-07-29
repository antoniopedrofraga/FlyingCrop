package com.flyingcrop;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;


import com.android.vending.billing.IInAppBillingService;

import com.flyingcrop.R;
import com.flyingcrop.util.IabHelper;
import com.flyingcrop.util.IabResult;
import com.flyingcrop.util.Inventory;
import com.flyingcrop.util.Purchase;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainMenu extends Activity {

    boolean bootRun;
    IabHelper mHelper;
    final String TAG = "IAB";
    public static final String ITEM_PURCHASED = "android.test.purchased";
    IInAppBillingService mService;
    boolean premium = false;
    Toast error = Toast.makeText(getBaseContext(), "IABHelper could not set up", Toast.LENGTH_SHORT);
    



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main_menu);


        //Premium Upgrade


        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5t/5e9gWhHZlV3OMGVaCZVRHk72o/anGhG2CP4ZzQyG+tLt1EwilUXLCN0joERFAJRMu5vNq2w6vSseUxtUkrJiRlT4ZPmvol0yZ2FjrCEHQUP3oRwH2p1QLlIJrHfFvWPUsG3g3gibCQFYClD/kX1Z8dz8GSIjjY7lmZOY7gjWHKXQXC3uiDCXMgl0fQ5xlg7kbze8OGNOdk/FglDAOh5GRpl5KnopKEAPye8WVeSaBlR8bd47uMDKA84QRrVF8jEEwH4gAoW4ODzEJva5itm3UKE3svAbephHKbMAYZWiJ4ece7AjXVeu1f5eOyGml16MaYPH8KeGjF3vLj05ywQIDAQAB";

        final String base64EncodedPublicKey_test = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCg" +
                "KCAQEAhNe2XQ70DceAwE6uyYJGK1dIBbZcPdlER/9EEzylr6RDU6tnGj0Tk7kceN03GKvRf/ucT+ERLL3O" +
                "aHR22PXRXLZ17NZ81x6oS2vGmLyXBnjrU/I+asl8cNuLGySaoCdXxPAV+A9g6OG13dk+KY9i0O1roGpFH" +
                "fsAFyKCgSqR0PMJZ1bS+wFFBYf3M4IxgBcxuuZKDmR+MztCgm5N4zc6w2CwFZn3mXeDoTg15mWDU3sZO" +
                "WeRwFeynhV+FCYdDp8DpAkLk1b5IiXYFQ53wxCh/GxiKqBB6uQMmAixFjAcZV1QWfcBABae9vxiV5" +
                "VAEJvOOnhPxnaT9HYadW0pQ/UbJwIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey_test);

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
                                           }
                                       }
                                   });




        //Get bootRun boolean
        SharedPreferences settings = getSharedPreferences("data", 0);
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
                Intent serviceIntent = new Intent(getApplicationContext(), EasyShareService.class);
                Intent CropService = new Intent(getApplicationContext(), com.flyingcrop.CropService.class);
                switch(position){
                    case 3: //quit
                        stopService(serviceIntent);
                        stopService(CropService);
                        finish();
                        break;
                    case 6://boot run
                        CheckBox cb = (CheckBox)view.findViewById(R.id.checkBox1);
                        cb.setChecked(!cb.isChecked());

                        if(!bootRun) {
                            Toast.makeText(getBaseContext(), "This app is now allowed to run on boot, a FlyingCrop notification will be created", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getBaseContext(), "FlyingCrop notification was created", Toast.LENGTH_SHORT).show();
                            startService(serviceIntent);
                        }
                        finish();
                        break;
                    case 9: // version
                        String versionName = BuildConfig.VERSION_NAME;
                        new AlertDialog.Builder(MainMenu.this)
                                .setTitle("FlyingCrop")
                                .setMessage("\nVersion " + versionName + "\n\n" + "FlyingCrop provides an easy share of taken screen shots, designed and programed to be as intuitive as possible. " +
                                        "\n\nDeveloped by Pedro Fraga\n\nCopyright © 2015 FlyingCrop")
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
                        if(mHelper != null) {
                            mHelper.launchPurchaseFlow(MainMenu.this, ITEM_PURCHASED, 10001,
                                    mPurchaseFinishedListener, "mypurchasetoken");
                        }else{
                            if(error.getView().getWindowVisibility() != View.VISIBLE)
                                error.show();
                        }

                        break;
                }

            }
        });

    }

    private ArrayList<Item> generateData(){
        ArrayList<Item> models = new ArrayList<Item>();
        models.add(new Item("MENU"));
        if(ServiceIsRunning()) {
            models.add(new Item(R.drawable.minimize, "Minimize", "Click to run service on background"));
        }else{
            models.add(new Item(R.drawable.minimize, "Run FlyingCrop", "Click to run service on background"));
        }
        models.add(new Item(R.drawable.settings,"Settings","Customize it at your own way"));
        if(!ServiceIsRunning()) {
            models.add(new Item(R.drawable.quit, "Quit", "Quit FlyingCrop"));
        }else{
            models.add(new Item(R.drawable.quit, "Stop FlyingCrop", "Disable notification and Quit"));
        }
        models.add(new Item("ENABLING"));

        Item premium = new Item("Premium","Get rid of the advertising and disable watermark",false);
        if(this.premium)
            premium.setPremium();

        models.add(premium);

        models.add(new Item("Boot Run","Allow running on boot",true,bootRun));

        models.add(new Item("SUPPORT"));
        models.add(new Item("Contact","Feel free to give some feedback or suggestions", false));

        String versionName = BuildConfig.VERSION_NAME;

        models.add(new Item("Version",versionName,false));
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
            if ("com.flyingcrop.EasyShareService"
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
                // Handle error
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
                // Handle failure
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

                    } else {
                        // handle error
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
                // handle error here
            }
            else {
                // does the user have the premium upgrade?
                boolean mIsPremium = inventory.hasPurchase(ITEM_PURCHASED);
                Toast.makeText(getBaseContext(), mIsPremium ? "Nao e premium" : "E premium", Toast.LENGTH_SHORT).show();
                // update UI accordingly
            }
        }
    };



}
