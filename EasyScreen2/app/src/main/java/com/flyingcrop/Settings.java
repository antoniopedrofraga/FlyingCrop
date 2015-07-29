package com.flyingcrop;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
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
        actionBar.setTitle("Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);

        toast = Toast.makeText(getBaseContext(),"Get premium to enjoy this setting", Toast.LENGTH_SHORT);


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
                    case 2:
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
                    case 3:

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
                    case 4:
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
                    case 6: //vibrate
                        CheckBox cb1 = (CheckBox)view.findViewById(R.id.checkBox1);
                        cb1.setChecked(!cb1.isChecked());
                        editor.putBoolean("vibration", cb1.isChecked());
                        editor.commit();
                        break;
                    case 7: //toast
                        CheckBox cb2 = (CheckBox)view.findViewById(R.id.checkBox1);
                        cb2.setChecked(!cb2.isChecked());
                        editor.putBoolean("toast", cb2.isChecked());
                        editor.commit();
                        break;
                    case 8: //dismiss
                        CheckBox cb3 = (CheckBox)view.findViewById(R.id.checkBox1);
                        cb3.setChecked(!cb3.isChecked());
                        editor.putBoolean("hide", cb3.isChecked());
                        editor.commit();
                        if(ServiceIsRunning()) {
                            Intent intent = new Intent(getBaseContext(), EasyShareService.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            stopService(intent);
                            startService(intent);
                        }

                        break;
                    case 10:

                        if(toast.getView().getWindowVisibility() != View.VISIBLE)
                            toast.show();
                        break;
                    case 11:
                        if(toast.getView().getWindowVisibility() != View.VISIBLE)
                            toast.show();
                        break;

                }
            }
        });
    }

    private ArrayList<Item> generateData(){
        ArrayList<Item> models = new ArrayList<Item>();
        models.add(new Item("Defaults"));
        models.add(new Item("Crop Quality","A higher resolution will result in a longer image processing",false));
        models.add(new Item("Cropable Area Color","Adapt to any applied theme, be aware of your selected area",false));
        SharedPreferences settings = getSharedPreferences("data", 0);
        models.add(new Item("Brush Color","Select the color that best suits your brush",false));
        models.add(new Item("Brush Size","Select the most appropriate size for your brush",false));
        models.add(new Item("Notifications"));

        models.add(new Item("Vibration","Enable a vibration when the image is fully processed",true, settings.getBoolean("vibration",false)));
        models.add(new Item("Toast","Enable toast display with the full storage path when the\nimage is fully processed",true, settings.getBoolean("toast",true)));
        models.add(new Item("Hide Actions","Hide created notification actions such as \"Brush\"\nand \"Dismiss\"",true, settings.getBoolean("hide",false)));

        models.add(new Item("Premium features"));
        models.add(new Item("Advertising","Disable advertising",true, false));
        models.get(models.size() - 1).setPremium();
        models.add(new Item("Watermark","Disable watermark, notice that by disabling this feature\nyou are not helping FlyingCrop expansion",true, settings.getBoolean("hide",false)));
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
            if ("com.flyingcrop.EasyShareService"
                    .equals(service.service.getClassName()))
            {
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

}