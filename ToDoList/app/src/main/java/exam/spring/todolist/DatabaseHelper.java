package exam.spring.todolist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
    public static final String NAME="notes.db";
    public static final String TABLE="notes";
    public static final int VERSION=3 ;

    private static final String CREATE= String.format("create table %s (%s integer primary key autoincrement, %s text not null, %s long not null, %s double not null, %s double not null, %s text, %s blob);", TABLE, DatabaseContract.ID, DatabaseContract.DESC, DatabaseContract.DEADLINE, DatabaseContract.LAT, DatabaseContract.LNG, DatabaseContract.IMAGE, DatabaseContract.AUDIO);

    /*private static final String CREATE="create table "+TABLE+" ("
            +DatabaseContract.ID+" integer primary key autoincrement, "
            +DatabaseContract.DESC+" text not null, "
            +DatabaseContract.DEADLINE+" date not null, "
            +DatabaseContract.LAT+" double not null, "
            +DatabaseContract.LNG+" double not null, "
            +DatabaseContract.IMAGE+" text, "
            +DatabaseContract.AUDIO+" text);";*/

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE);
        onCreate(db);
    }
}
