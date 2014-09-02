package com.skyemarine.sartrek;

import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.FontManager;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import com.skyemarine.sartrek.lib.*;

class OptionsScreen extends CustomMainScreen {
    private ObjectChoiceField _trackingInterval;
    private ObjectChoiceField _roundGrid;
    private ObjectChoiceField _mapDatum;
    private ObjectChoiceField _primaryDisplay;
    private ObjectChoiceField _units;
    private ObjectChoiceField _mapView;
    //private NumericChoiceField _zoomLevel;
    private ObjectChoiceField _positionLabel;
    private ObjectChoiceField _fontSize;
    private ObjectChoiceField _primaryFontSize;

    private static final String[] _booleanChoices = { "True", "False" };

    OptionsScreen() {
        super(MobileNavigator.Title);

        setStatusText("Options");
        
        VerticalFieldManager fields = new VerticalFieldManager();

        Options.loadOptions();
        
        String[] mapDatumChoices = { "NAD 27", "NAD 83", "WGS 84" };
        _mapDatum = new ObjectChoiceField("Map datum:", mapDatumChoices);
        String mapDatum = Options.getStringOption(Options.MAP_DATUM, Options.DEFAULT_MAP_DATUM);
        for (int i = 0; i < mapDatumChoices.length; i++)
            if (mapDatum.equalsIgnoreCase(mapDatumChoices[i])) _mapDatum.setSelectedIndex(i);
        fields.add(_mapDatum);

        String[] primaryDisplayChoices = { "UTM", "MGRS", "GRID" };
        _primaryDisplay = new ObjectChoiceField("Primary display:", primaryDisplayChoices);
        String primaryDisplay = Options.getStringOption(Options.PRIMARY_DISPLAY, Options.DEFAULT_PRIMARY_DISPLAY);
        for (int i = 0; i < primaryDisplayChoices.length; i++)
            if (primaryDisplay.equalsIgnoreCase(primaryDisplayChoices[i])) _primaryDisplay.setSelectedIndex(i);
        fields.add(_primaryDisplay);

        _roundGrid = new ObjectChoiceField("Round grid:", _booleanChoices);
        boolean roundGrid = Options.getBooleanOption(Options.ROUND_GRID_COORDINATES, Options.DEFAULT_ROUND_GRID_COORDINATES);
        if (roundGrid) _roundGrid.setSelectedIndex(0);
        else _roundGrid.setSelectedIndex(1);
        fields.add(_roundGrid);

        String[] trackingIntervalChoices = { "None", "5 minutes", "10 minutes", "30 minutes", "1 hour" };
        _trackingInterval = new ObjectChoiceField("Trackpoint interval:", trackingIntervalChoices);
        int trackingInterval = Options.getIntOption(Options.TRACKING_INTERVAL, Options.DEFAULT_TRACKING_INTERVAL);
        switch (trackingInterval) {
            case 0: _trackingInterval.setSelectedIndex(0); break;
            case 5: _trackingInterval.setSelectedIndex(1); break;
            case 10: _trackingInterval.setSelectedIndex(2); break;
            case 30: _trackingInterval.setSelectedIndex(3); break;
            case 60: _trackingInterval.setSelectedIndex(4); break;
        }
        fields.add(_trackingInterval);

        String[] unitsChoices = { "Metric", "English" };
        _units = new ObjectChoiceField("Units:", unitsChoices);
        String units = Options.getStringOption(Options.UNITS, Options.DEFAULT_UNITS);
        for (int i = 0; i < unitsChoices.length; i++)
            if (units.equalsIgnoreCase(unitsChoices[i])) _units.setSelectedIndex(i);
        fields.add(_units);

        String[] mapViewChoices = { "Satellite", "Map" };
        _mapView = new ObjectChoiceField("Default map view:", mapViewChoices);
        String mapView = Options.getStringOption(Options.MAP_VIEW, Options.DEFAULT_MAP_VIEW);
        for (int i = 0; i < mapViewChoices.length; i++)
            if (mapView.equalsIgnoreCase(mapViewChoices[i])) _mapView.setSelectedIndex(i);
        fields.add(_mapView);

        //_zoomLevel = new NumericChoiceField("Default zoom level:", 0, 21, 1);
        //int zoomLevel = Options.getIntOption(Options.ZOOM_LEVEL, Options.DEFAULT_ZOOM_LEVEL);
        //_zoomLevel.setSelectedValue(zoomLevel);
        //fields.add(_zoomLevel);

        _positionLabel = new ObjectChoiceField("Position label:", _booleanChoices);
        boolean positionLabel = Options.getBooleanOption(Options.POSITION_LABEL, Options.DEFAULT_POSITION_LABEL);
        if (positionLabel) _positionLabel.setSelectedIndex(0);
        else _positionLabel.setSelectedIndex(1);
        fields.add(_positionLabel);

        String[] fontSizeChoices = { "8", "9", "10", "11", "12" };
        _fontSize = new ObjectChoiceField("Font size:", fontSizeChoices);
        int fontSize = Options.getIntOption(Options.FONT_SIZE, Options.DEFAULT_FONT_SIZE);
        switch (fontSize) {
	        case 8: _fontSize.setSelectedIndex(0); break;
	        case 9: _fontSize.setSelectedIndex(1); break;
	        case 10: _fontSize.setSelectedIndex(2); break;
	        case 11: _fontSize.setSelectedIndex(3); break;
	        case 12: _fontSize.setSelectedIndex(4); break;
        }
        fields.add(_fontSize);

        String[] primaryFontSizeChoices = { "10", "11", "12", "14", "16", "18" };
        _primaryFontSize = new ObjectChoiceField("Primary font size:", primaryFontSizeChoices);
        int primaryFontSize = Options.getIntOption(Options.PRIMARY_FONT_SIZE, Options.DEFAULT_PRIMARY_FONT_SIZE);
        switch (primaryFontSize) {
	        case 10: _primaryFontSize.setSelectedIndex(0); break;
	        case 11: _primaryFontSize.setSelectedIndex(1); break;
	        case 12: _primaryFontSize.setSelectedIndex(2); break;
	        case 14: _primaryFontSize.setSelectedIndex(3); break;
	        case 16: _primaryFontSize.setSelectedIndex(4); break;
	        case 18: _primaryFontSize.setSelectedIndex(5); break;
        }
        fields.add(_primaryFontSize);

        add(fields);
    }
    
