package Main;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ProgressBar;

import androidx.core.app.ActivityCompat;

public class GPSTracker implements LocationListener {

    private static String TAG = GPSTracker.class.getName();

    private final Context mContext;

    // flag para el status del GPS
    boolean isGPSEnabled = false;
    boolean isGPSPermissionEnabled = false;

    // flag para el status de la red
    boolean isNetworkEnabled = false;

    // Declaring a Location Manager
    private LocationManager locationManager;
    private ConnectivityManager connectivityManager;
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
            connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (locationManager != null && connectivityManager != null) {
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = connectivityManager.getActiveNetworkInfo() != null;
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.getInstance(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                } else {
                    isGPSPermissionEnabled = true;
                    // Conseguir la ubicación en caso de que el GPS este activado
                    if (isGPSEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, this);
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
            } else {
                throw new CustomException(locationManager, connectivityManager);
            }
        } catch (CustomException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo para crear un loop infinito llamando a funcionaGPS()
     */
    public void esperarGPS() {
        while (true) {
            if (funcionaGPS())
                break;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, this);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    /**
     * Metodo para comprobar si el gps ha empezado a funcionar
     */
    public boolean funcionaGPS() {
        boolean result = false;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if(location != null){
                    result = true;
                }
        }
        return result;
    }

    /**
     * Metodo para mostrar el dialog que te dice si quieres activar el gps
     */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setMessage("El gps esta desactivado quieres activarlo?");
        alertDialogBuilder.setPositiveButton("Ir a configuración y activarlo", (dialog, id) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.startActivity(intent);
        });
        alertDialogBuilder.setNegativeButton("Cancelar",
                (dialog, id) -> dialog.cancel());
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    //Metodos propios de LocationListener
    @Override
    public void onLocationChanged(Location location) {}
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}
    @Override
    public void onProviderEnabled(String s) {}
    @Override
    public void onProviderDisabled(String s) {}
}