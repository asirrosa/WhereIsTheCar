package Main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChooseFolderAdapter extends RecyclerView.Adapter<ChooseFolderAdapter.ChooseFolderViewHolder> {

    private ListActivity listActivity;
    private ArrayList<UbicacionItem> folderList;
    private ChooseFolderDialog chooseFolderDialog;


    public ChooseFolderAdapter(ChooseFolderDialog chooseFolderDialog, ListActivity listActivity, ArrayList<UbicacionItem> folderList) {
        this.folderList = folderList;
        this.listActivity = listActivity;
        this.chooseFolderDialog = chooseFolderDialog;
    }

    @NonNull
    @Override
    public ChooseFolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(listActivity);
        View view = inflater.inflate(R.layout.list_folder_card_layout, parent, false);
        return new ChooseFolderViewHolder(view);
    }

    /**
     * Metodo para meterle los valores de los arrays al holder
     */
    @Override
    public void onBindViewHolder(@NonNull final ChooseFolderAdapter.ChooseFolderViewHolder holder, final int position) {
        String nombre = folderList.get(position).getNombre();
        int id = folderList.get(position).getFolderId();
        holder.folderName.setText(nombre);
        holder.folderId.setText(String.valueOf(id));
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }


    public class ChooseFolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;
        TextView folderId;

        /**
         * Aqui se inicializan las distintas variables y se hace un listener para cuando se pulse alguna ubicacion guardada
         */

        public ChooseFolderViewHolder(View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            folderId = itemView.findViewById(R.id.folderId);
            onClick(itemView);
        }

        private void onClick(View itemView){
            itemView.setOnClickListener(v -> {
                //para archivarlos
                MyDatabaseHelper myDB = new MyDatabaseHelper(listActivity);
                int id = Integer.parseInt(folderId.getText().toString());
                myDB.archiveSelected(listActivity.ubicacionAdapter.selectList,id);

                //para borrarlos de la lista original
                String deleteListString = listActivity.ubicacionAdapter.deleteSelectedFromScreen();
                myDB.deleteSelectedData(deleteListString);
                chooseFolderDialog.dismiss();
                Toast.makeText(listActivity, "Las ubicaciones se han archivado en la carpeta: " + folderName.getText().toString(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
