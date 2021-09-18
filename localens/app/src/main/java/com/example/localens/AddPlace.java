package com.example.localens;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.example.localens.API.APIinterface;
import com.example.localens.API.Retroclient;
import com.example.localens.databinding.ActivityAddPlaceBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPlace extends FragmentActivity implements OnMapReadyCallback {

    ImageView imageView;
    private GoogleMap mMap;
    private ActivityAddPlaceBinding binding;
    double latitude, longitude;
    TextView country, locality, addr;
    int flag = 0;
    Marker markerName = null;
    Button add_image, add_place;
    EditText name, desc;
    int IMAGE_REQUEST = 0;
    File imagefile;
    Uri uri;
    String imagestring, place_name, place_desc, place_country, place_locality, place_address,log,lat;
    APIinterface apIinterface;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddPlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        intialization();
    }

    void intialization() {
        country = findViewById(R.id.country);
        locality = findViewById(R.id.locality);
        addr = findViewById(R.id.Address);
        imageView = findViewById(R.id.imagev);
        add_image = findViewById(R.id.add_img);
        add_place = findViewById(R.id.addplace);
        name = findViewById(R.id.name);
        desc = findViewById(R.id.desc);
        apIinterface = Retroclient.retroinit();
        progressDialog = new ProgressDialog(this);

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

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if (flag == 1) {
                    markerName.remove();
                    flag = 0;
                }
                if (flag == 0) {
                    markerName = googleMap.addMarker(new MarkerOptions().position(new LatLng(point.latitude, point.longitude)).title("Title"));
                    //MarkerOptions marker = new MarkerOptions().position(new LatLng(point.latitude, point.longitude)).title("New Marker");
                   // Log.d("mark", point.latitude + "---" + point.longitude);
                    latitude = point.latitude;
                    longitude = point.longitude;
                    getaddress();
                    name.setText("");
                    desc.setText("");
                    flag = 1;
                }
            }
        });
    }

    private void getaddress() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(
                    latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append("\n");
                }
                country.setText(address.getCountryName());
                locality.setText(address.getLocality() + " / " + address.getPostalCode());
                addr.setText(addressList.get(0).getAddressLine(0));

            }
        } catch (IOException e) {
            Log.d("geo", "Unable connect to Geocoder", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void Addimage(View view) {
        String permission[] = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(permission, IMAGE_REQUEST);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == IMAGE_REQUEST) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, 0);
        } else {
            Toast.makeText(getBaseContext(), "cannot upload image", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            try {
                uri = data.getData();
                imagefile = FileUtil.from(this, uri);
                imageView.setImageURI(uri);

            } catch (Exception ex) {
                Log.d("fileUpload+Exception175", ex.toString());
            }
        } else {
            Toast.makeText(this, "image not selected...", Toast.LENGTH_LONG).show();
        }
    }

    private void enocdeimage() {
        progressDialog.setMessage("processing...");
        progressDialog.show();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            imagestring = Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            Log.d("bitmap", e + "");
        }
    }


    private void senddata() {
        data_model data = new data_model();

        place_name = name.getText().toString().trim();
        place_desc = desc.getText().toString().trim();
        place_country = country.getText().toString().trim();
        place_locality = locality.getText().toString().trim();
        place_address = addr.getText().toString().trim();
        log = String.valueOf(longitude);
        lat = String.valueOf(latitude);

        if (place_country.length() != 0 && place_name.length() != 0 && place_desc.length() != 0) {
            data.setImage64(imagestring);
            data.setAddress(place_address);
            data.setCountry(place_country);
            data.setLocality(place_locality);
            data.setDescription(place_desc);
            data.setName(place_name);
            data.setLatitude(lat);
            data.setLongitude(log);

            Call<String> c = apIinterface.insert_data(data);
            c.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.code() == 200) {
                        Toast.makeText(AddPlace.this, "Locality added to the world", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddPlace.this, "Locality can't be added! Try again", Toast.LENGTH_SHORT).show();

                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPlace.this, "server error", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Please Enter all the details", Toast.LENGTH_SHORT).show();
        }
    }

    public void Addplace(View view) {
        enocdeimage();
        senddata();
    }
}