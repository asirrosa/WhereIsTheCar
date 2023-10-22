package Main;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CustomException extends Exception {

    public CustomException(LocationManager locationManager, ConnectivityManager connectivityManager) {
        if(locationManager == null){
             Toast.makeText(MainActivity.getInstance(), "Comprueba el servicio GPS", Toast.LENGTH_SHORT).show();
             MainActivity.getInstance().actualizacionesLayout(ProgressBar.GONE, R.drawable.button_background, true);
             MainActivity.getInstance().txtAlert.setVisibility(TextView.INVISIBLE);
        }
        else if(connectivityManager == null){
            Toast.makeText(MainActivity.getInstance(), "Comprueba la conexión a Internet", Toast.LENGTH_SHORT).show();
            MainActivity.getInstance().actualizacionesLayout(ProgressBar.GONE, R.drawable.button_background, true);
            MainActivity.getInstance().txtAlert.setVisibility(TextView.INVISIBLE);
        }
    }
}