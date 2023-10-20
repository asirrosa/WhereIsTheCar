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

public class AparcamientoAdapter extends RecyclerView.Adapter<AparcamientoAdapter.MyViewHolder> implements Filterable {

    private Context context;
    private ArrayList<String> fechaHoraArray, ubicacionArray, latArray, lonArray;

    AparcamientoAdapter(Context context, ArrayList fechaHoraArray, ArrayList ubicacionArray, ArrayList latArray, ArrayList lonArray){
        this.context = context;
        this.fechaHoraArray = fechaHoraArray;
        this.ubicacionArray = ubicacionArray;
        this.latArray = latArray;
        this.lonArray = lonArray;
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ExampleItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(exampleListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (ExampleItem item : exampleListFull) {
                    if (item.getText2().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            exampleList.clear();
            exampleList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.aparcamiento_layout, parent, false);
        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    /**
     * Metodo para meterle los valores de los arrays al holder
     */
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.aparcamiento_fecha_hora.setText(String.valueOf(fechaHoraArray.get(position)));
        holder.aparcamiento_ubicacion.setText(String.valueOf(ubicacionArray.get(position)));
        holder.aparcamiento_lat.setText(String.valueOf(latArray.get(position)));
        holder.aparcamiento_lon.setText(String.valueOf(lonArray.get(position)));
    }

    @Override
    public int getItemCount() {
        return fechaHoraArray.size();
    }

    /**
     * Aqui se inicializan las distintas variables y se hace un listener para cuando se pulse algun aparcamiento guardado
     */
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView aparcamiento_ubicacion, aparcamiento_fecha_hora, aparcamiento_lat, aparcamiento_lon;
        LinearLayout mainLayout;
        CardView cardView;

        MyViewHolder(@NonNull View itemView) {
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
}
