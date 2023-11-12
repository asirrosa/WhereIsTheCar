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
import com.mapbox.mapboxsdk.maps.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnGuardarUbiManual;
    AutoCompleteTextView txtInput;
    MapView mapView;
    ProgressBar progressBarLocations;
    BusquedaAdapter busquedaAdapter;
    BusquedaItem busquedaItemGuardar;
    RecyclerView recyclerBusqueda;
    ArrayAdapter adapter;
    ImageView lupaFlecha;
    ImageView empty_imageview;
    TextView no_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //esta linea importante porque sino no inicializa el mapa
        Mapbox.getInstance(this);
        setContentView(R.layout.maps_layout);

        recyclerBusqueda = findViewById(R.id.recyclerBusqueda);

        lupaFlecha = findViewById(R.id.lupaFlecha);
        lupaFlecha.setOnClickListener(this);

        progressBarLocations = findViewById(R.id.progressBarLocations);

        btnGuardarUbiManual = findViewById(R.id.btnGuardarUbiManual);
        btnGuardarUbiManual.setOnClickListener(this);
        btnGuardarUbiManual.setEnabled(false);

        //para mostrar el mapa
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(mapboxMap -> {
            mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(40.416775,-3.703790)).zoom(3.5).build());
        });

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        adapter.setNotifyOnChange(true);
        txtInput = findViewById(R.id.txtInput);

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
                    SubCargarListaLocations cargar = new SubCargarListaLocations();
                    cargar.execute();
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

    private void storeDataInArrays() {
        if (busquedaAdapter.busquedaList.size() == 0) {
            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            recyclerBusqueda.setAdapter(busquedaAdapter);
            recyclerBusqueda.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    public void suggestApiCall() {
        String tempUrl = "https://api.mapbox.com/search/searchbox/v1/suggest?q=" + txtInput.getText() + "&language=es&session_token=" + getString(R.string.mapbox_session_token) + "&access_token=" + getString(R.string.mapbox_access_token);
        ArrayList<BusquedaItem> busquedaList = new ArrayList<>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, res -> {
            try {
                JSONObject jsonObject = new JSONObject(res);
                JSONArray suggestions = jsonObject.getJSONArray("suggestions");
                String address;
                if (suggestions.length() > 0) {
                    for (int i = 0; i < suggestions.length(); i++) {
                        JSONObject mainObject = suggestions.getJSONObject(i);
                        if (mainObject.getString("full_address") == null) {
                            address = mainObject.getString("place_formatted");
                        } else {
                            address = mainObject.getString("full_address");
                        }
                        String name = mainObject.getString("name");
                        String mapboxId = mainObject.getString("mapbox_id");
                        BusquedaItem busquedaItem = new BusquedaItem(name, address, mapboxId, 0.0, 0.0);
                        busquedaList.add(busquedaItem);
                    }
                    busquedaAdapter = new BusquedaAdapter(this,busquedaList);
                    storeDataInArrays();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);

    }

    public void guardarEnDB(){
        MyDatabaseHelper myDB = new MyDatabaseHelper(this);
        LocalDateTime startDateTime = LocalDateTime.now();
        //aqui tengo que añadir la latitud y la longitud
        myDB.addUbicacion(startDateTime, busquedaItemGuardar.getNombre(), busquedaItemGuardar.getLat(), busquedaItemGuardar.getLon());
    }

    private class SubCargarListaLocations extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            progressBarLocations.setVisibility(ProgressBar.VISIBLE);
            suggestApiCall();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            progressBarLocations.setVisibility(ProgressBar.INVISIBLE);
            super.onPostExecute(unused);
        }
    }
}