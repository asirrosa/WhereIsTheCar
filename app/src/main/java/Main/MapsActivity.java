package Main;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.maps.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnGuardarUbiManual;
    AutoCompleteTextView txtInput;
    MapView mapView;
    ProgressBar progressBarLocations;
    ArrayAdapter adapter;
    ImageView lupaFlecha;
    HashMap<String,AparcamientoItem> hashMap = new HashMap<>();
    HashMap<String,AparcamientoItem> hashMapCopy = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //esta linea importante porque sino no inicializa el mapa
        Mapbox.getInstance(this);
        setContentView(R.layout.maps_layout);

        lupaFlecha = findViewById(R.id.lupaFlecha);
        lupaFlecha.setOnClickListener(this);

        progressBarLocations = findViewById(R.id.progressBarLocations);

        btnGuardarUbiManual = findViewById(R.id.btnGuardarUbiManual);
        btnGuardarUbiManual.setOnClickListener(this);

        //para mostrar el mapa
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        String url = "https://api.maptiler.com/maps/streets-v2/style.json?key="+getResources().getString(R.string.maptiles_api);
        mapView.getMapAsync(mapboxMap -> {
            mapboxMap.setStyle(url);
            mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(40.416775,-3.703790)).zoom(3.5).build());
        });

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        adapter.setNotifyOnChange(true);
        txtInput = findViewById(R.id.txtInput);
        txtInput.setAdapter(adapter);

        txtInput.setOnItemClickListener((parent, view, position, id) -> {
            btnGuardarUbiManual.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_guardar_click, null));
            btnGuardarUbiManual.setVisibility(Button.VISIBLE);
            double lat = hashMapCopy.get(((AppCompatTextView) view).getText()).getLat();
            double lon = hashMapCopy.get(((AppCompatTextView) view).getText()).getLon();

            mapView.getMapAsync(mapboxMap -> {
                mapboxMap.setStyle(url);
                mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(lat,lon)).zoom(15.0).build());
            });

            //cerrar el teclado
            View teclado = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(teclado.getWindowToken(), 0);
            }
        });

        txtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lupaFlecha.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_back, null));
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    apiGeoCode();
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
                break;
            case R.id.btnGuardarUbiManual:
                guardarEnDB(hashMap.get(txtInput.getText().toString()));
                Toast.makeText(getApplicationContext(), "Se ha guardado la ubi!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ListActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    public void apiGeoCode() {
        if(!txtInput.getText().equals("")) {
            String tempUrl = "https://api.geoapify.com/v1/geocode/autocomplete?text="+txtInput.getText()+"?lang=es?&apiKey="+getResources().getString(R.string.geoapify_api);
            LocalDateTime startDateTime = LocalDateTime.now();
            hashMapCopy = (HashMap<String, AparcamientoItem>) hashMap.clone();
            hashMap.clear();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, res -> {
                try {
                    JSONObject jsonObject = new JSONObject(res);
                    JSONArray jsonArray = jsonObject.getJSONArray("features");
                    if(jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject mainObject = jsonArray.getJSONObject(i);
                            JSONObject propertiesObject = mainObject.getJSONObject("properties");
                            double lat = propertiesObject.getDouble("lat");
                            double lon = propertiesObject.getDouble("lon");
                            String address = propertiesObject.getString("formatted");
                            AparcamientoItem aparcamientoItem = new AparcamientoItem(startDateTime.toString(), address, lat, lon);
                            hashMap.put(address, aparcamientoItem);
                        }
                        SubCargarListaLocations cargar = new SubCargarListaLocations();
                        cargar.execute();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {});
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        }
    }

    public void llenarLista(){
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, new ArrayList<>(hashMap.keySet()));
        txtInput.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void guardarEnDB(AparcamientoItem aparcamientoItem){
        MyDatabaseHelper myDB = new MyDatabaseHelper(this);
        myDB.addAparcamiento(LocalDateTime.parse(aparcamientoItem.getFechaHora()),aparcamientoItem.getUbicacion(),aparcamientoItem.getLat(),aparcamientoItem.getLon());
    }

    private class SubCargarListaLocations extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            progressBarLocations.setVisibility(ProgressBar.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            progressBarLocations.setVisibility(ProgressBar.INVISIBLE);
            llenarLista();
            super.onPostExecute(unused);
        }
    }
}