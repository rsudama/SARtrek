package com.skyemarine.sartrek;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.gps.BlackBerryCriteria;
import net.rim.device.api.gps.BlackBerryLocationProvider;
import net.rim.device.api.gps.GPSInfo;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.FontManager;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.util.StringProvider;

import com.skyemarine.sartrek.lib.Options;
import com.skyemarine.sartrek.lib.PersistableLocation;

//import net.rim.blackberry.api.invoke.Invoke;
//import net.rim.blackberry.api.invoke.MapsArguments;

public class MobileNavigator extends UiApplication implements KeyListener {
    public static final String Title = "<b>SARtrek</b> - <i>Mobile Navigator</i>";
    public static final DateFormat ShortDateFormat = new SimpleDateFormat("HHmm | EEE dd/MM");
    
    private static CustomMainScreen _screen;
    //private FormattedField _titleField = 
    //	new FormattedField(Title, Field.USE_ALL_WIDTH | Field.READONLY | Field.NON_FOCUSABLE);    
    //private static final FormattedField _statusField = 
    //	new FormattedField("Status", Field.USE_ALL_WIDTH | Field.READONLY | Field.NON_FOCUSABLE);
    private static FormattedField _textContentField;
    private static LocationField _locationField;
    private static TrackpointField _trackpointField;
    
    private static final int _locationUpdateInterval = 1;   // Seconds - this is the period of position query
    private static BlackBerryLocationProvider _locationProvider = null;
    private static int _failedCount = 0; // Count of timed-out location requests
    private static boolean _showCurrentLocation = true;
    
    private static final Timer _trackingTimer = new Timer();
    private static TrackingTimerTask _trackingTimerTask = null;

    /* Option to generate beep on location update
    private boolean _alert = false;
    short[] _tune = { 1000, 200 }; // pitch, duration
    */
  
    private static final int _splashTimeout = 3; // Delay in seconds
    private static final Bitmap _splashImage = Bitmap.getBitmapResource("img/SplashScreen.png");

    /* for testing coordinate conversions
    double[] lat = { 49.2781, 51.3345, 4.2798, 88.8888, -27.2525, -73.2248 };
    double[] lon = { 123.4952, 22.9543, 170.8844, -17.2233, 145.8794, -123.4568 };
    // 51U 0536018 5458490
    // 34U 0636138 5688837
    // 59N 0487172 0473056
    // 28Z 0495187 9873993
    // 55J 0389065 6985101
    // 10C 0485284 1874387
    */
    
    /**
     * Instantiate the new application object and enter the event loop.
     * @param args No args are supported for this application.
     */
    public static void main(String[] args) {
    	try {
    		new MobileNavigator().enterEventDispatcher();
    	} catch (Exception ex) {
    		Dialog.alert(ex.getMessage());
    		System.exit(1);
    	}
    }

