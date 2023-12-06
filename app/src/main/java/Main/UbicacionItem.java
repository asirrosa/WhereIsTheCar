package Main;

public class UbicacionItem {
    private String fechaHora;
    private String nombre;
    private String descripcion;
    private Double lat;
    private Double lon;

    public UbicacionItem(String fechaHora, String nombre, String descripcion, Double lat, Double lon) {
        this.fechaHora = fechaHora;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.lat = lat;
        this.lon = lon;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion(){
        return descripcion;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }
}

