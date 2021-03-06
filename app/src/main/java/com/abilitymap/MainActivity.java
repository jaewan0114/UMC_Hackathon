package com.abilitymap;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.graphics.Color;
import android.graphics.PointF;
import android.location.Address;
import android.content.DialogInterface;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Overlay.OnClickListener, SetMarker {
    private GpsTracker gpsTracker;
    private NaverMap naverMap;
    private FusedLocationSource locationSource;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;

    private Location mLastlocation = null;
    private double speed, calSpeed, getSpeed;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    List<DTO> items;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    List<LatLng> latLngList = new ArrayList<>();

//    List<Double> latitudeList = new ArrayList<Double>();
//    List<Double> longitudeList = new ArrayList<Double>();
//
//    double LNG = Double.parseDouble(latitudeList.toString());
//    double LAT = Double.parseDouble(longitudeList.toString());
//
//
//try {
//
//        JSONObject Land = new JSONObject(result);
//        JSONArray jsonArray = Land.getJSONArray("Response");
//        for(int i = 0 ; i<jsonArray.length(); i++){
//            JSONObject subJsonObject = jsonArray.getJSONObject(i);
//
//            Double sLAT = subJsonObject.getDouble("latitude"); //String sLAT = subJsonObject.getString("latitude");
//            Double sLNG = subJsonObject.getDouble("longitude"); //String sLNG = subJsonObject.getString("longitude");
//
//            latitudeList.add(sLAT);
//            longitudeList.add(sLNG);
//        }
//    } catch (
//    JSONException e) {
//        e.printStackTrace();
//    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState){ //?????? ????????? ?????? ?????? ?????? ?????????.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);

        if(mapFragment ==null ){
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);


        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }
        locationSource = new FusedLocationSource(this,LOCATION_PERMISSION_REQUEST_CODE);

        items = new ArrayList<>();
        // ?????????



    }





//
    @Override
    public void onRequestPermissionsResult ( int requestCode,
                                             @NonNull String[] permissions, @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE  && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                //?????? ?????? ????????? ??? ??????
                ;
            } else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.2 ?????? ????????? ????????????.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainActivity.this, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????.", Toast.LENGTH_LONG).show();
                    finish();

                } else {

                    Toast.makeText(MainActivity.this, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ", Toast.LENGTH_LONG).show();

                }
            }

            if (grandResults.length > 0 && grandResults[0] == PackageManager.PERMISSION_GRANTED) {
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }


    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        ImageButton Call_button = (ImageButton)findViewById(R.id.call_button);
        ImageButton Report_button = (ImageButton)findViewById(R.id.repot_button);
        Call_button.setVisibility(View.INVISIBLE);
        Report_button.setVisibility(View.INVISIBLE);

        if(overlay instanceof Marker){
//            Toast.makeText(this.getApplicationContext(),"?????????????????????",Toast.LENGTH_LONG).show();

            LocationDetailFragment infoFragment = new LocationDetailFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.map, infoFragment).commit();
            naverMap.setOnMapClickListener(new NaverMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                    getSupportFragmentManager().beginTransaction().remove(infoFragment).commit();
                    Call_button.setVisibility(View.VISIBLE);
                    Report_button.setVisibility(View.VISIBLE);
                    Log.d("click event","onMapClick");
                }
            });


            return true;
        }
        return false;

    }




    void checkRunTimePermission() {
        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)


            // 3.  ?????? ?????? ????????? ??? ??????


        } else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.

            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Toast.makeText(MainActivity.this, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }
    }



    public String getCurrentAddress( double latitude, double longitude) {
        //????????????... GPS??? ????????? ??????
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    8);
        } catch (IOException ioException) {
            //???????????? ??????
            Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
            return "???????????? ????????? ????????????";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";

        }

        Address address = addresses.get(0);

        return address.getAddressLine(0).toString()+"\n";

    }



    //??????????????? GPS ???????????? ?????? ????????????
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);

                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, id);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS ????????? ?????????");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    private void UpdateCircle(double x, double y){
        CircleOverlay circle = new CircleOverlay();
        circle.setCenter(new LatLng(x, y));
        circle.setRadius(30);
        circle.setColor(Color.parseColor("#30FF7B00"));
        circle.setOutlineColor(Color.parseColor("#30FF7B00"));
        circle.setMap(naverMap);
        circle.setMinZoom(15);

        Marker marker = new Marker();
        marker.setPosition(new LatLng(x,y));
        marker.setIcon(OverlayImage.fromResource(R.drawable.invalid_name));
        marker.setMinZoom(8);
        marker.setMaxZoom(15);
        marker.setWidth(80);
        marker.setHeight(80);
        marker.setMap(naverMap);

        Marker marker2 = new Marker();
        marker2.setPosition(new LatLng(x,y));
        marker2.setIcon(OverlayImage.fromResource(R.drawable.invalid_name));
        marker2.setMinZoom(16);
        marker.setMaxZoom(15);
        marker2.setWidth(80);
        marker2.setHeight(80);
        marker2.setMap(naverMap);


    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
