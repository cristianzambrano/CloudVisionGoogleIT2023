package com.example.cloudvisiongoogleit2023;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    Vision vision;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Vision.Builder visionBuilder = new Vision.Builder(new NetHttpTransport(),
        new AndroidJsonFactory(),  null);

        visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer(
        "AIzaSyB5MkIB5lNnQH1kC1tZ3ATeEsv7z66moKs"));

        vision = visionBuilder.build();


    }

    public void AbrirGaleriaFotos(View view) {
         Intent i = new Intent(Intent.ACTION_PICK,
                 android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1);
  }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
        ImageView imageView = (ImageView) findViewById(R.id.imagenAReconocer);
        imageView.setImageURI(data.getData());
    }
 }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
    int originalWidth = bitmap.getWidth();   
    int originalHeight = bitmap.getHeight(); 
    int resizedWidth = maxDimension;    
    int resizedHeight = maxDimension;   
    
    if (originalHeight > originalWidth) {       
        resizedHeight = maxDimension;       
        resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);   
    } else if (originalWidth > originalHeight) {    
        resizedWidth = maxDimension;     
        resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth); 
    } else if (originalHeight == originalWidth) {
        resizedHeight = maxDimension;     
        resizedWidth = maxDimension;    
    }   
    return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
  }

    public Image getImageToProcess(){

        ImageView imagen = (ImageView)findViewById(R.id.imagenAReconocer);
        BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        bitmap = scaleBitmapDown(bitmap, 800);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] imageInByte = stream.toByteArray();

        Image inputImage = new Image();
        inputImage.encodeContent(imageInByte);

        return inputImage;
  }

    public BatchAnnotateImagesRequest setBatchRequest(String TipoSolic, Image inputImage){

        Feature desiredFeature = new Feature();
        desiredFeature.setType(TipoSolic);

        AnnotateImageRequest request = new AnnotateImageRequest();
        request.setImage(inputImage);
        request.setFeatures(Arrays.asList(desiredFeature));


        BatchAnnotateImagesRequest batchRequest =  new BatchAnnotateImagesRequest();
        batchRequest.setRequests(Arrays.asList(request));
        return batchRequest;
 }

    public void ProcesarTexto(View View){
    AsyncTask.execute(new Runnable() {
        @Override
        public void run() {
        BatchAnnotateImagesRequest batchRequest = setBatchRequest("TEXT_DETECTION", getImageToProcess());

        try {
                Vision.Images.Annotate  annotateRequest = vision.images().annotate(batchRequest);
                annotateRequest.setDisableGZipContent(true);
                BatchAnnotateImagesResponse response  = annotateRequest.execute();

                final TextAnnotation text = response.getResponses().get(0).getFullTextAnnotation();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView imageDetail = (TextView)findViewById(R.id.textView2);
                        imageDetail.setText(text.getText());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
}




}