    /**
     * Application initialisation - runs once on startup.
     */
    public MobileNavigator() {
        // Load options
        Options.loadOptions();
         	
    	try {
    		/* for testing coordinate conversions
    		double distance = PersistableLocation.distanceFrom
    			(new Double(43.67750), new Double(-80.733900), new Double(43.46310), new Double(-80.52070));
    		int bearing = PersistableLocation.bearingTo
				(new Double(43.67750), new Double(-80.733900), new Double(43.46310), new Double(-80.52070));
    		distance = PersistableLocation.distanceFrom
				(new Double(43.67750), new Double(-80.733900), new Double(43.67750), new Double(-80.733750));
    		bearing = PersistableLocation.bearingTo
				(new Double(43.67750), new Double(-80.733900), new Double(43.67750), new Double(-80.733750));

    	    for (int i = 0; i < lat.length; i++) {
    	    	String utm1 = CoordinateConverter.LLtoUTM("WGS 84", lat[i], lon[i]);
    	    	String mgrs1 = CoordinateConverter.LLtoMGRS("WGS 84", lat[i], lon[i]);
    	    	CoordinateConversion cc = new CoordinateConversion();
    	    	String utm2 = cc.latLon2UTM(lat[i], lon[i]);
    	    	String mgrs2 = cc.latLon2MGRUTM(lat[i], lon[i]);
    	    	System.out.println(lat[i] + " / " + lon[i] + ":");
    	    	System.out.println(utm1 + " @ " + mgrs1);
    	    	System.out.println(utm2 + " @ " + mgrs2);
    	    	System.out.println();
    	    }
    		*/
    		
    	    // Set a default font for the application
    		int fontSize = Options.getIntOption(Options.FONT_SIZE, Options.DEFAULT_FONT_SIZE);
    		FontFamily fontFamily = FontFamily.forName("BBGlobal Sans");
    		Font baseFont = fontFamily.getFont(Font.PLAIN, fontSize, Ui.UNITS_pt);
        	//FontManager.getInstance().setApplicationFont(FontFamily.forName("BBMillibank Tall").getFont(Font.PLAIN, Font.getDefault().getHeight()));
        	FontManager.getInstance().setApplicationFont(baseFont);
        	
        	/* test route mapping
        	String document = "<location-document>" + "<location lon='-8030000' lat='4326000' " +
        	"label='Kitchener, ON' description='Kitchener, Ontario,	Canada' />" +
        	"<location lon='-7569792' lat='4542349' label='Ottawa, ON' description='Ottawa, Ontario, Canada' />" +
        	"</location-document>";

        	document = 
        	"<location-document>" +
            "<GetRoute>" +
               "<location lon='-8030000' lat='4326000' label='Kitchener, ON' description='Kitchener, Ontario, Canada' />" +
               "<location lon='-7569792' lat='4542349' label='Ottawa, ON' description='Ottawa, Ontario, Canada' />" +
            "</GetRoute>" +
            "</location-document>";

        	Invoke.invokeApplication(Invoke.APP_TYPE_MAPS, new MapsArguments
        		( MapsArguments.ARG_LOCATION_DOCUMENT, document));
        	*/
        } catch (ClassNotFoundException ex) {
        	Dialog.alert("Unable to set application font!");
        	System.exit(1);
        }

        // Set up the main screen
        _screen = new CustomMainScreen(Title, makeMenu());        
        _screen.getMainManager().setBackground(BackgroundFactory.
            createLinearGradientBackground(Color.AZURE, Color.AZURE, Color.CADETBLUE, Color.CADETBLUE));                
         
        // Set base fields for later use
        _textContentField = new FormattedField(Field.READONLY | Field.USE_ALL_WIDTH);
        _textContentField.setBackground(BackgroundFactory.createSolidTransparentBackground(0, 0));
        _locationField = new LocationField();
        _trackpointField = new TrackpointField();

        _screen.addKeyListener(this);

        // Load trackpoints from persistent storage and start tracking timer if required
        Vector trackpoints = (Vector)PersistableLocation.trackpointsPersistData.getContents();
        if (trackpoints != null && trackpoints.size() > 0) {
            if (!(trackpoints.firstElement() instanceof PersistableLocation)) {
                PersistableLocation.trackpointsPersistData.setContents(new Vector());
                PersistableLocation.trackpointsPersistData.commit();
            }
        }
        
        if (Options.getIntOption(Options.TRACKING_INTERVAL) > 0) {
            _trackingTimerTask = new TrackingTimerTask();
            _trackingTimer.schedule(_trackingTimerTask, 0, Options.getIntOption(Options.TRACKING_INTERVAL) * 60000);
        }            
        
        // Start GPS receiver
        if (!startLocationUpdate()) {
            Dialog.alert("Start location update failed.");
        }

        // Render our screen via the splash screen
        new SplashScreen(this, _screen, _splashImage, _splashTimeout);
    }

    /**
     * Run on application exit (or going to background).
    public void deactivate() {
        // Shut down the GPS receiver
    	if (_locationProvider != null) {
            _locationProvider.reset();
            _locationProvider.setLocationListener(null, -1, -1, -1);
        }
    }
     */
    
    /**
     * Create main menu.
     */
    protected Menu makeMenu() {
        Menu menu = new Menu();
        menu.deleteAll();
        menu.add(new MenuItem(new StringProvider("Show trackpoints"), 100, 1) { public void run() { updateTrackpoints(); }});
        menu.add(new MenuItem(new StringProvider("Add trackpoint"), 100, 2) { public void run() { addTrackpoint(); }});
        menu.add(new MenuItem(new StringProvider("Clear trackpoints"), 100, 3) { public void run() { clearTrackpoints(); }});
        menu.addSeparator();
        menu.add(new MenuItem(new StringProvider("Last known location"), 100, 4) { public void run() { displayLastKnownLocation(); }});
        menu.addSeparator();
        menu.add(new MenuItem(new StringProvider("Map"), 100, 5) { public void run() { displayMap(); }});
        menu.addSeparator();
        menu.add(new MenuItem(new StringProvider("Options"), 100, 6) { public void run() { displayOptions(); }});
        menu.addSeparator();
        menu.add(new MenuItem(new StringProvider("Help"), 100, 7) { public void run() { displayHelp(); }});
        menu.addSeparator();
        return menu;
    }