//        LatLng initialPosition = new LatLng(mLastlocation);
//        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
//        naverMap.moveCamera(cameraUpdate);
        naverMap.setMaxZoom(18.0);
        naverMap.setMinZoom(8.0);

        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);



        final TextView location_text = (TextView)findViewById(R.id.location_text);


        latLngList.add(new LatLng(37.300909685747236,126.84036999665139 )); //????????????
        latLngList.add(new LatLng(37.30092006963348,126.84651707027692  )); //????????????
        latLngList.add(new LatLng(37.30080820319068,126.84365805640256  )); //119
        latLngList.add(new LatLng(37.30030995420335,126.8450464027002  )); //???????????????
        latLngList.add(new LatLng(37.299298647544646,126.84512742919043   )); //???????????????
        latLngList.add(new LatLng(37.30578504908008,126.84432454144101    )); //??????????????????


        latLngList.add(new LatLng(37.29964234222025,126.84612490571303   )); //?????????
        latLngList.add(new LatLng(37.299632110432704,126.8469200877772   )); //?????????
        latLngList.add(new LatLng(37.29891910144883,126.84600231252934    )); //?????????
        latLngList.add(new LatLng(37.298322034553244,126.84590202160551     )); //?????????
        latLngList.add(new LatLng(37.30157589850863,126.8450381659243      )); //?????????

        latLngList.add(new LatLng(37.30012291575613,126.83825685541521     )); //??????
        latLngList.add(new LatLng(37.30078496095471,126.843116709908      )); //??????

        latLngList.add(new LatLng(37.298925701379005,126.84588105222103       )); //???????????????

        latLngList.add(new LatLng(37.298495139953886,126.83723115856097        )); //?????????
        latLngList.add(new LatLng(37.30175911322991,126.84389859082773        )); //?????????
        latLngList.add(new LatLng(37.30021510929659,126.8448661337656        )); //?????????
        latLngList.add(new LatLng(37.29970314731508,126.8461135029482        )); //?????????
        latLngList.add(new LatLng(37.30160083561462,126.84515936590596        )); //?????????


        setMarker(0,latLngList,"slope",naverMap);
        setMarker(1,latLngList,"slope",naverMap);
        setMarker(2,latLngList,"slope",naverMap);
        setMarker(3,latLngList,"slope",naverMap);
        setMarker(4,latLngList,"slope",naverMap);
        setMarker(5,latLngList,"slope",naverMap);

        setMarker(6,latLngList,"slope",naverMap);
        setMarker(7,latLngList,"slope",naverMap);
        setMarker(8,latLngList,"slope",naverMap);
        setMarker(9,latLngList,"slope",naverMap);
        setMarker(10,latLngList,"slope",naverMap);

        setMarker(11,latLngList,"slope",naverMap);
        setMarker(12,latLngList,"slope",naverMap);

        setMarker(13,latLngList,"charger",naverMap);

        setMarker(14,latLngList,"wheelchair",naverMap);
        setMarker(15,latLngList,"wheelchair",naverMap);
        setMarker(16,latLngList,"wheelchair",naverMap);
        setMarker(17,latLngList,"wheelchair",naverMap);
        setMarker(18,latLngList,"wheelchair",naverMap);



        //?????? ?????? ??????
        UpdateCircle(37.30155838266366,126.84715868584975 );
        UpdateCircle(37.30731010483543,126.83602493657628 );
        UpdateCircle(37.30314238314502,126.8389891901272  );
        UpdateCircle(37.29787636235218,126.84966999005518);
        UpdateCircle(37.305613496417976,126.84751143174793 );
        UpdateCircle(37.30854279577155,126.841369080322  );

        /*

        Marker marker = new Marker();
        marker.setPosition(latLngList.get(0));
        marker.setMap(naverMap);

        marker.setOnClickListener(this);

*/

        /*
        final TextView textView_lat = findViewById(R.id.lat);
        final TextView textView_lon = findViewById(R.id.lon);
        */
