package com.skyemarine.sartrek.lib;

import java.util.*;
import javax.microedition.location.*;

import net.rim.device.api.system.*;
import net.rim.device.api.util.*;
import net.rim.device.api.i18n.*;

/**
 * A persistable version of the Java Location object.
 */
public class PersistableLocation implements Persistable {
	static final double METRES_TO_FEET = 3.2808399;
	static final double EARTH_RADIUS_METRES = 6371009.0;
	
	// Keys for persisting various data
    // Trackpoints contains a vector of locations
    private static final long _trackpointsPersistKey = 0xa4b4159478f59a03L;
    public static final PersistentObject trackpointsPersistData = PersistentStore.getPersistentObject(_trackpointsPersistKey);
    
    // Last known location
    private static final long _lkpPersistKey = 0xa4b4159478f59a04L;
    public static final PersistentObject lkpPersistData = PersistentStore.getPersistentObject(_lkpPersistKey);
    
    // Persistable properties
    public Boolean isValid = new Boolean(false);    
    public String date;
    public String grid = "--- ---";
    public String utm = "---";
    public String mgrs = "---";
    public String coord = "---";
    public String heading = "---";
    public String speed = "---";
    public String altitude = "---";
    public String accuracy = "---";
    //public String distLandBySpeed = "---";
    //public String distLandByDist = "---";
    public String distanceFrom = "---";
    public String bearingTo = "---";
    
    // Other useful source values
    public Long timestamp = new Long(0);
    public Double latitude = new Double(0.0);
    public Double longitude = new Double(0.0);    
    //public Double travelBySpeed = new Double(0.0);
    //public Double travelByDist = new Double(0.0);
    public Double distance = new Double(0.0);	// distance from last trackpoint
    public Integer bearing = new Integer(0);	// bearing to last trackpoint
    
    public static final DateFormat LongDateFormat = new SimpleDateFormat("EEE dd MMM yyyy | HHmm : ss");

    public PersistableLocation() {
    	date = LongDateFormat.format(new Date());
    }
    
    public PersistableLocation(Location location, String datum) {
        date = LongDateFormat.format(new Date());

        if (location != null && location.isValid() && location.getTimestamp() > 0 &&
        		location.getQualifiedCoordinates().getLatitude() != 0 &&
        		location.getQualifiedCoordinates().getLongitude() != 0) {
            isValid = new Boolean(true);
            
            QualifiedCoordinates qCoord = location.getQualifiedCoordinates();
            latitude = new Double(qCoord.getLatitude());
            longitude = new Double(qCoord.getLongitude());
            
            date = LongDateFormat.format(new Date(location.getTimestamp()));
            timestamp = new Long(location.getTimestamp());
            
            // Convert lat/long to UTM and extract grid coordinates from UTM string
            utm = CoordinateConverter.LLtoUTM(datum, qCoord.getLatitude(), qCoord.getLongitude());
            
            grid = CoordinateConverter.getGridCoordinates
            	(utm, Options.getBooleanOption(Options.ROUND_GRID_COORDINATES));
            
            mgrs = CoordinateConverter.LLtoMGRS(datum, qCoord.getLatitude(), qCoord.getLongitude());

            // Format lat/long strings
            //String latCoord = Coordinates.convert(qCoord.getLatitude(), Coordinates.DD_MM);
            String latCoord = Double.toString(qCoord.getLatitude());
            int latPrecision = latCoord.length() - (latCoord.indexOf('.') + 1);
            if (latPrecision < 5) {
            	// Add trailing zeroes
                for (int i = latPrecision; i < 5; i++)
                	latCoord += "0";
            }
            else if (latPrecision > 5)
            	// Truncate precision to 5 digits
            	latCoord = latCoord.substring(0, latCoord.indexOf('.') + 6);
            // Mark south and north based on sign
            if (latCoord.startsWith("-"))
                latCoord = latCoord.substring(1) + "S";
            else
                latCoord += "N";
            
            //String longCoord = Coordinates.convert(qCoord.getLongitude(), Coordinates.DD_MM);
            String longCoord = Double.toString(qCoord.getLongitude());
            int longPrecision = longCoord.length() - (longCoord.indexOf('.') + 1);
            if (longPrecision < 5) {
            	// Add trailing zeroes
                for (int i = longPrecision; i < 5; i++)
                	longCoord += "0";
            }
            else if (longPrecision > 5)
            	// Truncate precision to 5 digits
            	longCoord = longCoord.substring(0, longCoord.indexOf('.') + 6);
            // Mark west and east based on sign
            if (longCoord.startsWith("-"))
                longCoord = longCoord.substring(1) + "W";
            else
                longCoord += "E";
    
            coord = latCoord + "  " + longCoord;
            
            heading = Integer.toString((int)location.getCourse());
            
            // Convert speed in meters/sec to knots
            // speed = ((location.getSpeed() / (float)60) / (float)60) * (float)0.000539956803;
            // Convert speed in meters/sec to knots
            // speed = ((location.getSpeed() / (float)60) / (float)60) * (float)0.000539956803;
            if (Options.getStringOption(Options.UNITS).equalsIgnoreCase("metric"))
            	speed = Integer.toString((int)location.getSpeed());
            else
                // Convert speed in meters/sec to feet/sec
                speed = Integer.toString((int)(location.getSpeed() * (float)METRES_TO_FEET));            	
            
            // Nobody knows why this might be a negative number!!!
            int alt = (int)qCoord.getAltitude();
            if (alt < 0)
                alt = -alt;
            if (Options.getStringOption(Options.UNITS).equalsIgnoreCase("metric"))
            	altitude = Integer.toString(alt);
            else
            	// Convert altitude in meters to feet
            	altitude = Integer.toString((int)(alt * (float)METRES_TO_FEET));            	
    
            accuracy = Integer.toString(net.rim.device.api.util.MathUtilities.round(qCoord.getHorizontalAccuracy())) +
                "h/" + Integer.toString(net.rim.device.api.util.MathUtilities.round(qCoord.getVerticalAccuracy())) + "v";
            
            /*
            PersistableLocation lkl = getLastKnownLocation();
            if (lkl.isValid()) {
            	double elapsedSeconds = (double)((timestamp.longValue() - lkl.timestamp.longValue()) / 1000);
            	// travel always increments by whole metres to avoid "creep"
            	double avgSpeed = Math.floor((Float.parseFloat(lkl.speed) + location.getSpeed()) / 2);
            	travelBySpeed = new Double(Math.floor(lkl.travelBySpeed.doubleValue() + (avgSpeed * elapsedSeconds)));
                if (Options.getStringOption(Options.UNITS).equalsIgnoreCase("metric"))
                	distLandBySpeed = Integer.toString(travelBySpeed.intValue());
                else
                	distLandBySpeed = Integer.toString((int)(travelBySpeed.doubleValue() * METRES_TO_FEET));
            }

            if (lkl.isValid()) {
            	travelByDist = new Double(lkl.travelByDist.doubleValue() + 
            		(distanceFrom(lkl.latitude, lkl.longitude, latitude, longitude)));
                if (Options.getStringOption(Options.UNITS).equalsIgnoreCase("metric"))
                	distLandByDist = Integer.toString(travelByDist.intValue());
                else
                	distLandByDist = Integer.toString((int)(travelByDist.doubleValue() * METRES_TO_FEET));
            }
            */

            Vector trackpoints = getTrackpoints();
            if (trackpoints.size() > 0) {
            	PersistableLocation lastTrackpoint = (PersistableLocation)trackpoints.lastElement();
            	distance = new Double(distanceFrom(lastTrackpoint.latitude, lastTrackpoint.longitude, latitude, longitude));
                if (Options.getStringOption(Options.UNITS).equalsIgnoreCase("metric"))
                	distanceFrom = Integer.toString(distance.intValue());
                else
                	distanceFrom = Integer.toString((int)(distance.doubleValue() * METRES_TO_FEET));
                
                bearing = new Integer(bearingTo(lastTrackpoint.latitude, lastTrackpoint.longitude, latitude, longitude));
                bearingTo = Integer.toString(bearing.intValue());
            }
        }       
    }

