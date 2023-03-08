package exam.spring.todolist;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class NoteAdapter extends ArrayAdapter<Note>
{
    private ArrayList<Note> notes;
    private int resource;

    public NoteAdapter(Context context, int resource, ArrayList<Note> notes)
    {
        super(context,resource);
        this.resource=resource;
        this.notes=notes;
    }

    @Override
    public int getCount()
    {
        return notes.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView= View.inflate(getContext(),resource,null);
        TextView main=convertView.findViewById(R.id.main_text);
        TextView deadline=convertView.findViewById(R.id.deadline);
        main.setText(notes.get(position).getDescription());
        deadline.setText(notes.get(position).getDeadline().toString());
        return convertView;
    }

    public void setList(ArrayList<Note> notes)
    {
        this.notes=notes;
    }

    public ArrayList<Note> getList()
    {
        return notes;
    }
}
