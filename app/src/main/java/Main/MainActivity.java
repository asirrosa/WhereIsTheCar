package Main;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //texto para mostrar distintos mensajes en pantalla
    TextView txtAlert;

    //boton para guardar la ubicacion
    Button btnGuardar,btnLista,btnBuscar;
    ProgressBar progressBar;
    private LocalDateTime startDateTime;
    private double latitude,longitude;
    GPSTracker gps;

    //Singleton
    private static MainActivity main = null;

    public MainActivity() {
    }

    public static MainActivity getInstance() {
        if (main == null) {
            main = new MainActivity();
        }
        return main;
    }

    /**
     * Metodo onCreate que instanciara el main layout
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Para que al iniciar cargue el layout del inicio
        setContentView(R.layout.main_layout);

        progressBar = findViewById(R.id.progressBar);

        //texto que muestra mensajes para el usuario
        txtAlert = findViewById(R.id.txtAlert);

        //El botón de guardar
        btnGuardar = findViewById(R.id.btnGuardar);
        //esta linea necesaria para que funcione el onClick
        btnGuardar.setOnClickListener(this);

        btnBuscar = findViewById(R.id.btnNavegar);
        btnBuscar.setOnClickListener(this);

        //El botón de la lista de ubicaciones
        btnLista = findViewById(R.id.btnLista);
        btnLista.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btnNavegar:
                break;

            case R.id.btnGuardar:
                try {
                    añadirUbicacion();
                } catch (CustomException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btnLista:
                listaUbicaciones(view);
                break;
        }
    }

    /**
     * Metodo para añadir una ubicacion despues de darle al boton, se hacen diferentes comprobaciones
     */
    public void añadirUbicacion() throws CustomException {
        main = this;
        gps = new GPSTracker(this);
        //En primer lugar miras si el servicio esta habilitado
        if (gps.isGPSAllowed && gps.isGPSEnabled) {
            if(gps.location != null) {
                latitude = gps.location.getLatitude();
                longitude = gps.location.getLongitude();
                startDateTime = LocalDateTime.now();
                //PARA CONSEGUIR EL NOMBRE DE LA UBICACION ACTUAL
                if (gps.isNetworkAvailable()) {
                    conConexion();
                } else {
                    sinConexion();
                }
            }
            else{
                SubCargarGPS subCargarGPS = new SubCargarGPS();
                subCargarGPS.execute();
            }
        } else if(!gps.isGPSEnabled){
            gps.showSettingsAlert();
            actualizacionesLayout(ProgressBar.GONE, R.drawable.button_guardar_click, true);
        }
        else{
            actualizacionesLayout(ProgressBar.GONE, R.drawable.button_guardar_click, true);
        }
    }

    public void sinConexion(){
        SinConexionDialog exampleDialog = new SinConexionDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    /**
     * Metodo de llamada a la api en este caso de openweather para conseguir el nombre de la localidad donde se ha aparcado
     */
    public void conConexion() {
        //lo hago de esta manera para que se vea bien que con conexion se hace esta llamada a la api
        reverseGeocodingApiCall();
    }

    /**
     * Metodo para conseguir el nombre de un sitio teniendo la longitud y la la latitud
     */
    public void reverseGeocodingApiCall() {
        String tempUrl = "https://api.maptiler.com/geocoding/" + longitude + "," + latitude + ".json?language=es" +
                "&key=" + getString(R.string.maptiles_api_key);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, res -> {
            try {
                JSONObject mainObject = new JSONObject(res);
                JSONArray featuresArray = mainObject.getJSONArray("features");
                for (int i = 0; i < 1; i++) {
                    JSONObject iObject = featuresArray.getJSONObject(i);
                    String placeName = iObject.getString("place_name_es");
                    String name;
                    String description = "";
                    if (placeName.contains(",")) {
                        String[] array = iObject.getString("place_name_es").split(",", 2);
                        name = array[0];
                        description = array[1];
                    } else {
                        name = placeName;
                    }
                    //para guardarlo en la base de datos
                    guardarEnDB(name,description);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            sinConexion();
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    /**
     * Metodo para guardar la nueva ubicacion en la base de datos
     */
    public void guardarEnDB(String nombre, String descripcion){
        MyDatabaseHelper myDB = new MyDatabaseHelper(MainActivity.this);
        myDB.addUbicacion(startDateTime,nombre,descripcion,latitude,longitude);
        txtAlert.setVisibility(TextView.VISIBLE);
    }

    /**
     * Metodo para actualizar el layout principal, se utiliza para ver que algo esta cargando, se ha cargado...
     */
    public void actualizacionesLayout(int visibility, int drawable, boolean guardar){
        progressBar.setVisibility(visibility);
        btnGuardar.setBackground(ResourcesCompat.getDrawable(getResources(), drawable, null));
        btnGuardar.setEnabled(guardar);
    }

    /**
     * LLamada a otro layout donde se guardan la lista de todas las ubicaciones
     */
    public void listaUbicaciones(View view){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
        view.postDelayed(() -> txtAlert.setVisibility(TextView.INVISIBLE), 100);
    }

    /**
     * Metodo threads que sirve para esperar a que el gps se active
     */
    private class SubCargarGPS extends AsyncTask<Void,Void,Void> {

        //Aqui le decimos que es lo que va a hacer mientras el gps esta iniciandose
        @Override
        protected void onPreExecute() {
            actualizacionesLayout(ProgressBar.VISIBLE, R.drawable.button_background_cargar, false);
            txtAlert.setVisibility(TextView.VISIBLE);
            txtAlert.setText("El gps esta arrancando...");
            super.onPreExecute();
        }

        //Este es un metodo para que espere, de esta manera esperamos hasta que el gps esta activado
        @Override
        protected Void doInBackground(Void... params) {
            while(gps.location == null){
                gps.onLocationChanged(gps.location);
                gps.getLocation();
            }
            return null;
        }

        //Despues de que el GPS se active se hace esto
        @Override
        protected void onPostExecute(Void unused) {
            actualizacionesLayout(ProgressBar.GONE, R.drawable.button_guardar_click, true);
            txtAlert.setVisibility(TextView.INVISIBLE);
            txtAlert.setText("Se ha guardado la ubi!");
            Toast.makeText(getApplicationContext(), "GPS activado", Toast.LENGTH_SHORT).show();
            super.onPostExecute(unused);
        }
    }
}