    /**
     * Handle keypad input.
     */
    public boolean keyChar(char key, int status, int time) {
        try {
            switch (Character.toUpperCase(key)) {
                case 'A': { addTrackpoint(); break; }
                case 'T': { updateTrackpoints(); break; }
                case 'D':
                case Keypad.KEY_BACKSPACE:
                case Keypad.KEY_DELETE: { deleteTrackpoint(); break; }
                case 'C': { clearTrackpoints(); break; }
                case 'L': { displayLastKnownLocation(); break; }
                case 'M': { displayMap();  break; }
                case 'O': { displayOptions(); break; }
                case 'H': { displayHelp(); break; }
                //case 'X': { displayTrack();  break; }

                case Keypad.KEY_ENTER:
                case Keypad.KEY_ESCAPE: {
                	updateLocation(new PersistableLocation());
                	if (!_showCurrentLocation)
                    	_showCurrentLocation = true;                 
                    else {
                        // On an explicit exit, shut down the GPS receiver
                    	if (_locationProvider != null) {
                            _locationProvider.reset();
                            _locationProvider.setLocationListener(null, -1, -1, -1);
                        }
                        System.exit(0);
                    }
                    break;
                }
                default: return false;

                /*                
                case 'S': {
                    // Turn sound alert on/off
                    _alert = !_alert;
                    break;
                }
                */
            }
        }
        catch (Exception ex) {
            Dialog.alert(ex.toString());
        }
        return true;
    }
    
    // Other methods required by KeyListener interface
    public boolean keyDown(int keycode, int time) { return false; }
    public boolean keyUp(int keycode, int time) { return false; }
    public boolean keyRepeat(int keycode, int time) { return false; }
    public boolean keyStatus(int keycode, int time) { return false; }

    /**
    * Update the status section of the screen with a text message.
    */
    private void updateStatus(final String msg) {
        invokeLater(new Runnable() {
            public void run() {
                //RichTextFormatter.TextFormat format = RichTextFormatter.format(msg);
                _screen.setStatusText(msg);
            }
        });
    }

    /**
    * Update the contents section of the screen with a text message.
    private void updateContent(final String msg) {
        invokeLater(new Runnable() {
            public void run() {
                _textContentField.setText(msg);
                _screen.deleteAll();
                _screen.add(_textContentField);
            }
        });
    }
    */

    /**
    * Update the contents section of the screen with formatted text.
    private void updateContent(RichTextFormatter.TextFormat format) {
        if (format.text.length() == 0) 
            updateContent(format.text);
        else
            updateContent(format.text, format.offsets, format.attributes, format.fonts);
    }
    */

    /**
    * Update the contents section of the screen with formatted text.
    private void updateContent(final String msg, final int[] offsets, final byte[] attrs, final Font[] fonts) {
        invokeLater(new Runnable() {
            public void run() {
                _textContentField.setText(msg, offsets, attrs, fonts);
                _screen.deleteAll();
                _screen.add(_textContentField);
            }
        });
    }
    */

    /**
     * Update and display the location screen.
     */
    private void updateLocation(final PersistableLocation location) {
    	if (location == null)
    		return;
    	
        invokeLater(new Runnable() {
            public void run() {
                _locationField = new LocationField();
                _locationField.updateLocation(location);               
                _screen.deleteAll();
                _screen.add(_locationField);
            }
        });
    }
    
    /**
     * Update only the date field on the location screen.
     */
    private void updateDate() {
        invokeLater(new Runnable() {
            public void run() {
                _locationField.updateDate(new Date());                
                _screen.deleteAll();
                _screen.add(_locationField);
            }
        });
    }
    
    /**
     * Update and display the trackpoints screen.
     */
    private void updateTrackpoints() {
        invokeLater(new Runnable() {
            public void run() {
            	_showCurrentLocation = false;           	
                _screen.deleteAll();
                updateStatus("Trackpoints");
                _screen.add(_trackpointField);
                _trackpointField.refresh();
            }
        });
    }
    
    /**
     * Update and display the help screen.
     */
    private void updateHelp(final VerticalFieldManager manager) {
        invokeLater(new Runnable() {
            public void run() {
            	_showCurrentLocation = false;
            	_screen.deleteAll();                
            	updateStatus("Help - SARtrek V2.1.0");
                _screen.add(manager);
            }
        });
    }

