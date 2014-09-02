package com.skyemarine.sartrek;

import java.util.Date;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.ui.decor.*;

import com.skyemarine.sartrek.lib.*;

class LocationField extends VerticalFieldManager {
    HorizontalFieldManager _dateManager = new HorizontalFieldManager(Field.USE_ALL_WIDTH);
    //CustomHorizontalFieldManager _gridManager = new CustomHorizontalFieldManager(Field.USE_ALL_WIDTH);
    HorizontalFieldManager _primaryDisplayManager = new HorizontalFieldManager(Field.USE_ALL_WIDTH);
    HorizontalFieldManager _utmManager = new HorizontalFieldManager(Field.USE_ALL_WIDTH);
    HorizontalFieldManager _coordinateManager = new HorizontalFieldManager(Field.USE_ALL_WIDTH);
    HorizontalFieldManager _headingSpeedManager = new HorizontalFieldManager(Field.USE_ALL_WIDTH);
    HorizontalFieldManager _altitudeAccuracyManager = new HorizontalFieldManager(Field.USE_ALL_WIDTH);
    HorizontalFieldManager _distanceManager = new HorizontalFieldManager(Field.USE_ALL_WIDTH);
    //HorizontalFieldManager _distanceManager2 = new HorizontalFieldManager(Field.USE_ALL_WIDTH);

    LocationField() {
        setBackground(BackgroundFactory.createLinearGradientBackground(Color.AZURE, Color.AZURE, Color.CADETBLUE, Color.CADETBLUE));
        setBackground(BackgroundFactory.createSolidTransparentBackground(0, 0));
        
        _dateManager.add(new FormattedField(RichTextField.TEXT_ALIGN_HCENTER |
        	Field.USE_ALL_WIDTH | RichTextField.FIELD_VCENTER | Field.READONLY | Field.NON_FOCUSABLE, 30));
        _dateManager.setBackground(BackgroundFactory.createSolidBackground(Color.PALETURQUOISE));
        
        //_primaryDisplayManager.add(new FormattedField(
       	//	"<i>" + Options.getStringOption(Options.PRIMARY_DISPLAY) + ":</i>", (int)(Display.getWidth() * .40), 50));
        _primaryDisplayManager.add(new RichTextField(RichTextField.TEXT_ALIGN_HCENTER |
        	Field.USE_ALL_WIDTH | RichTextField.FIELD_VCENTER | Field.READONLY | Field.NON_FOCUSABLE));
        _primaryDisplayManager.setPadding(10, 0, 10, 0);
        _primaryDisplayManager.setBackground(BackgroundFactory.createSolidBackground(Color.ALICEBLUE));

        _utmManager.add(new FormattedField("<i>UTM:</i>", 100, 35));
        _utmManager.add(new FormattedField());
        
        _coordinateManager.add(new FormattedField("<i>Lat/Lon:</i>", 100, 35));
        _coordinateManager.add(new FormattedField());
       
        _headingSpeedManager.add(new FormattedField("<i>Heading:</i>", 100, 35));
        _headingSpeedManager.add(new FormattedField(.20));
        _headingSpeedManager.add(new FormattedField("<i>Speed:</i>", 100, 35));
        _headingSpeedManager.add(new FormattedField());
        
        _altitudeAccuracyManager.add(new FormattedField("<i>Altitude:</i>", 100, 35));
        _altitudeAccuracyManager.add(new FormattedField(.20));
        _altitudeAccuracyManager.add(new FormattedField("<i>Accur:</i>", 100, 35));
        _altitudeAccuracyManager.add(new FormattedField());
        
        _distanceManager.add(new FormattedField("<i>Distance:</i>", 100, 35));
        _distanceManager.add(new FormattedField(.20));
        _distanceManager.add(new FormattedField("<i>Bearing:</i>", 100, 35));
        _distanceManager.add(new FormattedField());
        
        /*
        _distanceManager2.add(new FormattedField("<i>Travel2:</i>", 100, 35));
        _distanceManager2.add(new FormattedField(.20));
		*/
        
        add(new SeparatorField());
        add(_dateManager);
        add(new SeparatorField());
        add(new SeparatorField());
        add(_primaryDisplayManager);
        add(new SeparatorField());
        add(new SeparatorField());
        add(_utmManager);
        add(new SeparatorField());
        add(_coordinateManager);
        add(new SeparatorField());
        add(_headingSpeedManager);
        add(new SeparatorField());
        add(_altitudeAccuracyManager);
        add(new SeparatorField());
        add(_distanceManager);
        add(new SeparatorField());
        //add(_distanceManager2);
        //add(new SeparatorField());
        
        this.updateLayout();
    }

    public void updateLocation(final PersistableLocation pLocation) {
        //((FormattedField)_dateManager.getField(0)).setText("<b>" + pLocation.date + "</b>");
        ((FormattedField)_dateManager.getField(0)).setText(pLocation.date);
    	_primaryDisplayManager.getField(0).setFont(Font.getDefault().derive
    		(Font.BOLD, Options.getIntOption(Options.PRIMARY_FONT_SIZE), Ui.UNITS_pt));
        if (Options.getStringOption(Options.PRIMARY_DISPLAY).equals("GRID"))
        	((RichTextField)_primaryDisplayManager.getField(0)).setText(pLocation.grid);
        if (Options.getStringOption(Options.PRIMARY_DISPLAY).equals("UTM"))
        	((RichTextField)_primaryDisplayManager.getField(0)).setText(pLocation.utm);
        else if (Options.getStringOption(Options.PRIMARY_DISPLAY).equals("MGRS"))
        	((RichTextField)_primaryDisplayManager.getField(0)).setText(pLocation.mgrs);
        ((FormattedField)_utmManager.getField(1)).setText(pLocation.utm);
        ((FormattedField)_coordinateManager.getField(1)).setText(pLocation.coord);
        ((FormattedField)_headingSpeedManager.getField(1)).setText(pLocation.heading);
        ((FormattedField)_headingSpeedManager.getField(3)).setText(pLocation.speed);
        ((FormattedField)_altitudeAccuracyManager.getField(1)).setText(pLocation.altitude);
        ((FormattedField)_altitudeAccuracyManager.getField(3)).setText(pLocation.accuracy);
        ((FormattedField)_distanceManager.getField(1)).setText(pLocation.distanceFrom);
        ((FormattedField)_distanceManager.getField(3)).setText(pLocation.bearingTo);
        //((FormattedField)_distanceManager2.getField(1)).setText(pLocation.distLandByDist);
        
        this.updateLayout();
    }

    public void updateDate(final Date datetime) {
        String date = PersistableLocation.LongDateFormat.format(datetime);
        ((FormattedField)_dateManager.getField(0)).setText("<b>" + date + "</b>");
    }
} 
