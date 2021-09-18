package com.example.localens;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.example.localens.API.APIinterface;
import com.example.localens.API.Retroclient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.localens.databinding.ActivityMapsBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationManager locationMangaer = null;
    public  double latitude,longitude;
    com.google.android.material.floatingactionbutton.FloatingActionButton floatingActionButton;
    data_model data;
    int map_flag = 0;
    APIinterface apIinterface;
    String country;
    LinearLayout bottomSheet;
    BottomSheetBehavior bottomSheetBehavior ;
    TextView mark_desc,mark_ad,mark_name;
    ImageView mark_image;
    byte[] imageBytes;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        data = new data_model();
        bottomSheet= findViewById(R.id.bottomdrawer);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mark_name = findViewById(R.id.mark_name);
        mark_desc = findViewById(R.id.mark_desc);
        mark_ad = findViewById(R.id.mark_ad);
        mark_image = findViewById(R.id.mark_image);


        apIinterface = Retroclient.retroinit();

        floatingActionButton = findViewById(R.id.addplace);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(MapsActivity.this, AddPlace.class);
                startActivity(intent);

            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void toogglebottomdrawr() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

    }

    private void checkPermissions() {

        String permission[] = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permission, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                checkPermissions();
            }
            Log.d("Log1","helo");

            locationMangaer.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, new MyLocationListener());

            }
        }

    @Override
    public boolean onMarkerClick(@NonNull @NotNull Marker marker) {
        int pos = (int) marker.getTag();
        String name = current_locality.localities.get(pos).getName();
        String adr = current_locality.localities.get(pos).getAddress();
        String des = current_locality.localities.get(pos).getDescription();

        mark_name.setText(name);
        mark_ad.setText(adr);
        mark_desc.setText(des);
        toogglebottomdrawr();
        data_model Dat = new data_model();
        Dat.setLatitude(current_locality.localities.get(pos).getLatitude());
        Dat.setLongitude(current_locality.localities.get(pos).getLongitude());

        Call<String> c = apIinterface.fetch_image(Dat);
        c.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.code()==200){
                    String imgstr = response.body();
                    Log.d("img",imgstr);
                    imageBytes = Base64.decode(imgstr, Base64.DEFAULT);
                    Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    mark_image.setImageBitmap(decodedImage);

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
        return false;
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            longitude = loc.getLongitude();
            latitude = loc.getLatitude();
//            Log.d("loc2", longitude + "");
//            Log.d("loc2", latitude+"");
            current_locality.latitu = latitude;
            current_locality.longi = longitude;
            if(map_flag == 0) {
                fetch_locality(latitude,longitude);
                onMapReady(mMap);
                map_flag = 1;
            }


        }
        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);


        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.removemark));

            Log.d("loc3", longitude + "");
            locationMangaer = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            checkPermissions();

            // Add a marker in Sydney and move the camera


            LatLng india = new LatLng(latitude, longitude);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(india).title("Marker in Sydney")
            .icon(BitmapFromVector(getApplicationContext(), R.drawable.target)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(india));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(india, 17));




    }
    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    void fetch_locality(double latitude,double longitude) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                country = address.getCountryName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(country.length()!=0)
            data.setCountry(country);
        else{
            Toast.makeText(this, "could not locate your location", Toast.LENGTH_SHORT).show();
        }
        Call<ArrayList<data_model>> c = apIinterface.fetch_data(data);
        c.enqueue(new Callback<ArrayList<data_model>>() {
            @Override
            public void onResponse(Call<ArrayList<data_model>> call, Response<ArrayList<data_model>> response) {
                if(response.code()==200){
                    ArrayList<data_model> temp = response.body();
                    if(temp.size()!=0){
                        current_locality.localities = temp;
                        putmarkers();
                        Log.d("list",current_locality.localities+"");
                    }
                    else{
                        Toast.makeText(MapsActivity.this,"No localities nearby",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(MapsActivity.this,"couldn't fetch localities",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ArrayList<data_model>> call, Throwable t) {
                Toast.makeText(MapsActivity.this,"server error",Toast.LENGTH_SHORT).show();
            }
        });
    }
    void putmarkers(){
        for(int i = 0 ; i < current_locality.localities.size() ; i++) {

            createMarker(current_locality.localities.get(i).getLatitude(), current_locality.localities.get(i).getLongitude(), current_locality.localities.get(i).getName(),i);
        }

    }
    public Marker createMarker(String lati, String longi, String title,int id) {

        Double la = Double.parseDouble(lati);
        Double lo = Double.parseDouble(longi);

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(la, lo))
                .anchor(0.5f, 0.5f)
                .title(title)
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.pinpoint)));
        marker.setTag(id);
        return  marker;
    }

}