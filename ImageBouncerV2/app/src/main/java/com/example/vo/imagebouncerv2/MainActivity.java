package com.example.vo.imagebouncerv2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Bitmap bitmap;
    Uri filePath;
    Activity myActiv;

    String imageScore;
    String imageTitle;

    Connection connection;

    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myActiv = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        connection = new Connection(this);
        connection.start();
    }

    public void selectImage(View v){
        Intent upload_intent = new Intent(Intent.ACTION_GET_CONTENT);
        upload_intent.setType("image/*");
        startActivityForResult(upload_intent, PICK_IMAGE_REQUEST);
    }

    public void getImage(View v){
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                connection.sendLine(" getImage ");
                connection.PW.println(" getImage ");
                return null;
            }
        };
        task.execute();
    }

    public void uploadImage(View v){
        if(bitmap!=null) {
            AsyncTask changeImage = new AsyncTask<Bitmap, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Bitmap... params) {
                    String encodedImageString = encodeImage(params[0]);
                    connection.sendLine("newImage " + " TESTNAME " +encodedImageString);
                    System.out.println("Finished sending encoded image.");

                    connection.sendLine(" endOfImageStream ");
                    if (connection.sendLine(" endOfImageStream ")) {
                        System.out.println("Sent end of stream signal.");
                    } else {
                        System.out.println("Couldn't send end of stream signal.");
                    }
                    return null;
                }

                protected void onPostExecution(Bitmap s) {

                }
            };
            Bitmap[] params = {bitmap};
            changeImage.execute(params);
        }
    }

    public void setImageFromString(String title, String score, String newImageString){
        String[] params = {newImageString};
        AsyncTask task = new AsyncTask<String,Void,Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                    bitmap = decodeimage(params[0]);
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap){
                setImageBitmap(bitmap);
            }
        };
        task.execute(params);

        imageScore = score;
        imageTitle = title;

        myActiv.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView imageTitleText = (TextView) findViewById(R.id.imageTitleText);
                TextView imageDetailsText = (TextView) findViewById(R.id.imageScoreText);

                imageTitleText.setText(imageTitle);
                imageDetailsText.setText("Score: "+imageScore);
            }
        });
    }

    public void setImageBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
        imageView.setImageBitmap(bitmap);
        System.out.println("Set new image bitmap.");
    }

    public String encodeImage(Bitmap bmp){
        System.out.println("Encoding image...");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG,50,baos);
        byte[] imagebytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imagebytes, Base64.DEFAULT);
        System.out.println("Done encoding.");
        return encodedImage;
    }
    public Bitmap decodeimage(String imageString){
        byte[] imageBytes = Base64.decode(imageString, Base64.DEFAULT);
        System.out.println("Finished decoding image.");
        return BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {


            try {
                InputStream is;
                is = getApplicationContext().getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(is);
                imageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

}


