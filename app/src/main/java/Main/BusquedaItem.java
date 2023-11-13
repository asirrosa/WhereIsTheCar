package Main;

public class BusquedaItem {

    private String nombre;
    private String descripcion;
    private Double lat;
    private Double lon;

    public BusquedaItem(String nombre, String descripcion, Double lat, Double lon) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.lat = lat;
        this.lon = lon;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }
}

