package com.studio.emacs.securevault.safe;

/**
 * Created by ganbhat on 11/12/2016.
 */
import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * DBHelper class.
 *
 * The overall theme of this class was borrowed from the Notepad
 * example Open Handset Alliance website.  It's essentially a very
 * primitive database layer.
 *
 * @author Steven Osborn - http://steven.bitsetters.com
 */
public class DBHelper {

    private static final String DATABASE_NAME = "passwordsafe";
    private static final String TABLE_DBVERSION = "dbversion";
    private static final String TABLE_PASSWORDS = "passwords";
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_VERIFY = "verify_crypto";
    private static final String TABLE_MASTER_KEY = "master_key";
    private static final String TABLE_PACKAGE_ACCESS = "package_access";
    private static final int DATABASE_VERSION = 4;
    private static String TAG = "DBHelper";
    Context myCtx;

    private static final String DBVERSION_CREATE =
            "create table " + TABLE_DBVERSION + " ("
                    + "version integer not null);";

    private static final String PASSWORDS_CREATE =
            "create table " + TABLE_PASSWORDS + " ("
                    + "id integer primary key autoincrement, "
                    + "category integer not null, "
                    + "password text not null, "
                    + "description text not null, "
                    + "username text, "
                    + "website text, "
                    + "note text, "
                    + "unique_name text, " //might be null
                    + "lastdatetimeedit text);";

    private static final String PASSWORDS_DROP =
            "drop table " + TABLE_PASSWORDS + ";";

    private static final String PACKAGE_ACCESS_CREATE =
            "create table " + TABLE_PACKAGE_ACCESS + " ("
                    + "id integer not null, "
                    + "package text not null);";

    private static final String PACKAGE_ACCESS_DROP =
            "drop table " + TABLE_PACKAGE_ACCESS + ";";

    private static final String CATEGORIES_CREATE =
            "create table " + TABLE_CATEGORIES + " ("
                    + "id integer primary key autoincrement, "
                    + "name text not null, "
                    + "lastdatetimeedit text);";

    private static final String CATEGORIES_DROP =
            "drop table " + TABLE_CATEGORIES + ";";

    private static final String MASTER_KEY_CREATE =
            "create table " + TABLE_MASTER_KEY + " ("
                    + "encryptedkey text not null);";

    private SQLiteDatabase db;
    private static boolean needsPrePopulation=false;
    private static boolean needsUpgrade=false;

