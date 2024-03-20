package Main;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;

public class ArchivedUbicacionAdapter extends RecyclerView.Adapter<ArchivedUbicacionAdapter.UbicacionViewHolder> implements Filterable {

    private ArchivedListActivity archivedListActivity;
    public ArrayList<UbicacionItem> ubicacionList;
    public ArrayList<UbicacionItem> ubicacionListFull;
    public ArrayList<String> folderList;
    public boolean isEnable = false;
    public ArrayList<UbicacionItem> selectList = new ArrayList<>();
    private int counter = 0;


    public ArchivedUbicacionAdapter(ArchivedListActivity archivedListActivity, ArrayList<UbicacionItem> ubicacionList, ArrayList<String> folderList) {
        this.ubicacionList = ubicacionList;
        this.ubicacionListFull = new ArrayList<>();
        this.archivedListActivity = archivedListActivity;
        this.folderList = folderList;
    }

    @NonNull
    @Override
    public UbicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(archivedListActivity);
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
            holder.relativeLayout.setBackground(archivedListActivity.getDrawable(R.drawable.card_loading_background));
        }
        else{
            holder.checkBox.setChecked(false);
            holder.relativeLayout.setBackground(archivedListActivity.getDrawable(R.drawable.card_main_background));
        }

        if (!isEnable) {
            holder.checkBox.setChecked(false);
            holder.relativeLayout.setBackground(archivedListActivity.getDrawable(R.drawable.item_click));
        }

    }

    public void editSelected(UbicacionItem ubicacionItem){
        notifyItemChanged(archivedListActivity.archivedUbicacionAdapter.selectList.get(0).getPosition());
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
            archivedListActivity.empty_imageview.setVisibility(View.VISIBLE);
            archivedListActivity.no_data.setVisibility(View.VISIBLE);
        }

        //para quitarle la ultima coma
        result = result.substring(0, result.length() - 1);
        disableContextualActionMode();
        return result;
    }

    private void enableContextualActionModeArchived(){
        isEnable=true;
        archivedListActivity.toolbar.getMenu().clear();
        archivedListActivity.toolbar.inflateMenu(R.menu.list_longpress_menu_archived);

        archivedListActivity.itemDeleteSelected = archivedListActivity.toolbar.getMenu().findItem(R.id.deleteSelected);
        archivedListActivity.itemDeleteSelected.setOnMenuItemClickListener(archivedListActivity);

        archivedListActivity.itemUnarchiveSelected = archivedListActivity.toolbar.getMenu().findItem(R.id.unarchiveSelected);
        archivedListActivity.itemUnarchiveSelected.setOnMenuItemClickListener(archivedListActivity);

        archivedListActivity.itemEditSelected = archivedListActivity.toolbar.getMenu().findItem(R.id.editSelected);
        archivedListActivity.itemEditSelected.setOnMenuItemClickListener(archivedListActivity);

        archivedListActivity.toolbar.setBackgroundColor(archivedListActivity.getColor(R.color.black));
        archivedListActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void updateCounter()
    {
        archivedListActivity.toolbarTitle.setText(counter+" Seleccionado");
    }

    public void disableContextualActionMode() {
        isEnable = false;
        archivedListActivity.toolbar.getMenu().clear();
        archivedListActivity.toolbar.inflateMenu(R.menu.list_archive_menu);

        archivedListActivity.itemSearch = archivedListActivity.toolbar.getMenu().findItem(R.id.searchUbicaciones);
        archivedListActivity.itemSearch.setOnMenuItemClickListener(archivedListActivity);

        archivedListActivity.itemAddLocation = archivedListActivity.toolbar.getMenu().findItem(R.id.add_location);
        archivedListActivity.itemAddLocation.setOnMenuItemClickListener(archivedListActivity);

        archivedListActivity.toolbar.setBackgroundColor(archivedListActivity.getColor(R.color.toolbarLight));
        archivedListActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        archivedListActivity.toolbarTitle.setText(archivedListActivity.folderName);
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
        TextView ubicacion_nombre, ubicacion_carpeta_id, ubicacion_descripcion, ubicacion_fecha_hora, ubicacion_hace_cuanto, ubicacion_lat, ubicacion_lon, ubicacion_id, ubicacion_position;
        CheckBox checkBox;
        RelativeLayout relativeLayout;

        /**
         * Aqui se inicializan las distintas variables y se hace un listener para cuando se pulse alguna ubicacion guardada
         */

        public UbicacionViewHolder(View itemView) {
            super(itemView);
            ubicacion_id = itemView.findViewById(R.id.ubicacion_id);
            ubicacion_position = itemView.findViewById(R.id.ubicacion_position);
            ubicacion_carpeta_id = itemView.findViewById(R.id.ubicacion_carpeta_id);
            ubicacion_nombre = itemView.findViewById(R.id.ubicacion_nombre);
            ubicacion_descripcion = itemView.findViewById(R.id.ubicacion_desc);
            ubicacion_fecha_hora = itemView.findViewById(R.id.ubicacion_fecha_hora);
            ubicacion_hace_cuanto = itemView.findViewById(R.id.ubicacion_hace_cuanto);
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
                    UbicacionItem ubicacionItem = new UbicacionItem(
                            1, 0, 0,
                            ubicacion_nombre.getText().toString(), null,null,
                            Double.parseDouble(ubicacion_lat.getText().toString()),
                            Double.parseDouble(ubicacion_lon.getText().toString()));
         /*           ChooseEngineDialog chooseEngineDialog = new ChooseEngineDialog(null,archivedListActivity, ubicacionItem);
                    chooseEngineDialog.show(archivedListActivity.getSupportFragmentManager(), "example dialog");
                    */
                    Uri mapUri = Uri.parse("geo:0,0?q=" + ubicacionItem.getLat() + "," + ubicacionItem.getLon() + "(Ubi: " + ubicacionItem.getNombre() + ")");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    archivedListActivity.startActivity(mapIntent);
                }
            });
        }

        private void onLongClick(View itemView) {
            itemView.setOnLongClickListener(v -> {
                if (!isEnable) {
                    enableContextualActionModeArchived();
                }
                makeSelection();
                return true;
            });
        }

        private void makeSelection() {

            UbicacionItem ubicacionItem = ubicacionListFull.get(getAdapterPosition());

            if(!checkBox.isChecked()){
                checkBox.setChecked(true);
                relativeLayout.setBackground(archivedListActivity.getDrawable(R.drawable.card_loading_background));
                selectList.add(ubicacionItem);
                counter++;
                updateCounter();
            }
            else{
                checkBox.setChecked(false);
                relativeLayout.setBackground(archivedListActivity.getDrawable(R.drawable.card_main_background));
                selectList.remove(ubicacionItem);
                counter--;
                updateCounter();
            }

            if(selectList.size() != 1){
                archivedListActivity.itemEditSelected.setVisible(false);
            }
            else {
                archivedListActivity.itemEditSelected.setVisible(true);
            }
        }
    }
}
