package Main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;


public class AddLocationDialog extends AppCompatDialogFragment {
    public EditText nombre;

    public AddLocationDialog() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_something_dialog, null);

        nombre = view.findViewById(R.id.nombre);

        builder.setView(view)
                .setTitle("Añadir ubicación")
                .setNegativeButton("cancelar", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                }).setCancelable(true)
                .setPositiveButton("ok", (dialogInterface, i) -> {
                    MainActivity.getInstance().displayLocation();
                });
        return builder.create();
    }
}