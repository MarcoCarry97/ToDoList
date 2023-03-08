package exam.spring.todolist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MAP_REQUEST = 1;
    private GoogleMap mMap;
    ArrayList<Note> notes;
    HashMap<Marker, Note> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Bundle bundle = getIntent().getExtras();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        notes = bundle.getParcelableArrayList("notes");
        markers = new HashMap<Marker, Note>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MapsActivity.this, NoteActivity.class);
                intent.putExtra("note", markers.get(marker));
                startActivity(intent);
            }
        });
        // Add a marker in Sydney and move the camera
        for (Note note : notes)
            addMarkerOnMap(googleMap, note);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            googleMap.setMyLocationEnabled(true);

    }

    private void addMarkerOnMap(GoogleMap googleMap, Note note) {
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(note.getPosition());
        markerOpt.title(note.getDescription());
        markerOpt.snippet(note.getDeadline().toString());
        Marker marker = googleMap.addMarker(markerOpt);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(note.getPosition()));
        markers.put(marker, note);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MAP_REQUEST)
            if (permissions.length == 1)
                if(permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION)
                    if( grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                            mMap.setMyLocationEnabled(true);

    }
}
