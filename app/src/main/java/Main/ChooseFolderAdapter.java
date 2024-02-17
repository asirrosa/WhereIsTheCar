package Main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChooseFolderAdapter extends RecyclerView.Adapter<ChooseFolderAdapter.DeleteFolderViewHolder> {

    private ListActivity listActivity;
    private ArrayList<String> deleteFolderList;
    private ChooseFolderDialog chooseFolderDialog;


    public ChooseFolderAdapter(ChooseFolderDialog chooseFolderDialog, ListActivity listActivity, ArrayList<String> deleteFolderList) {
        this.deleteFolderList = deleteFolderList;
        this.listActivity = listActivity;
        this.chooseFolderDialog = chooseFolderDialog;
    }

    @NonNull
    @Override
    public DeleteFolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(listActivity);
        View view = inflater.inflate(R.layout.list_folder_card_layout, parent, false);
        return new DeleteFolderViewHolder(view);
    }

    /**
     * Metodo para meterle los valores de los arrays al holder
     */
    @Override
    public void onBindViewHolder(@NonNull final ChooseFolderAdapter.DeleteFolderViewHolder holder, final int position) {
        String nombre = deleteFolderList.get(position);
        holder.folderName.setText(nombre);
    }

    @Override
    public int getItemCount() {
        return deleteFolderList.size();
    }


    public class DeleteFolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;

        /**
         * Aqui se inicializan las distintas variables y se hace un listener para cuando se pulse alguna ubicacion guardada
         */

        public DeleteFolderViewHolder(View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            onClick(itemView);
        }

        private void onClick(View itemView){
            itemView.setOnClickListener(v -> {
                //para archivarlos
                MyDatabaseHelper myDB = new MyDatabaseHelper(listActivity);
                myDB.archiveSelected(listActivity.ubicacionAdapter.selectList,folderName.getText().toString());

                //para borrarlos de la lista original
                String deleteListString = listActivity.ubicacionAdapter.deleteSelectedFromScreen();
                myDB.deleteSelectedData(deleteListString);
                chooseFolderDialog.dismiss();
                Toast.makeText(listActivity, "Las ubicaciones se han archivado en la carpeta: " + folderName.getText().toString(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
