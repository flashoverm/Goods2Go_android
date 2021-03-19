package org.goods2go.android.ui.fragment.deliverer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.goods2go.models.ShipmentAnnouncement;
import com.goods2go.models.ShipmentSubscription;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.goods2go.android.R;
import org.goods2go.android.geo.GeoClient;
import org.goods2go.android.geo.MarkerAdapter;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.dialog.FilterAnnouncementsDialog;
import org.goods2go.android.ui.fragment.NetworkFragment;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchAnnouncementFragment extends NetworkFragment
        implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        FilterAnnouncementsDialog.ApplyFilterListener {

    private static final int FILTER_DIALOG_CODE = 4711;
    private static final float ZOOM_ANNOUNCEMENTS = 12;
    private static final float WIDER_ZOOM_ANNOUNCEMENTS = 8;
    private static final float SQRT2D2 = 0.7071067811865475f;
    private static final float DISPERSION = 0.005f;

    private MapView mapView;
    private GoogleMap map;
    private HashMap<Integer, LatLng> markerPositions;

    private ShipmentSubscription usedFilter;
    private List<ShipmentAnnouncement> shipmentAnnouncements;

    private GetAnnouncementsTask getAnnouncementsTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deliverer_search, container, false);
        progress = view.findViewById(R.id.progress);
        content = view.findViewById(R.id.content);

        setHasOptionsMenu(true);

        mapView = content.findViewById(R.id.mapView);

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        if(googleAPI.isGooglePlayServicesAvailable(getActivity()) == ConnectionResult.SUCCESS){
            showProgress(true);
            mapView.onCreate(savedInstanceState);
            MapsInitializer.initialize(getActivity());
            mapView.getMapAsync(this);
        } else {
            //TODO error handling
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar_deliverer_search, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_filter:
                showFilterDialog();
                return true;
            case R.id.menu_test_location:
                new GeoClient().getLocation((NetworkActivity)getActivity(), map);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showFilterDialog(){
        FilterAnnouncementsDialog dialog = FilterAnnouncementsDialog.newInstance(usedFilter);
        dialog.setTargetFragment(this, FILTER_DIALOG_CODE);
        dialog.show(getFragmentManager(), FilterAnnouncementsDialog.TAG);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setInfoWindowAdapter(new MarkerAdapter(getActivity()));
        map.setOnInfoWindowClickListener(this);
        markerPositions = new HashMap<>();
        showProgress(false);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(marker.getSnippet() != null
                && marker.getSnippet().equals(getString(R.string.text_detail))
                && marker.getTag() instanceof ShipmentAnnouncement){

            SendRequestFragment sendRequestFragment = SendRequestFragment.newInstance();
            sendRequestFragment.setShipmentAnnouncement((ShipmentAnnouncement)marker.getTag());
            showLowerLevelFragment(sendRequestFragment, true);
        }
    }

    @Override
    public void onFilterApplied(ShipmentSubscription filter) {
        this.usedFilter = filter;

        map.clear();
        markerPositions.clear();

        Marker destination = null;
        if(filter.getDestinationcoordinates() != null){
            LatLng dst = generateMarkerPosition(GeoClient.getLatLng(filter.getDestinationcoordinates()));
            MarkerOptions targetOptions = new MarkerOptions()
                    .title(getString(R.string.text_destination_city))
                    .position(dst)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.target_marker));
            destination = map.addMarker(targetOptions);
            markerPositions.put(dst.hashCode(), dst);
        }

        if(filter.getSourcecoordinates() != null){
            LatLng src = generateMarkerPosition(GeoClient.getLatLng(filter.getSourcecoordinates()));
            MarkerOptions sourceOptions = new MarkerOptions()
                    .title(getString(R.string.text_source_city))
                    .position(src)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.source_marker));
            Marker source = map.addMarker(sourceOptions);
            source.showInfoWindow();
            map.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(source.getPosition(), ZOOM_ANNOUNCEMENTS));
            markerPositions.put(src.hashCode(), src);
        } else if(filter.getDestinationcoordinates() != null){
            destination.showInfoWindow();
            map.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(destination.getPosition(), WIDER_ZOOM_ANNOUNCEMENTS));
        }

        if(getAnnouncementsTask != null){
            return;
        }
        showProgress(true);
        getAnnouncementsTask = new GetAnnouncementsTask();
        getAnnouncementsTask.execute(filter);
    }

    private LatLng generateMarkerPosition(LatLng position){
        int i=0;
        int distanceFactor = 1;
        double latitude;
        double longitude;
        LatLng generatedPosition = position;
        while(markerPositions.containsKey(generatedPosition.hashCode())){
            latitude = position.latitude;
            longitude = position.longitude;
            switch (i){
                case 1:
                    latitude += DISPERSION * distanceFactor;
                    break;
                case 2:
                    longitude += DISPERSION * distanceFactor;
                    break;
                case 3:
                    latitude -= DISPERSION * distanceFactor;
                    break;
                case 4:
                    longitude -= DISPERSION * distanceFactor;
                    break;
                case 5:
                    latitude += DISPERSION * distanceFactor * SQRT2D2;
                    longitude += DISPERSION * distanceFactor * SQRT2D2;
                    break;
                case 6:
                    latitude -= DISPERSION * distanceFactor * SQRT2D2;
                    longitude += DISPERSION * distanceFactor * SQRT2D2;
                    break;
                case 7:
                    latitude -= DISPERSION * distanceFactor * SQRT2D2;
                    longitude -= DISPERSION * distanceFactor * SQRT2D2;
                    break;
                case 8:
                    latitude += DISPERSION * distanceFactor * SQRT2D2;
                    longitude -= DISPERSION * distanceFactor * SQRT2D2;
                    break;
            }
            generatedPosition = new LatLng(latitude, longitude);
            i++;
            if(i == 9){
                distanceFactor++;
                i = 1;
            }
        }
        return generatedPosition;
    }

    private void displayAnnouncements(){
        if(map != null){
            LatLng sourcePosition;
            LatLng generatedPosition;
            MarkerOptions options;
            Marker marker;



            for(ShipmentAnnouncement announcement : shipmentAnnouncements){
                sourcePosition = GeoClient.getLatLng(announcement.getAproxsourcecoordinates());
                generatedPosition = generateMarkerPosition(sourcePosition);
                markerPositions.put(generatedPosition.hashCode(), generatedPosition);

                options = new MarkerOptions()
                        .position(generatedPosition)
                        .title(announcement.getDescription())
                        .snippet(getString(R.string.text_detail))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.package_arrow_black));

                marker = map.addMarker(options);
                marker.setTag(announcement);
            }
        }
    }

    private class GetAnnouncementsTask extends NetworkTask<ShipmentSubscription, Void, Void> {

        @Override
        protected Void runInBackground(ShipmentSubscription... filter) throws NetworkException {
            shipmentAnnouncements = networkClient.getAnnouncements(filter[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getAnnouncementsTask = null;
            showProgress(false);

            if(networkException != null){
                if(networkException.httpError == HttpURLConnection.HTTP_NOT_FOUND){
                    showResult(R.string.error_size_not_existing);
                    return;
                }
                networkException.handleException((NetworkActivity)getActivity());
                return;
            }

            if(shipmentAnnouncements != null){
                displayAnnouncements();
            } else {
                showResult(R.string.error_getting_data);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void getMultibleTestAnnouncements(){
        List<ShipmentAnnouncement> list = new ArrayList<>();

        for(int i=0; i<4; i++){
            ShipmentAnnouncement announcement = new ShipmentAnnouncement();
            announcement.setAproxsourcecoordinates("48.530488, 12.093825");
            list.add(announcement);
            ShipmentAnnouncement announcement2 = new ShipmentAnnouncement();
            announcement2.setAproxsourcecoordinates("48.523648, 12.205363");
            list.add(announcement2);
        }
        shipmentAnnouncements = list;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(48.5441917, 12.1468532), ZOOM_ANNOUNCEMENTS));
    }
}
