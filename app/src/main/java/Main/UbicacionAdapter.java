package Main;

import android.content.Intent;
import android.net.Uri;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;

public class UbicacionAdapter extends RecyclerView.Adapter<UbicacionAdapter.UbicacionViewHolder> implements Filterable {

    private ListActivity listActivity;
    public ArrayList<UbicacionItem> ubicacionList;
    public ArrayList<UbicacionItem> ubicacionListFull;
    public ArrayList<UbicacionItem> folderList;
    public boolean isEnable = false;
    public ArrayList<UbicacionItem> selectList = new ArrayList<>();
    private int counter = 0;


    public UbicacionAdapter(ListActivity listActivity, ArrayList<UbicacionItem> ubicacionList,ArrayList<UbicacionItem> folderList) {
        this.ubicacionList = ubicacionList;
        this.ubicacionListFull = new ArrayList<>();
        this.listActivity = listActivity;
        this.folderList = folderList;
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
        holder.ubicacion_id.setText(String.valueOf(ubicacionItem.getId()));
        holder.ubicacion_position.setText(String.valueOf(position));

        LocalDateTime startDateTime = LocalDateTime.parse(String.valueOf(ubicacionItem.getFechaHora()));
        holder.ubicacion_hace_cuanto.setText(calculateTimeDiff(startDateTime));
        holder.ubicacion_fecha_hora.setText(String.valueOf(ubicacionItem.getFechaHora()));

        holder.ubicacion_nombre.setText(String.valueOf(ubicacionItem.getNombre()));
        holder.ubicacion_descripcion.setText(String.valueOf(ubicacionItem.getDescripcion()));
        holder.ubicacion_lat.setText(String.valueOf(ubicacionItem.getLat()));
        holder.ubicacion_lon.setText(String.valueOf(ubicacionItem.getLon()));

        if(selectList.contains(ubicacionItem)){
            holder.checkBox.setChecked(true);
            holder.relativeLayout.setBackground(listActivity.getDrawable(R.drawable.card_loading_background));
        }
        else{
            holder.checkBox.setChecked(false);
            holder.relativeLayout.setBackground(listActivity.getDrawable(R.drawable.card_main_background));
        }

        if (!isEnable) {
            holder.checkBox.setChecked(false);
            holder.relativeLayout.setBackground(listActivity.getDrawable(R.drawable.item_click));
        }

    }

    public void editSelected(UbicacionItem ubicacionItem){
        notifyItemChanged(listActivity.ubicacionAdapter.selectList.get(0).getPosition());
        ubicacionList.set(ubicacionList.indexOf(selectList.get(0)),ubicacionItem);
        ubicacionListFull.set(ubicacionListFull.indexOf(selectList.get(0)),ubicacionItem);
        disableContextualActionMode();
    }

    public String deleteSelectedFromScreen(){
        String result = "";
        for(int i = 0;i<selectList.size();i++){
            result = result + selectList.get(i).getId() + ",";
            ubicacionList.remove(selectList.get(i));
            ubicacionListFull.remove(selectList.get(i));
            //para que la pantalla muestre como los elementos se eliminan
            notifyItemRemoved(selectList.get(i).getPosition());
        }

        //para que muestre que no hay ninguna ubicacion en caso de que no la haya
        if(ubicacionList.isEmpty()){
            listActivity.empty_imageview.setVisibility(View.VISIBLE);
            listActivity.no_data.setVisibility(View.VISIBLE);
        }

        //para quitarle la ultima coma
        result = result.substring(0, result.length() - 1);
        disableContextualActionMode();
        return result;
    }

