package exam.spring.todolist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class AddDialog extends AlertDialog
{
    private AlertDialog.Builder dialogBuilder;
    private View view;
    private EditText desc;
    private ImageButton shoot;
    private ImageButton speak;

    protected AddDialog(Context context,int layoutId)
    {
        super(context);
        LayoutInflater inflater=getLayoutInflater().from(context);
        view=inflater.inflate(layoutId,null);
        dialogBuilder=new AlertDialog.Builder(context);
        dialogBuilder.setView(view);
        //dialogBuilder.setCancelable(false);
        desc=view.findViewById(R.id.field);
        shoot=view.findViewById(R.id.shoot);
        speak=view.findViewById(R.id.speak);
    }

    public void setTitle(int titleId)
    {
        dialogBuilder.setTitle(titleId);
    }

    public void setPositiveButton(int textId,DialogInterface.OnClickListener listener)
    {
        dialogBuilder.setPositiveButton(textId, listener);
    }

    public void setNegativeButton(int textId,DialogInterface.OnClickListener listener)
    {
        dialogBuilder.setPositiveButton(textId, listener);
    }

    public void setNeutralButton(int textId,DialogInterface.OnClickListener listener)
    {
        dialogBuilder.setPositiveButton(textId, listener);
    }

    public void setImageButtonListener(int buttonId,View.OnClickListener listener)
    {
        ImageButton button=view.findViewById(buttonId);
        button.setOnClickListener(listener);
    }

    public void show()
    {
        dialogBuilder.create().show();
    }

    public View getView()
    {
        return view;
    }

    public String getText()
    {
        Toast.makeText(getContext(),desc.getText().toString(),Toast.LENGTH_LONG);
        return desc.getText().toString();
    }


}
