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

public class DeleteFolderAdapter extends RecyclerView.Adapter<DeleteFolderAdapter.DeleteFolderViewHolder> {

    private FolderActivity folderActivity;
    public ArrayList<String> deleteFolderList;


    public DeleteFolderAdapter(FolderActivity folderActivity, ArrayList<String> deleteFolderList) {
        this.deleteFolderList = deleteFolderList;
        this.folderActivity = folderActivity;
    }

    @NonNull
    @Override
    public DeleteFolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(folderActivity);
        View view = inflater.inflate(R.layout.delete_folder_card_layout, parent, false);
        return new DeleteFolderViewHolder(view);
    }

    /**
     * Metodo para meterle los valores de los arrays al holder
     */
    @Override
    public void onBindViewHolder(@NonNull final DeleteFolderAdapter.DeleteFolderViewHolder holder, final int position) {
        String nombre = deleteFolderList.get(position);
        holder.deleteNombre.setText(nombre);
    }

    @Override
    public int getItemCount() {
        return deleteFolderList.size();
    }

    public void deleteSelectedFolders(){
        String result = "";
        for(int i = 0;i<deleteFolderList.size();i++){
            result = result + "'" + deleteFolderList.get(i) + "',";
        }
        result = result.substring(0, result.length() - 1);

        MyDatabaseHelper myDB = new MyDatabaseHelper(folderActivity);
        myDB.deleteSelectedFolders(result);
        Toast.makeText(folderActivity, "Se han borrado las carpetas seleccionadas", Toast.LENGTH_SHORT).show();
    }

    public class DeleteFolderViewHolder extends RecyclerView.ViewHolder {
        TextView deleteNombre;

        /**
         * Aqui se inicializan las distintas variables y se hace un listener para cuando se pulse alguna ubicacion guardada
         */

        public DeleteFolderViewHolder(View itemView) {
            super(itemView);
            deleteNombre = itemView.findViewById(R.id.deleteNombre);
        }
    }
}
