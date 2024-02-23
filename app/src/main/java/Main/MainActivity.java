package Main;


import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener, MenuItem.OnMenuItemClickListener {

    //boton para guardar la ubicacion
    Button btnNavegar, btnGuardar;
    FloatingActionButton btnLista;
    public double latitude, longitude;
    MenuItem itemHelp;
    private boolean pulsar;
    private Toolbar toolbar;
    private TextView toolbarTitle;

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

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("UbiManager");

        //El botón de guardar
        btnGuardar = findViewById(R.id.btnGuardar);
        //esta linea necesaria para que funcione el onClick
        btnGuardar.setOnClickListener(this);

        btnNavegar = findViewById(R.id.btnNavegar);
        btnNavegar.setOnClickListener(this);

        //El botón de la lista de ubicaciones
        btnLista = findViewById(R.id.btnLista);
        btnLista.setOnClickListener(this);

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
                    Intent intent = new Intent(this, FindRouteActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Por favor conectate a Internet", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnGuardar:
                main = this;
                pulsar = true;
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

    public void noConnectionDialog() {
        SinConexionDialog exampleDialog = new SinConexionDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    /**
     * Metodo para guardar la nueva ubicacion en la base de datos
     */
    public void guardarEnDB(String nombre, String descripcion) {
        MyDatabaseHelper myDB = new MyDatabaseHelper(this);
        String startDateTime = LocalDateTime.now().toString();
        myDB.addUbicacion(startDateTime, nombre, descripcion, latitude, longitude);
        Toast.makeText(getApplicationContext(), "Se ha guardado la ubi", Toast.LENGTH_SHORT).show();
    }

    /**
     * LLamada a otro layout donde se guardan la lista de todas las ubicaciones
     */
    public void listaUbicaciones(View view) {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("archiveMode", false);
        startActivity(intent);
    }

    private void getLocation() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastLocation == null) {
                        try {
                            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        processWithLocation(lastLocation);
                    }
                } else {
                    showSettingsAlert();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location != null) {
            if (pulsar) {
                pulsar = false;
                processWithLocation(location);
            }
        }
    }

    private void processWithLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        if (isNetworkAvailable()) {
            try {
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                String description = addresses.get(0).getAddressLine(0);
                String name = addresses.get(0).getLocality();
                guardarEnDB(name, description);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            noConnectionDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 100) {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    boolean askAgain = shouldShowRequestPermissionRationale(permission);
                    if (!askAgain) {
                        Toast.makeText(this, "La aplicación no tiene permisos de ubicación", Toast.LENGTH_SHORT).show();
                    }
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
            startActivity(intent);
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.create().show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
