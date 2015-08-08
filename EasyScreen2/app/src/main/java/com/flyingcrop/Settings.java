package com.flyingcrop;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import yuku.ambilwarna.AmbilWarnaDialog;

public class Settings extends Activity  {

    ArrayList<String> strg_location =  new ArrayList<String>();
    Toast toast;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main_menu);
        //Customize Action Bar
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.main_menu_settings));
        actionBar.setDisplayHomeAsUpEnabled(true);

        toast = Toast.makeText(getBaseContext(),getResources().getString(R.string.settings_get_premium), Toast.LENGTH_SHORT);

        final SharedPreferences settings = getSharedPreferences("data", 0);
        if(!settings.getBoolean("advertising",false)){
            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }


        final MyAdapter adapter = new MyAdapter(this, generateData());
        final ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final SharedPreferences settings = getSharedPreferences("data", 0);
                final SharedPreferences.Editor editor = settings.edit();

                switch (position) {
                    case 1:
                        int index = settings.getInt("type", 0);
                        String type [] = {"Notification", "Button"};
                        new AlertDialog.Builder(Settings.this)
                                .setSingleChoiceItems(type, index, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int wichButton) {
                                        editor.putInt("type", wichButton);
                                    }
                                })
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        editor.commit();
                                        if(ServiceIsRunning()) {
                                            Intent stopIntent = new Intent(getBaseContext(), NotificationService.class);
                                            stopService(stopIntent);
                                            Intent bIntent = new Intent(getBaseContext(), ButtonService.class);
                                            stopService(bIntent);
                                            startService(stopIntent);
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                })
                                .setTitle("Type")
                                .show();
                        break;
                    case 2:
                        String list [] = {"Low", "Medium", "High", "Original"};


                        int scale = settings.getInt("scale", 1);

                        new AlertDialog.Builder(Settings.this)
                                .setSingleChoiceItems(list, scale, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int wichButton) {
                                        editor.putInt("scale", wichButton);
                                    }
                                })
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        editor.commit();
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                         dialog.dismiss();
                                    }
                                })
                                .setTitle("Image Resolution")
                                .show();
                       break;
                    case 3:
                        String list2 [] = {"5", "10", "15", "20", "25", "30", "35", "40"};



                        int size = getSizePosition();


                        new AlertDialog.Builder(Settings.this)
                                .setSingleChoiceItems(list2, size, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int wichButton) {
                                        editor.putInt("size", 5 * (wichButton + 1));
                                    }
                                })
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        editor.commit();
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        dialog.dismiss();
                                    }
                                })
                                .setTitle("Brush Size")
                                .show();
                        break;
                    case 4:
                        CheckBox cb6 = (CheckBox)view.findViewById(R.id.checkBox1);
                        cb6.setChecked(!cb6.isChecked());
                        editor.putBoolean("dismiss", cb6.isChecked());
                        editor.commit();
                        break;
                    case 6:
                        int color = settings.getInt("color", Color.parseColor("#B3CCCCCC"));
                        AmbilWarnaDialog dialog = new AmbilWarnaDialog(Settings.this, color, true, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                            @Override
                            public void onOk(AmbilWarnaDialog dialog, int color) {
                                // color is the color selected by the user.
                                editor.putInt("color", color);
                                editor.commit();

                            }

                            @Override
                            public void onCancel(AmbilWarnaDialog dialog) {

                            }
                        });
                            dialog.show();
                        break;
                    case 7:
                        AmbilWarnaDialog dialog3 = new AmbilWarnaDialog(Settings.this, settings.getInt("secondary_color", Color.parseColor("#000000")), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                            @Override
                            public void onOk(AmbilWarnaDialog dialog, int color) {
                                editor.putInt("secondary_color", color);
                                editor.commit();
                            }

                            @Override
                            public void onCancel(AmbilWarnaDialog dialog) {

                            }
                        });
                        dialog3.show();
                        break;
                    case 8:

                        AmbilWarnaDialog dialog2 = new AmbilWarnaDialog(Settings.this, settings.getInt("brush_color", Color.parseColor("#FF0000")), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                            @Override
                            public void onOk(AmbilWarnaDialog dialog, int color) {
                                editor.putInt("brush_color", color);
                                editor.commit();
                            }

                            @Override
                            public void onCancel(AmbilWarnaDialog dialog) {

                            }
                        });
                        dialog2.show();
                        break;
                    case 9:
                        AmbilWarnaDialog dialog4 = new AmbilWarnaDialog(Settings.this, settings.getInt("brush_secondary_color", Color.parseColor("#FFFFFF")), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                            @Override
                            public void onOk(AmbilWarnaDialog dialog, int color) {
                                editor.putInt("brush_secondary_color", color);
                                editor.commit();
                            }

                            @Override
                            public void onCancel(AmbilWarnaDialog dialog) {

                            }
                        });
                        dialog4.show();
                        break;

                    case 11: //vibrate
                        CheckBox cb1 = (CheckBox)view.findViewById(R.id.checkBox1);
                        cb1.setChecked(!cb1.isChecked());
                        editor.putBoolean("vibration", cb1.isChecked());
                        editor.commit();
                        break;
                    case 12: //toast
                        CheckBox cb2 = (CheckBox)view.findViewById(R.id.checkBox1);
                        cb2.setChecked(!cb2.isChecked());
                        editor.putBoolean("toast", cb2.isChecked());
                        editor.commit();
                        break;


                    case 14:
                        String size_list[] =  {"Small", "Medium", "Large", "Very Large"};
                        int choice = (settings.getInt("button_size", 50) - 30) / 20;
                        new AlertDialog.Builder(Settings.this)
                                .setSingleChoiceItems(size_list, choice, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int wichButton) {
                                        editor.putInt("button_size", 30 + 20 * wichButton);
                                    }
                                })
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        editor.commit();
                                        if(ServiceIsRunning()) {
                                            Intent stopIntent = new Intent(getBaseContext(), NotificationService.class);
                                            stopService(stopIntent);
                                            Intent bIntent = new Intent(getBaseContext(), ButtonService.class);
                                            stopService(bIntent);
                                            startService(stopIntent);
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                })
                                .setTitle("Overlay button size")
                                .show();
                        break;
                    case 16:

                        if(!settings.getBoolean("premium",false)) {
                            if (toast.getView().getWindowVisibility() != View.VISIBLE)
                                toast.show();

                        }else{
                            CheckBox cb4 = (CheckBox)view.findViewById(R.id.checkBox1);
                            cb4.setChecked(!cb4.isChecked());
                            editor.putBoolean("advertising", cb4.isChecked());
                            editor.commit();
                        }
                        break;
                    case 17:
                        if(!settings.getBoolean("premium",false)) {
                            if (toast.getView().getWindowVisibility() != View.VISIBLE)
                                toast.show();

                        }else{
                            CheckBox cb5 = (CheckBox)view.findViewById(R.id.checkBox1);
                            cb5.setChecked(!cb5.isChecked());
                            editor.putBoolean("watermark", cb5.isChecked());
                            editor.commit();
                        }
                        break;
                }
            }
        });
    }

    private ArrayList<Item> generateData(){
        ArrayList<Item> models = new ArrayList<Item>();
        SharedPreferences settings = getSharedPreferences("data", 0);
        models.add(new Item(getResources().getString(R.string.settings_defaults)));
        models.add(new Item(getResources().getString(R.string.settings_type),getResources().getString(R.string.settings_type_secondary),false));
        models.add(new Item(getResources().getString(R.string.settings_quality),getResources().getString(R.string.settings_quality_secondary),false));
        models.add(new Item(getResources().getString(R.string.settings_brush_size),getResources().getString(R.string.settings_brush_size_secondary),false));
        models.add(new Item(getResources().getString(R.string.settings_dismiss),getResources().getString(R.string.settings_dismiss_secondary),true, settings.getBoolean("dismiss",false)));
        models.add(new Item(getResources().getString(R.string.settings_crop_theme)));
        models.add(new Item(getResources().getString(R.string.settings_crop_color),getResources().getString(R.string.settings_crop_color_secondary),false));
        models.add(new Item(getResources().getString(R.string.settings_secondary_crop_color), getResources().getString(R.string.settings_secondary_crop_color_secondary),false));

        models.add(new Item(getResources().getString(R.string.settings_brush_color),getResources().getString(R.string.settings_brush_color_secondary),false));
        models.add(new Item(getResources().getString(R.string.settings_secondary_brush_color), getResources().getString(R.string.settings_secondary_brush_color_secondary),false));

        models.add(new Item(getResources().getString(R.string.settings_notifications)));

        models.add(new Item(getResources().getString(R.string.settings_vibrate),getResources().getString(R.string.settings_vibrate_secondary),true, settings.getBoolean("vibration",false)));
        models.add(new Item("Toast", getResources().getString(R.string.settings_toast),true, settings.getBoolean("toast",true)));



        models.add(new Item(getResources().getString(R.string.settings_button)));
        models.add(new Item(getResources().getString(R.string.settings_button_size),getResources().getString(R.string.settings_button_size_secondary),false));
        models.add(new Item("Premium"));
        models.add(new Item(getResources().getString(R.string.settings_advertising),getResources().getString(R.string.settings_advertising_secondary),true, settings.getBoolean("advertising",false)));
        models.get(models.size() - 1).setPremium();
        models.add(new Item(getResources().getString(R.string.settings_watermark),getResources().getString(R.string.settings_watermark_secondary),true, settings.getBoolean("watermark",false)));
        models.get(models.size() - 1).setPremium();


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

        this.finish();


        return super.onOptionsItemSelected(item);
    }

    public boolean ServiceIsRunning(){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if ("com.flyingcrop.NotificationService"
                    .equals(service.service.getClassName()))
            {
                return true;
            }else  if ("com.flyingcrop.ButtonService"
                    .equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public int getSizePosition(){
        SharedPreferences settings = getSharedPreferences("data", 0);
        int size = settings.getInt("size", 20);
        switch(size){
            case 5:
                return 0;
            case 10:
                return 1;

            case 15:
                return 2;

            case 20:
                return 3;

            case 25:
                return 4;

            case 30:
                return 5;

            case 35:
                return 6;

            case 40:
                return 7;

        }
        return 3;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                Intent intent = new Intent(getBaseContext(), MainMenu.class);
                startActivity(intent);
                finish();
                break;

        }

        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getBaseContext(), MainMenu.class);
        startActivity(intent);
        finish();
    }

}