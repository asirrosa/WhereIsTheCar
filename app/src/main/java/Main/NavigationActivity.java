package Main;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static Main.SearchActivity.REQUEST_CODE_AUTOCOMPLETE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavigationActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener, LocationListener, View.OnClickListener, OnMapReadyCallback, MenuItem.OnMenuItemClickListener {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;
    public DirectionsRoute currentRoute, oneRoute, twoRoute;
    private NavigationMapRoute navigationMapRoute;
    private MenuItem itemSearch, itemRouteOptions;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private FloatingActionButton btnStartNavegation, btnUno, btnDos, btnMyLocation;
    public String transporte;
    public String[] exclude;
    private Point originPoint;
    private Point destinationPoint;
    private ImageView ivTransporte;
    private TextView textDuration, textDistance;
    private LinearLayout barraAbajo;
    private boolean nav;
    private boolean navegar;
    private NetworkStateReceiver networkStateReceiver;
    private Toolbar toolbar;
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.navigation_layout);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        toolbar=findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Navegación");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnStartNavegation = findViewById(R.id.btnStartNavegation);
        btnStartNavegation.setOnClickListener(this);

        btnUno = findViewById(R.id.btnUno);
        btnUno.setOnClickListener(this);

        btnDos = findViewById(R.id.btnDos);
        btnDos.setOnClickListener(this);

        btnMyLocation = findViewById(R.id.btnMyLocation);
        btnMyLocation.setOnClickListener(this);

        barraAbajo = findViewById(R.id.barraAbajo);
        ivTransporte = findViewById(R.id.ivTransporte);
        textDuration = findViewById(R.id.textDuration);
        textDistance = findViewById(R.id.textDistance);

        //para que de default este cargado el array y ademas el modo sea el de driving
        exclude = new String[3];
        transporte = "driving";

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
        inflater.inflate(R.menu.navigation_menu, menu);

        itemSearch = menu.findItem(R.id.searchNavigation);
        itemSearch.setOnMenuItemClickListener(this);

        itemRouteOptions = menu.findItem(R.id.routeOptions);
        itemRouteOptions.setOnMenuItemClickListener(this);

        return true;
    }

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnStartNavegation:
                nav = true;
                navegar = true;
                getLocation();
                break;
            case R.id.btnUno:
                changeRoute(oneRoute);
                changeRouteButtonColor(R.color.mapbox_blue, R.color.greyDark);
                break;

            case R.id.btnDos:
                changeRoute(twoRoute);
                changeRouteButtonColor(R.color.greyDark, R.color.mapbox_blue);
                break;
            case R.id.btnMyLocation:
                nav = false;
                navegar = true;
                getLocation();
                break;
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.searchNavigation:
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.mapbox_access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#7678ED"))
                                .limit(10)
                                .hint("Busca aqui")
                                .language("es")
                                .build(PlaceOptions.MODE_CARDS))
                        .build(this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
                //para que no se muestre lo de la pasada busqueda
                barraAbajo.setVisibility(LinearLayout.INVISIBLE);
                btnStartNavegation.setVisibility(FloatingActionButton.INVISIBLE);
                btnUno.setVisibility(FloatingActionButton.INVISIBLE);
                btnDos.setVisibility(FloatingActionButton.INVISIBLE);
                currentRoute = null;
                break;

            case R.id.routeOptions:
                navDialog();
                break;
        }
        return false;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            style.addImage("symbolIconId", BitmapFactory.decodeResource(NavigationActivity.this.getResources(), R.drawable.mapbox_marker_icon_default));
            addDestinationIconSymbolLayer(style);
            style.addSource(new GeoJsonSource(geojsonSourceLayerId));
            // Set up a new symbol layer for displaying the searched location's feature coordinates
            style.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                    iconImage("symbolIconId"),
                    iconOffset(new Float[]{0f, -8f})));
            //en caso de que vengas de la listactivity te enseña el destino y sino tu ubicacion donde tu estas
            double[] arrayLatLng = getIntent().getDoubleArrayExtra("arrayLatLng");
            if (arrayLatLng != null) {
                irAlSitioGuardadoPreviamente(arrayLatLng);
            } else {
                showLocationIfActivated();
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void irAlSitioGuardadoPreviamente(double[] arrayLatLng) {
        destinationPoint = Point.fromLngLat(
                arrayLatLng[1],
                arrayLatLng[0]
        );

        //para que se vea el botón
        btnStartNavegation.setVisibility(FloatingActionButton.VISIBLE);

        // Move map camera to the selected location
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(destinationPoint.latitude(), (destinationPoint.longitude())))
                        .zoom(14)
                        .build()), 4000);

        //para poner el marker
        LatLng point = new LatLng(destinationPoint.latitude(), destinationPoint.longitude());
        MarkerOptions markerOptions = new MarkerOptions().position(point);
        mapboxMap.addMarker(markerOptions);
    }

    private void showLocationIfActivated() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
                }
            }
        }
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    //para conseguir la ruta
                    destinationPoint = Point.fromLngLat(
                            ((Point) selectedCarmenFeature.geometry()).longitude(),
                            ((Point) selectedCarmenFeature.geometry()).latitude()
                    );

                    //para que se vea el botón
                    btnStartNavegation.setVisibility(FloatingActionButton.VISIBLE);

                    // Move map camera to the selected location
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(destinationPoint.latitude(), (destinationPoint.longitude())))
                                    .zoom(14)
                                    .build()), 4000);

                }
            }
        }
    }

    public void getRoutes() {
        navigationOptions().getRoute(new Callback<DirectionsResponse>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                if (response.body() == null) {
                    Toast.makeText(getApplicationContext(), "Developer Message: Algun parametro de la api esta mal", Toast.LENGTH_SHORT).show();
                    return;
                } else if (response.body().routes().size() < 1) {
                    Toast.makeText(getApplicationContext(), "No se han encontrado rutas a esa ubicacion", Toast.LENGTH_SHORT).show();
                    return;
                }

                LatLng originLatLng = new LatLng(originPoint.latitude(), originPoint.longitude());
                LatLng destinationLatLng = new LatLng(destinationPoint.latitude(), destinationPoint.longitude());
                //esto sirve para calcular el centro entre dos puntos, de esta manera se muestra la ruta desde arriba
                LatLngBounds latLngBounds = new LatLngBounds.Builder().include(originLatLng).include(destinationLatLng).build();
                //este metodo sirve para mostrarte la ruta entre dos puntos de manera completa
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 80));

                //hacer el boton de navegacion visible
                btnStartNavegation.setVisibility(FloatingActionButton.VISIBLE);

                oneRoute = response.body().routes().get(0);

                //se mira si la segunda ruta existe o no, si es asi se muestran los botones sino no
                if (response.body().routes().size() > 1) {
                    twoRoute = response.body().routes().get(1);
                    changeVisibilityRouteButtons(FloatingActionButton.VISIBLE);
                } else {
                    changeVisibilityRouteButtons(FloatingActionButton.INVISIBLE);
                }

                //de default te va a mostrar la primera ruta
                changeRoute(oneRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Developer Message: No funciona la api", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String excludeOptions() {
        String excludeText = "";
        if (exclude[0] != null) {
            excludeText = exclude[0];
            if (exclude[1] != null) {
                excludeText = excludeText + "," + exclude[1];
                if (exclude[2] != null) {
                    excludeText = excludeText + "," + exclude[2];
                }
            } else if (exclude[2] != null) {
                excludeText = excludeText + "," + exclude[2];
            }
        } else if (exclude[1] != null) {
            excludeText = exclude[1];
            if (exclude[2] != null) {
                excludeText = excludeText + "," + exclude[2];
            }
        } else if (exclude[2] != null) {
            excludeText = exclude[2];
        }
        return excludeText;
    }

    private NavigationRoute navigationOptions() {
        NavigationRoute result;
        String excludeText = excludeOptions();
        if (excludeText.equals("")) {
            result = NavigationRoute.builder(this)
                    .accessToken(Mapbox.getAccessToken())
                    .alternatives(Boolean.TRUE)
                    .language(new Locale("es_ES"))
                    .profile(transporte)
                    .origin(originPoint)
                    .destination(destinationPoint)
                    .build();
        } else {
            result = NavigationRoute.builder(this)
                    .accessToken(Mapbox.getAccessToken())
                    .alternatives(Boolean.TRUE)
                    .language(new Locale("es_ES"))
                    .exclude(excludeText)
                    .profile(transporte)
                    .origin(originPoint)
                    .destination(destinationPoint)
                    .build();
        }
        return result;
    }


    /**
     * Este metodo sirve para cuando solo haya una ruta, de esta manera no se muestran los botones ya que sería inutil
     */
    @SuppressLint("RestrictedApi")
    private void changeVisibilityRouteButtons(int visibility) {
        btnUno.setVisibility(visibility);
        btnDos.setVisibility(visibility);
    }


    private void changeRoute(DirectionsRoute route) {
        //esto de aqui sirve para mostrar la ruta en el mapa
        if (navigationMapRoute != null) {
            navigationMapRoute.removeRoute();
        } else {
            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationViewDark);
        }

        //esto de aqui sirve para que en la barra de abajo te ponga que tipo de transporte tiene la ruta, que distancia tiene y el tiempo que vas a tardar
        if (transporte.equals("driving")) {
            ivTransporte.setBackgroundResource(R.drawable.ic_coche);
        } else if (transporte.equals("walking")) {
            ivTransporte.setBackgroundResource(R.drawable.ic_andar);
        } else if (transporte.equals("cycling")) {
            ivTransporte.setBackgroundResource(R.drawable.ic_cycling);
        }

        //para mostrar las horas y minutos que dura la ruta
        barraAbajo.setVisibility(LinearLayout.VISIBLE);
        double seconds = route.duration();
        if (seconds >= 3600) {
            int hours = (int) (seconds / 3600);
            seconds = seconds % 3600;
            int minutes = (int) (seconds / 60);
            textDuration.setText(hours + " h " + minutes + " min ");
        } else {
            int minutes = (int) (seconds / 60);
            textDuration.setText(minutes + " min ");
        }

        //para mostrar los km o metros que tiene la ruta
        double meters = route.distance();
        if (meters >= 1000) {
            int kilometers = (int) (meters / 1000);
            textDistance.setText(kilometers + " km ");
        } else {
            textDistance.setText(meters + " m ");
        }

        currentRoute = route;
        navigationMapRoute.addRoute(route);
    }

    private void changeRouteButtonColor(int uno, int dos) {
        btnUno.setBackgroundTintList(ColorStateList.valueOf(
                Color.parseColor(getString(uno))));
        btnDos.setBackgroundTintList(ColorStateList.valueOf(
                Color.parseColor(getString(dos))));
    }

    public void navDialog() {
        //mostrar el dialog con las opciones de la ruta
        NavigationDialog navigationDialog = new NavigationDialog(this);
        //para que aun clicando el back button o fuera del dialog este no se cierre
        navigationDialog.setCancelable(false);
        navigationDialog.show(getSupportFragmentManager(), "example dialog");
    }

    private void getLocation() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastLocation == null) {
                        try {
                            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if(navegar) {
                            navegar = false;
                            processWithLocation(lastLocation);
                        }
                    }
                } else {
                    showSettingsAlert();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if(navegar) {
                navegar = false;
                processWithLocation(location);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void processWithLocation(Location location) {
        originPoint = Point.fromLngLat(
                location.getLongitude(),
                location.getLatitude()
        );

        if (nav) {
            if (currentRoute == null) {
                getRoutes();
                Toast.makeText(getApplicationContext(), "Elige la ruta que quieras", Toast.LENGTH_SHORT);
            } else {
                boolean simulateRoute = false;
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .shouldSimulateRoute(simulateRoute)
                        .build();
                NavigationLauncher.startNavigation(NavigationActivity.this, options);
            }
        } else {
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, Objects.requireNonNull(mapboxMap.getStyle()));
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(new LatLng(originPoint.latitude(), (originPoint.longitude())))
                            .zoom(14)
                            .build()), 4000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 100) {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    boolean askAgain = shouldShowRequestPermissionRationale(permission);
                    if (!askAgain) {
                        Toast.makeText(this, "La aplicación no tiene permisos de ubicación", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("El gps esta desactivado. ¿Quieres activarlo?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.create().show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void networkAvailable() {
    }

    @Override
    public void networkUnavailable() {
        Toast.makeText(this, "Se ha perdido la conexión a Internet", Toast.LENGTH_SHORT).show();
    }

}