    /**
     * Invoke the Location API with the default criteria.
     * @return True if the Location Provider was successfully started; false otherwise.
     */
    private boolean startLocationUpdate() {
        try {
      	    BlackBerryCriteria criteria = new BlackBerryCriteria(GPSInfo.GPS_MODE_AUTONOMOUS);
        	criteria.setAddressInfoRequired(false);
        	criteria.setAltitudeRequired(true);
        	criteria.setCostAllowed(false);
        	criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
        	criteria.setPreferredPowerConsumption(Criteria.NO_REQUIREMENT);
        	criteria.setPreferredResponseTime(1000);
        	criteria.setSpeedAndCourseRequired(true);
        	criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
        	
            _locationProvider = (BlackBerryLocationProvider)LocationProvider.getInstance(criteria);
                        
            if (_locationProvider == null) {
                updateStatus("GPS is not supported on this device.");
                return false;
            }

            // Display the last known position stored by the GPS receiver
            if (PersistableLocation.lkpPersistData != null) {
                PersistableLocation pLocation = (PersistableLocation)PersistableLocation.lkpPersistData.getContents();
                updateStatus("Last known location...");
                updateLocation(pLocation);
            }
            
            //
            // Parameters:
            // listener - the listener to be registered. If set to null the registration of any
            //      previously set listener is cancelled.
            // interval - the interval in seconds. -1 is used for the default interval of this provider.
            //      0 is used to indicate that the application wants to receive only provider status updates
            //      and not location updates at all.
            // timeout - timeout value in seconds, must be greater than 0. if the value is -1, the default
            //      timeout for this provider is used. Also, if the interval is -1 to indicate the default,
            //      the value of this parameter has no effect and the default timeout for this provider is used.
            //      If the interval is 0, this parameter has no effect.
            // maxAge - maximum age of the returned location in seconds, must be greater than 0 or equal to -1
            //      to indicate that the default maximum age for this provider is used. Also, if the interval is
            //      -1 to indicate the default, the value of this parameter has no effect and the default maximum
            //      age for this provider is used. If the interval is 0, this parameter has no effect. 
            //
            // Passing in -1 as the interval selects the default interval which is dependent on the
            // used location method. Passing in 0 as the interval registers the listener to only receive 
            // provider status updates and not location updates at all. 
            //
            // Only one listener can be registered with each LocationProvider instance. Setting the listener
            // replaces any possibly previously set listener. Setting the listener to null cancels the
            // registration of any previously set listener. 
            //
            // The implementation shall initiate obtaining the first location result immediately when the
            // listener is registered and provide the location to the listener as soon as it is available.
            // Subsequent location updates will happen at the defined interval after the first one. If the
            // specified update interval is smaller than the time it takes to obtain the first result, the
            // listener shall receive location updates with invalid Locations at the defined interval until
            // the first location result is available. 
            //
            // The timeout parameter determines a timeout that is used if it's not possible to obtain a new
            // location result when the update is scheduled to be provided. This timeout value indicates how
            // many seconds the update is allowed to be provided late compared to the defined interval. If it's
            // not possible to get a new location result (interval + timeout) seconds after the previous update,
            // the update will be made and an invalid Location instance is returned. This is also done if the
            // reason for the inability to obtain a new location result is due to the provider being temporarily
            // unavailable or out of service. 
            //
            // For example, if the interval is 60 seconds and the timeout is 10 seconds, the update must be
            // delivered at most 70 seconds after the previous update and if no new location result is available
            // by that time the update will be made with an invalid Location instance. 
            //
            // The maxAge parameter defines how old the location result is allowed to be provided when the update
            // is made. This allows the implementation to reuse location results if it has a recent location
            // result when the update is due to be delivered. This parameter can only be used to indicate a larger
            // value than the normal time of obtaining a location result by a location method. The normal time of
            // obtaining the location result means the time it takes normally to obtain the result when a request
            // is made. If the application specifies a time value that is less than what can be realized with the
            // used location method, the implementation shall provide as recent location results as are possible
            // with the used location method. For example, if the interval is 60 seconds, the maxAge is 20 seconds
            // and normal time to obtain the result is 10 seconds, the implementation would normally start obtaining
            // the result 50 seconds after the previous update. If there is a location result otherwise available
            // that is more recent than 40 seconds after the previous update, then the maxAge setting to 20 seconds
            // allows to return this result and not start obtaining a new one. 
            //
            _locationProvider.setLocationListener(new LocationListenerImpl(), _locationUpdateInterval, 1, -1);
        }
        catch (LocationException ex) {
            Dialog.alert("Failed to instantiate the LocationProvider object: " + ex.toString());
            return false;
        }        
        return true;
    }
       
