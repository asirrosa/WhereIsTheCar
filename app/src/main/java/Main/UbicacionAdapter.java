package Main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

public class UbicacionAdapter extends RecyclerView.Adapter<UbicacionAdapter.UbicacionViewHolder> implements Filterable {

    private ListActivity listActivity;
    public ArrayList<UbicacionItem> ubicacionList;
    public ArrayList<UbicacionItem> ubicacionListFull;

    public class UbicacionViewHolder extends RecyclerView.ViewHolder {
        TextView ubicacion_nombre, ubicacion_descripcion, ubicacion_fecha_hora, ubicacion_lat, ubicacion_lon;
        LinearLayout mainLayout;
        RelativeLayout relativeLayout;

        /**
         * Aqui se inicializan las distintas variables y se hace un listener para cuando se pulse alguna ubicacion guardada
         */
        UbicacionViewHolder(View itemView) {
            super(itemView);
            ubicacion_fecha_hora = itemView.findViewById(R.id.ubicacion_fecha_hora);
            ubicacion_nombre = itemView.findViewById(R.id.ubicacion_nombre);
            ubicacion_descripcion = itemView.findViewById(R.id.ubicacion_desc);
            ubicacion_lat = itemView.findViewById(R.id.ubicacion_lat);
            ubicacion_lon = itemView.findViewById(R.id.ubicacion_lon);

            mainLayout = itemView.findViewById(R.id.mainLayout);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);
            relativeLayout.setOnClickListener(view -> {
                UbicacionItem ubicacionItem = new UbicacionItem("",ubicacion_nombre.getText().toString(),"",
                        Double.parseDouble(ubicacion_lat.getText().toString()),
                        Double.parseDouble(ubicacion_lon.getText().toString()));
                ChooseEngineDialog chooseEngineDialog = new ChooseEngineDialog(listActivity, ubicacionItem);
                chooseEngineDialog.show(listActivity.getSupportFragmentManager(), "example dialog");
            });
        }
    }

    public UbicacionAdapter(ListActivity listActivity, ArrayList<UbicacionItem> ubicacionList){
        this.ubicacionList = ubicacionList;
        this.ubicacionListFull = new ArrayList<>();
        this.listActivity = listActivity;
    }

    @NonNull
    @Override
    public UbicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(listActivity);
        View view = inflater.inflate(R.layout.list_card_layout, parent, false);
        return new UbicacionViewHolder(view);
    }

    /**
     * Metodo para meterle los valores de los arrays al holder
     */
    @Override
    public void onBindViewHolder(@NonNull final UbicacionViewHolder holder, final int position) {
        UbicacionItem ubicacionItem = ubicacionList.get(position);
        holder.ubicacion_fecha_hora.setText(String.valueOf(ubicacionItem.getFechaHora()));
        holder.ubicacion_nombre.setText(String.valueOf(ubicacionItem.getNombre()));
        holder.ubicacion_descripcion.setText(String.valueOf(ubicacionItem.getDescripcion()));
        holder.ubicacion_lat.setText(String.valueOf(ubicacionItem.getLat()));
        holder.ubicacion_lon.setText(String.valueOf(ubicacionItem.getLon()));
    }

    @Override
    public int getItemCount() {
        return ubicacionList.size();
    }

    @Override
    public Filter getFilter() {
        return ubicacionFilter;
    }

    private Filter ubicacionFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<UbicacionItem> filteredList = new ArrayList<>();
            if (charSequence.toString().isEmpty()) {
                filteredList.addAll(ubicacionListFull);
            } else {
                for (UbicacionItem ubicacionItem : ubicacionListFull) {
                    if (ubicacionItem.getNombre().toLowerCase().contains(charSequence.toString().toLowerCase()) ||
                            ubicacionItem.getDescripcion().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        filteredList.add(ubicacionItem);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults filterResults) {
            ubicacionList.clear();
            ubicacionList.addAll((Collection<? extends UbicacionItem>) filterResults.values);
            notifyDataSetChanged();
        }
    };
}
