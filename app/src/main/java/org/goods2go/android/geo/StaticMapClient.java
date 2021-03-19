package org.goods2go.android.geo;

import android.graphics.drawable.Drawable;

import com.goods2go.models.util.GeoCoordinates;

import java.io.InputStream;
import java.net.URL;

public class StaticMapClient {

    public static Drawable getShipmentMap(String coordinates){
        return getMap(coordinates, 16, 500);
    }

    public static Drawable getMap(String coordinates, int zoom, int size){
        double[] coords = GeoCoordinates.fromString(coordinates);
        String url = "http://maps.google.com/maps/api/staticmap?center="
                + coords[0] + "," + coords[1] + "&zoom=" + zoom + "&size=" + size + "x" + size
                + "&markers=color:blue%7Clabel:%7C" + coords[0] + "," + coords[1];
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            return Drawable.createFromStream(is, "map");
        } catch (Exception e) {
            return null;
        }
    }
}
