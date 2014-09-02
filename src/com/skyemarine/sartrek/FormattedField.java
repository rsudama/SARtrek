package com.skyemarine.sartrek;

import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;

class FormattedField extends RichTextField {
    private static final long DEFAULT_STYLE = 
    	Field.READONLY | Field.NON_FOCUSABLE | Field.USE_ALL_WIDTH | Field.FIELD_VCENTER;
    
    private int _width = 0;
    double _widthPercent = 1;
    private int _height;
  
    public FormattedField() {
    	this("", 0, 0);
    }
    
    public FormattedField(String text) {
        this(text, 0, 0);
    }
    
    // Width of the field as a fixed value
    public FormattedField(String text, int width) {
        this(text, width, 0);
    }

    // Width of the field as a percentage of the display width
    public FormattedField(String text, double widthPercent) {
        this(text, widthPercent, 0);
    }

    public FormattedField(int width) {
    	this("", width, 0);
    }
       
    // Width of the field as a percentage of the display width
    public FormattedField(double widthPercent) {
    	this("", widthPercent, 0);
    }

    public FormattedField(long style) {
        this("", style);
    }
    
    public FormattedField(long style, int height) {
        this("", style, height);
    }
    
    public FormattedField(int width, long style) {
        this("", width, style);
    }
    
    public FormattedField(String text, long style) {
        this(text, 0, style);
    }

    public FormattedField(String text, int width, long style) {
        super(style);
        super.setFont(Font.getDefault());
        setText(text);
        _width = width;
    }

    public FormattedField(String text, long style, int height) {
        super(style);
        super.setFont(Font.getDefault());
        setText(text);
        _height = height;
    }

    public FormattedField(String text, int width, int height) {
        super(DEFAULT_STYLE);
        super.setFont(Font.getDefault());
        setText(text);
        _width = width;
        _height = height;
    }

    public FormattedField(String text, double widthPercent, int height) {
        super(DEFAULT_STYLE);
        super.setFont(Font.getDefault());
        setText(text);
        _widthPercent = widthPercent;
        _height = height;
    }

    public int getPreferredWidth(){
        if (_width == 0)
            return (int)(Display.getWidth() * _widthPercent);
            //return this.getFont().getAdvance(this.getLabel() + this.getText());
            //return this.getFont().getAdvance(this.getText());
        else return _width;
    }
    
    public int getPreferredHeight(){
        if (_height == 0) 
            return this.getFont().getHeight();
        else return _height;
    }
    
    protected void layout(int width, int height) {
        width = getPreferredWidth();
        height = getPreferredHeight();
 //       if (_truncate)
 //       super.setExtent(width, height);
        super.layout(width, height);
    	super.setBackground(this.getBackground());
    }
    
    public void setText(String text) {
        RichTextFormatter.TextFormat format = RichTextFormatter.format(text);
        super.setText(format.text, format.offsets, format.attributes, format.fonts);
    }        
}