    /**
     * Implementation of the LocationListener interface.
     */
    private class LocationListenerImpl implements LocationListener {
        public void locationUpdated(LocationProvider provider, Location location) {
        	// Don't update the location screen if trackpoints, options, help, map (etc) are active
        	if (!_showCurrentLocation)
        		return;
        	
            if (location == null || !location.isValid()) {
                updateStatus("Unable to obtain location... " + ++_failedCount);
                updateDate();
                return;
            }
            _failedCount = 0;
            updateStatus("Current location...");

            PersistableLocation pLocation = 
            	new PersistableLocation(location, Options.getStringOption(Options.MAP_DATUM));
            updateLocation(pLocation);
            
            PersistableLocation.lkpPersistData.setContents(pLocation);
            PersistableLocation.lkpPersistData.commit();        
            
            /*
            // Beep if requested
            if (_alert) {
                invokeLater(new Runnable() {
                    public void run() {
                        Alert.startAudio(_tune, 100); // pitch/duration, volume
                    }
                });
            }
            */
        }

        public void providerStateChanged(LocationProvider provider, int newState) {
            // Not implemented.
        }        
    }  

    /**
     * Add a trackpoint to the trackpoint list.
     */
    private void addTrackpoint() {
        // Add the current position to the list of trackpoints
        PersistableLocation pLocation = PersistableLocation.getLastKnownLocation();
        if (!pLocation.isValid()) {
            Dialog.alert("Last known location is not available");
            return;
        }
        addTrackpointBackground(true);
        //Dialog.inform("Trackpoint added");
        updateTrackpoints();        
    }

    /**
     * Add a trackpoint to the trackpoint list as a background operation.
     */
    private void addTrackpointBackground(boolean allowDuplicates)
    {
    	// Get trackpoints from persistent storage
        Vector trackpoints = PersistableLocation.getTrackpoints();
        
        // Get the last recorded location
        PersistableLocation pLocation = PersistableLocation.getLastKnownLocation();

        if (pLocation.isValid())
        {
            // Only record new trackpoint if it differs from the last one recorded 
            // (no point in recording multiple trackpoints at the same location)
        	if (allowDuplicates || trackpoints.isEmpty() ||
        	!((PersistableLocation)trackpoints.lastElement()).grid.equals(pLocation.grid)) {
	         	// Add this location to the list of trackpoints and save the list to persistent storage
	    		trackpoints.addElement(pLocation);
	        	PersistableLocation.trackpointsPersistData.setContents(trackpoints);
	        	PersistableLocation.trackpointsPersistData.commit();
        	}
        	// reset the travel value
        	//pLocation.travelBySpeed = new Double(0.0);
        	//pLocation.travelByDist = new Double(0.0);
        	PersistableLocation.lkpPersistData.setContents(pLocation);
            PersistableLocation.lkpPersistData.commit();        
    	}
    }
    
    /**
     * Delete the selected trackpoint from the trackpoint list.
     */
    private void deleteTrackpoint() {
    	if (!_screen.getStatusText().equalsIgnoreCase("Trackpoints"))
    		return;
        if (Dialog.ask(Dialog.D_OK_CANCEL, "Are you sure you want to delete this trackpoint?", Dialog.CANCEL) != Dialog.OK)
            return;

        Vector trackpoints = PersistableLocation.getTrackpoints();
        int index = _trackpointField.getFieldWithFocusIndex();
        if (index != -1) {
        	// before deleting this trackpoint, adjust travel and distance values of next trackpoint
        	if (trackpoints.size() > index + 1) {
        		//PersistableLocation thisTrackpoint = (PersistableLocation)trackpoints.elementAt(index);
        		PersistableLocation nextTrackpoint = (PersistableLocation)trackpoints.elementAt(index + 1);
        		// set accumulated travel value
        		//nextTrackpoint.travelBySpeed = 
        		//	new Double(nextTrackpoint.travelBySpeed.doubleValue() + thisTrackpoint.travelBySpeed.doubleValue());
        		//nextTrackpoint.travelByDist = 
        		//	new Double(nextTrackpoint.travelByDist.doubleValue() + thisTrackpoint.travelByDist.doubleValue());
        		if (index > 0) {
        			PersistableLocation prevTrackpoint = (PersistableLocation)trackpoints.elementAt(index - 1);
        			// re-calculate distance
                	nextTrackpoint.distance = new Double(PersistableLocation.distanceFrom
                		(prevTrackpoint.latitude, prevTrackpoint.longitude,
                		nextTrackpoint.latitude, nextTrackpoint.longitude));                	
        		}
        		trackpoints.setElementAt(nextTrackpoint, index + 1);
            }
            trackpoints.removeElementAt(index);
            PersistableLocation.trackpointsPersistData.setContents(trackpoints);
            PersistableLocation.trackpointsPersistData.commit();
        }
        updateTrackpoints();
    }

