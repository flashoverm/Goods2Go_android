package org.goods2go.android.geo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.goods2go.models.Address;
import com.goods2go.models.util.GeoCoordinates;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.util.PermissionHandler;

public class GeoClient {

    private static final int LOCATION_TIMEOUT = 5000;

    public interface LocationListener {
        int PERMISSION_ERROR = 1;
        int LOCATION_NOT_ENABLED = 2;
        int GPS_NOT_ENABLED = 3;
        int NETWORK_NOT_ENABLED = 4;

        void onLocationUpdated(LatLng latLng);
        void onError(int errorCode);
    }

    public static void getLocation(Activity activity, final LocationListener locationListener){
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            final LocationManager locationManager = (LocationManager)
                    activity.getSystemService(Context.LOCATION_SERVICE);

            boolean gps = false;
            boolean network = false;

            try {
                gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch(Exception ex) {}

            try {
                network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ex) {}

            if(!gps && !network){
                locationListener.onError(LocationListener.LOCATION_NOT_ENABLED);
                return;
            }

                //Location locationObject = locationManager.getLastKnownLocation(locationManager
            //        .getBestProvider(new Criteria(), true));

            Looper looper = Looper.myLooper();
            final Handler handler = new Handler(looper);

            final android.location.LocationListener listener = new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location locationObject) {
                    handler.removeCallbacksAndMessages(null);
                    locationListener.onLocationUpdated(new LatLng(
                            locationObject.getLatitude(),
                            locationObject.getLongitude()));
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                }

                @Override
                public void onProviderEnabled(String s) {
                }

                @Override
                public void onProviderDisabled(String s) {
                }
            };
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, looper);

            handler.postDelayed(new Runnable() {
                public void run() {
                    locationManager.removeUpdates(listener);
                    locationListener.onError(0);
                }
            }, LOCATION_TIMEOUT);

        } else {
            PermissionHandler.requestLocationPermission(activity);
            locationListener.onError(LocationListener.PERMISSION_ERROR);
        }
    }

    public static int getErrorMessage(int errorCode){
        switch (errorCode){
            case LocationListener.PERMISSION_ERROR:
                return R.string.error_location_permission;
            case LocationListener.LOCATION_NOT_ENABLED:
                return R.string.error_location_deactivated;
            case LocationListener.GPS_NOT_ENABLED:
                return R.string.error_gps_deactivated;
            case LocationListener.NETWORK_NOT_ENABLED:
                return R.string.error_network_deactivated;
            default:
                return R.string.error_location;
        }
    }

    public static LatLng getLatLng(String coordinatesString){
        double[] coordinates = GeoCoordinates.fromString(coordinatesString);
        return new LatLng(coordinates[0], coordinates[1]);
    }

    public void getLocation(final NetworkActivity activity, final GoogleMap map){
        getLocation(activity, new LocationListener() {
            @Override
            public void onLocationUpdated(LatLng latLng) {
                GetLocationTask getLocationTask = new GetLocationTask(map, activity);
                getLocationTask.execute(latLng);
            }

            @Override
            public void onError(int errorCode) {
                activity.showResult(getErrorMessage(errorCode));
            }
        });
    }

    private class GetLocationTask extends NetworkTask<LatLng, Void, Address> {

        GetLocationTask(GoogleMap map, Context context){
            this.map = map;
            this.context = context;
        }

        private GoogleMap map;
        private Context context;
        private LatLng coords;

        @Override
        protected Address runInBackground(LatLng... params) throws NetworkException{
            coords = params[0];
            return GeocodingClient.getAddressFromCoordinates(coords);
        }

        @Override
        protected void onPostExecute(Address result) {
            if(networkException != null){
                Toast.makeText(context, R.string.error_network, Toast.LENGTH_LONG).show();
            }

            if (result != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(coords, 12));
                MarkerOptions markerOptions = new MarkerOptions().position(coords);
                Marker marker = map.addMarker(markerOptions);
                marker.showInfoWindow();
                marker.setTitle(context.getString(R.string.text_own_location));
                marker.setSnippet(result.getStreet() + " " + result.getStreetno()
                        + ", " + result.getPostcode() + " " + result.getCity());
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.source_marker));
                marker.showInfoWindow();
            } else {
                Toast.makeText(context, R.string.error_getting_data, Toast.LENGTH_LONG).show();
            }
        }
    }
}
