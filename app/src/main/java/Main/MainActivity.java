package Main;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jovanovic.stefan.sqlitetutorial.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    TextView txtAlert;

    ProgressBar progressBar;

    Button btnGuardar;
    Animation scaleUp, scaleDown;

    private String location,dateTime;
    private double lat,lon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtAlert = findViewById(R.id.txtAlert);
        txtAlert.setVisibility(TextView.GONE);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.GONE);

        //Para añadir la animacion en el boton
        btnGuardar = findViewById(R.id.btnGuardar);
        scaleUp = AnimationUtils.loadAnimation(this,R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this,R.anim.scale_down);

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                txtAlert.setVisibility(TextView.GONE);
                btnGuardar.setEnabled(false);
                btnGuardar.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_background_cargar, null));
                try {
                    añadirAparcamiento();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void añadirAparcamiento() throws IOException, InterruptedException {

        //para comprobar los permisos de la app
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            progressBar.setVisibility(ProgressBar.GONE);
            btnGuardar.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_background, null));
            btnGuardar.setEnabled(true);
        }

        conseguirInfo();
    }

    private void conseguirInfo() throws InterruptedException {

        //AQUI ES DONDE SE LIA
        GPSTracker gps = new GPSTracker(this);
        if(gps.isGPSTrackingEnabled){
            if(!gps.funcionaGPS()){
                //esto es el thread para que te mire si ya funciona el gps mientras te enseña el mensaje
                SubCargar cargar = new SubCargar();
                cargar.execute(gps);
            }
            else {
                lat = gps.latitude;
                lon = gps.longitude;

                //PARA CONSEGUIR LA FECHA HORA ACTUAL
                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                dateTime = formatter.format(date);

                //PARA CONSEGUIR EL NOMBRE DE LA UBICACION ACTUAL
                try {
                    apiGeo();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else{
            gps.showSettingsAlert();
            progressBar.setVisibility(ProgressBar.GONE);
            txtAlert.setVisibility(TextView.INVISIBLE);
            btnGuardar.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_background, null));
            btnGuardar.setEnabled(true);
            txtAlert.setText("Se ha guardado la ubi del aparcamiento!");
        }
    }

    private void guardarEnDB(){
        MyDatabaseHelper myDB = new MyDatabaseHelper(MainActivity.this);
        myDB.addAparcamiento(dateTime,location,lat,lon);
        progressBar.setVisibility(ProgressBar.GONE);
        btnGuardar.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_background, null));
        txtAlert.setVisibility(TextView.VISIBLE);
        btnGuardar.setEnabled(true);
    }

    public void apiGeo() throws IOException, InterruptedException {

        String tempUrl = "https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&appid=<API>   ";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    JSONObject jsonObject = new JSONObject(res);
                    location = jsonObject.getString("name");
                    //PARA GUARDAR EL NUEVO APARCAMIENTO EN LA BASE DE DATOS DEL MOVIL
                    guardarEnDB();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Comprueba la conexión a internet", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(ProgressBar.GONE);
                btnGuardar.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_background, null));
                btnGuardar.setEnabled(true);
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }


    public void listaAparcamientos(View view){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
        view.postDelayed(new Runnable(){
            @Override
            public void run()
            {
                txtAlert.setVisibility(TextView.INVISIBLE);
            }
        }, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            recreate();
        }
    }


    //EL THREAD EN CUESTION

    private class SubCargar extends AsyncTask<GPSTracker,Void,Void>{

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            txtAlert.setVisibility(TextView.VISIBLE);
            btnGuardar.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_background_cargar, null));
            btnGuardar.setEnabled(false);
            txtAlert.setText("El gps esta arrancando...");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(GPSTracker... params) {

            params[0].esperarGPS();

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            progressBar.setVisibility(ProgressBar.GONE);
            txtAlert.setVisibility(TextView.INVISIBLE);
            btnGuardar.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_background, null));
            btnGuardar.setEnabled(true);
            txtAlert.setText("Se ha guardado la ubi del aparcamiento!");
            Toast.makeText(getApplicationContext(), "GPS activado\uD83E\uDD11\uD83E\uDD75\uD83E\uDD76", Toast.LENGTH_SHORT).show();
            super.onPostExecute(unused);
        }

    }


}
