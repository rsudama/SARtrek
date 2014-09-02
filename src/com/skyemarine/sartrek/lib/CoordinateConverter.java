package com.skyemarine.sartrek.lib;

/**
 * This class contains the implementation of published algorithms for converting
 * latitude and longitude (geographic coordinates) to Universal Transverse Mercator (UTM)
 * coordinates as used on topographic maps and in search and rescue operations.
 * 
 * Ellipsoids for NAD 27 and NAD 83 added by me, based on Clarke 1866 and GRS 1980 respectively.
 */
public class CoordinateConverter
{
    // Conversion factors for degrees and radians
    static final double DEG2RAD = 0.01745329;
    //private static final double RAD2DEG = 57.29577951;

    static final double[] EQUATORIAL_RADIUS = {
            6377563, 6378160, 6377397, 6377484, 6378206,
            6378249, 6377276, 6378166, 6378150, 6378160, 6378137,
            6378200, 6378270, 6378388, 6378245, 6377340, 6377304,
            6378155, 6378160, 6378165, 6378145, 6378135, 6378137,
            6378206, 6378137 };
    static final double[] SQUARE_OF_ECCENTRICITY = {
            .006670540, .006694542, .006674372, .006674372, .006768658,
            .006803511, .006637847, .006693422, .006693422, .006694605, .006694380,
            .006693422, .006722670, .006722670, .006693422, .006670540, .006637847,
            .006693422, .006694542, .006693422, .006694542, .006694318, .006694380,
            .006768658, .006694380 };
    static final String[] ELLIPSOID_NAME = {
            "Airy", "Australian National", "Bessel 1841", "Bessel 1841 (Nambia)", "Clarke 1866",
            "Clarke 1880", "Everest", "Fischer 1960 (Mercury)", "Fischer 1968", "GRS 1967", "GRS 1980",
            "Helmert 1906", "Hough", "International", "Krassovsky", "Modified Airy", "Modified Everest",
            "Modified Fischer 1960", "South American 1969", "WGS 60", "WGS 66", "WGS 72", "WGS 84",
            "NAD 27", "NAD 83" };
 
    /**
     * Convert latitude and longitude to UTM coordinates using equations from USGS Bulletin 1532.
     * Lat and Long are in decimal degrees.
     * North latitudes are positive, South latitudes are negative
     * East longitudes are positive, West longitudes are negative. 
     */
    public static String LLtoUTM(String datum, double Lat, double Long) {
        int ReferenceEllipsoid = 0;
        for (; ReferenceEllipsoid < ELLIPSOID_NAME.length; ReferenceEllipsoid++) {
            if (ELLIPSOID_NAME[ReferenceEllipsoid].equals(datum))
                break;
        }
        if (ReferenceEllipsoid == ELLIPSOID_NAME.length)
            return ("Invalid reference ellipsoid name: " + datum);

        double a = EQUATORIAL_RADIUS[ReferenceEllipsoid];
        double eccSquared = SQUARE_OF_ECCENTRICITY[ReferenceEllipsoid];
        double k0 = 0.9996;
 
        double LongOrigin;
        double eccPrimeSquared;
        double N, T, C, AA, M;
 
        // Make sure the longitude is between -180.00 .. 179.9
        double LongTemp = (Long+180)-Math.floor((Long+180)/360)*360-180; // -180.00 .. 179.9;
        double LatRad = Lat*DEG2RAD;
        double LongRad = LongTemp*DEG2RAD;
        double LongOriginRad;
        double ZoneNumber = Math.floor((LongTemp + 180)/6) + 1;
 
        if( Lat >= 56.0 && Lat < 64.0 && LongTemp >= 3.0 && LongTemp < 12.0 )
            ZoneNumber = 32;
        // Special zones for Svalbard
        if( Lat >= 72.0 && Lat < 84.0 ) {
            if  ( LongTemp >= 0.0  && LongTemp <  9.0 )
                ZoneNumber = 31;
            else if( LongTemp >= 9.0  && LongTemp < 21.0 )
                ZoneNumber = 33;
            else if(LongTemp >= 21.0 && LongTemp < 33.0 )
                ZoneNumber = 35;
            else if(LongTemp >= 33.0 && LongTemp < 42.0 )
                ZoneNumber = 37;
        }
 
        LongOrigin = (ZoneNumber - 1)*6 - 180 + 3;  //+3 puts origin in middle of zone
        LongOriginRad = LongOrigin * DEG2RAD;
 
        // Compute the UTM Zone from the latitude and longitude
        eccPrimeSquared = (eccSquared)/(1-eccSquared);
 
        N = a/Math.sqrt(1-eccSquared*Math.sin(LatRad)*Math.sin(LatRad));
        T = Math.tan(LatRad)*Math.tan(LatRad);
        C = eccPrimeSquared*Math.cos(LatRad)*Math.cos(LatRad);
        AA = Math.cos(LatRad)*(LongRad-LongOriginRad);
        M = (1 - eccSquared/4 - 3*eccSquared*eccSquared/64 - 5*eccSquared*eccSquared*eccSquared/256)*LatRad;
        M -= (3*eccSquared/8 + 3*eccSquared*eccSquared/32 + 45*eccSquared*eccSquared*eccSquared/1024)*Math.sin(2*LatRad);
        M += (15*eccSquared*eccSquared/256 + 45*eccSquared*eccSquared*eccSquared/1024)*Math.sin(4*LatRad);
        M -= (35*eccSquared*eccSquared*eccSquared/3072)*Math.sin(6*LatRad);
        M *= a;
 
        double UTMEasting = (k0*N*(AA+(1-T+C)*AA*AA*AA/6+
            (5-18*T+T*T+72*C-58*eccPrimeSquared)*AA*AA*AA*AA*AA/120)+ 500000.0);
        double UTMNorthing = (k0*(M+N*Math.tan(LatRad)*(AA*AA/2+(5-T+9*C+4*C*C)*AA*AA*AA*AA/24 +
            (61-58*T+T*T+600*C-330*eccPrimeSquared)*AA*AA*AA*AA*AA*AA/720)));
        if (Lat < 0)
            UTMNorthing += 10000000.0; // 10000000 meter offset for southern hemisphere
        String UTMZone = ((int)ZoneNumber)+UTMLetterDesignator(Lat);

        String easting = Integer.toString((int)UTMEasting);
        while (easting.length() < 7)
            easting = "0" + easting;
        String northing = Integer.toString((int)UTMNorthing);
        while (northing.length() < 7)
            northing = "0" + northing;
        return UTMZone + " " + easting + " " + northing;
    }
 
