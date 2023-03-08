package exam.spring.todolist;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class Store
{
    private DatabaseManager manager;
    private Context context;
    private Tools tools;
    //private boolean cloud;
    private FirebaseFirestore firestore;
    private FirebaseUser user;
    //private ArrayList<Note> notes;
    private ArrayList<Note> noteList;
    private NoteAdapter adapter;

    public Store(Context context)
    {
        this.context=context;
        manager=new DatabaseManager(context);
        tools=new Tools(context);
        firestore=FirebaseFirestore.getInstance();
        //cloud=false;
        user= FirebaseAuth.getInstance().getCurrentUser();
        noteList=new ArrayList<Note>();
    }

    public Note add(String desc, java.sql.Date date, LatLng position, Bitmap image, MediaStore.Audio audio) throws IllegalAccessException {

       if(isOnline()) return addOnFireStore(desc,date,position,image,audio);
       else return addOnDatabase(desc,date,position,image,audio);
    }

    private Note addOnFireStore(String desc, java.sql.Date date, LatLng position, Bitmap image, MediaStore.Audio audio)
    {
        HashMap<String,Object> noteMap=new HashMap<String,Object>();
        noteMap.put("description",desc);
        noteMap.put("date",date);
        noteMap.put("latitude",position.latitude);
        noteMap.put("longitude",position.longitude);
        noteMap.put("image",tools.convert(image));
        //noteMap.put("audio",tools.conv)
        Note note=new Note(desc,date,position);
        note.setImage(image);
        note.setAudio(audio);
        firestore.collection("users").document(user.getEmail())
                .collection("notes").add(noteMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                tools.toast(R.string.note_added);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                tools.toast(e.getMessage());
            }
        });
        return note;
    }

    private Note addOnDatabase(String desc, java.sql.Date deadline, LatLng position, Bitmap image, MediaStore.Audio  audio) throws IllegalAccessException
    {
        if(desc.trim().equals("")) throw new IllegalAccessException(context.getString(R.string.desc_error));
        //LocationTracker tracker=new LocationTracker(MainActivity.this);
        //LatLng position=tracker.getCurrentLocation();

        Note note=new Note(desc,deadline,position);
        note.setImage(image);
        manager.add(desc,deadline,position,image,null);
        noteList.add(note);
        tools.toast(R.string.note_added);
        return note;
    }

    public void update(ListView list,int position, Note note, String desc, Bitmap image, MediaStore.Audio audio)
    {
        if(!isOnline()) modifyDatabase(position,note,desc,image,audio);
        else modifyFirestore(list,position,note,desc,image,audio);
    }

    private void modifyFirestore(final ListView list,int pos, final Note note, final String desc, final Bitmap image, final MediaStore.Audio audio)
    {
        HashMap<String,Object> noteMap=new HashMap<String,Object>();
        noteMap.put("description",desc);
        if(image==null) noteMap.put("image",tools.convert(image));
        else noteMap.put("image",tools.convert(note.getImage()));
        LatLng position=note.getPosition();
        noteMap.put("latitude",position.longitude);
        noteMap.put("longitude",position.longitude);
        noteMap.put("date",note.getDeadline());
        Log.e("ID",note.getId());
        firestore.collection("users").document(user.getEmail())
                .collection("notes")
                .document(note.getId()).set(noteMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        note.setDescription(desc);
                        if(image!=null) note.setImage(image);
                        note.setAudio(audio);
                        tools.adapt(list,noteList);
                        tools.toast(R.string.note_modified);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                tools.toast(e.getMessage());
            }
        });
    }

    private void modifyDatabase(int position,Note note,String desc,Bitmap image,MediaStore.Audio audio)
    {
        if(desc.trim().equals("")) throw new IllegalArgumentException(context.getString(R.string.desc_error));
        Cursor cursor=manager.get();
        cursor.moveToPosition(position);
        int index=cursor.getColumnIndexOrThrow(DatabaseContract.ID);
        long id=cursor.getLong(index);
        if(image==null) image=note.getImage();
        else note.setImage(image);
        manager.update(id,desc,note.getDeadline(),note.getPosition(),image,note.getAudio());
        note.setDescription(desc);
    }

    public void remove(ListView list,int position,Note note)
    {
        if(!isOnline()) removeFromDB(position,note);
        else removeFromFire(list,position,note);
    }

    private void removeFromFire(ListView list,int position, Note note)
    {
        firestore.collection("users").document(user.getEmail())
                .collection("notes").document(note.getId()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        tools.toast(R.string.note_removed);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                tools.toast(e.getMessage());
            }
        });
        noteList.remove(note);
        adapter=tools.adapt(list,noteList);
    }

    private void removeFromDB(int position,Note note)
    {
        Cursor cursor=manager.get();
        cursor.moveToFirst();
        boolean end=false;
        int index=cursor.getColumnIndexOrThrow(DatabaseContract.DESC);
        while(!end && !cursor.isClosed())
        {
            String value=cursor.getString(index);
            if(value.equals(note.getDescription()))
                end=true;
            else cursor.moveToNext();
        }
        index=cursor.getColumnIndexOrThrow(DatabaseContract.ID);
        long id=cursor.getLong(index);
        manager.remove(id);
        noteList.remove(note);
    }

    public ArrayList<Note> get(ListView list) throws ExecutionException, InterruptedException {
        return isOnline() ? getFromFire(list) : getFromDB();
    }

    private ArrayList<Note> getFromFire(final ListView list) throws ExecutionException, InterruptedException {
        FireAsync fire=new FireAsync(list);
        fire.execute();
        return fire.get();
    }

    private ArrayList<Note> getFromDB()
    {
        ArrayList<Note> dataNotes=new ArrayList<Note>();
        Cursor cursor=manager.get();
        if(cursor.moveToFirst()) do
        {
            int index=cursor.getColumnIndexOrThrow(DatabaseContract.DESC);
            String desc=cursor.getString(index);
            index=cursor.getColumnIndexOrThrow(DatabaseContract.DEADLINE);
            java.sql.Date deadline=new java.sql.Date(cursor.getLong(index));
            index=cursor.getColumnIndexOrThrow(DatabaseContract.LAT);
            double lat=cursor.getDouble(index);
            index=cursor.getColumnIndexOrThrow(DatabaseContract.LNG);
            double lng=cursor.getDouble(index);
            index=cursor.getColumnIndexOrThrow(DatabaseContract.IMAGE);
            String base=cursor.getString(index);
            Bitmap photo=tools.convert(base);
            LatLng position=new LatLng(lat,lng);
            Note note=new Note(desc,deadline,position);
            index=cursor.getColumnIndexOrThrow(DatabaseContract.IMAGE);
            String id=cursor.getString(index);
            note.setImage(photo);
            dataNotes.add(note);
        }
        while(cursor.moveToNext());
        return dataNotes;
    }

    public void setCloud()
    {
        user=FirebaseAuth.getInstance().getCurrentUser();
    }

    public boolean isOnline()
    {
        return user!=null;
    }

    public NoteAdapter getAdapter()
    {
        return adapter;
    }

    private class FireAsync extends AsyncTask<Note,Note,ArrayList<Note>>
    {
        //private ArrayList<Note> noteList;
        private ListView list;


        public FireAsync(ListView list)
        {
            this.list=list;
            noteList=new ArrayList<Note>();
        }

        @Override
        protected ArrayList<Note> doInBackground(final Note... notes) {
            noteList=new ArrayList<Note>();
            firestore.collection("users").document(user.getEmail()).collection("notes").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for(DocumentSnapshot doc:queryDocumentSnapshots)
                            {
                                if(doc.exists())
                                {
                                    Log.d("NOTE","EXIST");
                                    String desc=doc.getString("description");
                                    Date date=new Date(doc.getDate("date").getTime());
                                    double lat=doc.getDouble("latitude");
                                    double lng=doc.getDouble("longitude");
                                    LatLng position=new LatLng(lat,lng);
                                    Bitmap image=tools.convert(doc.getString("image"));
                                    Note note=new Note(desc,date,position);
                                    note.setId(doc.getId());
                                    note.setImage(image);
                                    noteList.add(note);
                                }
                            }
                            adapter=tools.adapt(list,noteList);
                        }
                    });
            return noteList;
        }
    }
}
