package com.android.textrecognition;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.net.URI;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Button capture, recognize;
    TextView output;
    Bitmap bitmap;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.imageView);
        capture=findViewById(R.id.capture);
        recognize=findViewById(R.id.recognize);
        recognize.setEnabled(false);
        output=findViewById(R.id.output);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        !=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values=new ContentValues(2);
                values.put(MediaStore.Images.Media.TITLE, "New Image");
                values.put(MediaStore.Images.Media.DESCRIPTION, "Image for Text Recognition");
                ContentResolver resolver=getContentResolver();
                uri=resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, 4);
            }
        });

        recognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable= (BitmapDrawable) imageView.getDrawable();
                bitmap=bitmapDrawable.getBitmap();
                if(bitmap!=null){
                    final FirebaseVisionImage firebaseVisionImage=FirebaseVisionImage.fromBitmap(bitmap);
                    FirebaseVisionLabelDetector labelDetector=FirebaseVision.getInstance().getVisionLabelDetector();
                    labelDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(
                            new OnSuccessListener<List<FirebaseVisionLabel>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionLabel> firebaseVisionLabels) {
                                    String op="";
                                    for(int i=0; i<firebaseVisionLabels.size(); i++){
                                        op+=firebaseVisionLabels.get(i).getLabel()+"\n";
                                    }
                                    output.setText(op);
                                }
                            }
                    ).addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    output.setText(e.getMessage());
                                }
                            }
                    );
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            imageView.setImageURI(uri);
            recognize.setEnabled(true);
        }
    }
}
