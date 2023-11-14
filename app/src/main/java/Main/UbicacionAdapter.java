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
import java.util.ArrayList;
import java.util.Collection;

public class UbicacionAdapter extends RecyclerView.Adapter<UbicacionAdapter.UbicacionViewHolder> implements Filterable {

    private Context context;
    public ArrayList<UbicacionItem> ubicacionList;
    public ArrayList<UbicacionItem> ubicacionListFull;

    public class UbicacionViewHolder extends RecyclerView.ViewHolder {
        TextView ubicacion_nombre, ubicacion_descripcion, ubicacion_fecha_hora, ubicacion_lat, ubicacion_lon;
        LinearLayout mainLayout;
        CardView cardView;

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
            cardView = itemView.findViewById(R.id.cardView);
            cardView.setOnClickListener(view -> {
                //Para que cada vez que clickes en un item te mande a la ubicación de google maps
                Uri mapUri = Uri.parse("geo:0,0?q="+ubicacion_lat.getText()+","+ubicacion_lon.getText()+"(Ubi: "+ ubicacion_nombre.getText()+")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                context.startActivity(mapIntent);
            });
        }
    }

    public UbicacionAdapter(Context context, ArrayList<UbicacionItem> ubicacionList){
        this.ubicacionList = ubicacionList;
        this.ubicacionListFull = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public UbicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
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
