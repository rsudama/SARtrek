package com.skyemarine.sartrek;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.container.*;

/**
 * This horizontal field manager allows a size to be specified for each field, so you can control
 * the width of each control in the row. It also allows a row height to be specified.
 */
class CustomHorizontalFieldManager extends HorizontalFieldManager {
    public CustomHorizontalFieldManager(long style) {
        super(style);
    }
  
    public int getPreferredHeight() {
        int height = 0;
        for (int i = 0; i < getFieldCount(); ++i) 
            if (getField(i).getPreferredHeight() > height)
                 height = getField(i).getPreferredHeight();
        return height;
    }

    protected void sublayout(int width, int height) {
        int x = 0;
        int maxHeight = getPreferredHeight();
        for (int i = 0; i < getFieldCount(); ++i) {
            Field field = getField(i);
            layoutChild(field, width, maxHeight);
            setPositionChild(field, x, (maxHeight - field.getPreferredHeight()) / 2);
            x += field.getPreferredWidth();
        }
        setExtent(width, maxHeight);
    }
} 
