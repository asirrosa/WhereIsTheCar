package Main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.time.LocalDateTime;

class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UbiManager.db";
    private static final int DATABASE_VERSION = 1;

    //tabla ubicaciones
    private static final String TABLE_NAME_UBICACION = "ubicaciones";
    private static final String COLUMN_UBICACION_ID = "ubicacion_id";
    private static final String COLUMN_UBICACION_NOMBRE = "ubicacion_nombre";
    private static final String COLUMN_UBICACION_DESCRIPCION = "ubicacion_descripción";
    private static final String COLUMN_UBICACION_DATE_TIME = "ubicacion_fecha_hora";
    private static final String COLUMN_UBICACION_LAT = "ubicacion_lat";
    private static final String COLUMN_UBICACION_LON = "ubicacion_lon";


    MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Metodo para crear la base de datos con sus columnas
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String queryUbicacion = "CREATE TABLE " + TABLE_NAME_UBICACION +
                " (" + COLUMN_UBICACION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_UBICACION_DATE_TIME + " DATETIME, " +
                COLUMN_UBICACION_NOMBRE + " TEXT, " +
                COLUMN_UBICACION_DESCRIPCION + " TEXT, " +
                COLUMN_UBICACION_LAT + " REAL, " +
                COLUMN_UBICACION_LON + " REAL);";

        db.execSQL(queryUbicacion);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_UBICACION);
        onCreate(db);
    }

    /**
     * Metodo para añadir una nueva ubicacion a la base de datos
     */
    public void addUbicacion(LocalDateTime fechaHora, String nombre, String descipcion, double lat, double lon){
        SQLiteDatabase db = this.getWritableDatabase();
        //De momento lo desecho porque no es seguro
        /*String addUbicacion = "INSERT INTO " + TABLE_NAME_UBICACION +
                "(" + COLUMN_UBICACION_DATE_TIME + "," +
                COLUMN_UBICACION_NOMBRE + "," +
                COLUMN_UBICACION_DESCRIPCION + "," +
                COLUMN_UBICACION_LAT + "," +
                COLUMN_UBICACION_LON + ") VALUES " +
                "('" + fechaHora.toString() + "'," +
                "'" + nombre + "'," +
                "'" + descipcion + "'," +
                lat + "," +
                lon + ");";

                db.execSQL(addUbicacion);
        */

        ContentValues values = new ContentValues();
        values.put(COLUMN_UBICACION_DATE_TIME,fechaHora.toString());
        values.put(COLUMN_UBICACION_NOMBRE,nombre);
        values.put(COLUMN_UBICACION_DESCRIPCION,descipcion);
        values.put(COLUMN_UBICACION_LAT,lat);
        values.put(COLUMN_UBICACION_LON,lon);
        db.insert(TABLE_NAME_UBICACION,null,values);
    }

    /**
     * Metodo para leer todos las ubicaciones guardadas
     */
    public Cursor readAllData(){
        String query = "SELECT * FROM " + TABLE_NAME_UBICACION + " ORDER BY " + COLUMN_UBICACION_ID + " DESC" ;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    /**
     * Metodo para borrar todas las entradas de la base de datos
     */
    public void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME_UBICACION);
    }
}