    protected boolean onSave() {
        String mapDatum = (String)_mapDatum.getChoice(_mapDatum.getSelectedIndex());
        Options.setStringOption(Options.MAP_DATUM, mapDatum);
        
        String primaryDisplay = (String)_primaryDisplay.getChoice(_primaryDisplay.getSelectedIndex());
        Options.setStringOption(Options.PRIMARY_DISPLAY, primaryDisplay);
        
        boolean roundGrid = false;
        if (_roundGrid.getSelectedIndex() == 0)
            roundGrid = true;
        Options.setBooleanOption(Options.ROUND_GRID_COORDINATES, roundGrid);
        
        int trackingInterval = 0;
        switch (_trackingInterval.getSelectedIndex()) {
            case 0: trackingInterval = 0; break;
            case 1: trackingInterval = 5; break;
            case 2: trackingInterval = 10; break;
            case 3: trackingInterval = 30; break;
            case 4: trackingInterval = 60; break;
        }        
        Options.setIntOption(Options.TRACKING_INTERVAL, trackingInterval);
        
        String units = "";
        switch (_units.getSelectedIndex()) {
            case 0: units = "Metric"; break;
            case 1: units = "English"; break;
        }
        Options.setStringOption(Options.UNITS, units);
        
        String mapView = "";
        switch (_mapView.getSelectedIndex()) {
            case 0: mapView = "Satellite"; break;
            case 1: mapView = "Map"; break;
        }
        Options.setStringOption(Options.MAP_VIEW, mapView);
        
        //int zoomLevel = _zoomLevel.getSelectedValue();
        //Options.setIntOption(Options.ZOOM_LEVEL, zoomLevel);
        
        boolean positionLabel = false;
        if (_positionLabel.getSelectedIndex() == 0)
            positionLabel = true;
        Options.setBooleanOption(Options.POSITION_LABEL, positionLabel);

        int fontSize = 0;
        switch (_fontSize.getSelectedIndex()) {
            case 0: fontSize = 8; break;
            case 1: fontSize = 9; break;
            case 2: fontSize = 10; break;
            case 3: fontSize = 11; break;
            case 4: fontSize = 12; break;
        }        
		
        int currentFontSize = Options.getIntOption(Options.FONT_SIZE, Options.DEFAULT_FONT_SIZE);
		if (fontSize != currentFontSize) {
	    	try {
	    		FontFamily fontFamily = FontFamily.forName("BBGlobal Sans");
	    		Font baseFont = fontFamily.getFont(Font.PLAIN, fontSize, Ui.UNITS_pt);
	    		FontManager.getInstance().setApplicationFont(baseFont);
	    	}
	    	catch (ClassNotFoundException ex) {}
		}	
        Options.setIntOption(Options.FONT_SIZE, fontSize);

        int primaryFontSize = 0;
        switch (_primaryFontSize.getSelectedIndex()) {
            case 0: primaryFontSize = 10; break;
            case 1: primaryFontSize = 11; break;
            case 2: primaryFontSize = 12; break;
            case 3: primaryFontSize = 14; break;
            case 4: primaryFontSize = 16; break;
            case 5: primaryFontSize = 18; break;
        }        
        Options.setIntOption(Options.PRIMARY_FONT_SIZE, primaryFontSize);		

        Options.saveOptions();
        return true;
    }
} 
