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


public class ChooseFolderDialog extends AppCompatDialogFragment {
    private ListActivity listActivity;
    private RecyclerView recyclerChooseFolder;
    private ArrayList<String> folderList;
    private ChooseFolderAdapter chooseFolderAdapter;

    public ChooseFolderDialog(ListActivity listActivity, ArrayList<String> folderList) {
        this.listActivity = listActivity;
        this.folderList = folderList;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.list_folder_dialog, null);

        recyclerChooseFolder = view.findViewById(R.id.recyclerUbicaciones);

        chooseFolderAdapter = new ChooseFolderAdapter(this,listActivity, folderList);
        recyclerChooseFolder.setAdapter(chooseFolderAdapter);
        recyclerChooseFolder.setLayoutManager(new LinearLayoutManager(listActivity));

        builder.setView(view)
                .setTitle("Guardar ubicacion en la carpeta:")
                .setNegativeButton("cancelar", (dialogInterface, i) -> {
                    //para que se quite el modo de seleccion
                    listActivity.ubicacionAdapter.disableContextualActionMode();
                    dialogInterface.cancel();
                })
                .setCancelable(false)
                .setNeutralButton("añadir carpeta",(dialog, which) -> {
                    AddFolderDialog addFolderDialog = new AddFolderDialog(listActivity,null);
                    addFolderDialog.show(listActivity.getSupportFragmentManager(), "example dialog");
                });
        return builder.create();
    }
}