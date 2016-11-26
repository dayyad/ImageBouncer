package com.example.jerem.imagebouncer;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

// The main class that handles all GUI elements of the app. All my code is in here
public class MainActivity extends AppCompatActivity {
    connectionClientSide connection;
    public MainActivity(){
        System.out.println("Creating connection class.");
        connection = new connectionClientSide(this);
        connection.start();
    }
    // This variable is used by the gallery function, it identifies which activity is occurring (at the moment there is only one, the getting image from gallery activity)
    private static final int PICK_PHOTO_FOR_AVATAR = 0;

    // The class wide variables for the bitmap image, the byte_array, and the input stream.
    private Bitmap bit;
    private byte[] byte_array;
    private InputStream is;

    @Override
    // The function that calls when the app is run, not written by me.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //Android doesn't allow calls to a socket directly from the main class as it techincally is blocking the main class
    //while perfomring operations on networ, therefore they need to be done asyncronously via the following class
    //USE this class to call all methods that would be called on the connection class.
    private void performNetworkOperation(final String key) {
        final String asyncTaksKey = key;

        AsyncTask newTask = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                System.out.println(key);
                if(key.equals("submitImage")){
                    connection.submitImage(byte_array);
                } else if(key.equals("getNewImage")){
                    connection.getNewImage();
                } else if(key.equals("upVote")){
                    //TODO
                } else if(key.equals("downVote")){
                    //TODO
                }

                return null;
            }
        };

        newTask.execute();
    }

    public void setByte_array(byte[] b) {
        System.out.println("trying to change byte array");
        this.byte_array = b;
        is = new ByteArrayInputStream(byte_array);
        updateImage(is);
    }


    public void bumpImage(View v) {
        System.out.println("requesting new image");

        AsyncTask myTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                InputStream in = null;
                try {
                    in = new java.net.URL("128.199.236.107/1.jpg").openStream();

                    Bitmap bmp = BitmapFactory.decodeStream(in);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    setByte_array(byteArray);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
        myTask.execute();



       // performNetworkOperation("getNewImage");
    }

    // The function DIRECTLY called by clicking the button "upload". It calls the intent, (which is basically what I imagine as the external call to open the gallery. It relies on another function
    // , onActivityResult(), to do the rest once the user has selected the image.
    public void uploadImage(View v) {
        System.out.println("trying to upload image");
        Log.v("debug", "It works");
        Intent upload_intent = new Intent(Intent.ACTION_GET_CONTENT);
        upload_intent.setType("image/*");
        startActivityForResult(upload_intent, PICK_PHOTO_FOR_AVATAR);

    }

    public void voteSuccess(boolean success){
        //TODO
    }

    // Takes an input stream, converts it into a bitmap, and updates the image
    public void updateImage(InputStream is) {
        System.out.println("trying to update image");
        this.bit = BitmapFactory.decodeStream(is);
        ImageView mImg;
        mImg = (ImageView) findViewById(R.id.imageView);
        mImg.setImageBitmap(this.bit);

        //Jonahs experimental way of setting the byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.bit.compress(Bitmap.CompressFormat.PNG, 100, stream);
        this.byte_array = stream.toByteArray();
        //

//        try {
//            this.byte_array = IOUtils.toByteArray(is);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    //Called after image is selected, previously was in the updateImage method but this caused infinite loop.
    public void finishUploadingImage(){
        performNetworkOperation("submitImage");
    }

    // This is a GENERAL function which is called when an activity is resolved. At the moment, it only does something meaningful when the PICK_PHOTO_FOR_AVATAR activity is resolved, but could
    // be expanded if the more activities were called. It creates an inputstream based on the file selected, and then calls updateImage().
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            try {
                this.is = getApplicationContext().getContentResolver().openInputStream(data.getData());
                updateImage(this.is);
                finishUploadingImage();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

}