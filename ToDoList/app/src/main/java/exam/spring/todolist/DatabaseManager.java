package exam.spring.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Base64;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.util.Date;

public class DatabaseManager
{
    private DatabaseHelper helper;
    private SQLiteDatabase database;
    private Tools tools;

    public DatabaseManager(Context context)
    {
        helper=new DatabaseHelper(context,DatabaseHelper.NAME,null,DatabaseHelper.VERSION);
        database=helper.getWritableDatabase();
        tools=new Tools(context);
    }

    public void close()
    {
        helper.close();
        database.close();
    }

    public void add(String desc, java.sql.Date deadline, LatLng position, Bitmap image, MediaStore.Audio audio)
    {
        ContentValues values=new ContentValues();
        values.put(DatabaseContract.DESC,desc);
        values.put(DatabaseContract.DEADLINE,deadline.getTime());
        values.put(DatabaseContract.LAT,position.latitude);
        values.put(DatabaseContract.LNG,position.longitude);
        String base=tools.convert(image);
        values.put(DatabaseContract.IMAGE, base);
        //values.put(DatabaseContract.AUDIO,audio);
        database.insert(DatabaseHelper.TABLE,null,values);
    }

    public void update(long id, String desc, Date deadline, LatLng position, Bitmap image, MediaStore.Audio audio)
    {
        ContentValues values=new ContentValues();
        values.put(DatabaseContract.ID,id);
        values.put(DatabaseContract.DESC,desc);
        values.put(DatabaseContract.DEADLINE,deadline.getTime());
        values.put(DatabaseContract.LAT,position.latitude);
        values.put(DatabaseContract.LNG,position.longitude);
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        String encoded=null;
        if(image!=null)
        {
            image.compress(Bitmap.CompressFormat.PNG,100,stream);
            byte[] bytes=stream.toByteArray();
            encoded=Base64.encodeToString(bytes,Base64.DEFAULT);
        }
        String where=DatabaseContract.ID+" = "+id;
        database.update(DatabaseHelper.TABLE,values,where,null);
    }

    public void remove(long id)
    {
        String where=DatabaseContract.ID+" = "+id;
        //String args[]={String.valueOf(id)};
        database.delete(DatabaseHelper.TABLE, where, null);
    }

    public Cursor get()
    {
        String columns[]=new String[]{DatabaseContract.ID,
            DatabaseContract.DESC,
            DatabaseContract.DEADLINE,
            DatabaseContract.LAT,
            DatabaseContract.LNG,
            DatabaseContract.IMAGE,
            DatabaseContract.AUDIO};
        String where=null;
        String args[]=null;
        String groupBy=null;
        String having=null;
        String order=DatabaseContract.DEADLINE+" DESC";
        return database.query(DatabaseHelper.TABLE,columns,where,args,groupBy,having,order);
    }
}