//        final TextView tvGetSpeed = findViewById(R.id.tvGetspeed);
//        final TextView tvCalSpeed = findViewById(R.id.tvCalspeed);


        naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {

            @Override
            public void onLocationChange(@NonNull Location location) {

                gpsTracker = new GpsTracker(MainActivity.this);

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                double deltaTime = 0;

                // getSpeed() ????????? ???????????? ?????? ??????(m/s -> km/h)
                getSpeed = Double.parseDouble(String.format("%.3f", location.getSpeed() * 3.6));

                // ?????? ????????? ???????????? ????????? ?????? ????????? ?????? ?????? ??????
                if(mLastlocation != null){
                    deltaTime = (location.getTime() - mLastlocation.getTime());
                    // ?????? ??????(??????=ms, ??????=m -> km/h)
                    speed = (mLastlocation.distanceTo(location) / deltaTime) * 3600;
                    calSpeed = Double.parseDouble(String.format("%.3f", speed));
                }
                //??????????????? ?????? ????????? ??????
                mLastlocation = location;

                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                String address = getCurrentAddress(latitude, longitude);


                String addrCut[] = address.split(" ");
                location_text.setText(addrCut[1]+" "+addrCut[2]+" "+addrCut[3]);




                String lat_str = Double.toString(latitude);
                String lon_str = Double.toString(longitude);

                /*
                textView_lat.setText(lat_str);
                textView_lon.setText(lon_str);
                 */
                UiSettings uiSettings = naverMap.getUiSettings();
                uiSettings.setCompassEnabled(true);
                uiSettings.setScaleBarEnabled(true);
                uiSettings.setZoomControlEnabled(false); //?????? ?????????
                uiSettings.setLocationButtonEnabled(true);

                LocationButtonView locationButtonView = findViewById(R.id.navermap_location_button);
                locationButtonView.setMap(naverMap);

                String gs_str = Double.toString(getSpeed);
                String cs_str = Double.toString(calSpeed);


                //api ???????????? ??????

                Thread th = new Thread(String.valueOf(MainActivity.this));
                new Thread(() -> {
                    th.start(); // network ??????, ??????????????? xml??? ???????????? ??????
                }).start();



                try {
                    StringBuffer sb = new StringBuffer();
                    URL url = new URL("http://3.35.237.29/total");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    // ??? ????????? source??? ????????????.
                    if (conn != null) {
                        conn.setConnectTimeout(5000);
                        conn.setUseCaches(false);

                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                            while (true) {
                                String line = br.readLine();
                                if (line == null)
                                    break;
                                sb.append(line + "\n");
                            }
                            Log.d("myLog", sb.toString());
                            br.close();
                        }
                        conn.disconnect();
                    }

                    // ????????? source??? JSONObject??? ????????????.
                    JSONObject jsonObj = new JSONObject(sb.toString());
                    JSONArray jArray = (JSONArray) jsonObj.get("result");

                    // 0?????? JSONObject??? ?????????
                    JSONObject row = jArray.getJSONObject(0);
                    DTO dto = new DTO();
                    dto.setName(row.getString("name"));
                    dto.setTel(row.getString("tel"));
                    items.add(dto);

                    Log.d("????????????1 : ", row.getString("name"));
                    Log.d("????????????2 : ", row.getString("tel"));

                    // 1?????? JSONObject??? ?????????
                    JSONObject row2 = jArray.getJSONObject(1);
                    DTO dto2 = new DTO();
                    dto2.setName(row2.getString("name"));
                    dto2.setTel(row2.getString("tel"));
                    items.add(dto2);

                    Log.d("????????????3 : ", row2.getString("name"));
                    Log.d("????????????4 : ", row2.getString("tel"));

                }catch (Exception e){
                    e.printStackTrace();

                }

//                Toast.makeText(getApplicationContext(), ""+items+"ek", Toast.LENGTH_SHORT).show();

            }

        });


    }



}