    /**
     * Clear all trackpoints from the trackpoint list.
     */
    private void clearTrackpoints() {
        // Clear all trackpoints
        if (Dialog.ask(Dialog.D_OK_CANCEL, "Are you sure you want to clear all trackpoints?", Dialog.CANCEL) != Dialog.OK)
            return;
        PersistableLocation.trackpointsPersistData.setContents(new Vector());
        PersistableLocation.trackpointsPersistData.commit();
       	if (_screen.getStatusText().equalsIgnoreCase("Trackpoints"))
        	updateTrackpoints();
        
    	// reset the travel value
        PersistableLocation pLocation = PersistableLocation.getLastKnownLocation();
    	//pLocation.travelBySpeed = new Double(0.0);
    	//pLocation.travelByDist = new Double(0.0);
    	PersistableLocation.lkpPersistData.setContents(pLocation);
        PersistableLocation.lkpPersistData.commit();        
    }
    
    /**
     * A scheduled task to automatically add a trackpoint to the list at a specified interval.
     */
    private class TrackingTimerTask extends TimerTask {
		public void run() {
            if (_locationProvider != null && LocationProvider.getLastKnownLocation() != null)
                addTrackpointBackground(false);
        }
    }
    
    /**
     * Display the last known location in the location screen.
     */
    private void displayLastKnownLocation() {
        PersistableLocation pLocation = PersistableLocation.getLastKnownLocation();
        if (!pLocation.isValid.booleanValue()) {
            Dialog.alert("Last known location is not available");
            return;
        }

        // Display the last known position stored in persistent storage
        _showCurrentLocation = false;
        updateStatus("Last known location");
        updateLocation(pLocation);
    }
    
    /**
     * Display a map with a location marker in Google Maps.
     */
    private void displayMap() {
        // Check if Google Maps is installed
    	int moduleHandle = CodeModuleManager.getModuleHandle("GoogleMaps");
    	if (moduleHandle == 0) {
		     Dialog.alert("Google Maps isn't installed");
		     return;
		}
    	
    	// See if a trackpoint is selected - if so, use it as the location to map
		PersistableLocation pLocation;
    	if (_screen.getStatusText().equalsIgnoreCase("Trackpoints")) {
    		pLocation = _trackpointField.getSelectedTrackpoint();
    		if (pLocation == null) {
    			Dialog.alert("No trackpoint selected");
    			return;
    		}
    	}
    	// If no trackpoint selected, use the last known location
    	else {
    		pLocation = PersistableLocation.getLastKnownLocation();        
    		if (!pLocation.isValid()) {
    			Dialog.alert("Last known location is not available");
    			return;
    		}
    	}
        
    	// Create a URL for invoking Google Maps
		URLEncodedPostData uepd = new URLEncodedPostData(null, false);
		uepd.append("action", "LOCN");
		uepd.append("a", "@latlon:" + pLocation.latitude.toString() + "," + pLocation.longitude.toString());
		if (Options.getBooleanOption(Options.POSITION_LABEL, Options.DEFAULT_POSITION_LABEL)) {
			uepd.append("title", pLocation.grid + " @ " + ShortDateFormat.format(new Date(pLocation.timestamp.longValue())));
			uepd.append("description", "SARtrek: " + pLocation.utm);
		}
		if (Options.getStringOption(Options.MAP_VIEW).equalsIgnoreCase("Satellite"))
			uepd.append("view", "SATV");
		else
			uepd.append("view", "MAPV");
		//uepd.append("zoom", Integer.toString(Options.getIntOption(Options.ZOOM_LEVEL)));
		
		// Invoke Google Maps
		String[] args = { "http://gmm/x?" + uepd.toString() };
		ApplicationDescriptor app1 = CodeModuleManager.getApplicationDescriptors(moduleHandle)[0];
		ApplicationDescriptor app2 = new ApplicationDescriptor(app1, args);
		try {
			ApplicationManager.getApplicationManager().runApplication(app2, true);
		} catch (ApplicationManagerException ex) {
			Dialog.alert(ex.getMessage());
		}
    }
       
