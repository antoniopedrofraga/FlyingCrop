package com.flyingcrop;

import android.widget.CheckBox;

/**
 * Created by Pedro on 11/02/2015.
 */
public class Item {
    private int icon;
    private String title;
    private String description;
    private boolean isGroupHeader = false;
    private boolean isCheckBox = false;
    private boolean checked = false;
    private boolean premium = false;
    public Item(String title) {
        this(-1,title,null);
        isGroupHeader = true;
    }
    public Item(String title,String description, boolean checkbox) {
        this(-1,title,description);
        if(checkbox) {
            isCheckBox = true;
        }
    }



    public Item(String title,String description, boolean checkbox, boolean checked) {
        this(-1,title,description);
        if(checkbox) {
            isCheckBox = true;
            this.checked = checked;
        }
    }


    public Item(int icon, String title, String description) {
        super();
        this.title = title;
        this.description = description;
        this.icon = icon;
    }
    int getIcon(){
        return icon;
    }
    String getTitle(){
        return title;
    }
    String getDescription(){
        return description;
    }
    boolean isGroupHeader(){
        return isGroupHeader;
    }
    boolean isCheckBox(){
        return isCheckBox;
    }


    public void setDescription(String text) {
        this.description = text;
    }

    public boolean isChecked() {
        return checked;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(){
        premium = true;
    }
}
