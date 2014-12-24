package edu.miami.c09472237.phlogging;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.Size;

public class CreateEntryActivity extends Activity 
implements SurfaceHolder.Callback, Camera.PictureCallback, SensorEventListener, LocationListener {
	
	private static final boolean SAVE_TO_FILE = false;
	private final int COMPLETE = 0;
	private final int NOT_COMPLETE = 1;
	private String cameraFileName;
	
	private LocationManager locationManager;
	
	private SensorManager sensorManager;
	
	private Uri imageUri;
	private SurfaceView cameraPreview;
	private SurfaceHolder surfaceHolder;
	private Camera camera;
	private boolean cameraIsPreviewing = true;
	
	private EditText editTitle, editText;
	private ImageView currentPhoto;
	
	private int phlogInt;
	private String orientation,newLocation,date,time;
	
	private Bitmap photoBitmap;
	private byte[] photoByteArray;
	private Location mobileLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_entry_layout);
		
		phlogInt = getIntent().getIntExtra("edu.miami.c09472237.phlogging.phlog_int", 0);
		
		//Sets view variables to view in layout
		cameraPreview = (SurfaceView)findViewById(R.id.surface);
		editTitle = (EditText)findViewById(R.id.edit_title);
		editText = (EditText)findViewById(R.id.edit_text);
		
		//Sets up managers for the sensor and location
		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		
		//Sets up surface for camera preview
		surfaceHolder = cameraPreview.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.addCallback(this);
		currentPhoto = (ImageView)findViewById(R.id.image);
		cameraFileName = 
				getApplicationContext().getExternalFilesDir(null).toString() + "/" + String.valueOf(phlogInt) +  
				getString(R.string.camera_file_name);
		detectLocators();
		camera = Camera.open();
		
	}
	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
		try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            ((Button)findViewById(R.id.snap_button)).setClickable(true);
        } catch (Exception e) {
            //----Do something
        }
		
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
		//** for this part, code from notes would not work. Manually setting the preview size always
		//resulted in an error. Unfortunately, camera is sideways and cuts off picture. Setting parameters
		//didn't quite work
		
		Camera.Parameters parameters = camera.getParameters();  
		   List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();  
		   Camera.Size cs = sizes.get(0);  
		   parameters.setPreviewSize(cs.width, cs.height);  
		   //camera.setParameters(parameters);
		
		/*Camera.Parameters cameraParameters;
        boolean sizeFound;
        
        sizeFound = false;
        cameraParameters = camera.getParameters();
        for (Size size : cameraParameters.getSupportedPreviewSizes()) {
            if (size.width == width || size.height == height) {
                width = size.width;
                height = size.height;
                sizeFound = true;
                break;
            }
        }
        if (sizeFound) {
            cameraParameters.setPreviewSize(width,height);
            camera.setParameters(cameraParameters);
        } else {
            Toast.makeText(getApplicationContext(),
"Camera cannot do "+width+"x"+height,Toast.LENGTH_LONG).show();
            finish();
        }*/
        if (cameraIsPreviewing) {
            camera.startPreview();
        }
		
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		
		//Sets imageview to the photo taken with camera
		recycleView(currentPhoto);
        currentPhoto.setVisibility(View.VISIBLE);
        photoBitmap = BitmapFactory.decodeByteArray(data,0,data.length);
        photoByteArray = data;
        
        currentPhoto.setImageBitmap(photoBitmap);
        
        camera.startPreview();
		
	}


	@Override
	public void onLocationChanged(Location location) {
		//Sets new location string to result of androidGeodecode()
		newLocation = androidGeodecode(location);
		if (newLocation == null) {
			newLocation = String.valueOf(location.getLatitude()) + ", " + String.valueOf(location.getLongitude());
		}
		
		//Starts sensor
        startSensor(Sensor.TYPE_ORIENTATION);
		
	}
	
	private boolean startSensor(int sensorType) {
        
        if (sensorManager.getSensorList(sensorType).isEmpty()) {
            return(false);
        } else {
            sensorManager.registerListener(this,
sensorManager.getDefaultSensor(sensorType),SensorManager.SENSOR_DELAY_NORMAL);
            return(true);
        }
    }


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		orientation = "";
		
		//Messy string for Orientation values
		orientation = String.valueOf(event.values[0]) + ", " + String.valueOf(event.values[1]) + ", " + String.valueOf(event.values[2]);	
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	private void recycleView(View view) {
        
        ImageView imageView;
        Bitmap imageBitmap;
        BitmapDrawable imageBitmapDrawable;
        
        if (view != null) {
            if (view instanceof ImageView) {
                imageView = (ImageView)view;
                if ((imageBitmapDrawable = 
(BitmapDrawable)imageView.getDrawable()) != null &&
(imageBitmap = imageBitmapDrawable.getBitmap()) != null) {
                    imageBitmap.recycle();
                }
                imageView.setImageURI(null);
                imageView.setImageBitmap(null);
            }
            if ((imageBitmapDrawable = 
(BitmapDrawable)view.getBackground()) != null &&
(imageBitmap = imageBitmapDrawable.getBitmap()) != null) {
                imageBitmap.recycle();
            }
            view.setBackgroundDrawable(null);
            System.gc();
        }
    }
	
	public void myClickHandler(View v) {
		
		FileOutputStream photoStream;
		
		switch(v.getId()) {
			//Takes picture and sets lower image view to said picture
			case R.id.snap_button:
				camera.takePicture(null,null,null,this);
				break;
			//Puts all data into intent. Saves photo to storage to retrieve later with Uri.
			//Finishes activity and sends result.
			case R.id.save_button:
				Intent intent = new Intent();
				String path = null;
				
				Time formatTime = new Time();
				formatTime.setToNow();
				date = formatTime.format3339(true);
				time = String.valueOf(formatTime.hour) +  ":" + String.valueOf(formatTime.minute);
				
				intent.putExtra("edu.miami.c09472237.phlogging.new_title", editTitle.getText().toString());
				intent.putExtra("edu.miami.c09472237.phlogging.new_text", editText.getText().toString());
				intent.putExtra("edu.miami.c09472237.phlogging.new_date", date);
				intent.putExtra("edu.miami.c09472237.phlogging.new_time", time);
				intent.putExtra("edu.miami.c09472237.phlogging.new_location", newLocation);
				intent.putExtra("edu.miami.c09472237.phlogging.new_orientation", orientation);
				
				try {
		            photoStream = new FileOutputStream(cameraFileName);
		            photoBitmap.compress(CompressFormat.JPEG,100,photoStream);
		            path = Images.Media.insertImage(getContentResolver(), photoBitmap, editTitle.getText().toString(), null);
		            photoStream.close();
		            Toast.makeText(this, cameraFileName, Toast.LENGTH_LONG).show();
		        } catch (IOException e) {
		            Toast.makeText(this,"ERROR: Cannot save photo to file",
		Toast.LENGTH_LONG).show();
		            }
		   
				//Toast.makeText(this, cameraFileName, Toast.LENGTH_LONG).show();	
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
		        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		        byte[] bytes = stream.toByteArray(); 
		        intent.putExtra("edu.miami.c09472237.phlogging.new_image_path",path);
				//intent.putExtra("edu.miami.c09472237.phlogging.new_byte_array", photoByteArray);
				setResult(RESULT_OK,intent);
				finish();
				onDestroy();
		}
			
		
	}
	
	private void detectLocators() {

		//Sets up locators for location manager. GPS was not working and would return null
		//so I went with using the network. This provided a good location for the data.
		
        List<String> locators;

        locators = locationManager.getProviders(true);
        for (String aProvider : locators) {
            /*if (aProvider.equals(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this,"GPS available",Toast.LENGTH_LONG).show();
                locationManager.requestLocationUpdates(
                		LocationManager.GPS_PROVIDER,getResources().getInteger(
                        		R.integer.time_between_location_updates_ms),0,this);
                return;
            }*/
            if (aProvider.equals(LocationManager.NETWORK_PROVIDER)) {
                Toast.makeText(this,"Network available",Toast.LENGTH_LONG).show();
                locationManager.requestLocationUpdates(
                		LocationManager.NETWORK_PROVIDER,getResources().getInteger(
                		R.integer.time_between_location_updates_ms),0,this);
                return;
            }
        }
    }
	
	private String androidGeodecode(Location thisLocation) {

        Geocoder androidGeocoder;
        List<Address> addresses;
        Address firstAddress;
        String addressLine;
        String locationName;
        int index;

        if (Geocoder.isPresent()) {
            androidGeocoder = new Geocoder(this);
            try {
                addresses = androidGeocoder.getFromLocation(
thisLocation.getLatitude(),thisLocation.getLongitude(),1);
                if (addresses.isEmpty()) {
                    return("ERROR: Unkown location");
                } else {
                    firstAddress = addresses.get(0);
                    locationName = "";
                    index = 0;
                    while ((addressLine = firstAddress.getAddressLine(index)) != null) {
                        locationName += addressLine + ", ";
                        index++;
                    }
                    return (locationName);
                }
            } catch (Exception e) {
                return("ERROR: " + e.getMessage());
            }
        } else {
            return null;
        }
    }
	
	@Override
	public void onPause() {
		super.onPause();
        locationManager.removeUpdates(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		detectLocators();
	}
	
	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(this);
		sensorManager.unregisterListener(this);
		camera.release();
	}

}
