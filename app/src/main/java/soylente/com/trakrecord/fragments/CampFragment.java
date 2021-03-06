package soylente.com.trakrecord.fragments;


import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;

import soylente.com.trakrecord.DAO.Camp;
import soylente.com.trakrecord.R;


public class CampFragment extends Fragment implements OnMapReadyCallback {

    private Location mLocation;
    private MapView campMap;
    private Camp camp;
    private Location campLoc;
    private TextView distanceFrom;
    private boolean locationScanning = false;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener mLocationListener;

    private long UPDATE_INTERVAL = 2000;
    private long FASTEST_INTERVAL = 100;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        camp = (Camp) getArguments().getParcelable("CAMP");
            campLoc = new Location("");
        campLoc.setLatitude(camp.getLatitude());
        campLoc.setLongitude(camp.getLongitude());

        mLocationListener = new com.google.android.gms.location.LocationListener() {

            @Override
            public void onLocationChanged(Location currentLocation) {
                mLocation = currentLocation;
                setDistanceFrom();
            }
        };

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                    @Override
                    public void onConnected(Bundle arg0) {
                        // Get last known recent location.
                        try {
                            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            startLocationUpdates();
                        } catch (SecurityException e) {
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                }).build();
        mGoogleApiClient.connect();
    }

    protected void startLocationUpdates() throws SecurityException {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, mLocationListener);
        locationScanning = true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camp, container, false);
        TextView title = (TextView) view.findViewById(R.id.camp_title);
        title.setText(camp.getCampName());
        distanceFrom = (TextView) view.findViewById(R.id.camp_distance_label);
        TextView foundLabel = (TextView) view.findViewById(R.id.found_label);
        setFoundLabel(foundLabel);
        campMap = (MapView) view.findViewById(R.id.camp_view);
        campMap.onCreate(savedInstanceState);
        campMap.getMapAsync(this);

        return view;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(camp.getCoords()));

        googleMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(camp.getCoords()));
        campMap.onResume();
    }

    //Convert to Image (badge)
    void setFoundLabel(TextView txt) {
        String s;
        if(camp.getIsFound())
            s = "YOU FOUND THIS!!!!!!!!!!!!!!!!";
        else
            s = "NAH, AIN'T FOUND YET";
        txt.setText(s);
    }

    void setDistanceFrom() {
        float distance = -1;
        if (mLocation != null && camp.getCoords() != null)
            distance = mLocation.distanceTo(campLoc) / 1000;
        if(distance != -1 && distanceFrom != null && this.isVisible()) {
            distanceFrom.setText("Distance from: " + String.format("%.2f", distance) + " km");
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if(locationScanning) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
        }
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();

    }

}