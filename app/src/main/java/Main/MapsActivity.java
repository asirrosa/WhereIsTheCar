package Main;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.geometry.LatLngSpan;
import com.mapbox.mapboxsdk.maps.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnGuardarUbiManual;
    AutoCompleteTextView txtInput;
    MapView mapView;
    ProgressBar progressBarLocations;
    BusquedaAdapter busquedaAdapter;
    BusquedaItem busquedaItemGuardar;
    RecyclerView recyclerBusqueda;
    ImageView lupaFlecha;
    TextView no_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //esta linea importante porque sino no inicializa el mapa
        Mapbox.getInstance(this);
        setContentView(R.layout.maps_layout);

        no_data = findViewById(R.id.no_data);

        recyclerBusqueda = findViewById(R.id.recyclerBusqueda);

        lupaFlecha = findViewById(R.id.lupaFlecha);
        lupaFlecha.setOnClickListener(this);

        progressBarLocations = findViewById(R.id.progressBarLocations);

        btnGuardarUbiManual = findViewById(R.id.btnGuardarUbiManual);
        btnGuardarUbiManual.setOnClickListener(this);

        //para mostrar el mapa
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            mapboxMap.setStyle("https://api.maptiler.com/maps/streets-v2/style.json?key="+getString(R.string.maptiles_api_key));
            mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(40.416775,-3.703790)).zoom(3.5).build());
        });

        txtInput = findViewById(R.id.txtInput);

        txtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recyclerBusqueda.setVisibility(RecyclerView.VISIBLE);
                lupaFlecha.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_back, null));
                btnGuardarUbiManual.setVisibility(Button.INVISIBLE);
                suggestApiCall(s.toString());
                SubCargarListaLocations cargar = new SubCargarListaLocations();
                cargar.execute();
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0){
                    storeDataInArrays(false);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.lupaFlecha:
                txtInput.setText("");
                lupaFlecha.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_searchlocation, null));
                btnGuardarUbiManual.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_background_cargar, null));
                break;
            case R.id.btnGuardarUbiManual:
                //todo tengo que llamar a Busqueda adapter
                guardarEnDB();
                Toast.makeText(getApplicationContext(), "Se ha guardado la ubi!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ListActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    private void storeDataInArrays(boolean chivato) {
        if (chivato == false) {
            recyclerBusqueda.setVisibility(RecyclerView.INVISIBLE);
        } else {
            recyclerBusqueda.setAdapter(busquedaAdapter);
            recyclerBusqueda.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    public void suggestApiCall(String text) {
        String tempUrl = "https://api.maptiler.com/geocoding/"+text+".json?autocomplete=true&limit=3&language=es&fuzzyMatch=true" +
                "&key="+getString(R.string.maptiles_api_key);
        ArrayList<BusquedaItem> busquedaList = new ArrayList<>();
        no_data.setVisibility(TextView.INVISIBLE);
        if(text.length() > 3) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, res -> {
                try {
                    JSONObject mainObject = new JSONObject(res);
                    JSONArray featuresArray = mainObject.getJSONArray("features");
                    if (featuresArray.length() > 0) {
                        for (int i = 0; i < featuresArray.length(); i++) {
                            JSONObject iObject = featuresArray.getJSONObject(i);
                            String placeName = iObject.getString("place_name_es");
                            String name = "";
                            String description = "";
                            if(placeName.contains(",")){
                                String[] array = iObject.getString("place_name_es").split(",",2);
                                name = array[0];
                                description = array[1];
                            }
                            else{
                                name = placeName;
                            }
                            JSONObject geometryObject = iObject.getJSONObject("geometry");
                            JSONArray coordinatesObject = geometryObject.getJSONArray("coordinates");
                            Double lat = (Double) coordinatesObject.get(1);
                            Double lon = (Double) coordinatesObject.get(0);
                            BusquedaItem busquedaItem = new BusquedaItem(name, description, lat, lon);
                            busquedaList.add(busquedaItem);
                        }
                    }
                    else{
                        no_data.setVisibility(TextView.VISIBLE);
                    }
                    busquedaAdapter = new BusquedaAdapter(this, busquedaList);
                    storeDataInArrays(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
            });
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        }
    }

    public void guardarEnDB(){
        MyDatabaseHelper myDB = new MyDatabaseHelper(this);
        LocalDateTime startDateTime = LocalDateTime.now();
        //aqui tengo que añadir la latitud y la longitud
        myDB.addUbicacion(startDateTime, busquedaItemGuardar.getNombre(), busquedaItemGuardar.getLat(), busquedaItemGuardar.getLon());
    }

    public void cambiarMapa(String text, Double lat, Double lon){
        //para cerrar el teclado
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        busquedaItemGuardar = new BusquedaItem(text,"",lat,lon);
        recyclerBusqueda.setVisibility(RecyclerView.INVISIBLE);
        btnGuardarUbiManual.setVisibility(Button.VISIBLE);
        //esto falla por problemas con el conversor lat long 
        mapView.getMapAsync(mapboxMap -> {
            mapboxMap.setStyle("https://api.maptiler.com/maps/streets-v2/style.json?key="+getString(R.string.maptiles_api_key));
            mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(lat,lon)).zoom(17).build());
            mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lon))
                    .title(text));
        });
    }

    private class SubCargarListaLocations extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            progressBarLocations.setVisibility(ProgressBar.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            progressBarLocations.setVisibility(ProgressBar.INVISIBLE);
            super.onPostExecute(unused);
        }
    }
}