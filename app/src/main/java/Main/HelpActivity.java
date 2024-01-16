package Main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HelpActivity extends AppCompatActivity{

    private ImageView imageView;
    private int contador;
    private Toolbar toolbar;
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_layout);

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Ayuda");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(v -> {

            if(contador == 5) {
                Intent intent = new Intent(getApplicationContext(), SanMamesActivity.class);
                startActivity(intent);
            }
            contador ++;
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}