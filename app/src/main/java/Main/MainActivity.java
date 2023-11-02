package Main;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //texto para mostrar distintos mensajes en pantalla
    TextView txtAlert;

    //circulo de carga
    ProgressBar progressBar;

    //boton para guardar la ubicacion
    Button btnGuardar;
    Button btnLista;

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

        //texto que muestra mensajes para el usuario
        txtAlert = findViewById(R.id.txtAlert);
        txtAlert.setVisibility(TextView.INVISIBLE);

        //barra de carga
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.GONE);

        //El botón de guardar
        btnGuardar = findViewById(R.id.btnGuardar);
        //esta linea necesaria para que funcione el onClick
        btnGuardar.setOnClickListener(this);

        //El botón de la lista de aparcamientos
        btnLista = findViewById(R.id.btnLista);
        btnLista.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btnGuardar:
                try {
                    añadirAparcamiento();
                } catch (CustomException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btnLista:
                listaAparcamientos(view);
                break;
        }
    }

    /**
     * Metodo para añadir un aparcamiento despues de darle al boton, se hacen diferentes comprobaciones
     */
    public void añadirAparcamiento() throws CustomException {
        main = this;
        gps = new GPSTracker(this);
        //En primer lugar miras si el servicio esta habilitado
        if (gps.isGPSEnabled && gps.isGPSPermissionEnabled) {
            //Luego compruebas que se active el gps es decir esperar un poco a que se active
            if (!gps.funcionaGPS()) {
                SubCargarGPS cargar = new SubCargarGPS();
                cargar.execute(gps);
            } else {
                latitude = gps.location.getLatitude();
                longitude = gps.location.getLongitude();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startDateTime = LocalDateTime.now();
                }
                //PARA CONSEGUIR EL NOMBRE DE LA UBICACION ACTUAL
                if (gps.isNetworkEnabled) {
                    conConexion();
                } else {
                    sinConexion();
                }
            }
        } else if (!gps.isGPSEnabled) {
            gps.showSettingsAlert();
            actualizacionesLayout(ProgressBar.GONE, R.drawable.button_guardar_click, true);
        } else if (!gps.isGPSPermissionEnabled) {
            actualizacionesLayout(ProgressBar.GONE, R.drawable.button_guardar_click, true);
        }
    }

    private void sinConexion(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        myDialog.setTitle("Modo sin conexión. Escribe el nombre del aparcamiento.");
        txtAlert.setVisibility(TextView.INVISIBLE);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        myDialog.setView(input);
        myDialog.setPositiveButton("OK", (dialog, which) -> {
            String address = input.getText().toString();
            guardarEnDB(address);
            actualizacionesLayout(ProgressBar.GONE, R.drawable.button_guardar_click, true);
            txtAlert.setVisibility(TextView.VISIBLE);
        });
        myDialog.setNegativeButton("Cancel", (dialog, which) -> {
            actualizacionesLayout(ProgressBar.GONE, R.drawable.button_guardar_click, true);
            txtAlert.setVisibility(TextView.INVISIBLE);
            dialog.cancel();
        });
        myDialog.setCancelable(false);
        myDialog.show();
    }

    /**
     * Metodo de llamada a la api en este caso de openweather para conseguir el nombre de la localidad donde se ha aparcado
     */
    @SuppressLint("ResourceType")
    public void conConexion() throws CustomException {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            if (geocoder != null) {
                List<Address> list = geocoder.getFromLocation(latitude, longitude, 1);
                if (list.size() > 0) {
                    String address = list.get(0).getLocality();
                    guardarEnDB(address);
                } else {
                    sinConexion();
                }
            } else {
                throw new CustomException(geocoder);
            }
        } catch (CustomException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new CustomException(e);
        }
    }
        /**
     * Metodo para guardar el nuevo aparcamiento en la base de datos
     */
    public void guardarEnDB(String address){
        MyDatabaseHelper myDB = new MyDatabaseHelper(MainActivity.this);
        myDB.addAparcamiento(startDateTime,address,latitude,longitude);
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
     * LLamada a otro layout donde se guardan la lista de todos los aparcamientos
     */
    public void listaAparcamientos(View view){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
        view.postDelayed(() -> txtAlert.setVisibility(TextView.INVISIBLE), 100);
    }

    /**
     * Metodo threads que sirve para esperar a que el gps se active
     */
    private class SubCargarGPS extends AsyncTask<GPSTracker,Void,Void> {

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
        protected Void doInBackground(GPSTracker... params) {
            params[0].esperarGPS();
            return null;
        }

        //Despues de que el GPS se active se hace esto
        @Override
        protected void onPostExecute(Void unused) {
            actualizacionesLayout(ProgressBar.GONE, R.drawable.button_guardar_click, true);
            txtAlert.setVisibility(TextView.INVISIBLE);
            txtAlert.setText("Se ha guardado la ubi del aparcamiento!");
            Toast.makeText(getApplicationContext(), "GPS activado", Toast.LENGTH_SHORT).show();
            super.onPostExecute(unused);
        }

    }
}