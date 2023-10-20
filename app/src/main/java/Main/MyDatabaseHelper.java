package Main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

class MyDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "WhereIsTheCar.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "aparcamiento";
    private static final String COLUMN_ID = "aparcamiento_id";
    private static final String COLUMN_UBICACION = "aparcamiento_ubicacion";
    private static final String COLUMN_DATE_TIME = "aparcamiento_fecha_hora";
    private static final String COLUMN_LAT = "aparcamiento_lat";
    private static final String COLUMN_LON = "aparcamiento_lon";

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
     * Metodo para añadir un nuevo aparcamiento a la base de datos
     */
    public void addAparcamiento(LocalDateTime startDateTime, String location, double lat, double lon){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_UBICACION,location);
        cv.put(COLUMN_DATE_TIME,startDateTime.toString());
        cv.put(COLUMN_LAT, lat);
        cv.put(COLUMN_LON, lon);
        db.insert(TABLE_NAME,null, cv);
    }

    /**
     * Metodo para leer todos los aparcamientos guardados
     */
    Cursor readAllData(){
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC" ;
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
    void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }
}
