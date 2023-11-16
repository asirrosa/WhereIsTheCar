package Main;

import android.location.Geocoder;
import android.location.LocationManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class CustomException extends Exception {

    public CustomException(LocationManager locationManager) {
        if(locationManager == null){
             Toast.makeText(MainActivity.getInstance(), "Comprueba el servicio GPS", Toast.LENGTH_SHORT).show();
             MainActivity.getInstance().actualizacionesLayout(ProgressBar.GONE, R.drawable.button_main_click_light, true);
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