    /**
     *
     * @param ctx
     */
    public DBHelper(Context ctx) {
        myCtx = ctx;
        try {
            db = myCtx.openOrCreateDatabase(DATABASE_NAME, 0,null);

            // Check for the existence of the DBVERSION table
            // If it doesn't exist than create the overall data,
            // otherwise double check the version
            Cursor c =
                    db.query("sqlite_master", new String[] { "name" },
                            "type='table' and name='"+TABLE_DBVERSION+"'", null, null, null, null);
            int numRows = c.getCount();
            if (numRows < 1) {
                createDatabase(db);
            } else {
                int version=0;
                Cursor vc = db.query(true, TABLE_DBVERSION, new String[] {"version"},
                        null, null, null, null, null,null);
                if(vc.getCount() > 0) {
                    vc.moveToFirst();
                    version=vc.getInt(0);
                }
                vc.close();
                if (version!=DATABASE_VERSION) {
                    needsUpgrade=true;
                    Log.e(TAG,"database version mismatch");
                }
            }
            c.close();


        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
    }

    private void createDatabase(SQLiteDatabase db)
    {
        try {
            db.execSQL(DBVERSION_CREATE);
            ContentValues args = new ContentValues();
            args.put("version", DATABASE_VERSION);
            db.insert(TABLE_DBVERSION, null, args);

            db.execSQL(CATEGORIES_CREATE);
            needsPrePopulation=true;

            db.execSQL(PASSWORDS_CREATE);
            db.execSQL(PACKAGE_ACCESS_CREATE);
            db.execSQL(MASTER_KEY_CREATE);
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
    }

    public void deleteDatabase()
    {
        try {
            db.execSQL(PASSWORDS_DROP);
            db.execSQL(PASSWORDS_CREATE);

            db.execSQL(CATEGORIES_DROP);
            db.execSQL(CATEGORIES_CREATE);

            db.execSQL(PACKAGE_ACCESS_DROP);
            db.execSQL(PACKAGE_ACCESS_CREATE);
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
    }

    public boolean needsUpgrade()
    {
        return needsUpgrade;
    }

    public boolean getPrePopulate()
    {
        return needsPrePopulation;
    }

    public void clearPrePopulate()
    {
        needsPrePopulation=false;
    }
    /**
     * Close database connection
     */
    public void close() {
        try {
            db.close();
        } catch (SQLException e)
        {
            Log.d(TAG,"close exception: " + e.getLocalizedMessage());
        }
    }

    public int fetchVersion() {
        int version=0;
        try {
            Cursor c = db.query(true, TABLE_DBVERSION,
                    new String[] {"version"},
                    null, null, null, null, null,null);
            if(c.getCount() > 0) {
                c.moveToFirst();
                version=c.getInt(0);
            }
            c.close();
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
        return version;
    }

    public String fetchOldConfirm() {
        String key="";
        try {
            Cursor c = db.query(true, TABLE_VERIFY, new String[] {"confirm"},
                    null, null, null, null, null,null);
            if(c.getCount() > 0) {
                c.moveToFirst();
                key=c.getString(0);
            }
            c.close();
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
        return key;
    }
    /**
     *
     * @return
     */
    public String fetchMasterKey() {
        String key="";
        try {
            Cursor c = db.query(true, TABLE_MASTER_KEY, new String[] {"encryptedkey"},
                    null, null, null, null, null,null);
            if(c.getCount() > 0) {
                c.moveToFirst();
                key=c.getString(0);
            }
            c.close();
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
        return key;
    }

    /**
     *
     * @param
     */
    public void storeMasterKey(String MasterKey) {
        ContentValues args = new ContentValues();
        try {
            db.delete(TABLE_MASTER_KEY, "1=1", null);
            args.put("encryptedkey", MasterKey);
            db.insert(TABLE_MASTER_KEY, null, args);
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
    }

//////////Category Functions ////////////////

    /**
     * Doesn't add the category if it already exists.
     * @param entry
     * @returns row id
     */
    public long addCategory(CategoryEntry entry) {
        ContentValues initialValues = new ContentValues();

        long rowID=-1;
        Cursor c =
                db.query(true, TABLE_CATEGORIES, new String[] {
                        "id", "name"}, "name='" + entry.name + "'" , null, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            rowID = c.getLong(0);

        } else {// there's not already such a category...
            initialValues.put("name", entry.name);

            try {
                rowID=db.insert(TABLE_CATEGORIES, null, initialValues);
            } catch (SQLException e)
            {
                Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
            }
        }
        c.close();
        return rowID;
    }

    /**
     *
     * @param Id
     */
    public void deleteCategory(long Id) {
        try {
            db.delete(TABLE_CATEGORIES, "id=" + Id, null);
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
    }

    /**
     *
     * @return
     */
    public List<CategoryEntry> fetchAllCategoryRows(){
        ArrayList<CategoryEntry> ret = new ArrayList<CategoryEntry>();
        try {
            Cursor c =
                    db.query(TABLE_CATEGORIES, new String[] {
                                    "id", "name"},
                            null, null, null, null, null);
            int numRows = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < numRows; ++i) {
                CategoryEntry row = new CategoryEntry();
                row.id = c.getLong(0);
                row.name = c.getString(1);
                ret.add(row);
                c.moveToNext();
            }
            c.close();
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
        return ret;
    }

    /**
     *
     * @param Id
     * @return
     */
    public CategoryEntry fetchCategory(long Id) {
        CategoryEntry row = new CategoryEntry();
        try {
            Cursor c =
                    db.query(true, TABLE_CATEGORIES, new String[] {
                            "id", "name"}, "id=" + Id, null, null, null, null, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                row.id = c.getLong(0);

                row.name = c.getString(1);
            } else {
                row.id = -1;
                row.name = null;
            }
            c.close();
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
        return row;
    }

    /**
     *
     * @param Id
     * @param entry
     */
    public void updateCategory(long Id, CategoryEntry entry) {
        ContentValues args = new ContentValues();
        args.put("name", entry.name);

        try {
            db.update(TABLE_CATEGORIES, args, "id=" + Id, null);
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
    }


////////// Password Functions ////////////////


    /**
     *
     * @param categoryId
     */
    public int countPasswords(long categoryId) {
        int count=0;
        try {
            Cursor c = db.query(TABLE_PASSWORDS, new String[] {
                            "count(*)"},
                    "category="+categoryId, null, null, null, null);
            c.moveToFirst();
            count=c.getInt(0);
            c.close();
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
        Log.i(TAG,"count="+count);
        return count;
    }

    /**
     *
     * @return
     */
    public List<PassEntry> fetchAllRows(Long CategoryId){
        ArrayList<PassEntry> ret = new ArrayList<PassEntry>();
        try {
            Cursor c;
            if (CategoryId==0)
            {
                c = db.query(TABLE_PASSWORDS, new String[] {
                                "id", "password", "description", "username", "website", "note", "category", "unique_name"},
                        null, null, null, null, null);
            } else {
                c = db.query(TABLE_PASSWORDS, new String[] {
                                "id", "password", "description", "username", "website", "note", "category", "unique_name"},
                        "category="+CategoryId, null, null, null, null);
            }
            int numRows = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < numRows; ++i) {
                PassEntry row = new PassEntry();
                row.id = c.getLong(0);

                row.password = c.getString(1);
                row.description = c.getString(2);
                row.username = c.getString(3);
                row.website = c.getString(4);
                row.note = c.getString(5);

                row.category = c.getLong(6);
                row.uniqueName = c.getString(7);

                ret.add(row);
                c.moveToNext();
            }
            c.close();
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
        return ret;
    }

    /**
     *
     * @param Id
     * @return
     */
    public PassEntry fetchPassword(long Id) {
        PassEntry row = new PassEntry();
        try {
            Cursor c =
                    db.query(true, TABLE_PASSWORDS, new String[] {
                            "id", "password", "description", "username", "website",
                            "note", "category, unique_name"}, "id=" + Id, null, null, null, null, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                row.id = c.getLong(0);

                row.password = c.getString(1);
                row.description = c.getString(2);
                row.username = c.getString(3);
                row.website = c.getString(4);
                row.note = c.getString(5);

                row.category = c.getLong(6);
                row.uniqueName = c.getString(7);
            } else {
                row.id = -1;
                row.description = row.password = null;
            }
            c.close();
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
        return row;
    }

    public PassEntry fetchPassword(String uniqueName) {
        PassEntry row = new PassEntry();
        row.id = -1;
        row.description = row.password = null;
        try {
            Cursor c =
                    db.query(true, TABLE_PASSWORDS, new String[] {
                                    "id", "password", "description", "username", "website",
                                    "note", "category", "unique_name"}, "unique_name='" + uniqueName + "'",
                            null, null, null, null, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                row.id = c.getLong(0);

                row.password = c.getString(1);
                row.description = c.getString(2);
                row.username = c.getString(3);
                row.website = c.getString(4);
                row.note = c.getString(5);

                row.category = c.getLong(6);
                row.uniqueName = c.getString(7);
            }
            c.close();
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
        return row;
    }


    public ArrayList<String> fetchPackageAccess (long passwordID) {
        ArrayList <String> pkgs = new ArrayList<String>();
        Cursor c = null;
        try {
            c =
                    db.query(true, TABLE_PACKAGE_ACCESS, new String[] {
                                    "package"}, "id=" + passwordID,
                            null, null, null, null, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                while (! c.isAfterLast()) {
                    pkgs.add(c.getString(0));
                    c.moveToNext();
                }
            }
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        } finally {
            if (c != null) c.close();
        }

        return pkgs;
    }

    public void addPackageAccess (long passwordID, String packageToAdd) {
        ContentValues packageAccessValues = new ContentValues ();
        packageAccessValues.put("id", passwordID);
        packageAccessValues.put("package", packageToAdd);
        try {
            db.insert(TABLE_PACKAGE_ACCESS, null, packageAccessValues);
        } catch (SQLException e) {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
    }

    /**
     *
     * @param Id
     * @param entry
     */
    public void updatePassword(long Id, PassEntry entry) {
        ContentValues args = new ContentValues();
        args.put("description", entry.description);
        args.put("username", entry.username);
        args.put("password", entry.password);
        args.put("website", entry.website);
        args.put("note", entry.note);
        args.put("unique_name", entry.uniqueName);
        try {
            db.update(TABLE_PASSWORDS, args, "id=" + Id, null);
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
    }

    /**
     * Only update the category field of the password entry.
     *
     * @param Id the id of the password entry
     * @param newCategoryId the updated category id
     */
    public void updatePasswordCategory(long Id, long newCategoryId) {
        if (Id<0 || newCategoryId<0) {
            //make sure values appear valid
            return;
        }
        ContentValues args = new ContentValues();
        args.put("category", newCategoryId);

        try {
            db.update(TABLE_PASSWORDS, args, "id=" + Id, null);
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
    }

    /**
     *
     * @param entry
     */
    public long addPassword(PassEntry entry) {
        long id = -1;
        ContentValues initialValues = new ContentValues();
        initialValues.put("category", entry.category);
        initialValues.put("password", entry.password);
        initialValues.put("description", entry.description);
        initialValues.put("username",entry.username);
        initialValues.put("website", entry.website);
        initialValues.put("note", entry.note);
        initialValues.put("unique_name", entry.uniqueName);

        try {
            id = db.insert(TABLE_PASSWORDS, null, initialValues);
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
        return (id);
    }

    /**
     *
     * @param Id
     */
    public void deletePassword(long Id) {
        try {
            db.delete(TABLE_PASSWORDS, "id=" + Id, null);
            db.delete(TABLE_PACKAGE_ACCESS, "id=" + Id, null);
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
    }
    /**
     * Begin a transaction on an open database.
     *
     * @return true if successful
     */
    public boolean beginTransaction() {
        try {
            db.execSQL("begin transaction;");
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * Commit all changes since the begin transaction on an
     * open database.
     */
    public void commit() {
        try {
            db.execSQL("commit;");
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
    }

    /**
     * Rollback all changes since the begin transaction on an
     * open database.
     */
    public void rollback() {
        try {
            db.execSQL("rollback;");
        } catch (SQLException e)
        {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
    }
}