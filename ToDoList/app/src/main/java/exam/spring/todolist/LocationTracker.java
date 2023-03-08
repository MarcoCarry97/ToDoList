package exam.spring.todolist;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.security.Permission;
import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class LocationTracker extends Service
{
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private LatLng current;
    private GoogleApiAvailability availability;
    private Context context;
    private Activity activity;
    private Tools tools;
    private FusedLocationProviderClient fused;
    private double latitude;
    private double longitude;

    public LocationTracker(Activity activity) {
        Log.i("CONSTRUCTOR","OK");
        this.activity = activity;
        context = activity.getApplicationContext();
        tools = new Tools(context);
        availability = GoogleApiAvailability.getInstance();
        fused=LocationServices.getFusedLocationProviderClient(activity);
    }

    public boolean isServiceAvailable() {
        Log.i("AVAILABLE","OK");
        int result = availability.isGooglePlayServicesAvailable(context);
        return result == ConnectionResult.SUCCESS;
    }

    public boolean checkPermission() {
        Log.i("CHECK","OK");
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PERMISSION_GRANTED)
        {
            Log.i("CHECK","GRANTED");
            return true;
        }
        else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION))
        {
            Log.i("CHECK","DENIED");
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(activity, permissions, LOCATION_PERMISSION_REQUEST);
            return false;
        }
        else
        {
            Log.e("CHECK","LOST");
            return true;
        }
    }

    public void initService()
    {
        Log.i("INIT","OK");
        //FusedLocationProviderClient fused;
        //fused = LocationServices.getFusedLocationProviderClient(activity);
        /*if (ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {*/
            Log.i("INIT","CHECK");
            fused.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location!=null)
                        {
                            Log.e("YEAH:","IT WORKS");
                            double lat=location.getLatitude();
                            double lng=location.getLongitude();
                            current=new LatLng(lat,lng);
                        }
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("NONE","NONE");
                        tools.toast(e.getMessage());
                    }
                });
       // }
    }

    public LatLng getCurrentLocation()
    {
        Log.i("GET","OK");
        if(current!=null) return current;
        else throw new IllegalArgumentException(context.getString(R.string.error_location));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
