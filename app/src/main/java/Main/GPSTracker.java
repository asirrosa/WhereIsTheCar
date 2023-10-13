package Main;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

public class GPSTracker extends Service implements LocationListener {

    private static String TAG = GPSTracker.class.getName();

    private final Context mContext;

    // flag para el status del GPS
    boolean isGPSEnabled = false;

    // flag para el status de la red
    boolean isNetworkEnabled = false;

    // flag que dice si se ha conseguido obtener las coordenadas ya sea por GPS o internet
    boolean isGPSTrackingEnabled = false;

    Location location;
    double latitude;
    double longitude;

    // How many Geocoder should return our GPSTracker
    int geocoderMaxResults = 1;

    // The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    // Store LocationManager.GPS_PROVIDER or LocationManager.NETWORK_PROVIDER information
    private String provider_info;

    //La clase constructor
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

            //Devuelve el status del GPS y si hay conexión debido a que en caso de no tener conexion a internet tira del gps solamente y viceversa
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // Conseguir la ubicación en caso de que el GPS este activado
            if (isGPSEnabled) {
                this.isGPSTrackingEnabled = true;
                Log.d(TAG, "La aplicación ha utilizado en servicio GPS");
                provider_info = LocationManager.GPS_PROVIDER;

            } else if (isNetworkEnabled) {
                this.isGPSTrackingEnabled = true;
                Log.d(TAG, "La apliacción ha utilizado la conexión a Internet para devolver las coordenadas del GPS");
                provider_info = LocationManager.NETWORK_PROVIDER;

            }
            if (!provider_info.isEmpty()) {
                //te consigue la ultima ubicación
                //razon del error: basicamente esto se suele hacer en la clase que tiene el layout, no obstante al utilizar otra clase
                //te pide que checkees por si el usuario le ha dado permisos o no, aun asi eso ya lo hago desde el main

                //La primera linea sirve para que el gps se actualice, sino en caso de que el usuario justo haya encendido el gps, nos pondrá
                //que el gps sigue apagado
                locationManager.requestLocationUpdates(provider_info,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                location = locationManager.getLastKnownLocation(provider_info);
                if (location != null) {
                    actualizarCoordenadas();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "No se ha podido conectar al gestor de la ubicación", e);
        }
    }

    /**
     * Metodo para actualizar los valores de latitud y longitud
     */
    public void actualizarCoordenadas() {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    /**
     * Metodo para comprobar si el gps ha empezado a funcionar
     */
    public boolean funcionaGPS(){
        boolean resul = false;
        if(locationManager.getLastKnownLocation(provider_info) != null){
            resul = true;
        }
        return resul;
    }

    /**
     * Metodo para crear un loop infinito llamando a funcionaGPS()
     */
    public void esperarGPS(){
        while(true){
            if(funcionaGPS())
                break;
        }
    }

    /**
     * Metodo para mostrar el dialog que te dice si quieres activar el gps
     */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setMessage("El gps esta desactivado quieres activarlo?");
        alertDialogBuilder.setPositiveButton("Ir a configuración y activarlo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }

        });
        alertDialogBuilder.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /**
     * Metodo para mostrar el dialog que te dice si quieres activar el gps
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

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
}