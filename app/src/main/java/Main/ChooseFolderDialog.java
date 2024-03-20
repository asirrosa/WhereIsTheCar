package Main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class ChooseFolderDialog extends AppCompatDialogFragment {
    private ListActivity listActivity;
    private RecyclerView recyclerChooseFolder;
    private ArrayList<UbicacionItem> folderList;
    private ChooseFolderAdapter chooseFolderAdapter;
    private TextView no_data;


    public ChooseFolderDialog(ListActivity listActivity, ArrayList<UbicacionItem> folderList) {
        this.listActivity = listActivity;
        this.folderList = folderList;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.list_folder_dialog, null);

        no_data = view.findViewById(R.id.no_data);

        if(folderList.isEmpty()){
            no_data.setVisibility(TextView.VISIBLE);
        }

        recyclerChooseFolder = view.findViewById(R.id.recyclerUbicaciones);

        chooseFolderAdapter = new ChooseFolderAdapter(this,listActivity, folderList);
        recyclerChooseFolder.setAdapter(chooseFolderAdapter);
        recyclerChooseFolder.setLayoutManager(new LinearLayoutManager(listActivity));

        builder.setView(view)
                .setTitle("Guardar ubicacion en la carpeta:")
                .setNegativeButton("cancelar", (dialogInterface, i) -> {
                    //para que se quite el modo de seleccion
                    if(listActivity != null){
                        listActivity.ubicacionAdapter.disableContextualActionMode();
                    }
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