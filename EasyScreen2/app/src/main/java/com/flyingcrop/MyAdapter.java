package com.flyingcrop;

/**
 * Created by Pedro on 11/02/2015.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MyAdapter extends ArrayAdapter<Item> {

    private final Context context;
    private final ArrayList<Item> modelsArrayList;

    public MyAdapter(Context context, ArrayList<Item> modelsArrayList) {

        super(context, com.flyingcrop.R.layout.row, modelsArrayList);
        this.context = context;
        this.modelsArrayList = modelsArrayList;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater

        View rowView = null;
        if(!modelsArrayList.get(position).isGroupHeader() && !modelsArrayList.get(position).isCheckBox()){


                rowView = inflater.inflate(com.flyingcrop.R.layout.row, parent, false);


            // 3. Get icon,title & counter views from the rowView
            ImageView imgView = (ImageView) rowView.findViewById(com.flyingcrop.R.id.item_icon);
            TextView titleView = (TextView) rowView.findViewById(com.flyingcrop.R.id.string);
            TextView counterView = (TextView) rowView.findViewById(com.flyingcrop.R.id.substring);

            // 4. Set the text for textView
            imgView.setImageResource(modelsArrayList.get(position).getIcon());
            titleView.setText(modelsArrayList.get(position).getTitle());
            counterView.setText(modelsArrayList.get(position).getDescription());

            if(modelsArrayList.get(position).isPremium()) {
                final SharedPreferences settings = getContext().getSharedPreferences("data", 0);
                if(settings.getBoolean("premium",false)) {
                    counterView.setTextColor(R.color.DarkSeaGreen);
                    titleView.setTextColor(R.color.DarkSeaGreen);
                    rowView.setClickable(false);
                    rowView.setEnabled(false);
                }
            }
        }
        else if (modelsArrayList.get(position).isGroupHeader()){
            rowView = inflater.inflate(com.flyingcrop.R.layout.group_header_item, parent, false);
            TextView titleView = (TextView) rowView.findViewById(com.flyingcrop.R.id.header);
            titleView.setText(modelsArrayList.get(position).getTitle());
            rowView.setEnabled(false);
            rowView.setOnClickListener(null);
            rowView.setMinimumHeight(20);
        }else if(modelsArrayList.get(position).isCheckBox()){

                rowView = inflater.inflate(com.flyingcrop.R.layout.checkbox_item, parent, false);

            TextView titleView = (TextView) rowView.findViewById(com.flyingcrop.R.id.string);
            TextView counterView = (TextView) rowView.findViewById(com.flyingcrop.R.id.substring);
            CheckBox cb = (CheckBox)rowView.findViewById(com.flyingcrop.R.id.checkBox1);

            cb.setChecked(modelsArrayList.get(position).isChecked());
            titleView.setText(modelsArrayList.get(position).getTitle());
            counterView.setText(modelsArrayList.get(position).getDescription());



            if(modelsArrayList.get(position).isPremium()){
                final SharedPreferences settings = getContext().getSharedPreferences("data", 0);
                if(!settings.getBoolean("premium",false)) {
                    counterView.setTextColor(R.color.DarkSeaGreen);
                    titleView.setTextColor(R.color.DarkSeaGreen);

                    cb.setEnabled(false);
                    cb.setClickable(false);
                }
            }
        }


        // 5. retrn rowView
        return rowView;
    }
}
