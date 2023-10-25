package Main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collection;

public class AparcamientoAdapter extends RecyclerView.Adapter<AparcamientoAdapter.AparcamientoViewHolder> implements Filterable {

    private Context context;
    public ArrayList<AparcamientoItem> aparcamientoList;
    public ArrayList<AparcamientoItem> aparcamientoListFull;

    public class AparcamientoViewHolder extends RecyclerView.ViewHolder {
        TextView aparcamiento_ubicacion, aparcamiento_fecha_hora, aparcamiento_lat, aparcamiento_lon;
        LinearLayout mainLayout;
        CardView cardView;

        /**
         * Aqui se inicializan las distintas variables y se hace un listener para cuando se pulse algun aparcamiento guardado
         */
        AparcamientoViewHolder(View itemView) {
            super(itemView);
            aparcamiento_fecha_hora = itemView.findViewById(R.id.aparcamiento_fecha_hora);
            aparcamiento_ubicacion = itemView.findViewById(R.id.aparcamiento_ubicacion);
            aparcamiento_lat = itemView.findViewById(R.id.aparcamiento_lat);
            aparcamiento_lon = itemView.findViewById(R.id.aparcamiento_lon);

            mainLayout = itemView.findViewById(R.id.mainLayout);
            cardView = itemView.findViewById(R.id.cardView);
            cardView.setOnClickListener(view -> {
                //Para que cada vez que clickes en un item te mande a la ubicación de google maps
                Uri mapUri = Uri.parse("geo:0,0?q="+aparcamiento_lat.getText()+","+aparcamiento_lon.getText()+"(Aparcamiento "+ aparcamiento_ubicacion.getText()+")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                context.startActivity(mapIntent);
            });
        }
    }

    public AparcamientoAdapter(Context context, ArrayList<AparcamientoItem> aparcamientoList){
        this.aparcamientoList = aparcamientoList;
        this.aparcamientoListFull = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public AparcamientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.aparcamiento_layout, parent, false);
        return new AparcamientoViewHolder(view);
    }

    /**
     * Metodo para meterle los valores de los arrays al holder
     */
    @Override
    public void onBindViewHolder(@NonNull final AparcamientoViewHolder holder, final int position) {
        AparcamientoItem aparcamientoItem = aparcamientoList.get(position);
        holder.aparcamiento_fecha_hora.setText(String.valueOf(aparcamientoItem.getFechaHora()));
        holder.aparcamiento_ubicacion.setText(String.valueOf(aparcamientoItem.getUbicacion()));
        holder.aparcamiento_lat.setText(String.valueOf(aparcamientoItem.getLat()));
        holder.aparcamiento_lon.setText(String.valueOf(aparcamientoItem.getLon()));
    }

    @Override
    public int getItemCount() {
        return aparcamientoList.size();
    }

    @Override
    public Filter getFilter() {
        return aparcamientoFilter;
    }

    private Filter aparcamientoFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<AparcamientoItem> filteredList = new ArrayList<>();

            if (charSequence.toString().isEmpty()) {
                filteredList.addAll(aparcamientoListFull);
            } else {
                for (AparcamientoItem aparcamientoItem : aparcamientoListFull) {
                    if (aparcamientoItem.getUbicacion().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        filteredList.add(aparcamientoItem);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults filterResults) {
            aparcamientoList.clear();
            aparcamientoList.addAll((Collection<? extends AparcamientoItem>) filterResults.values);
            notifyDataSetChanged();
        }
    };




}
