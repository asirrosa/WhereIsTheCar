package Main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class DeleteFolderDialog extends AppCompatDialogFragment{
    private RecyclerView recyclerDeleteList;
    private ArrayList<String> deleteList;
    private DeleteFolderAdapter deleteFolderAdapter;
    private FolderActivity folderActivity;

    public DeleteFolderDialog(FolderActivity folderActivity, ArrayList<String> deleteList){
        this.folderActivity = folderActivity;
        this.deleteList = deleteList;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.list_folder_dialog, null);

        recyclerDeleteList = view.findViewById(R.id.recyclerUbicaciones);

        deleteFolderAdapter = new DeleteFolderAdapter(folderActivity,deleteList);
        recyclerDeleteList.setAdapter(deleteFolderAdapter);
        recyclerDeleteList.setLayoutManager(new LinearLayoutManager(folderActivity));

        builder.setView(view)
                .setTitle("¿Estas seguro de querer borrar la/las capetas y su contenido?")
                .setNegativeButton("cancelar", (dialogInterface, i) -> {
                    if(folderActivity != null){
                        folderActivity.folderAdapter.disableContextualActionMode();
                    }
                    dialogInterface.cancel();
                })
                .setCancelable(false)
                .setPositiveButton("ok", (dialogInterface, i) -> {
                    confirmDialogDeleteSelected();
                });
        return builder.create();
    }

    private void confirmDialogDeleteSelected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(folderActivity);
        builder.setTitle("¿No podras recuperar las ubicaciones, estas seguro?");
        //builder.setMessage("¿Estas seguro que quieres añadir una ubicación de manera manual?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> {
            folderActivity.folderAdapter.deleteSelectedFolders();
            deleteFolderAdapter.deleteSelectedFolders();
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            folderActivity.folderAdapter.disableContextualActionMode();
            dialogInterface.cancel();
        });
        builder.create().show();
    }

}