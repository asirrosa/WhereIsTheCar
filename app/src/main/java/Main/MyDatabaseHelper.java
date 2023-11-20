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

    //tabla darkmode
    private static final String TABLE_NAME_DARKMODE = "darkmode";
    private static final String COLUMN_DARKMODE_ID = "dark_id";
    private static final String COLUMN_DARKMODE_VALUE = "dark_value";


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

        String queryDarkMode = "CREATE TABLE " + TABLE_NAME_DARKMODE +
                " (" + COLUMN_DARKMODE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DARKMODE_VALUE + " BOOLEAN);";

        db.execSQL(queryDarkMode);

        //le meto esto para que al principio el dark sea 0 es decir false
        String queryInicialDarkMode = "INSERT INTO TABLE" + TABLE_NAME_DARKMODE +
                "(" + COLUMN_DARKMODE_VALUE + ") VALUES" + "(0);";
        db.execSQL(queryInicialDarkMode);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_UBICACION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DARKMODE);
        onCreate(db);
    }

    /**
     * Metodo para añadir una nueva ubicacion a la base de datos
     */
    public void addUbicacion(LocalDateTime fechaHora, String nombre, String descipcion, double lat, double lon){
        SQLiteDatabase db = this.getWritableDatabase();
        String addUbicacion = "INSERT INTO " + TABLE_NAME_UBICACION +
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

    public boolean notDarkMode(){
        boolean result = false;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_DARKMODE_VALUE + " FROM " + TABLE_NAME_DARKMODE + " WHERE " + COLUMN_DARKMODE_ID + " = 1;";
        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
            cursor.moveToNext();
            if(cursor.getInt(0) == 0){
                result = true;
            }
        }
        return result;
    }

    /**
     * Metodo para cambiar el valor de la columna darkMode
     */
    public void changeDarkMode(int value){
        SQLiteDatabase db = this.getWritableDatabase();
        String changeDarkMode = "UPDATE " + TABLE_NAME_DARKMODE +
                " SET " + COLUMN_DARKMODE_VALUE + "=" + value +
                " WHERE " + COLUMN_DARKMODE_ID + "= 1;";
        db.execSQL(changeDarkMode);
    }
}
