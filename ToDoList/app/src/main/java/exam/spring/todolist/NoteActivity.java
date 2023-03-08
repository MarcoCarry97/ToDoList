package exam.spring.todolist;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class NoteActivity extends AppCompatActivity implements TextToSpeech.OnInitListener
{
    private TextView within, desc;
    private FloatingActionButton show, listen;
    private Intent intent;
    private TextToSpeech speech;
    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Tools tools=new Tools(NoteActivity.this);
        within=findViewById(R.id.within);
        desc=findViewById(R.id.desc);
        show=findViewById(R.id.show_image);
        //listen=findViewById(R.id.listen_audio);
         speech=new TextToSpeech(this,this);
        intent=getIntent();
        Bundle bundle=intent.getExtras();
        note=bundle.getParcelable("note");
        String[] dateData=note.getDeadline().toString().split(" ");
        String dateString=getString(R.string.remember_to)+" "+dateData[0]+" "+dateData[1]+" "+dateData[2]+" "+dateData[5];
        within.setText(dateString);
        tools.toast(note.getDescription());
        desc.setText(note.getDescription());
        desc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                speak();
            }
        });
        show.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showImage();
            }
        });
        /*listen.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listenAudio();
            }
        });*/
    }

    private void showImage()
    {
        Intent intent=new Intent(NoteActivity.this,ImageActivity.class);
        intent.putExtra("image",note.getImage());
        startActivity(intent);
        /*LayoutInflater inflater= LayoutInflater.from(getApplicationContext());
        View view=inflater.inflate(R.layout.show_dialog,null);
        AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(NoteActivity.this);
        dialogBuilder.setView(view);
        ImageView image=view.findViewById(R.id.image);
        image.setImageBitmap((Bitmap) intent.getExtras().get(getString(R.string.image)));
        AlertDialog dialog=dialogBuilder.create();
        dialog.show();*/
    }

    private void listenAudio()
    {
        final Handler handler=new Handler();
        LayoutInflater inflater= LayoutInflater.from(getApplicationContext());
        View view=inflater.inflate(R.layout.listen_dialog,null);
        AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(NoteActivity.this);
        dialogBuilder.setView(view);
        dialogBuilder.setCancelable(false);
        final TextView progressText=view.findViewById(R.id.progress);
        final SeekBar seekBar=view.findViewById(R.id.audio_line);
        final MediaPlayer player= MediaPlayer.create(getApplicationContext(),R.raw.alten);
        final Thread update=new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                int position=player.getCurrentPosition();
                seekBar.setProgress(position);
                int seconds=position/1000;
                int minutes=seconds/60;
                if(seconds>=60)
                {
                    minutes++;
                    seconds%=60;
                }
                int hours=minutes/60;
                if(minutes>=60)
                {
                    hours++;
                    minutes%=60;
                }
                progressText.setText(hours+":"+minutes+":"+seconds);
                handler.postDelayed(this,player.getDuration()/1000);
            }
        });
        final AlertDialog dialog=dialogBuilder.create();
        final ImageButton play, pause, stop;
        play=view.findViewById(R.id.play);
        pause=view.findViewById(R.id.pause);
        stop=view.findViewById(R.id.stop);
        play.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!player.isPlaying())
                {
                    player.start();
                    seekBar.setMax(player.getDuration());
                    handler.postDelayed(update, player.getDuration() / 1000);
                }
            }
        });
        pause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(player.isPlaying())
                    player.pause();
            }
        });
        stop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                player.stop();
                dialog.cancel();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                player.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
               // if(player.isPlaying()) player.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
              //  if(player.isPlaying()) player.start();
                //else player.pause();
            }
        });
        dialog.show();
    }

    private void speak()
    {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP)
            speech.speak(within.getText().toString()+": "+desc.getText().toString(), TextToSpeech.QUEUE_FLUSH,null,null);
        else speech.speak(within.getText().toString()+": "+desc.getText().toString(), TextToSpeech.QUEUE_FLUSH,null);
    }

    @Override
    public void onInit(int status)
    {
        if(status== TextToSpeech.SUCCESS)
        {
            int result=speech.setLanguage(Locale.getDefault());
            if(result== TextToSpeech.LANG_MISSING_DATA || result== TextToSpeech.LANG_NOT_SUPPORTED)
                Log.e(getString(R.string.error),getString(R.string.language_not_supposed));
        }
        else Log.e(getString(R.string.error),getString(R.string.init_fail));
    }
}
