package com.commandercool.alex.print;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.admob.android.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Printer extends Activity implements Runnable {
	
	// Connection settings
	private String ip = null;
	private Integer port = 0;
	
	// Preferences
	private SharedPreferences prefs = null;;
	private static final String PREFS_NAME = "myprefs";
	
	// Connection constants
	static final byte PDF = 0;
	static final byte JPG = 1;
	static final int MAX_SIZE = 50000;

	private byte[] file = null;
	private String scheme = null;
    private ProgressDialog dialog = null;
    
    // Dialog constants
    private static final int ERR_CONNECTION = 0;
    private static final int ERR_IO_SETTINGS = 1;
    private static final int ERR_IO_NO_CONNECTION = 2;
    
    
    /** Getting preferences on start */
    public void onStart(){
    	super.onStart();
    	prefs =  getSharedPreferences(PREFS_NAME, 1);
    	ip = prefs.getString("ip_address", (String) getText(R.string.set_ip_address));
    	port = prefs.getInt("port", 8080);
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main1);
       
        
        //advertisment
        //AdManager.setTestDevices( new String[] { AdManager.TEST_EMULATOR } );
    	AdView ad = (AdView)findViewById(R.id.ads);
    	ad.requestFreshAd();
        
        TextView pathLabel = (TextView)findViewById(R.id.path);
        Button send = (Button)findViewById(R.id.print_it);
        send.setOnClickListener(bSendListener);
        
        Button preferences = (Button)findViewById(R.id.preferences);
        preferences.setOnClickListener(preferencesListener);
        
        Button cancel = (Button)findViewById(R.id.cancel);
        cancel.setOnClickListener(bCancelListener);
        
        ImageView preview = (ImageView)findViewById(R.id.preview);
            
        
        
        Intent intent = getIntent();
        Uri uri = (Uri)intent.getExtras().get(Intent.EXTRA_STREAM);
        String type = getContentResolver().getType(uri);
        scheme = uri.getScheme();
        
        pathLabel.setText(uri.getPath());
        
        if (scheme.matches("file")){
        	String path = uri.getPath();  
        	
        	
        	try {
   
        		File f = new File(path);
        		FileInputStream input = new FileInputStream(f);
        		
        		ByteArray byteArray = new ByteArray((int)f.length());
            	byte[] buffer = new byte[1024];
            	while(true) {
                    int receivedBytes = input.read(buffer);
                    if (receivedBytes == -1) {
                        break;
                    }
                    if (receivedBytes > 0) {
                        byteArray.append(buffer, receivedBytes);
                    }
                }
            	
            	file = byteArray.getBytes();
            
        	} catch (FileNotFoundException ex) {
        		
        	} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
        
        	
        } else if (scheme.matches("content")) {
        	try{
        	InputStream in = getContentResolver().openInputStream((Uri)intent.getExtras().get(Intent.EXTRA_STREAM));
        	
        	ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        	byte[] buffer = new byte[1024];
        
        	while(true) {
                int receivedBytes = in.read(buffer);
                if (receivedBytes == -1) {
                    break;
                }
                if (receivedBytes > 0) {
                    tmp.write(buffer, 0, receivedBytes);
                    
                }
            }
        	
        	file = tmp.toByteArray();
        
         	
        	}catch(Exception ex){
        		pathLabel.setText(ex.toString());
        	}
        }
        
           
    	if ( ((type != null) && (type.matches("image/jpeg"))) || (uri.getPath().toLowerCase().endsWith("jpg")) ){
    		
    		DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
    		
            BitmapFactory.Options opts = new BitmapFactory.Options();
            
            if ((file.length / MAX_SIZE) > 1){
            	opts.inSampleSize = file.length / MAX_SIZE;
            }
            
    		Bitmap img = BitmapFactory.decodeByteArray(file, 0, file.length, opts);
    		
            //actual width of the image (img is a Bitmap object)
            int width = img.getWidth();
            int height = img.getHeight();
            
            float newWidth = 0;
            float newHeight = 0;
            
            if (width >= height){
            	newWidth = (float) (((float)dm.widthPixels) / 1.5);
            	newHeight = height*(newWidth/width);
            } else {
            	newHeight = (float) (((float)dm.heightPixels) / 2.5);
            	newWidth = width*(newHeight/height);
            }

            // calculate the scale
            float scaleWidth = (float) newWidth / width;
            float scaleHeight = (float) newHeight / height;

            // create a matrix for the manipulation
            Matrix matrix = new Matrix();

            // resize the bit map
            matrix.postScale(scaleWidth, scaleHeight);
			
            // recreate the new Bitmap and set it back
            Bitmap resizedBitmap = Bitmap.createBitmap(img, 0, 0,width, height, matrix, true);
            img = resizedBitmap;
 			
            preview.setImageBitmap(getRefelection(img));
            
    		
		}
    	
    	

    }
    
    /** Saving preferences on pause */
    /*
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ip_address", ip);
        editor.putInt("port", port);
        editor.commit();
    }
    */


    /** Sending file to server */
    private void sendFile(byte type) throws UnknownHostException, IOException {
    	Socket socket = new Socket(ip, port);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		
		int size = file.length;
		
    	out.writeInt(size);
    	out.writeByte(type);
        out.write(file);
       
    	out.close();
    	socket.close();
    }
    
    /** On send button press */
    private OnClickListener bSendListener = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			if (file != null){
				dialog = ProgressDialog.show(Printer.this, "", 
		                getText(R.string.sending_wait), true);
				Thread transmit = new Thread(Printer.this);
				transmit.start();
			}
		}
    	
    };
    
    /** On cancel press */
    private OnClickListener bCancelListener = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			Printer.this.finish();
		}
    	
    };
    
    private Handler handler = new Handler(){
   	 @Override
   	 public void handleMessage(Message msg) {
   		 showDialog(msg.what);
   	 }
   	};
    
    public void run(){
    	
			Intent intent = getIntent();
	        String path = ((Uri)intent.getExtras().get(Intent.EXTRA_STREAM)).getPath();
	        
	        try {
	        	
	        
	        if (scheme.matches("file")){
	        	if (path.toLowerCase().endsWith("jpg")) sendFile(JPG);
	        	else if (path.toLowerCase().endsWith("pdf")) sendFile(PDF);
	        } else if (scheme.matches("content")){
	        	Uri uri = (Uri)intent.getExtras().get(Intent.EXTRA_STREAM);
	            String type = getContentResolver().getType(uri);
	            if (type.matches("image/jpeg")) sendFile(JPG);
	            if (type.matches("application/pdf")) sendFile(PDF);
	        }
	        
	        Printer.this.finish();
	        } catch (UnknownHostException e) {
				// TODO Auto-generated catch block
	        	handler.sendMessage(Message.obtain(handler, ERR_CONNECTION));
			} catch (IOException e) {
				ConnectivityManager mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		        NetworkInfo mNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		        if(!mNetworkInfo.isConnected()){
		        	handler.sendMessage(Message.obtain(handler, ERR_IO_NO_CONNECTION));
		        } else {
		        	handler.sendMessage(Message.obtain(handler, ERR_IO_SETTINGS));
		        }
			} finally {
				dialog.dismiss();
			}
		
    }
    
    /** Error dialogs */
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        switch(id) {
        case ERR_CONNECTION:
        	builder.setMessage(getText(R.string.err_io))
        	       .setCancelable(false)
        	       .setPositiveButton(getText(R.string.yes), new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	        	   Intent intent = new Intent();
        	   		       intent.setClass(getBaseContext(), Preferences.class);
        	   		       startActivityForResult(intent, 1);
        	           }
        	       })
        	       .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                dialog.cancel();
        	           }
        	       });
        	dialog = builder.create();
            break;
        case ERR_IO_SETTINGS:
        	builder.setMessage(getText(R.string.err_connection))
 	       .setCancelable(false)
 	       .setNegativeButton(getText(R.string.no), new DialogInterface.OnClickListener() {
 	           public void onClick(DialogInterface dialog, int id) {
 	   		       dialog.cancel();
 	   		       Printer.this.finish(); 	   		       
 	           }
 	       })
 	       .setPositiveButton(getText(R.string.yes), new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	        	   if (file != null){
        	   				dialog = ProgressDialog.show(Printer.this, "", 
        	   		                getText(R.string.sending_wait), true);
        	   				Thread transmit = new Thread(Printer.this);
        	   				transmit.start();
        	   			}
        	           }
        	});
        	dialog = builder.create();
            break;
        case ERR_IO_NO_CONNECTION:
        	builder.setMessage(getText(R.string.err_io_no_connection))
  	       .setCancelable(false)
  	       .setNegativeButton(getText(R.string.no), new DialogInterface.OnClickListener() {
  	           public void onClick(DialogInterface dialog, int id) {
  	   		       dialog.cancel(); 	   		       
  	           }
  	       })
  	       .setPositiveButton(getText(R.string.yes), new DialogInterface.OnClickListener() {
         	           public void onClick(DialogInterface dialog, int id) {
								startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
         	           }
         	});
         	dialog = builder.create();
         	break;
        default:
            dialog = null;
        }
        return dialog;
    }
    
    /** Preferences on press */
    private OnClickListener preferencesListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
		    intent.setClass(getBaseContext(), Preferences.class);
		    startActivityForResult(intent, 1);

		}
    	
    };
    
    /** Geting the refelection */
    public Bitmap getRefelection(Bitmap image){
      //The gap we want between the reflection and the original image
        final int reflectionGap = 4;
      
        //Get you bit map from drawable folder
        Bitmap originalImage = image ;
      
    
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
      
    
        //This will not scale but will flip on the Y axis
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);
      
        //Create a Bitmap with the flip matix applied to it.
        //We only want the bottom half of the image
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height/2, width, height/2, matrix, false);
      
          
        //Create a new bitmap with same width but taller to fit reflection
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width
          , (height + height/2), Config.ARGB_8888);
    
       //Create a new Canvas with the bitmap that's big enough for
       //the image plus gap plus reflection
       Canvas canvas = new Canvas(bitmapWithReflection);
       //Draw in the original image
       canvas.drawBitmap(originalImage, 0, 0, null);
       //Draw in the gap
       Paint deafaultPaint = new Paint();
       canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);
       //Draw in the reflection
       canvas.drawBitmap(reflectionImage,0, height + reflectionGap, null);
    
       //Create a shader that is a linear gradient that covers the reflection
       Paint paint = new Paint();
       LinearGradient shader = new LinearGradient(0, originalImage.getHeight(), 0,
         bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff,
         TileMode.CLAMP);
       //Set the paint to use this shader (linear gradient)
       paint.setShader(shader);
       //Set the Transfer mode to be porter duff and destination in
       paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
       //Draw a rectangle using the paint with our linear gradient
       canvas.drawRect(0, height, width,
         bitmapWithReflection.getHeight() + reflectionGap, paint);
       return bitmapWithReflection;
    }
}