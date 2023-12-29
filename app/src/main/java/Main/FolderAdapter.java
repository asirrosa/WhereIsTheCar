package Main;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.UbicacionViewHolder> implements Filterable {

    private FolderActivity folderActivity;
    public ArrayList<UbicacionItem> folderList;
    public ArrayList<UbicacionItem> folderListFull;
    public boolean isEnable = false;
    public ArrayList<UbicacionItem> selectList = new ArrayList<>();
    private int counter = 0;


    public FolderAdapter(FolderActivity folderActivity, ArrayList<UbicacionItem> ubicacionList) {
        this.folderList = ubicacionList;
        this.folderListFull = new ArrayList<>();
        this.folderActivity = folderActivity;
    }

    @NonNull
    @Override
    public UbicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(folderActivity);
        View view = inflater.inflate(R.layout.list_folder_layout, parent, false);
        return new UbicacionViewHolder(view);
    }

    /**
     * Metodo para meterle los valores de los arrays al holder
     */
    @Override
    public void onBindViewHolder(@NonNull final UbicacionViewHolder holder, final int position) {
        UbicacionItem folderItem = folderList.get(position);
        holder.folderName.setText(String.valueOf(folderItem.getNombre()));
        holder.folderPosition.setText(String.valueOf(folderItem.getPosition()));

        if(selectList.contains(folderItem)){
            holder.checkBox.setChecked(true);
            holder.relativeLayout.setBackground(folderActivity.getDrawable(R.drawable.card_loading_background));
        }
        else{
            holder.checkBox.setChecked(false);
            holder.relativeLayout.setBackground(folderActivity.getDrawable(R.drawable.card_main_background));
        }

        if (!isEnable) {
            holder.checkBox.setChecked(false);
            holder.relativeLayout.setBackground(folderActivity.getDrawable(R.drawable.item_click));
        }
    }

    public ArrayList<String> getSelectedFoldersName(){
        ArrayList<String> result = new ArrayList<>();
        for(int i = 0;i<selectList.size();i++){
            result.add(selectList.get(i).getNombre());
        }
        return result;
    }

    public void deleteSelectedFolders(){

        for(int i = 0;i<selectList.size();i++){
            folderList.remove(selectList.get(i));
            folderListFull.remove(selectList.get(i));
            //para que la pantalla muestre como los elementos se eliminan
            notifyItemRemoved(selectList.get(i).getPosition());
        }

        //para que muestre que no hay ninguna ubicacion en caso de que no la haya
        if(folderList.isEmpty()){
            folderActivity.empty_imageview.setVisibility(View.VISIBLE);
            folderActivity.no_data.setVisibility(View.VISIBLE);
        }

        disableContextualActionMode();
        Toast.makeText(folderActivity, "Se han borrado las carpetas seleccionadas", Toast.LENGTH_SHORT).show();
    }

    public void enableContextualActionMode(){
        isEnable=true;
        folderActivity.toolbar.getMenu().clear();
        folderActivity.toolbar.inflateMenu(R.menu.list_folder_longpress_menu);

        folderActivity.itemDeleteSelected = folderActivity.toolbar.getMenu().findItem(R.id.deleteSelected);
        folderActivity.itemDeleteSelected.setOnMenuItemClickListener(folderActivity);

        folderActivity.toolbar.setBackgroundColor(folderActivity.getColor(R.color.black));
        folderActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void updateCounter()
    {
        folderActivity.toolbarTitle.setText(counter+" Item Selected");
    }

    public void disableContextualActionMode() {
        isEnable = false;
        folderActivity.toolbar.getMenu().clear();
        folderActivity.toolbar.inflateMenu(R.menu.list_folder_menu);

        folderActivity.itemSearch = folderActivity.toolbar.getMenu().findItem(R.id.searchUbicaciones);
        folderActivity.itemSearch.setOnMenuItemClickListener(folderActivity);

        folderActivity.toolbar.setBackgroundColor(folderActivity.getColor(R.color.toolbarLight));
        folderActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        folderActivity.toolbarTitle.setText("Carpetas archivadas");
        counter = 0;
        selectList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return folderList.size();
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
                filteredList.addAll(folderListFull);
            } else {
                for (UbicacionItem ubicacionItem : folderListFull) {
                    if (ubicacionItem.getNombre().toLowerCase().contains(charSequence.toString().toLowerCase())){
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
            folderList.clear();
            folderList.addAll((Collection<? extends UbicacionItem>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class UbicacionViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        RelativeLayout relativeLayout;
        TextView folderName,folderPosition;

        /**
         * Aqui se inicializan las distintas variables y se hace un listener para cuando se pulse alguna ubicacion guardada
         */

        public UbicacionViewHolder(View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            folderPosition = itemView.findViewById(R.id.folderPosition);
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
                    Intent intent = new Intent(folderActivity, ListActivity.class);
                    intent.putExtra("archiveMode",true);
                    intent.putExtra("folderName",folderName.getText());
                    folderActivity.startActivity(intent);
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

            UbicacionItem ubicacionItem = folderListFull.get(getAdapterPosition());

            if(!checkBox.isChecked()){
                checkBox.setChecked(true);
                relativeLayout.setBackground(folderActivity.getDrawable(R.drawable.card_loading_background));
                selectList.add(ubicacionItem);
                counter++;
                updateCounter();
            }
            else{
                checkBox.setChecked(false);
                relativeLayout.setBackground(folderActivity.getDrawable(R.drawable.card_main_background));
                selectList.remove(ubicacionItem);
                counter--;
                updateCounter();
            }
        }
    }
}
