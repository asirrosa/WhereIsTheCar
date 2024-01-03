package Main;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.time.LocalDateTime;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener,OnMapReadyCallback, View.OnClickListener, MenuItem.OnMenuItemClickListener{

    static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    private UbicacionItem ubicacionItem;
    private FloatingActionButton btnSave;
    private MenuItem itemSearch;
    private NetworkStateReceiver networkStateReceiver;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private boolean archiveMode;
    private String folderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.search_layout);

        archiveMode = getIntent().getBooleanExtra("archiveMode", false);
        folderName = getIntent().getStringExtra("folderName");

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Busqueda");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        btnSave = findViewById(R.id.fab_location_save);
        btnSave.setOnClickListener(this);

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    /**
     * Metodo para crear el inflater con el menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        itemSearch = menu.findItem(R.id.searchManual);
        itemSearch.setOnMenuItemClickListener(this);

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_location_save:
                guardarEnDB();
                Intent intent = new Intent();
                if(archiveMode){
                    intent.putExtra("archiveMode", true);
                    intent.putExtra("folderName",folderName);
                }
                finish();
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.searchManual:
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.mapbox_access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#7678ED"))
                                .limit(10)
                                .hint("Busca aqui")
                                .language("es")
                                .build(PlaceOptions.MODE_CARDS))
                        .build(SearchActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
                break;
        }
        return false;
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            // Add the symbol layer icon to map for future use
            style.addImage(symbolIconId, BitmapFactory.decodeResource(
                    SearchActivity.this.getResources(), R.drawable.mapbox_marker_icon_default));

            // Create an empty GeoJSON source using the empty feature collection
            setUpSource(style);

            // Set up a new symbol layer for displaying the searched location's feature coordinates
            setupLayer(style);
        });
    }

    private void guardarEnDB(){
        MyDatabaseHelper myDB = new MyDatabaseHelper(this);
        String startDateTime = LocalDateTime.now().toString();
        Toast.makeText(getApplicationContext(), "Se ha guardado la ubi!", Toast.LENGTH_SHORT).show();
        if(archiveMode){
            myDB.addUbicacionArchived(
                    folderName,startDateTime,
                    this.ubicacionItem.getNombre(),
                    this.ubicacionItem.getDescripcion(),
                    this.ubicacionItem.getLat(),
                    this.ubicacionItem.getLon());
        }
        else{
            myDB.addUbicacion(startDateTime,
                    this.ubicacionItem.getNombre(),
                    this.ubicacionItem.getDescripcion(),
                    this.ubicacionItem.getLat(),
                    this.ubicacionItem.getLon());
        }
    }

    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    private void setupLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[] {0f, -8f})
        ));
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
            // Then retrieve and update the source designated for showing a selected location's symbol layer icon

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    // Move map camera to the selected location
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(14)
                                    .build()), 4000);

                    btnSave.setVisibility(FloatingActionButton.VISIBLE);
                    this.ubicacionItem = new UbicacionItem(
                            1, 0, null,
                            selectedCarmenFeature.text(),
                            selectedCarmenFeature.placeName(),
                            ((Point) selectedCarmenFeature.geometry()).latitude(),
                            ((Point) selectedCarmenFeature.geometry()).longitude());
                }
            }
        }
    }

    @Override
    public void networkAvailable() {}

    @Override
    public void networkUnavailable() {
        Toast.makeText(this, "Se ha perdido la conexión a Internet", Toast.LENGTH_SHORT).show();
    }
}
