package Main;

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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

public class UbicacionAdapter extends RecyclerView.Adapter<UbicacionAdapter.UbicacionViewHolder> implements Filterable {

    private ListActivity listActivity;
    public ArrayList<UbicacionItem> ubicacionList;
    public ArrayList<UbicacionItem> ubicacionListFull;
    public boolean isEnable = false;
    public ArrayList<UbicacionItem> selectList = new ArrayList<>();
    private int counter = 0;


    public UbicacionAdapter(ListActivity listActivity, ArrayList<UbicacionItem> ubicacionList) {
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

        if (!isEnable) {
            holder.checkBox.setChecked(false);
            holder.relativeLayout.setBackground(listActivity.getDrawable(R.drawable.item_click));
        }
        /*holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ActionMode.Callback callback = new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        enableContextualActionMode();
                        return false;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        isEnable = true;
                        makeSelection(holder);
                        if (isEnable) {
                            enableContextualActionMode();
                        }
                        return true;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        isEnable = false;
                    }
                };
                return true;
            }
        });*/
    }

    private void makeSelection(UbicacionViewHolder holder) {

        UbicacionItem ubicacionItem = ubicacionListFull.get(holder.getAdapterPosition());

        if(!holder.checkBox.isChecked()){
            holder.checkBox.setChecked(true);
            holder.relativeLayout.setBackground(listActivity.getDrawable(R.drawable.card_loading_background));
            selectList.add(ubicacionItem);
            counter++;
            updateCounter();
        }
        else{
            holder.checkBox.setChecked(false);
            holder.relativeLayout.setBackground(listActivity.getDrawable(R.drawable.item_click));
            selectList.remove(ubicacionItem);
            counter--;
            updateCounter();
        }
    }

    public void enableContextualActionMode(){
        isEnable=true;
        listActivity.toolbar.getMenu().clear();
        listActivity.toolbar.inflateMenu(R.menu.list_longpress_menu);
        listActivity.toolbar.setBackgroundColor(listActivity.getColor(R.color.black));
        listActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void updateCounter()
    {
        listActivity.toolbarTitle.setText(counter+" Item Selected");
    }

    public void disableContextualActionMode() {
        isEnable = false;
        listActivity.toolbar.getMenu().clear();
        listActivity.toolbar.inflateMenu(R.menu.list_menu);
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

    public class UbicacionViewHolder extends RecyclerView.ViewHolder {
        TextView ubicacion_nombre, ubicacion_descripcion, ubicacion_fecha_hora, ubicacion_lat, ubicacion_lon;
        CheckBox checkBox;
        RelativeLayout relativeLayout;

        /**
         * Aqui se inicializan las distintas variables y se hace un listener para cuando se pulse alguna ubicacion guardada
         */

        public UbicacionViewHolder(View itemView) {
            super(itemView);
            ubicacion_fecha_hora = itemView.findViewById(R.id.ubicacion_fecha_hora);
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
                    UbicacionItem ubicacionItem = new UbicacionItem("", ubicacion_nombre.getText().toString(), "",
                            Double.parseDouble(ubicacion_lat.getText().toString()),
                            Double.parseDouble(ubicacion_lon.getText().toString()));
                    ChooseEngineDialog chooseEngineDialog = new ChooseEngineDialog(listActivity, ubicacionItem);
                    chooseEngineDialog.show(listActivity.getSupportFragmentManager(), "example dialog");
                }
            });
        }

        private void onLongClick(View itemView) {
            itemView.setOnLongClickListener(v -> {
                makeSelection();
                if (!isEnable) {
                    enableContextualActionMode();
                }
                return true;
            });
        }

        private void makeSelection() {

            UbicacionItem ubicacionItem = ubicacionListFull.get(getAdapterPosition());

            if(!checkBox.isChecked()){
                checkBox.setChecked(true);
                relativeLayout.setBackground(listActivity.getDrawable(R.drawable.card_loading_background));
                //notifyItemChanged(getAdapterPosition());
                selectList.add(ubicacionItem);
                counter++;
                updateCounter();
            }
            else{
                checkBox.setChecked(false);
                relativeLayout.setBackground(listActivity.getDrawable(R.drawable.card_main_background));
                //notifyItemChanged(getAdapterPosition());
                selectList.remove(ubicacionItem);
                counter--;
                updateCounter();
            }
        }
    }
}
