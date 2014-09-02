package com.skyemarine.sartrek.lib;

import java.util.*;

import net.rim.device.api.system.*;
import net.rim.device.api.util.*;

/**
 * This class allows application options (user preferences) to be saved in persistent storage.
 */
public class Options implements Persistable {
    public static final String MAP_DATUM = "MapDatum";
    public static final String DEFAULT_MAP_DATUM = "NAD 83";
    
    public static final String PRIMARY_DISPLAY = "PrimaryDisplay";
    public static final String DEFAULT_PRIMARY_DISPLAY = "UTM";

    public static final String ROUND_GRID_COORDINATES = "RoundGrid";
    public static final boolean DEFAULT_ROUND_GRID_COORDINATES = false;

    public static final String TRACKING_INTERVAL = "TrackingInterval";
    public static final int DEFAULT_TRACKING_INTERVAL = 0;

    public static final String UNITS = "Units";
    public static final String DEFAULT_UNITS = "Metric";

    public static final String MAP_VIEW = "MapView";
    public static final String DEFAULT_MAP_VIEW = "Satellite";

    // Zoom doesn't work when using markers in Google Maps
    //public static final String ZOOM_LEVEL = "ZoomLevel";
    //public static final int DEFAULT_ZOOM_LEVEL = 15;
    
    public static final String POSITION_LABEL = "PositionLabel";
    public static final boolean DEFAULT_POSITION_LABEL = true;

    public static final String FONT_SIZE = "FontSize";
    public static final int DEFAULT_FONT_SIZE = 10;

    public static final String PRIMARY_FONT_SIZE = "PrimaryFontSize";
    public static final int DEFAULT_PRIMARY_FONT_SIZE = 12;

	//public static final boolean ROUND_GRID = true;

	// Vector of application options
    private static final long _optionsPersistKey = 0xa4b4159478f59a15L;
    private static final PersistentObject _optionsPersistData = PersistentStore.getPersistentObject(_optionsPersistKey);

    private static Hashtable _options = new Hashtable();
    private static Hashtable _defaultValues = new Hashtable();
    
    public static boolean loadOptions() {
        _defaultValues.put(MAP_DATUM, DEFAULT_MAP_DATUM);
        _defaultValues.put(PRIMARY_DISPLAY, DEFAULT_PRIMARY_DISPLAY);
        _defaultValues.put(ROUND_GRID_COORDINATES, new Boolean(DEFAULT_ROUND_GRID_COORDINATES));
        _defaultValues.put(TRACKING_INTERVAL, new Integer(DEFAULT_TRACKING_INTERVAL));
        _defaultValues.put(UNITS, DEFAULT_UNITS);
        _defaultValues.put(MAP_VIEW, DEFAULT_MAP_VIEW);
        //_defaultValues.put(ZOOM_LEVEL, new Integer(DEFAULT_ZOOM_LEVEL));
        _defaultValues.put(POSITION_LABEL, new Boolean(DEFAULT_POSITION_LABEL));
        _defaultValues.put(FONT_SIZE, new Integer(DEFAULT_FONT_SIZE));
        _defaultValues.put(PRIMARY_FONT_SIZE, new Integer(DEFAULT_PRIMARY_FONT_SIZE));

    	Vector options = (Vector)_optionsPersistData.getContents();
        if (options == null || options.isEmpty()) {
            setStringOption(MAP_DATUM, DEFAULT_MAP_DATUM);
            setStringOption(PRIMARY_DISPLAY, DEFAULT_PRIMARY_DISPLAY);
            setBooleanOption(ROUND_GRID_COORDINATES, DEFAULT_ROUND_GRID_COORDINATES);
            setIntOption(TRACKING_INTERVAL, DEFAULT_TRACKING_INTERVAL);
            setStringOption(UNITS, DEFAULT_UNITS);
            setStringOption(MAP_VIEW, DEFAULT_MAP_VIEW);
            //setIntOption(ZOOM_LEVEL, DEFAULT_ZOOM_LEVEL);
            setBooleanOption(POSITION_LABEL, DEFAULT_POSITION_LABEL);
            setIntOption(FONT_SIZE, DEFAULT_FONT_SIZE);
            setIntOption(PRIMARY_FONT_SIZE, DEFAULT_PRIMARY_FONT_SIZE);
            saveOptions();
            return true;
        }
        for (int i = 0; i < options.size(); i++) {
            Option option = (Option)options.elementAt(i);
            _options.put(option.key, option.value);
        }
        return true;
    }

    public static void saveOptions() {
        Vector options = new Vector(_options.size());
        Enumeration e = _options.keys();
        while (e.hasMoreElements()) {
            Option option = new Option();
            option.key = (String)e.nextElement();
            option.value = (Object)_options.get(option.key);
            options.addElement(option);
        }
        _optionsPersistData.setContents(options);
        _optionsPersistData.commit();
    }
    
    public static String getStringOption(String key) {
    	return getStringOption(key, (String)_defaultValues.get(key));
    }
    
    public static String getStringOption(String key, String defaultValue) {
    	String value = defaultValue;
    	if (_options.containsKey(key))
    		value = (String)_options.get(key);
        return value;
    }
    
    public static void setStringOption(String key, String value) {
        _options.put(key, value);
    }
    
    public static int getIntOption(String key) {
    	return getIntOption(key, ((Integer)_defaultValues.get(key)).intValue());
    }
    
    public static int getIntOption(String key, int defaultValue) {
        String value = getStringOption(key, Integer.toString(defaultValue));        
        return Integer.parseInt(value);
    }

    public static void setIntOption(String key, int value)
    {
    	_options.put(key, Integer.toString(value));
    }
    
    public static boolean getBooleanOption(String key) {
    	return getBooleanOption(key, ((Boolean)_defaultValues.get(key)).booleanValue());
    }
    
    public static boolean getBooleanOption(String key, boolean defaultValue) {
        String defaultString = "FALSE";
    	if (defaultValue)
    		defaultString = "TRUE";
        return getStringOption(key, defaultString).equalsIgnoreCase("TRUE");
    }
    
    public static void setBooleanOption(String key, boolean value) {
    	if (value)
    		_options.put(key, "TRUE");
    	else
    		_options.put(key, "FALSE");
    }
} 
