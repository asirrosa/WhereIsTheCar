package Main;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, MenuItem.OnMenuItemClickListener {

    //boton para guardar la ubicacion
    Button btnNavegar, btnGuardar;
    FloatingActionButton btnLista;
    ProgressBar progressBar;
    public double latitude, longitude;
    MenuItem itemHelp;
    public FusedLocationProviderClient fusedLocationProviderClient;

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

        //El botón de guardar
        btnGuardar = findViewById(R.id.btnGuardar);
        //esta linea necesaria para que funcione el onClick
        btnGuardar.setOnClickListener(this);

        btnNavegar = findViewById(R.id.btnNavegar);
        btnNavegar.setOnClickListener(this);

        //El botón de la lista de ubicaciones
        btnLista = findViewById(R.id.btnLista);
        btnLista.setOnClickListener(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        itemHelp = menu.findItem(R.id.help);
        itemHelp.setOnMenuItemClickListener(this);

        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnNavegar:
                if (isNetworkAvailable()) {
                    main = this;
                    Intent intent = new Intent(this, NavigationActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Por favor conectate a Internet", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnGuardar:
                main = this;
                getLocation();
                break;

            case R.id.btnLista:
                listaUbicaciones(view);
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                Intent intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                break;
        }
        return false;
    }

    public void sinConexion() {
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
                    guardarEnDB(name, description);
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
    public void guardarEnDB(String nombre, String descripcion) {
        MyDatabaseHelper myDB = new MyDatabaseHelper(this);
        LocalDateTime startDateTime = LocalDateTime.now();
        myDB.addUbicacion(startDateTime, nombre, descripcion, latitude, longitude);
        Toast.makeText(getApplicationContext(), "Se ha guardado la ubi!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Metodo para actualizar el layout principal, se utiliza para ver que algo esta cargando, se ha cargado...
     */
    public void actualizacionesLayout(int visibility, int drawable, boolean guardar) {
        progressBar.setVisibility(visibility);
        btnGuardar.setBackground(ResourcesCompat.getDrawable(getResources(), drawable, null));
        btnGuardar.setEnabled(guardar);
        btnNavegar.setBackground(ResourcesCompat.getDrawable(getResources(), drawable, null));
        btnNavegar.setEnabled(guardar);
    }

    /**
     * LLamada a otro layout donde se guardan la lista de todas las ubicaciones
     */
    public void listaUbicaciones(View view) {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private void getLocation() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Por favor dale los permisos de ubicación a la aplicación", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

            } else {
                if (isGPSEnabled) {
                    fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                        @NonNull
                        @Override
                        public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                            return null;
                        }

                        @Override
                        public boolean isCancellationRequested() {
                            return false;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                if (isNetworkAvailable()) {
                                    conConexion();
                                } else {
                                    sinConexion();
                                }
                            }
                        }
                    });
                } else {
                    showSettingsAlert();
                }
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Metodo para mostrar el dialog que te dice si quieres activar el gps
     */
    public void showSettingsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("El gps esta desactivado. ¿Quieres activarlo?");
        //builder.setMessage("¿Estas seguro que quieres añadir una ubicación de manera manual?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            getApplicationContext().startActivity(intent);
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.create().show();
    }
}