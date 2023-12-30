package Main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Queue;

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

    //tabla archivar
    private static final String TABLE_NAME_ARCHIVADO = "archivados";
    private static final String COLUMN_ARCHIVADO_ID = "archivado_id";
    private static final String COLUMN_ARCHIVADO_CARPETA = "archivado_carpeta";
    private static final String COLUMN_ARCHIVADO_NOMBRE = "archivado_nombre";
    private static final String COLUMN_ARCHIVADO_DESCRIPCION = "archivado_descripción";
    private static final String COLUMN_ARCHIVADO_DATE_TIME = "archivado_fecha_hora";
    private static final String COLUMN_ARCHIVADO_LAT = "archivado_lat";
    private static final String COLUMN_ARCHIVADO_LON = "archivado_lon";


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

        String queryArchivar = "CREATE TABLE " + TABLE_NAME_ARCHIVADO +
                " (" + COLUMN_ARCHIVADO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ARCHIVADO_CARPETA + " TEXT, " +
                COLUMN_ARCHIVADO_DATE_TIME + " DATETIME, " +
                COLUMN_ARCHIVADO_NOMBRE + " TEXT, " +
                COLUMN_ARCHIVADO_DESCRIPCION + " TEXT, " +
                COLUMN_ARCHIVADO_LAT + " REAL, " +
                COLUMN_ARCHIVADO_LON + " REAL);";

        db.execSQL(queryUbicacion);
        db.execSQL(queryArchivar);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_UBICACION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ARCHIVADO);
        onCreate(db);
    }

    /**
     * Metodo para añadir una nueva ubicacion a la base de datos
     */
    public void addUbicacion(String fechaHora, String nombre, String descipcion, double lat, double lon){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_UBICACION_DATE_TIME,fechaHora.toString());
        values.put(COLUMN_UBICACION_NOMBRE,nombre);
        values.put(COLUMN_UBICACION_DESCRIPCION,descipcion);
        values.put(COLUMN_UBICACION_LAT,lat);
        values.put(COLUMN_UBICACION_LON,lon);
        db.insert(TABLE_NAME_UBICACION,null,values);
    }

    public void addUbicacionArchived(String folderName, String fechaHora, String nombre, String descipcion, double lat, double lon){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ARCHIVADO_CARPETA,folderName);
        values.put(COLUMN_ARCHIVADO_NOMBRE,nombre);
        values.put(COLUMN_ARCHIVADO_DESCRIPCION,descipcion);
        values.put(COLUMN_ARCHIVADO_DATE_TIME,fechaHora);
        values.put(COLUMN_ARCHIVADO_LAT,lat);
        values.put(COLUMN_ARCHIVADO_LON,lon);
        db.insert(TABLE_NAME_ARCHIVADO,null,values);
    }

    /**
     * Metodo para leer todos los nombres de las carpetas
     */
    public Cursor readAllArchivedFolders(){
        String query = "SELECT DISTINCT " + COLUMN_ARCHIVADO_CARPETA + " FROM " + TABLE_NAME_ARCHIVADO + " ORDER BY " + COLUMN_ARCHIVADO_CARPETA + " DESC" ;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public void archiveSelected(ArrayList<UbicacionItem> archiveList, String folderName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for(int i = 0;i<archiveList.size();i++){
            values.put(COLUMN_ARCHIVADO_CARPETA,folderName);
            values.put(COLUMN_ARCHIVADO_NOMBRE,archiveList.get(i).getNombre());
            values.put(COLUMN_ARCHIVADO_DESCRIPCION,archiveList.get(i).getDescripcion());
            values.put(COLUMN_ARCHIVADO_DATE_TIME,archiveList.get(i).getFechaHora());
            values.put(COLUMN_ARCHIVADO_LAT,archiveList.get(i).getLat());
            values.put(COLUMN_ARCHIVADO_LON,archiveList.get(i).getLon());
            db.insert(TABLE_NAME_ARCHIVADO,null,values);
        }
    }


    /**
     * Metodo para leer todos las ubicaciones guardadas
     */
    public Cursor readAllOriginalData(){
        String query = "SELECT * FROM " + TABLE_NAME_UBICACION + " ORDER BY " + COLUMN_UBICACION_ID + " DESC" ;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    /**
     * Metodo para leer todos las ubicaciones archivadas
     */
    public Cursor readAllArchivedData(String folderName){
        String query = "SELECT * FROM " + TABLE_NAME_ARCHIVADO + " WHERE " + COLUMN_ARCHIVADO_CARPETA + " = '" + folderName + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public boolean folderExistsAlready(String folderName){
        boolean result;
        String query = "SELECT * FROM " + TABLE_NAME_ARCHIVADO + " WHERE " + COLUMN_ARCHIVADO_CARPETA + " = '" + folderName + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }

        assert cursor != null;

        if(!cursor.moveToNext()){
            result = false;
        }
        else{
            result = true;
        }

        return result;
    }

    public void addFolder(String folderName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ARCHIVADO_CARPETA,folderName);
        db.insert(TABLE_NAME_ARCHIVADO,null,values);
    }

    public void deleteSelectedFolders(String deleteListNames){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME_ARCHIVADO + " WHERE " + COLUMN_ARCHIVADO_CARPETA + " in (" + deleteListNames + ");";
        db.execSQL(query);
    }

    public void deleteSelectedData(String deleteListId){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME_UBICACION + " WHERE " + COLUMN_UBICACION_ID + " in (" + deleteListId + ");";
        db.execSQL(query);
    }

    public void deleteSelectedArchivedData(String deleteListId){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME_ARCHIVADO + " WHERE " + COLUMN_ARCHIVADO_ID + " in (" + deleteListId + ");";
        db.execSQL(query);
    }
}
