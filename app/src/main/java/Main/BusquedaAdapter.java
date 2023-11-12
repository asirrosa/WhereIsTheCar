package Main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class BusquedaAdapter extends RecyclerView.Adapter<BusquedaAdapter.BusquedaViewHolder> {

    public ArrayList<BusquedaItem> busquedaList;
    private MapsActivity mapsActivity;

    public class BusquedaViewHolder extends RecyclerView.ViewHolder {
        TextView ubicacion_nombre, ubicacion_descripcion, ubicacion_mapboxId, ubicacion_lat, ubicacion_lon;
        LinearLayout mainLayout;
        CardView cardView;

        /**
         * Aqui se inicializan las distintas variables y se hace un listener para cuando se pulse alguna ubicacion guardada
         */
        BusquedaViewHolder(View itemView) {
            super(itemView);
            ubicacion_nombre = itemView.findViewById(R.id.ubicacion_nombre);
            ubicacion_descripcion = itemView.findViewById(R.id.ubicacion_descripcion);
            ubicacion_mapboxId = itemView.findViewById(R.id.ubicacion_mapboxId);
            ubicacion_lat = itemView.findViewById(R.id.ubicacion_lat);
            ubicacion_lon = itemView.findViewById(R.id.ubicacion_lon);

            mainLayout = itemView.findViewById(R.id.mainLayout);
            cardView = itemView.findViewById(R.id.cardView);
            cardView.setOnClickListener(view -> {
                retrieveApiCall();
            });
        }

        private void retrieveApiCall() {
            String tempUrl = "https://api.mapbox.com/search/searchbox/v1/retrieve/"+ubicacion_mapboxId;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, res -> {
                try {
                    JSONObject jsonObject = new JSONObject(res);
                    JSONArray suggestions = jsonObject.getJSONArray("features");
                    JSONObject mainObject = suggestions.getJSONObject(0);
                    JSONObject geometryObject = mainObject.getJSONObject("geometry");
                    JSONArray coordinatesObject = geometryObject.getJSONArray("coordinates");
                    Double lat = (Double) coordinatesObject.get(0);
                    Double lon = (Double) coordinatesObject.get(1);
                    BusquedaItem busquedaItem = new BusquedaItem(ubicacion_nombre.getText().toString(),
                            ubicacion_descripcion.getText().toString(),ubicacion_mapboxId.getText().toString(), lat, lon);
                    mapsActivity.busquedaItemGuardar = busquedaItem;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {});
            RequestQueue requestQueue = Volley.newRequestQueue(mapsActivity);
            requestQueue.add(stringRequest);
        }
    }

    public BusquedaAdapter(MapsActivity mapsActivity, ArrayList<BusquedaItem> busquedaList){
        this.mapsActivity = mapsActivity;
        this.busquedaList = busquedaList;
    }

    @NonNull
    @Override
    public BusquedaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mapsActivity);
        View view = inflater.inflate(R.layout.maps_card_layout, parent, false);
        return new BusquedaViewHolder(view);
    }

    /**
     * Metodo para meterle los valores de los arrays al holder
     */
    @Override
    public void onBindViewHolder(@NonNull final BusquedaViewHolder holder, final int position) {
        BusquedaItem busquedaItem = busquedaList.get(position);
        holder.ubicacion_nombre.setText(String.valueOf(busquedaItem.getNombre()));
        holder.ubicacion_descripcion.setText(String.valueOf(busquedaItem.getDescripcion()));
        holder.ubicacion_mapboxId.setText(String.valueOf(busquedaItem.getMapboxId()));
        holder.ubicacion_lat.setText(String.valueOf(busquedaItem.getLat()));
        holder.ubicacion_lon.setText(String.valueOf(busquedaItem.getLon()));
    }

    @Override
    public int getItemCount() {
        return busquedaList.size();
    }
}
