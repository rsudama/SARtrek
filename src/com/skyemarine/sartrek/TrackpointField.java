package com.skyemarine.sartrek;

import java.util.*;

//import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.ui.decor.*;
//import net.rim.device.api.i18n.*;

import com.skyemarine.sartrek.lib.*;

class TrackpointField extends VerticalFieldManager implements FocusChangeListener {
    //private static final DateFormat _shortDate = new SimpleDateFormat("HHmm || EEE dd MMM");

	//private static final FormattedField _field1 = new FormattedField("<i>99 </i>");
    //private static final int _width1 = _field1.getFont().getAdvance(_field1.getText());
    private static final int _width1 = 40;
    //private static final FormattedField _field2 = new FormattedField("<b>999 999 </b>");
    //private static final int _width2 = _field2.getFont().getAdvance(_field2.getText());
    private static int _width2;
    
    TrackpointField() {        
    	super(Manager.FOCUSABLE | Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
    	//setBackground(BackgroundFactory.createLinearGradientBackground(Color.AZURE, Color.AZURE, Color.CADETBLUE, Color.CADETBLUE));
        setBackground(BackgroundFactory.createSolidTransparentBackground(0, 0));        
        _width2 = 200;
        refresh();
    }

    public void refresh() {        
    	this.deleteAll();
    	Vector trackpoints = PersistableLocation.getTrackpoints();
        int i = 0;
        for (; i < trackpoints.size(); i++)
            addTrackpoint(Integer.toString(i+1), (PersistableLocation)trackpoints.elementAt(i));
    }
    
    public void addTrackpoint(String label, PersistableLocation pLocation) {
        String grid = "<b>--- ---</b>";
        String date = MobileNavigator.ShortDateFormat.format(new Date());
        if (pLocation.isValid()) {
            grid = pLocation.grid;
            date = MobileNavigator.ShortDateFormat.format(new Date(pLocation.timestamp.longValue()));
        }
        
        HorizontalFieldManager trackpointManager = new HorizontalFieldManager(Field.USE_ALL_WIDTH);
        FormattedField labelField = new FormattedField("<i>" + label + "</i>", _width1,
        		Field.FOCUSABLE | Field.HIGHLIGHT_SELECT);
        labelField.setFocusListener(this);
        trackpointManager.add(labelField);
        trackpointManager.add(new FormattedField(date, _width2));
        trackpointManager.add(new FormattedField("<b>" + grid + "</b>"));
        add(trackpointManager);
        
        HorizontalFieldManager distanceManager = new HorizontalFieldManager(Field.USE_ALL_WIDTH);
        distanceManager.add(new FormattedField(_width1));
        distanceManager.add(new FormattedField(
        	"<i>Distance:</i> " + pLocation.distanceFrom + "  <i>Bearing:</i> " + pLocation.bearingTo));
        add(distanceManager);
    }
    
	public void focusChanged(Field field, int eventType) {
		FormattedField fField = (FormattedField)field;
		if (eventType == FOCUS_LOST)
			fField.setSelection(0, true, 0);
		else if (eventType == FOCUS_GAINED)
			fField.setSelection(0, true, fField.getTextLength());
	}

	public PersistableLocation getSelectedTrackpoint() {
        int selectedIndex = this.getFieldWithFocusIndex() / 2;
        Vector trackpoints = PersistableLocation.getTrackpoints();
    	if (trackpoints.size() > selectedIndex)
    		return (PersistableLocation)trackpoints.elementAt(selectedIndex);
    	else
    		return null;
    }
} 
