package edu.miami.c09472237.phlogging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class PhloggingMain extends Activity 
implements OnItemClickListener, ViewBinder, OnCancelListener{
	
	private final int BIG_PIC = 0;
	
	private Cursor phlogDBCursor;
	private Cursor imageMediaCursor;
	private DataSQLiteDB phlogDB;
	private ListView list;
	private SimpleCursorAdapter cursorAdapter;
	private Uri bigImageUri;
	private View dialogView;
	private long editRow;
	private AlertDialog theDialog;
	private long currentRow;
	private int phlogInt = 1;
	
	private String currentTitle;
	private String currentDate;
	private String currentText;
	private String currentTime;
	private String currentLocation;
	private String currentOrientation;
	private String currentUriString;
	private Uri currentUri;
	private Bitmap currentBitmap;
	

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phlogging_main);
		
		String displayFields[] = {
				"image_id", "title", "date"
		};
		
		int[] displayViews = {
	            R.id.item_thumbnail,
	            R.id.item_title,
	            R.id.item_date
	        };
		
		String queryFields[] = {
				MediaStore.Images.Media._ID,
		};
		
		imageMediaCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
				queryFields, null, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
		
		//New Database
		phlogDB = new DataSQLiteDB(this);
		list = (ListView)findViewById(R.id.the_list);
		
		//Phlog Database cursor. Sets adapter to list and onItemClickListener
		phlogDBCursor = phlogDB.fetchAllPhlogs();
		cursorAdapter = new SimpleCursorAdapter(this, R.layout.item_layout, phlogDBCursor, displayFields,displayViews);
		cursorAdapter.setViewBinder(this);
		list.setOnItemClickListener(this);
		list.setAdapter(cursorAdapter);
		
	}
	
	public void onActivityResult(int requestCode,int resultCode,Intent data) {
				
		//Gets results and data from create a new entry activity
		switch(resultCode) {
		case RESULT_OK: 
			//Sets data to variables then puts variables into content value
			String title = data.getStringExtra("edu.miami.c09472237.phlogging.new_title");
			String text = data.getStringExtra("edu.miami.c09472237.phlogging.new_text");
			String date = data.getStringExtra("edu.miami.c09472237.phlogging.new_date");
			String time = data.getStringExtra("edu.miami.c09472237.phlogging.new_time");
			String location = data.getStringExtra("edu.miami.c09472237.phlogging.new_location");
			String orienation = data.getStringExtra("edu.miami.c09472237.phlogging.new_orientation");
			String uri = data.getStringExtra("edu.miami.c09472237.phlogging.new_image_path");
			
			
            ContentValues phlogData = new ContentValues();
            phlogData.put("text", text);
            phlogData.put("title", title);
            phlogData.put("date", date);
            phlogData.put("time", time);
            phlogData.put("location", location);
            phlogData.put("orientation", orienation);
            phlogData.put("image_id", uri);

            //Adds content values to phlog db
            phlogDB.addPhlog(phlogData);
            phlogDBCursor.requery();
            break;
        default:
        	break;
		}
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		//Get data from database
		ContentValues imageData = phlogDB.getPhlogById(id);
		currentRow = imageData.getAsInteger("_id");
			
			//Sets current data to current variables
					
			currentTitle = imageData.getAsString("title");
			currentText = imageData.getAsString("text");
			currentDate = imageData.getAsString("date");
			currentTime = imageData.getAsString("time");
			currentOrientation = imageData.getAsString("orientation");
			currentLocation = imageData.getAsString("location");
			currentUriString = imageData.getAsString("image_id");
			currentUri = Uri.parse(currentUriString);
			
			//Shows dialog with all information
			showDialog(BIG_PIC);

					
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		
		//Assigning views of item layout to view variables
		ImageView image = (ImageView)view.findViewById(R.id.item_thumbnail);
	    TextView title = (TextView)view.findViewById(R.id.item_title);
	    TextView date = (TextView)view.findViewById(R.id.item_date);

	    
		//Sets title, date, and small scale picture from cursor to item layout 
		if (columnIndex == cursor.getColumnIndex("image_id")) {
			String uriString = cursor.getString(columnIndex);
			Bitmap bitmap = null;
			Uri uri = Uri.parse(uriString);
			
			try {
				bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			image.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false));
			return true;
		} else if (columnIndex == cursor.getColumnIndex("title")) {
			String s = cursor.getString(columnIndex);	
			title.setText(s);
			return true;
		} else if (columnIndex == cursor.getColumnIndex("date")) {
			String s = cursor.getString(columnIndex);	
			date.setText(s);
		}
		return false;
	}
	
	protected Dialog onCreateDialog(int dialogId) {
        
		//Creates Alert Dialog and sets onCancelListener
		
        AlertDialog.Builder dialogBuilder;
        LayoutInflater dialogInflator;
        
        dialogBuilder = new AlertDialog.Builder(this);
        switch (dialogId) {
        case BIG_PIC:
        	dialogInflator = (LayoutInflater)getSystemService(
            Activity.LAYOUT_INFLATER_SERVICE);
            dialogView = dialogInflator.inflate(
        R.layout.entry_view_dialog_layout, (ViewGroup)findViewById(R.id.dialog_root));
            dialogBuilder.setView(dialogView);
            theDialog = dialogBuilder.create();
            theDialog.setOnCancelListener(this);
            break;
        default:
            break;
        }
        return(dialogBuilder.create());
    }
	
	@Override
    protected void onPrepareDialog(int dialogId,Dialog dialog) {
		Bitmap bitmap = null;
        
		//Set all views in dialog to view variables
		ImageView dialogImageView = (ImageView)dialog.findViewById(R.id.full_image);
		TextView titleView = (TextView)dialog.findViewById(R.id.view_title);
		TextView dateView = (TextView)dialog.findViewById(R.id.view_date);
		TextView timeView = (TextView)dialog.findViewById(R.id.view_time);
		TextView textView = (TextView)dialog.findViewById(R.id.view_text);
		TextView locationView = (TextView)dialog.findViewById(R.id.view_location);
		TextView orientationView = (TextView)dialog.findViewById(R.id.view_orientation);
		
		//Set views to correct data
		titleView.setText(currentTitle);
		dateView.setText(currentDate);
		timeView.setText(currentTime);
		textView.setText(currentText);
		locationView.setText(currentLocation);
		orientationView.setText(currentOrientation);
        
		//Set up bitmap for the phlog image Uri
		try {
			bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentUri);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dialogImageView.setImageURI(currentUri);
    }
	
	@SuppressWarnings("deprecation")
	public void myClickHandler(View v) {
		switch(v.getId()) {
			//Deletes phlog from DB and closes dialog
			case R.id.delete_button:
				phlogDB.deletePhlog(currentRow);
				phlogDBCursor.requery();
				theDialog.cancel();
				break;
			//Closes dialog
			case R.id.dismiss_button:
				theDialog.cancel();
				break;
			//Launches new intent Create Entry Activity for result
			case R.id.new_entry_button:
				Intent intent = new Intent();
				intent.setClassName("edu.miami.c09472237.phlogging", "edu.miami.c09472237.phlogging.CreateEntryActivity");
				intent.putExtra("edu.miami.c09472237.phlogging.phlog_int", phlogInt++);
				startActivityForResult(intent,2);
				break;
			default:
				break;
		}
	}
	
	@Override
	public void onDestroy() {
		//Closes DB
		super.onDestroy();
		phlogDB.close();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCancel(DialogInterface dialog) {
		//Overkill dialog closer. Works though
		dismissDialog(BIG_PIC);
		dialog.dismiss();
		removeDialog(BIG_PIC);
		
	}
	
}