    /**
     * Display a map with a path in Google Maps.
    private void displayTrack() {
    	// Get trackpoints from persistent storage
        Vector trackpoints = PersistableLocation.getTrackpoints();
        if (trackpoints.isEmpty() || trackpoints.size() == 1) {
        	Dialog.alert("No trackpoints to plot");
        	return;
        }

        // Check if Google Maps is installed
    	int moduleHandle = CodeModuleManager.getModuleHandle("GoogleMaps");
    	if (moduleHandle == 0) {
		     Dialog.alert("Google Maps isn't installed");
		     return;
		}    	

    	//<?xml version=\"1.0\" encoding=\"UTF-8\"?>
    	//<kml xmlns=\"http://www.opengis.net/kml/2.2\">
    	//<Document>
    	//<Folder>
    	//<name>Paths</name>
    	//<open>0</open>
    	//<Placemark>
    	//<LineString><tessellate>1</tessellate>
    	//<coordinates> -112.0814237830345,36.10677870477137,0 -112.0870267752693,36.0905099328766,0</coordinates>
    	//</LineString>
    	//</Placemark>
    	//</Folder>
    	//</Document>
    	//</kml>
    	
    	String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    	kml += "<kml xmlns=\"http://www.opengis.net/kml/2.2\">";
    	kml += "<Document><Folder><name>Paths</name><open>0</open><Placemark>";
    	kml += "<LineString><tessellate>1</tessellate>";
    	kml += "<coordinates> -112.0814237830345,36.10677870477137,0 -112.0870267752693,36.0905099328766,0</coordinates>";
    	kml += "</LineString>";
    	kml += "</Placemark></Folder></Document></kml>";
    	
    	// Create a URL for invoking Google Maps
// "-URL \"?action=rout&start=32.067429,34.776256&end=32.119074,34.821832\""   for route 
		URLEncodedPostData uepd = new URLEncodedPostData(null, false);
		uepd.append("action", "PATH");
		for (Enumeration e = trackpoints.elements(); e.hasMoreElements(); ) {
			PersistableLocation pLocation = (PersistableLocation)e.nextElement();
			uepd.append("a", "@latlon:" + pLocation.latitude.toString() + "," + pLocation.longitude.toString());
		}
		//uepd.append("start", "@" + ((PersistableLocation)trackpoints.firstElement()).latitude.toString()
		//		+ "," + ((PersistableLocation)trackpoints.firstElement()).longitude.toString());
		//uepd.append("end", "@" + ((PersistableLocation)trackpoints.lastElement()).latitude.toString()
		//		+ "," + ((PersistableLocation)trackpoints.lastElement()).longitude.toString());
		if (Options.getStringOption(Options.MAP_VIEW).equalsIgnoreCase("Satellite"))
			uepd.append("view", "SATV");
		else
			uepd.append("view", "MAPV");
		//uepd.append("zoom", Integer.toString(Options.getIntOption(Options.ZOOM_LEVEL)));
		
		// Invoke Google Maps
		String[] args = { "http://gmm/x?" + uepd.toString() };
		ApplicationDescriptor app1 = CodeModuleManager.getApplicationDescriptors(moduleHandle)[0];
		ApplicationDescriptor app2 = new ApplicationDescriptor(app1, args);
		try {
			ApplicationManager.getApplicationManager().runApplication(app2, true);
		} catch (ApplicationManagerException ex) {
			Dialog.alert(ex.getMessage());
		}
    }
     */

    /**
     * Display options screen.
     */
    private void displayOptions() {    	
		pushScreen(new OptionsScreen());
    }
    
    /**
     * Display help screen.
     */
    private void displayHelp() {
        VerticalFieldManager manager = new VerticalFieldManager();
        manager.add(new FormattedField(" <b>T</b> - Display Trackpoints"));
        manager.add(new FormattedField(" <b>A</b> - Add Trackpoint"));
        manager.add(new FormattedField(" <b>D</b> - Delete Selected Trackpoint"));
        manager.add(new FormattedField(" <b>C</b> - Clear Trackpoints"));
        manager.add(new FormattedField(" <b>L</b> - Display Last Known Location"));
        manager.add(new FormattedField(" <b>M</b> - Display Map"));
        manager.add(new FormattedField(" <b>O</b> - Set Options"));
        manager.add(new FormattedField(" <b>H</b> - Display Help"));
        manager.add(new FormattedField(" <b>Esc</b> - Close"));
        updateHelp(manager);
    }
}

