package com.skyemarine.sartrek;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.ui.decor.*;

public class CustomMainScreen extends MainScreen {
    public static final int BackgroundColor = Color.LIGHTSKYBLUE;
    public static final int ContentColor = Color.POWDERBLUE;

    private FormattedField _titleField = new FormattedField
		(Field.USE_ALL_WIDTH | Field.READONLY | Field.NON_FOCUSABLE);    
    private VerticalFieldManager _container;
    //private FormattedField _statusField = new FormattedField(false);
    private FormattedField _statusField = new FormattedField
    	(Field.USE_ALL_WIDTH | Field.READONLY | Field.NON_FOCUSABLE);    
    private Menu _menu = null;

    public CustomMainScreen(String title, Menu menu) {
        this(title);
        _menu = menu;
    }
    
    public CustomMainScreen(String title) {
        super(Manager.FOCUSABLE | Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR | Manager.USE_ALL_WIDTH);
        
        _titleField.setText(title);
        setTitle(_titleField);

        getMainManager().setBackground(BackgroundFactory.
            createLinearGradientBackground(Color.AZURE, Color.AZURE, Color.CADETBLUE, Color.CADETBLUE));        
        
        _statusField.setBackground(BackgroundFactory.createSolidBackground(Color.LIGHTSTEELBLUE));
        setStatus(_statusField);

        _container = new VerticalFieldManager
        	(Manager.FOCUSABLE | Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR | Manager.USE_ALL_WIDTH);
        super.add(_container);
    }
   
    public void add(Field field) {
        _container.add(field);
    }
    
    public void deleteAll() {
        _container.deleteAll();
    }
    
    public void delete(Field field) {
        _container.delete(field);
    }
    
    /*
    private Field getMyTitleField() {
        Manager delegate = getDelegate();
        Field titleField = null;
        try { titleField = delegate.getField(0);  }
        catch (IndexOutOfBoundsException ignored) { }
        return titleField;
    }
    */
    
    public void setStatusText(String msg) {
        RichTextFormatter.TextFormat format = RichTextFormatter.format(msg);
        _statusField.setText(format.text, format.offsets, format.attributes, format.fonts);
    	//_statusField.setText(msg);   
    }
    
    public String getStatusText() {
    	return _statusField.getText();
    }
    
    protected void makeMenu(Menu menu, int instance) {
        if (_menu != null) {
            menu.deleteAll();
            for (int i = 0; i < _menu.getSize(); i++)
                menu.add(_menu.getItem(i));
        }        
        super.makeMenu(menu, instance);
    }
}
