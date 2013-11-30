package pl.zgora.andre.poznanlbgame.util;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import pl.zgora.andre.poznanlbgame.R;

/** Creates the main menu with pictures on the left. */
public class StartArrayAdapter extends ArrayAdapter<String>
{
	/** Application environment. */
	private final Context context;
	/** List of strings which appear in the main menu. */
	private final ArrayList<String> values;
 
	/** Creates new instance of the class.
	 * @param context	in general, class owner where StartArrayAdapter will be used
	 * @param values	list of string to be displayed in the main menu */
	public StartArrayAdapter(Context context, ArrayList<String> values) {
		super(context, R.layout.list_start_item, values);
		this.context = context;
		this.values = values;
	}
 
	/** Connects strings with proper images. */
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.list_start_item, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
		textView.setText(values.get(position));

		String s = values.get(position);
 
		if (s.equals(context.getString(R.string.new_game))) 
			imageView.setImageResource(R.drawable.start);
		else if (s.equals(context.getString(R.string.resume_game)))
			imageView.setImageResource(R.drawable.restart);
		else if (s.equals(context.getString(R.string.about_app)))
			imageView.setImageResource(R.drawable.about);
		else if (s.equals(context.getString(R.string.tutorial)))
			imageView.setImageResource(R.drawable.tutorial);
		else
			imageView.setImageResource(R.drawable.quit);
		
 
		return rowView;
	}
}