/* Other ways of displaying maps
                case 'M': {
                    if (_locationProvider == null || _locationProvider.getLastKnownLocation() == null) {
                        Dialog.alert("Last known location is not available");
                        break;
                    }
                    Location location = _locationProvider.getLastKnownLocation();
                    QualifiedCoordinates coord = location.getQualifiedCoordinates();
                    int mh = CodeModuleManager.getModuleHandle("GoogleMaps");
                    if (mh == 0) {
                        throw new ApplicationManagerException("GoogleMaps isn't installed");
                    }
                    URLEncodedPostData uepd = new URLEncodedPostData(null, false);
                    uepd.append("action", "LOCN");
                    //uepd.append("a", "@latlon:" + coord.getLatitude() + "," + coord.getLongitude());
                    uepd.append("a", coord.getLatitude() + "," + coord.getLongitude());
                    uepd.append("view", "SATV");
                    uepd.append("title", "Current Location");
                    uepd.append("description", _shortDate.format(new Date(location.getTimestamp())));
                    uepd.append("z", "10");
                    String[] args = { "http://gmm/x?" + uepd.toString() };
                    ApplicationDescriptor ad = CodeModuleManager.getApplicationDescriptors(mh)[0];
                    ApplicationDescriptor ad2 = new ApplicationDescriptor(ad, args);
                    ApplicationManager.getApplicationManager().runApplication(ad2, true);
                    break;
                }

                case 'N': {
                    if (_locationProvider == null || _locationProvider.getLastKnownLocation() == null) {
                        Dialog.alert("Last known location is not available");
                        break;
                    }
                    Location location = _locationProvider.getLastKnownLocation();
                    QualifiedCoordinates coord = location.getQualifiedCoordinates();
                    String document = "<location-document><GetRoute>" +
                        //"<location y='-12311908' x='4928758'" + 
                        "<location lon='" + coord.getLongitude() + 
                        "' lat='" + coord.getLatitude() +
                        " label='Kitchener, ON' description='Kitchener, Ontario, Canada' />" +
                        "<location lon='" + (coord.getLongitude()+.1) +
                        "' lat='" + (coord.getLatitude()+.1) +
                        //"<location y='-12312008' x='4928858'" + 
                        " label='Ottawa, ON' description='Ottawa, Ontario, Canada' />" +
                        "</GetRoute></location-document>";
                    Invoke.invokeApplication(Invoke.APP_TYPE_MAPS,
                        new MapsArguments(MapsArguments.ARG_LOCATION_DOCUMENT, document));
                    break;
                }
                
                case 'G': {
                    Location location = null;
                    if (_locationProvider != null) {
                        location = _locationProvider.getLastKnownLocation();
                        _locationProvider.reset();
                        _locationProvider = null;
                    }

                    QualifiedCoordinates coord = location.getQualifiedCoordinates();

                    _mapField.moveTo(coord);
                    _screen.deleteAll();
                    _screen.add(_mapField);
                    _screen.getGraphics().setColor(Color.RED);
                    _screen.getGraphics().drawLine(10, 10, 50, 50);
                    break;
                }
                
                case 'O': {
                    if (_mapField.getZoom() < _mapField.getMaxZoom()) { 
                        _mapField.setZoom(_mapField.getZoom() + 1);
                    }
                    break;
                }
                
                case 'P': {
                    if (_mapField.getZoom() > _mapField.getMinZoom()) {
                        _mapField.setZoom(_mapField.getZoom() - 1);
                    }
                    break;
                }
                   
                case 'B': {
                    Browser.getDefaultSession().displayPage("file:///system/Map.html");
                    break;
                }
                
                case 'X': {
                    int mh = CodeModuleManager.getModuleHandle("J2meMap");
                    if (mh == 0) {
                        throw new ApplicationManagerException("J2meMap isn't installed");
                    }
                    String[] args = { "", "" };
                    ApplicationDescriptor ad = CodeModuleManager.getApplicationDescriptors(mh)[0];
                    ApplicationDescriptor ad2 = new ApplicationDescriptor(ad, args);
                    ApplicationManager.getApplicationManager().runApplication(ad2, true);
                    break;
                }
*/
