package com.mapbox.search.sample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.time.LocalDateTime;

class MyDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "WhereIsTheCar.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "ubicaciones";
    private static final String COLUMN_ID = "ubicacion_id";
    private static final String COLUMN_UBICACION = "ubicacion_nombre";
    private static final String COLUMN_DATE_TIME = "ubicacion_fecha_hora";
    private static final String COLUMN_LAT = "ubicacion_lat";
    private static final String COLUMN_LON = "ubicacion_lon";

    MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Metodo para crear la base de datos con sus columnas
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_DATE_TIME + " DATETIME, " +
                        COLUMN_UBICACION + " TEXT, " +
                        COLUMN_LAT + " REAL, " +
                        COLUMN_LON + " REAL);";
        db.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Metodo para añadir una nueva ubicacion a la base de datos
     */
    public void addUbicacion(LocalDateTime startDateTime, String location, double lat, double lon){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_UBICACION,location);
        cv.put(COLUMN_DATE_TIME,startDateTime.toString());
        cv.put(COLUMN_LAT, lat);
        cv.put(COLUMN_LON, lon);
        db.insert(TABLE_NAME,null, cv);
    }

    /**
     * Metodo para leer todos las ubicaciones guardadas
     */
    public Cursor readAllData(){
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC" ;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        //cursor.close();
        return cursor;
    }

    /**
     * Metodo para borrar todas las entradas de la base de datos
     */
    void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }
}
