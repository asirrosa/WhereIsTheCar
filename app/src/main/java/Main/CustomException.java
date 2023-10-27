package Main;

import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class CustomException extends Exception {

    public CustomException(LocationManager locationManager, ConnectivityManager connectivityManager) {
        if(locationManager == null){
             Toast.makeText(MainActivity.getInstance(), "Comprueba el servicio GPS", Toast.LENGTH_SHORT).show();
             MainActivity.getInstance().actualizacionesLayout(ProgressBar.GONE, R.drawable.button_guardar_click, true);
             MainActivity.getInstance().txtAlert.setVisibility(TextView.INVISIBLE);
        }
        else if(connectivityManager == null){
            Toast.makeText(MainActivity.getInstance(), "Comprueba la conexión a Internet", Toast.LENGTH_SHORT).show();
            MainActivity.getInstance().actualizacionesLayout(ProgressBar.GONE, R.drawable.button_guardar_click, true);
            MainActivity.getInstance().txtAlert.setVisibility(TextView.INVISIBLE);
        }
    }
    public CustomException(Geocoder geocoder){
        if(geocoder == null){
            Toast.makeText(MainActivity.getInstance(), "No se ha podido conseguir la localidad", Toast.LENGTH_SHORT).show();
        }
    }
    public CustomException(IOException exception){
        Toast.makeText(MainActivity.getInstance(), "No se ha podido conseguir la localidad", Toast.LENGTH_SHORT).show();
    }
}