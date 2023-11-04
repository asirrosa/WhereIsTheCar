package Main;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MapsActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnBuscarUbiManual,btnGuardarUbiManual;
    AutoCompleteTextView txtInput;
    ListView listaUbicacionesAPIManual;
    ProgressBar progressBar;
    ArrayAdapter adapter;
    HashMap<String,AparcamientoItem> hashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_layout);

        progressBar = findViewById(R.id.progressBarLocations);

        btnBuscarUbiManual = findViewById(R.id.btnBuscarUbiManual);
        btnBuscarUbiManual.setOnClickListener(this);

        btnGuardarUbiManual = findViewById(R.id.btnGuardarUbiManual);
        btnGuardarUbiManual.setOnClickListener(this);
        btnGuardarUbiManual.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.button_background_cargar,null));
        btnGuardarUbiManual.setEnabled(false);

        listaUbicacionesAPIManual = findViewById(R.id.listaUbicacionesAPIManual);
        listaUbicacionesAPIManual.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        adapter.setNotifyOnChange(true);
        txtInput = findViewById(R.id.txtInput);
        txtInput.setAdapter(adapter);
        txtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count > 0) {
                    apiGeoCode();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnGuardarUbiManual:
                guardarEnDB(hashMap.get(txtInput));
                Toast.makeText(getApplicationContext(), "Se ha guardado la ubi!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ListActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    public void apiGeoCode() {
        if(!txtInput.getText().equals("")) {
            String tempUrl = "https://api.geoapify.com/v1/geocode/autocomplete?text="+txtInput.getText()+"&apiKey="+getResources().getString(R.string.geoapify_api);
            LocalDateTime startDateTime = LocalDateTime.now();
            hashMap = new HashMap<>();
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
            progressBar.setVisibility(ProgressBar.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            llenarLista();
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            super.onPostExecute(unused);
        }
    }
}