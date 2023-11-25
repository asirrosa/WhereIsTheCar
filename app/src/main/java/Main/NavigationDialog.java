package Main;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;

public class NavigationDialog extends AppCompatDialogFragment {

    private CheckBox checkBoxPeaje, checkBoxAutopista, checkBoxFerri;
    private RadioButton btnWalking,btnDriving;
    private NavigationActivity navigationActivity;

    public NavigationDialog(NavigationActivity navigationActivity){
        this.navigationActivity = navigationActivity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.navigation_dialog, null);

        checkBoxPeaje = view.findViewById(R.id.checkBoxPeaje);
        checkBoxAutopista = view.findViewById(R.id.checkBoxAutopista);
        checkBoxFerri = view.findViewById(R.id.checkBoxFerri);

        btnDriving = view.findViewById(R.id.btnDriving);
        btnWalking = view.findViewById(R.id.btnWalking);

        if(navigationActivity.exclude == null && navigationActivity.transporte == null){
            //para que el modo default sea el de driving
            navigationActivity.transporte = "driving";
            btnDriving.setChecked(true);
        }
        else{
            //checkbox
            if(navigationActivity.exclude[0] != null){
                checkBoxPeaje.setChecked(true);
            }
            if(navigationActivity.exclude[1] != null){
                checkBoxAutopista.setChecked(true);
            }
            if(navigationActivity.exclude[2] != null){
                checkBoxFerri.setChecked(true);
            }
            //radio button
            if(navigationActivity.transporte.equals("walking")){
                btnWalking.setChecked(true);
                btnDriving.setChecked(false);
            }
            else{
                btnWalking.setChecked(false);
                btnDriving.setChecked(true);
            }
        }

        btnWalking.setOnClickListener(v -> {
            btnWalking.setChecked(true);
            btnDriving.setChecked(false);
            navigationActivity.transporte = "walking";
        });

        btnDriving.setOnClickListener(v -> {
            btnWalking.setChecked(false);
            btnDriving.setChecked(true);
            navigationActivity.transporte = "driving";
        });

        builder.setView(view)
                .setTitle("Opciones de ruta")
                .setPositiveButton("ok", (dialogInterface, i) -> {
                    if(btnDriving.isChecked() == false && btnWalking.isChecked() == false){
                        Toast.makeText(MainActivity.getInstance(), "Por favore elije un medio de transporte", Toast.LENGTH_SHORT).show();
                        navigationActivity.navDialog();
                    }
                    else{
                        navigationActivity.exclude = new String[3];
                        if(checkBoxPeaje.isChecked() || checkBoxAutopista.isChecked() ||
                        checkBoxFerri.isChecked()) {
                            if (checkBoxPeaje.isChecked()) {
                                navigationActivity.exclude[0] = "toll";
                            }
                            if (checkBoxAutopista.isChecked()) {
                                navigationActivity.exclude[1] = "motorway";
                            }
                            if (checkBoxFerri.isChecked()) {
                                navigationActivity.exclude[2] = "ferry";
                            }
                        }
                    }
                });
        return builder.create();
    }
}