package exam.spring.todolist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Tools
{
    private Context context;

    public Tools(Context context)
    {
        this.context=context;
    }

    public void toast(int messageId)
    {
        toast(context.getString(messageId));
    }

    public void toast(String message)
    {
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }

    public NoteAdapter adapt(ListView list, ArrayList<Note> notes)
    {
        NoteAdapter adapter=new NoteAdapter(context,R.layout.note_layout,notes);
        list.setAdapter(adapter);
        return adapter;
    }

    public static Bitmap convert(String base64Str) throws IllegalArgumentException
    {
        if(base64Str!=null)
        {
            byte[] bytes=Base64.decode(base64Str,Base64.DEFAULT);
            Bitmap image=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            return image;
        }
        else return null;
    }

    public static String convert(Bitmap bitmap)
    {
       if(bitmap!=null)
       {
           ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
           bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
           byte[] byteArray = byteArrayOutputStream .toByteArray();
           String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
           return encoded;
       }
       else return null;
    }
}
