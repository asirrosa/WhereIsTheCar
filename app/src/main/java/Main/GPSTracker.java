package Main;

import static android.content.Context.LOCATION_SERVICE;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

public class GPSTracker implements LocationListener {

    private final Context mContext;

    // flag para el status del GPS
    boolean isGPSEnabled = false;
    // flag para el status de la red
    boolean isGPSAllowed = false;
    // Declaring a Location Manager
    public LocationManager locationManager;
    Location location;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    /**
     * Metodo para conseguir la ubicación
     */
    public void getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            if (locationManager != null) {
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.getInstance(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    MainActivity.getInstance().txtAlert.setText("Por favor dale los permisos de ubicación a la aplicación");
                    MainActivity.getInstance().txtAlert.setVisibility(TextView.VISIBLE);
                } else {
                    //cuando entra aqui ya sabemos que la aplicacion tiene permiso de gps
                    if (isGPSEnabled) {
                        isGPSAllowed = true;
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, this);
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        MainActivity.getInstance().txtAlert.setVisibility(TextView.INVISIBLE);
                        MainActivity.getInstance().txtAlert.setText("Se ha guardado la ubi!");
                    }
                }
            } else {
                throw new CustomException(locationManager);
            }
        } catch (CustomException e) {
            e.printStackTrace();
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Metodo para mostrar el dialog que te dice si quieres activar el gps
     */
    public void showSettingsAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("El gps esta desactivado. ¿Quieres activarlo?");
        //builder.setMessage("¿Estas seguro que quieres añadir una ubicación de manera manual?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.startActivity(intent);
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.create().show();
    }

    //Metodos propios de LocationListener
    @Override
    public void onLocationChanged(Location location){}
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}
    @Override
    public void onProviderEnabled(String s) {}
    @Override
    public void onProviderDisabled(String s) {}
}