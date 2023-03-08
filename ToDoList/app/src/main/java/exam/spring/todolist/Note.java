package exam.spring.todolist;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class Note implements Parcelable
{
    private String id;
    private String description;
    private java.sql.Date deadline;
    private Bitmap image;
    private MediaStore.Audio audio;
    private LatLng position;

    public Note(String description, java.sql.Date deadline, LatLng position)
    {
        this.description = description;
        this.deadline = deadline;
        this.position = position;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id=id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(java.sql.Date deadline) {
        this.deadline = deadline;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public MediaStore.Audio getAudio() {
        return audio;
    }

    public void setAudio(MediaStore.Audio audio) {
        this.audio = audio;
    }

    public LatLng getPosition()
    {
        return position;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(description);
        parcel.writeLong(deadline.getTime());
        parcel.writeParcelable(position,i);
        parcel.writeParcelable(image,i);
    }

    public static final Parcelable.Creator<Note> CREATOR
            = new Parcelable.Creator<Note>() {
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int i) {
            return new Note[i];
        }
    };

    private Note(Parcel in) {
        description=in.readString();
        deadline = new java.sql.Date(in.readLong());
        position = (LatLng) in.readParcelable(LatLng.class.getClassLoader());
        image=(Bitmap) in.readParcelable(Bitmap.class.getClassLoader());
    }
}
