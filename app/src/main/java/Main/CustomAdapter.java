package Main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private Context context;
    private ArrayList fechaHoraArray, ubicacionArray, latArray, lonArray;

    CustomAdapter(Context context, ArrayList fechaHoraArray, ArrayList ubicacionArray, ArrayList latArray, ArrayList lonArray){
        this.context = context;
        this.fechaHoraArray = fechaHoraArray;
        this.ubicacionArray = ubicacionArray;
        this.latArray = latArray;
        this.lonArray = lonArray;
    }

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
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Para que cada vez que clickes en un item te mande a la ubicación de google maps
                    Uri mapUri = Uri.parse("geo:0,0?q="+aparcamiento_lat.getText()+","+aparcamiento_lon.getText()+"(Aparcamiento)");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    context.startActivity(mapIntent);
                }
            });
        }
    }
}
