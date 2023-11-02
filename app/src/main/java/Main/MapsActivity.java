package Main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

public class MapsActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnBuscarUbiManual,btnGuardarUbiManual;
    TextView txtInput;
    ListView listaUbicacionesAPIManual;

    ArrayList<AparcamientoItem> listaAparcamientos = new ArrayList<>();
    ArrayList<String> listaNombres = new ArrayList<>();

    int posicion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_layout);

        btnBuscarUbiManual = findViewById(R.id.btnBuscarUbiManual);
        btnBuscarUbiManual.setOnClickListener(this);

        btnGuardarUbiManual = findViewById(R.id.btnGuardarUbiManual);
        btnGuardarUbiManual.setOnClickListener(this);
        btnGuardarUbiManual.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.button_background_cargar,null));

        txtInput = findViewById(R.id.txtInput);

        listaUbicacionesAPIManual = findViewById(R.id.listaUbicacionesAPIManual);
        listaUbicacionesAPIManual.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnBuscarUbiManual:
                try {
                    apiGeoCode();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;

            case R.id.btnGuardarUbiManual:
                guardarEnDB(listaAparcamientos.get(posicion));
                Toast.makeText(getApplicationContext(), "Se ha guardado la ubi!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ListActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    public void llenarListaUbicaciones(){
        if(listaNombres.size() == 0){
            txtInput.setHint("Escribe la ubicación deseada");
            txtInput.setText("");
            Toast.makeText(getApplicationContext(), "No se ha encontrado ninguna ubicación con ese nombre", Toast.LENGTH_SHORT).show();
        }
        else {
            listaUbicacionesAPIManual.setVisibility(ListView.VISIBLE);
            ArrayAdapter adapter = new ArrayAdapter(this,R.layout.manual_location_layout, R.id.txtView, listaNombres);
            listaUbicacionesAPIManual.setAdapter(adapter);
            listaUbicacionesAPIManual.setOnItemClickListener((parent, view, position, l) -> {
                btnGuardarUbiManual.setEnabled(true);
                btnGuardarUbiManual.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.button_guardar_click,null));
                posicion = position;
            });
        }
    }

    public void apiGeoCode() throws IOException, InterruptedException {
        if(!txtInput.getText().toString().equals("")) {
            String tempUrl = "https://geocode.maps.co/search?q={" + txtInput.getText() + "}";
            LocalDateTime startDateTime = LocalDateTime.now();
            listaAparcamientos = new ArrayList<>();
            listaNombres = new ArrayList<>();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, res -> {
                try {
                    JSONArray jsonArray = new JSONArray(res);
                    if(jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            double lat = jsonObject.getDouble("lat");
                            double lon = jsonObject.getDouble("lon");
                            String address = jsonObject.getString("display_name");
                            AparcamientoItem aparcamientoItem = new AparcamientoItem(startDateTime.toString(), address, lat, lon);
                            listaAparcamientos.add(aparcamientoItem);
                            listaNombres.add(address);
                            llenarListaUbicaciones();
                        }
                    }
                    else{
                        txtInput.setText("");
                        txtInput.setHint("Escribe la ubicación deseada");
                        txtInput.setHintTextColor(Color.BLACK);
                        Toast.makeText(getApplicationContext(), "No se ha encontrado ninguna ubicación con ese nombre", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                txtInput.setHint("Por favor escribe algo");
            });
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        }
        else{
            txtInput.setHint("Por favor escribe algo");
            txtInput.setHintTextColor(Color.RED);
        }
    }

    public void guardarEnDB(AparcamientoItem aparcamientoItem){
        MyDatabaseHelper myDB = new MyDatabaseHelper(this);
        myDB.addAparcamiento(LocalDateTime.parse(aparcamientoItem.getFechaHora()),aparcamientoItem.getUbicacion(),aparcamientoItem.getLat(),aparcamientoItem.getLon());
    }
}