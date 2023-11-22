package Main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity{

    private ImageView imageView;
    private int contador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_layout);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(v -> {

            if(contador == 5) {
                Intent intent = new Intent(getApplicationContext(), SanMamesActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                contador++;
            }
        });
    }
}