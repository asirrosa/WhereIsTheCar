package Main;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //texto para mostrar distintos mensajes en pantalla
    TextView txtAlert;

    //circulo de carga
    ProgressBar progressBar;

    //boton para guardar la ubicacion
    Button btnGuardar;

    //variables para conseguir la latitud y la longitud
    private String address;
    private LocalDateTime startDateTime;
    private double latitude,longitude;

    //Singleton
    private static MainActivity main;

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
        setContentView(R.layout.activity_main);

        //texto que muestra mensajes para el usuario
        txtAlert = findViewById(R.id.txtAlert);
        txtAlert.setVisibility(TextView.GONE);

        //barra de carga
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.GONE);

        //El botón de guardar
        btnGuardar = findViewById(R.id.btnGuardar);

        btnGuardar.setOnClickListener(view -> {
            txtAlert.setVisibility(TextView.GONE);
            actualizacionesLayout(ProgressBar.VISIBLE, R.drawable.button_background_cargar, false);
            try {
                añadirAparcamiento();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Metodo para añadir un aparcamiento despues de darle al boton, se hacen diferentes comprobaciones
     */
    public void añadirAparcamiento() throws IOException, InterruptedException {
            GPSTracker gps = new GPSTracker(this);
            //En primer lugar miras si el servicio esta habilitado
            if(gps.isGPSEnabled){
                //Luego compruebas que se active el gps es decir esperar un poco a que se active
                if (!gps.funcionaGPS()) {
                    //esto es el thread para que te mire si ya funciona el gps mientras te enseña el mensaje
                    SubCargar cargar = new SubCargar();
                    cargar.execute(gps);
                } else {
                    latitude = gps.location.getLatitude();
                    longitude = gps.location.getLongitude();

                    //PARA CONSEGUIR LA FECHA HORA ACTUAL
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startDateTime = LocalDateTime.now();
                    }

                    //PARA CONSEGUIR EL NOMBRE DE LA UBICACION ACTUAL
                    try {
                        if (gps.isNetworkEnabled) {
                            apiGeo();
                        } else {
                            AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
                            myDialog.setTitle("Sin conexión. Escribe el nombre del aparcamiento");
                            final EditText input = new EditText(this);
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            myDialog.setView(input);
                            myDialog.setPositiveButton("OK", (dialog, which) -> {
                                address = input.getText().toString();
                                guardarEnDB();
                            });
                            myDialog.setNegativeButton("Cancel", (dialog, which) -> {
                                actualizacionesLayout(ProgressBar.GONE, R.drawable.button_background, true);
                                txtAlert.setVisibility(TextView.INVISIBLE);
                                dialog.cancel();
                            });
                            myDialog.setOnDismissListener(dialog -> {
                                actualizacionesLayout(ProgressBar.GONE, R.drawable.button_background, true);
                                txtAlert.setVisibility(TextView.INVISIBLE);
                                dialog.cancel();
                            });
                            myDialog.show();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                gps.showSettingsAlert();
                actualizacionesLayout(ProgressBar.GONE, R.drawable.button_background, true);
                txtAlert.setVisibility(TextView.INVISIBLE);
                txtAlert.setText("Se ha guardado la ubi del aparcamiento!");
            }
        }

    /**
     * Metodo de llamada a la api en este caso de openweather para conseguir el nombre de la localidad donde se ha aparcado
     */
    public void apiGeo() throws IOException {
        String tempUrl = "https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=1725788a5fa982f5a33a407898764e84";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, res -> {
            try {
                JSONObject jsonObject = new JSONObject(res);
                address = jsonObject.getString("name");
                guardarEnDB();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Comprueba la conexión a internet", Toast.LENGTH_SHORT).show();
                actualizacionesLayout(ProgressBar.GONE,R.drawable.button_background,true);
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    /**
     * Metodo para guardar el nuevo aparcamiento en la base de datos
     */
    private void guardarEnDB(){
        MyDatabaseHelper myDB = new MyDatabaseHelper(MainActivity.this);
        myDB.addAparcamiento(startDateTime,address,latitude,longitude);
        actualizacionesLayout(ProgressBar.GONE, R.drawable.button_background, true);
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
    private class SubCargar extends AsyncTask<GPSTracker,Void,Void>{

        //Aqui le decimos que es lo que va a hacer mientras el gps esta iniciandose
        @Override
        protected void onPreExecute() {
            actualizacionesLayout(ProgressBar.VISIBLE,R.drawable.button_background_cargar,false);
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
            actualizacionesLayout(ProgressBar.GONE,R.drawable.button_background,true);
            txtAlert.setVisibility(TextView.INVISIBLE);
            txtAlert.setText("Se ha guardado la ubi del aparcamiento!");
            Toast.makeText(getApplicationContext(), "GPS activado", Toast.LENGTH_SHORT).show();
            super.onPostExecute(unused);
        }

    }
}