    private void enableContextualActionMode(){
        isEnable=true;
        listActivity.toolbar.getMenu().clear();
        listActivity.toolbar.inflateMenu(R.menu.list_longpress_menu);

        listActivity.itemDeleteSelected = listActivity.toolbar.getMenu().findItem(R.id.deleteSelected);
        listActivity.itemDeleteSelected.setOnMenuItemClickListener(listActivity);

        listActivity.itemArchiveSelected = listActivity.toolbar.getMenu().findItem(R.id.archiveSelected);
        listActivity.itemArchiveSelected.setOnMenuItemClickListener(listActivity);

        listActivity.itemEditSelected = listActivity.toolbar.getMenu().findItem(R.id.editSelected);
        listActivity.itemEditSelected.setOnMenuItemClickListener(listActivity);

        listActivity.toolbar.setBackgroundColor(listActivity.getColor(R.color.black));
        listActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void updateCounter()
    {
        listActivity.toolbarTitle.setText(counter+" Seleccionado");
    }

    public void disableContextualActionMode() {
        isEnable = false;
        listActivity.toolbar.getMenu().clear();
        listActivity.toolbar.inflateMenu(R.menu.list_menu);

        listActivity.itemSearch = listActivity.toolbar.getMenu().findItem(R.id.searchUbicaciones);
        listActivity.itemSearch.setOnMenuItemClickListener(listActivity);

        listActivity.itemAddLocation = listActivity.toolbar.getMenu().findItem(R.id.add_location);
        listActivity.itemAddLocation.setOnMenuItemClickListener(listActivity);

        listActivity.itemArchived = listActivity.toolbar.getMenu().findItem(R.id.archived);
        listActivity.itemArchived.setOnMenuItemClickListener(listActivity);

        listActivity.toolbar.setBackgroundColor(listActivity.getColor(R.color.toolbarLight));
        listActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listActivity.toolbarTitle.setText("Ubicaciones");
        counter = 0;
        selectList.clear();
        notifyDataSetChanged();
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

    private String calculateTimeDiff(LocalDateTime startDateTime) {
        String result = "";
        LocalDateTime endDateTime;
        endDateTime = LocalDateTime.now();
        LocalDateTime tempDateTime = LocalDateTime.from(startDateTime);
        long years = startDateTime.until(endDateTime, ChronoUnit.YEARS);
        tempDateTime = tempDateTime.plusYears(years);
        long months = tempDateTime.until(endDateTime, ChronoUnit.MONTHS);
        tempDateTime = tempDateTime.plusMonths(months);
        long days = tempDateTime.until(endDateTime, ChronoUnit.DAYS);
        tempDateTime = tempDateTime.plusDays(days);
        long hours = tempDateTime.until(endDateTime, ChronoUnit.HOURS);
        tempDateTime = tempDateTime.plusHours(hours);
        long minutes = tempDateTime.until(endDateTime, ChronoUnit.MINUTES);

        if (years == 0) {
            if (months == 0) {
                if (days == 0) {
                    if (hours == 0) {
                        if (minutes == 0) {
                            result = "Hace unos instantes";
                        } else {
                            result = "Hace " + minutes + " min";
                        }
                    } else {
                        result = "Hace " + hours + " h";
                    }
                } else if (days == 1) {
                    result = "Hace 1 día";
                } else {
                    result = "Hace " + days + " días";
                }
            } else if (months == 1) {
                result = "Hace 1 mes";
            } else {
                result = "Hace " + months + " meses";
            }
        } else if (years == 1) {
            result = "Hace 1 año";
        } else {
            result = "Hace " + years + " años";
        }

        return result;
    }

    public class UbicacionViewHolder extends RecyclerView.ViewHolder {
        TextView ubicacion_nombre, ubicacion_descripcion, ubicacion_fecha_hora, ubicacion_hace_cuanto, ubicacion_lat, ubicacion_lon, ubicacion_id, ubicacion_position;
        CheckBox checkBox;
        RelativeLayout relativeLayout;

        /**
         * Aqui se inicializan las distintas variables y se hace un listener para cuando se pulse alguna ubicacion guardada
         */

        public UbicacionViewHolder(View itemView) {
            super(itemView);
            ubicacion_id = itemView.findViewById(R.id.ubicacion_id);
            ubicacion_position = itemView.findViewById(R.id.ubicacion_position);
            ubicacion_fecha_hora = itemView.findViewById(R.id.ubicacion_fecha_hora);
            ubicacion_hace_cuanto = itemView.findViewById(R.id.ubicacion_hace_cuanto);
            ubicacion_nombre = itemView.findViewById(R.id.ubicacion_nombre);
            ubicacion_descripcion = itemView.findViewById(R.id.ubicacion_desc);
            ubicacion_lat = itemView.findViewById(R.id.ubicacion_lat);
            ubicacion_lon = itemView.findViewById(R.id.ubicacion_lon);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);
            checkBox = itemView.findViewById(R.id.checkSelected);

            onClick(itemView);
            onLongClick(itemView);
        }

        private void onClick(View itemView) {
            itemView.setOnClickListener(v -> {
                if (isEnable) {
                    makeSelection();
                } else {
                    //le pongo un id cualquiera porque me da igual en este caso solo lo utilizo para pasar info al google maps
                    UbicacionItem ubicacionItem = new UbicacionItem(1, 0,0,ubicacion_nombre.getText().toString(), null,null,
                            Double.parseDouble(ubicacion_lat.getText().toString()),
                            Double.parseDouble(ubicacion_lon.getText().toString()));
                    /*ChooseEngineDialog chooseEngineDialog = new ChooseEngineDialog(listActivity, null, ubicacionItem);
                    chooseEngineDialog.show(listActivity.getSupportFragmentManager(), "example dialog");*/
                    Uri mapUri = Uri.parse("geo:0,0?q=" + ubicacionItem.getLat() + "," + ubicacionItem.getLon() + "(Ubi: " + ubicacionItem.getNombre() + ")");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    listActivity.startActivity(mapIntent);
                }
            });
        }

        private void onLongClick(View itemView) {
            itemView.setOnLongClickListener(v -> {
                if (!isEnable) {
                    enableContextualActionMode();
                }
                makeSelection();
                return true;
            });
        }

        private void makeSelection() {

            UbicacionItem ubicacionItem = ubicacionListFull.get(getAdapterPosition());

            if(!checkBox.isChecked()){
                checkBox.setChecked(true);
                relativeLayout.setBackground(listActivity.getDrawable(R.drawable.card_loading_background));
                selectList.add(ubicacionItem);
                counter++;
                updateCounter();
            }
            else{
                checkBox.setChecked(false);
                relativeLayout.setBackground(listActivity.getDrawable(R.drawable.card_main_background));
                selectList.remove(ubicacionItem);
                counter--;
                updateCounter();
            }

            if(selectList.size() != 1){
                listActivity.itemEditSelected.setVisible(false);
            }
            else {
                listActivity.itemEditSelected.setVisible(true);
            }
        }
    }
}
