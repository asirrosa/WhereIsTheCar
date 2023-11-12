package Main;

public class BusquedaItem {

    private String nombre;
    private String descripcion;
    private String mapboxId;
    private Double lat;
    private Double lon;

    public BusquedaItem(String nombre, String descripcion, String mapboxId, Double lat, Double lon) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.mapboxId = mapboxId;
        this.lat = lat;
        this.lon = lon;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getMapboxId() {
        return mapboxId;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }
}