    /**
     * Determine the correct UTM letter designator for the given latitude.
     * Returns "Z" if latitude is outside the UTM limits of 84N to 80S.
     */
    private static String UTMLetterDesignator(double Lat) { 
        if ((84 >= Lat) && (Lat >= 72))       return "X";
        else if ((72 > Lat) && (Lat >= 64))   return "W";
        else if ((64 > Lat) && (Lat >= 56))   return "V";
        else if ((56 > Lat) && (Lat >= 48))   return "U";
        else if ((48 > Lat) && (Lat >= 40))   return "T";
        else if ((40 > Lat) && (Lat >= 32))   return "S";
        else if ((32 > Lat) && (Lat >= 24))   return "R";
        else if ((24 > Lat) && (Lat >= 16))   return "Q";
        else if ((16 > Lat) && (Lat >= 8))    return "P";
        else if ((8 > Lat) && (Lat >= 0))     return "N";
        else if ((0 > Lat) && (Lat >= -8))    return "M";
        else if ((-8 > Lat) && (Lat >= -16))  return "L";
        else if ((-16 > Lat) && (Lat >= -24)) return "K";
        else if ((-24 > Lat) && (Lat >= -32)) return "J";
        else if ((-32 > Lat) && (Lat >= -40)) return "H";
        else if ((-40 > Lat) && (Lat >= -48)) return "G";
        else if ((-48 > Lat) && (Lat >= -56)) return "F";
        else if ((-56 > Lat) && (Lat >= -64)) return "E";
        else if ((-64 > Lat) && (Lat >= -72)) return "D";
        else if ((-72 > Lat) && (Lat >= -80)) return "C";
        else return "Z"; //This is here as an error flag to show that the Latitude is outside the UTM limits
    }

