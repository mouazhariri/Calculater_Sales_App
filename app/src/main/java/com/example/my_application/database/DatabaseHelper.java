package com.example.my_application.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SalesCommissions.db";
    private static final int DATABASE_VERSION = 5;

    // Table names
    private static final String TABLE_REPRESENTATIVES = "Representatives";
    private static final String TABLE_SALES = "Sales";
    private static final String TABLE_COMMISSIONS = "Commissions";

    // Common column names
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_REP_ID = "_id";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_REGION = "region";
    private static final String COLUMN_IMAGE = "photo"; // For storing image as BLOB

    // Sales table column names
    private static final String COLUMN_SALE_ID = "saleID";
    private static final String COLUMN_MONTH = "month";
    private static final String COLUMN_YEAR = "year";
    private static final String COLUMN_SALES_IN_REGION = "salesInRegion";
    private static final String COLUMN_SALES_OUT_REGION = "salesOutRegion";

    // Commissions table column names
    private static final String COLUMN_COMM_ID = "commID";
    private static final String COLUMN_COMMISSION_IN_MONTH = "commissionInMonth";
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context; }
    public String getColumnName() {
        return COLUMN_NAME;
    }

    public String getColumnPhone() {
        return COLUMN_PHONE;
    }

    public String getColumnRegion() {
        return COLUMN_REGION;
    }

    public String getColumnImage() {
        return COLUMN_IMAGE;
    }
    public String getColumnId() {
        return COLUMN_REP_ID;
    }
    public String getColumnCommissionInMonth() {
        return COLUMN_COMMISSION_IN_MONTH;
    } public String getColumnCommissionInid() {
        return COLUMN_COMM_ID;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Representatives table
        String CREATE_REPRESENTATIVES_TABLE = "CREATE TABLE " + TABLE_REPRESENTATIVES + "("
                + COLUMN_REP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_PHONE + " TEXT NOT NULL, "
                + COLUMN_REGION + " TEXT NOT NULL, "
                + COLUMN_IMAGE + " BLOB)";
        db.execSQL(CREATE_REPRESENTATIVES_TABLE);

        // Create Sales table
        String CREATE_SALES_TABLE = "CREATE TABLE " + TABLE_SALES + "("
                + COLUMN_SALE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_REP_ID + " INTEGER, "
                + COLUMN_MONTH + " INTEGER, "
                + COLUMN_YEAR + " INTEGER, "
                + COLUMN_SALES_IN_REGION + " REAL, "
                + COLUMN_SALES_OUT_REGION + " REAL, "
                + "FOREIGN KEY(" + COLUMN_REP_ID + ") REFERENCES " + TABLE_REPRESENTATIVES + "(" + COLUMN_REP_ID + "))";
        db.execSQL(CREATE_SALES_TABLE);

        // Create Commissions table
        String CREATE_COMMISSIONS_TABLE = "CREATE TABLE " + TABLE_COMMISSIONS + "("
                + COLUMN_COMM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_REP_ID + " INTEGER, "
                + COLUMN_MONTH + " INTEGER, "
                + COLUMN_YEAR + " INTEGER, "
                + COLUMN_COMMISSION_IN_MONTH + " REAL, "
                + "FOREIGN KEY(" + COLUMN_REP_ID + ") REFERENCES " + TABLE_REPRESENTATIVES + "(" + COLUMN_REP_ID + "))";
        db.execSQL(CREATE_COMMISSIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the old tables if they exist and create new ones
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPRESENTATIVES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMISSIONS);
        onCreate(db);  // Recreate tables
    }

    // Method to insert a new representative
    public long insertRepresentative(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_REPRESENTATIVES, null, values);
    }

    // Method to update a representative
    public int updateRepresentative(long id, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TABLE_REPRESENTATIVES, values, COLUMN_REP_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Method to delete a representative
    public int deleteRepresentative(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_REPRESENTATIVES, COLUMN_REP_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Method to insert sales data
    public long insertSales(int repId, int month, int year, double salesInRegion, double salesOutRegion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REP_ID, repId);
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_YEAR, year);
        values.put(COLUMN_SALES_IN_REGION, salesInRegion);
        values.put(COLUMN_SALES_OUT_REGION, salesOutRegion);

        return db.insert(TABLE_SALES, null, values);
    }

    // Method to insert commission data
    public long insertCommission(int repId, int month, int year, double commissionInMonth) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REP_ID, repId);
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_YEAR, year);
        values.put(COLUMN_COMMISSION_IN_MONTH, commissionInMonth);


        return db.insert(TABLE_COMMISSIONS, null, values);
    }

    // Method to get all representatives
    public Cursor getAllRepresentatives() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_REPRESENTATIVES, null);
    }

    // Method to get sales data for a specific representative, month, and year
    public Cursor getSalesData(int repId, int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SALES + " WHERE " + COLUMN_REP_ID + " = ? AND " + COLUMN_MONTH + " = ? AND " + COLUMN_YEAR + " = ?",
                new String[]{String.valueOf(repId), String.valueOf(month), String.valueOf(year)});
    }
    // Method to save image to internal storage
    public String saveImageToInternalStorage(Uri imageUri) {
        long timestamp = System.currentTimeMillis();

        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                     // Use context
             FileOutputStream outputStream = new FileOutputStream(new File(context.getFilesDir(), "rep_" + timestamp + ".jpg"))) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Use the correct file path
            String savedImagePath = new File(context.getFilesDir(), "rep_" + timestamp + ".jpg").getAbsolutePath();

            // Log the file path for debugging
            Log.d("saveImageToInternalStorage", "Image saved at: " + savedImagePath);

            // Return the file path, which can be stored in the database
            return savedImagePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    // Method to get commission data for a specific representative, month, and year
    public Cursor getCommissionData(int repId, int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_COMMISSIONS + " WHERE " + COLUMN_REP_ID + " = ? AND " + COLUMN_MONTH + " = ? AND " + COLUMN_YEAR + " = ?",
                new String[]{String.valueOf(repId), String.valueOf(month), String.valueOf(year)});
    }

    public Cursor getRepresentativeById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            return db.rawQuery("SELECT * FROM " + TABLE_REPRESENTATIVES + " WHERE " + COLUMN_REP_ID + " = ?", new String[]{String.valueOf(id)});

        } catch (Exception e) {
            Log.e("Exception in db", String.valueOf(e));
            throw new RuntimeException(e);
        }
    }
}