    //public QualifiedCoordinates getQualifiedCoordinates() {
    //    QualifiedCoordinates qCoord = new QualifiedCoordinates(latitude.doubleValue(), longitude.doubleValue(), 0, 0, 0);
    //    return qCoord;
    //}
    
    public boolean isValid() {
        return isValid.booleanValue();
    }
    
    public static PersistableLocation getLastKnownLocation() {
        PersistableLocation pLocation = new PersistableLocation();
        if (lkpPersistData != null && lkpPersistData.getContents() != null)
            pLocation = (PersistableLocation)lkpPersistData.getContents();
        return pLocation;
    }    

    public static Vector getTrackpoints() {
        Vector trackpoints = new Vector(1);
        if (trackpointsPersistData != null && trackpointsPersistData.getContents() != null)
            trackpoints = (Vector)trackpointsPersistData.getContents();
        return trackpoints;
    }
    
    public static double distanceFrom(Double lat1, Double lon1, Double lat2, Double lon2) {
    	// Haversine formula
    	double lt1 = Math.toRadians(lat1.doubleValue());
    	double lt2 = Math.toRadians(lat2.doubleValue());
    	double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
    	double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

    	double a = (Math.sin(dLat/2) * Math.sin(dLat/2)) +
    		(Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lt1) *	Math.cos(lt2));
    	double c = 2 * MathUtilities.atan2(Math.sqrt(a), Math.sqrt(1-a));
    	c = c * EARTH_RADIUS_METRES;
    	
    	/*
    	/*
    	double dLon = Math.abs(lon2.doubleValue() - lon1.doubleValue());
    	double dLat = Math.abs(lat2.doubleValue() - lat1.doubleValue());
    	// Pythagorean formula - good for short distances
    	double x = dLon * Math.cos((lat1.doubleValue() + lat2.doubleValue()) / 2);
    	double y = dLat;
    	double distance = Math.sqrt((x * x) + (y * y)) * EARTH_RADIUS_METRES;
    	
    	double d = XMath.acos(Math.sin(lat1.doubleValue()) * Math.sin(lat2.doubleValue()) + 
                Math.cos(lat1.doubleValue()) * Math.cos(lat2.doubleValue()) *
                Math.cos(lon2.doubleValue() - lon1.doubleValue()));
    	d = d  * EARTH_RADIUS_METRES;
    	*/
    	
    	// times the radius of the earth in miles
    	//double dist = c * 3958.75;
    	// convert miles to metres  
    	//return dist * 1609.344;
    	// or just use metres    	
    	return c;
    }

    public static int bearingTo(Double lat1, Double lon1, Double lat2, Double lon2) {
    	double lt1 = Math.toRadians(lat1.doubleValue());
    	double lt2 = Math.toRadians(lat2.doubleValue());
    	double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
    	
    	double y = Math.sin(dLon) * Math.cos(lt2);
    	double x = (Math.cos(lt1) * Math.sin(lt2)) - (Math.sin(lt1) * Math.cos(lt2) * Math.cos(dLon));
    	double bearing = Math.toDegrees(MathUtilities.atan2(y, x));
    	bearing = (bearing + 180) % 360;
    	return (int)MathUtilities.round(bearing);
    }
} 