    // Extract grid coordinates from a string UTM value. UTM is in the format:
    //
    //  "ZONE easting northing" (e.g. "10U 3955247 0403712")
    //
    // This method would return "552 037"
    //
    public static String getGridCoordinates(String utm, boolean round) {
        // Get easting
        // Start at the 3rd digit of the easting value
    	int eGridStart = utm.indexOf(' ') + 3;
    	// Extract the next 3 digits
        int eGrid = Integer.parseInt(utm.substring(eGridStart, eGridStart + 3));
        // Also get the remaining 2 digits
        int e2 = Integer.parseInt(utm.substring(eGridStart + 3, eGridStart + 5));
        
        // Get northing
        // Start at the 3rd digit of the northing value
        int nGridStart = utm.lastIndexOf(' ') + 3;
        // Extract the next 3 digits
        int nGrid = Integer.parseInt(utm.substring(nGridStart, nGridStart + 3));
        // Also get the remaining 2 digits
        int n2 = Integer.parseInt(utm.substring(nGridStart + 3, nGridStart + 5));
        
        // Round up the grid coordinates if required
        if (round) {
            if (e2 >= 50)
                eGrid++;
            if (n2 >= 50)
                nGrid++;
        }
        
        // Insure both values have 3 digits using leading zeroes
        String eString = Integer.toString(eGrid);
        while (eString.length() < 3)
        	eString = "0" + eString;
        String nString = Integer.toString(nGrid);
        while (nString.length() < 3)
        	nString = "0" + nString;
        
        return eString + "  " + nString;
    }
    
	public static String LLtoMGRS(String datum, double latitude, double longitude) {
		String utm = LLtoUTM(datum, latitude, longitude);

		String eString = utm.substring(utm.indexOf(' '), utm.lastIndexOf(' '));
		double easting = Double.parseDouble(eString);
		eString = eString.substring(eString.length() - 5);

		String nString = utm.substring(utm.lastIndexOf(' '));
		double northing = Double.parseDouble(nString);
		nString = nString.substring(nString.length() - 5);

		String latZone = getLatZone(latitude);
		String lonZone = getLonZone(longitude);

		String digraph1 = getDigraph1(Integer.parseInt(lonZone), easting);
		String digraph2 = getDigraph2(Integer.parseInt(lonZone), northing);

		String mgrs = lonZone + latZone + digraph1 + digraph2 + eString + nString;
		return mgrs;
	}

	private static String getLatZone(double latitude) {
		final char[] negLetters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M' };
		final int[] negDegrees = { -90, -84, -72, -64, -56, -48, -40, -32, -24, -16, -8 };
		final char[] posLetters = { 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Z' };
		final int[] posDegrees = { 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

		int latIndex = -2;
		int lat = (int) latitude;

		if (lat >= 0) {
			int len = posLetters.length;
			for (int i = 0; i < len; i++) {
				if (lat == posDegrees[i]) {
					latIndex = i;
					break;
				}

				if (lat > posDegrees[i])
					continue;
				else {
					latIndex = i - 1;
					break;
				}
			}
		} 
		else {
			int len = negLetters.length;
			for (int i = 0; i < len; i++) {
				if (lat == negDegrees[i]) {
					latIndex = i;
					break;
				}

				if (lat < negDegrees[i]) {
					latIndex = i - 1;
					break;
				} 
				else
					continue;
			}
		}

		if (latIndex == -1)
			latIndex = 0;
		if (lat >= 0) {
			if (latIndex == -2)
				latIndex = posLetters.length - 1;
			return String.valueOf(posLetters[latIndex]);
		} 
		else {
			if (latIndex == -2)
				latIndex = negLetters.length - 1;
			return String.valueOf(negLetters[latIndex]);
		}
	}

	private static String getLonZone(double longitude) {
		double lonZone = 0;
		if (longitude < 0.0)
			lonZone = ((180.0 + longitude) / 6) + 1;
		else
			lonZone = (longitude / 6) + 31;
		String lzString = String.valueOf((int)lonZone);
		if (lzString.length() == 1)
			lzString = "0" + lzString;
		return lzString;
	}

	private static String getDigraph1(int lonZone, double easting) {
		final String[] digraph1 = { "A", "B", "C", "D", "E", "F", "G", "H", "J",
			"K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

		int a1 = lonZone;
		double a2 = 8 * ((a1 - 1) % 3) + 1;
		double a3 = easting;
		double a4 = a2 + ((int) (a3 / 100000)) - 1;
		return digraph1[(int)Math.floor(a4) - 1];
	}

	private static String getDigraph2(int lonZone, double northing) {
		final String[] digraph2 = { "V", "A", "B", "C", "D", "E", "F", "G", "H", "J",
			"K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V" };

		int a1 = lonZone;
		double a2 = 1 + 5 * ((a1 - 1) % 2);
		double a3 = northing;
		double a4 = (a2 + ((int) (a3 / 100000)));
		a4 = (a2 + ((int) (a3 / 100000.0))) % 20;
		if (a4 < 0)
			a4 = a4 + 19;
		return digraph2[(int)Math.floor(a4)];
	}
}
