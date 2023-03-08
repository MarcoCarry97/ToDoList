package exam.spring.todolist;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.sql.Date;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private final int CAPTURE_IMAGE=0;
    private final int CAPTURE_AUDIO=1;
    private final int CREATE_IMAGE=2;
    private final int CREATE_AUDIO=3;
    private final int CAPTURE_SPEECH=4;
    private static final int LOCATION_PERMISSION_REQUEST = 5;

    private ArrayList<Note> noteList;
    private FloatingActionButton add;
    private ListView notes;
    private Store store;
    private Tools tools;
    private EditText desc;
    private Bitmap image;
    private FirebaseApp app;
    private FirebaseAuth auth;
    private LocationTrack track;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);
        setContentView(R.layout.activity_main);
        app=FirebaseApp.initializeApp(MainActivity.this);
        auth=FirebaseAuth.getInstance();
        tools=new Tools(MainActivity.this);
        notes=findViewById(R.id.notes);
        add=findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNote();
            }
        });
        store=new Store(MainActivity.this);
        track=new LocationTrack(MainActivity.this);
        notes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                chooseDialog(i);
                return false;
            }
        });
        notes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(MainActivity.this,NoteActivity.class);
                intent.putExtra("note",noteList.get(i));
                startActivity(intent);
            }
        });
        /*if(tracker.isServiceAvailable())
            if(tracker.checkPermission())
                tracker.initService();*/
    }

    private void addNote()
    {
        AddDialog dialog=new AddDialog(MainActivity.this,R.layout.input_dialog);
        setButtons(dialog);
        dialog.setTitle(R.string.add);
        dialog.show();
    }

    private void setButtons(final AddDialog dialog)
    {
        dialog.setImageButtonListener(R.id.speak,new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                try
                {
                    desc=dialog.getView().findViewById(R.id.field);
                    startActivityForResult(intent,CAPTURE_SPEECH);
                }
                catch (ActivityNotFoundException e)
                {
                    tools.toast(R.string.desc_error);
                }
            }
        });
        dialog.setImageButtonListener(R.id.shoot, new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(intent.resolveActivity(getPackageManager())!=null)
                    startActivityForResult(intent,CAPTURE_IMAGE);
            }
        });
        dialog.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                try
                {
                    String desc=dialog.getText();
                    if(desc.trim().equals(""))
                        throw new IllegalAccessException(getString(R.string.desc_error));
                    Date date=new Date(System.currentTimeMillis());
                    //LocationTrack tracker=
                    double lat=track.getLatitude();
                    double lng=track.getLongitude();
                    LatLng position=new LatLng(lat,lng);
                    Note note=store.add(desc,date,position,image,null);
                    noteList.add(note);
                    tools.adapt(notes,noteList);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private void chooseDialog(final int position)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.choose_message);
        builder.setPositiveButton(R.string.modify, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                modify(position);
            }
        });
        builder.setNegativeButton(R.string.remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                remove(position);
            }
        });
        builder.create().show();
    }

    private void modify(final int position)
    {
        final AddDialog dialog=new AddDialog(MainActivity.this,R.layout.input_dialog);
        dialog.setTitle(R.string.modify);
        setButtons(dialog);
        dialog.setPositiveButton(R.string.modify, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String desc=dialog.getText();
               store.update(notes,position,noteList.get(position),desc,image,null);
            }
        });
        dialog.show();
    }

    private void remove(int position)
    {
        if(store.isOnline()) noteList=store.getAdapter().getList();
        store.remove(notes,position,noteList.get(position));
       // Log.e(String.valueOf(position),String.valueOf(noteList.size()));
        if(!store.isOnline()) {
            noteList.remove(position);
            tools.adapt(notes, noteList);
        }
        tools.toast(R.string.note_removed);
    }

    private void readapt()
    {
        NoteAdapter adapter=new NoteAdapter(MainActivity.this,R.layout.note_layout,noteList);
        notes.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String message = "";
            switch (requestCode) {
                case CAPTURE_IMAGE:
                    image = (Bitmap) bundle.get("data");
                    message = getString(R.string.image_added);
                    break;
                case CAPTURE_AUDIO:
                    message = getString(R.string.audio_added);
                    break;
                case CREATE_IMAGE:
                    image = (Bitmap) bundle.get("data");
                    message = getString(R.string.image_added);
                    break;
                case CREATE_AUDIO:
                    message = getString(R.string.audio_added);
                    break;
                case CAPTURE_SPEECH:
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    desc.setText(result.get(0));
                    break;
            }
            tools.toast(message);
        } else tools.toast(R.string.cancelled);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case(R.id.map): showMap(); break;
            case(R.id.login):
                if(store.isOnline())
                {
                    auth.signOut();
                    item.setTitle(R.string.sign_in);
                    store.setCloud();
                    try {
                        noteList=store.get(notes);
                        if(!store.isOnline())
                            tools.adapt(notes,noteList);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    //readapt();
                }
                else
                {
                    login(item);
                    item.setTitle(R.string.sign_out);
                }
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void login(MenuItem item)
    {
        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
    }

    private void showMap()
    {
        try {
            Intent intent=new Intent(MainActivity.this,MapsActivity.class);
            if(store.isOnline()) noteList=store.getAdapter().getList();
            intent.putParcelableArrayListExtra("notes",noteList);
            for(Note note:noteList) Log.d("Note",note.toString());
            startActivity(intent);
        }
        catch (Exception e)
        {
            Log.e("ERROR",e.toString());
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST)
            if (permissions.length == 1)
                if(permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION)
                    if( grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        {
                            Log.e("LOCATION PERMISSION","OK");
                           // tracker.initService();
                        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser user=auth.getCurrentUser();
        store.setCloud();
        try {
            noteList=store.get(notes);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!store.isOnline())
            tools.adapt(notes,noteList);
        else
        {
            NoteAdapter adapter=store.getAdapter();
            //noteList=adapter.getList();
        }
    }
}
