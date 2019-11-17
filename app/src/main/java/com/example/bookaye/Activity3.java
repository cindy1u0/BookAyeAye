package com.example.bookaye;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import java.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import org.apache.commons.text.similarity.LevenshteinDetailedDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class Activity3 extends AppCompatActivity {
  static final int REQUEST_TAKE_PHOTO = 1;
  static final int REQUEST_IMAGE_CAPTURE = 1;
  //static int refresh = 0;
  String currentPhotoPath;

  private File createImageFile() throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "JPEG_" + timeStamp + "_";
    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
    );

    // Save a file: path for use with ACTION_VIEW intents
    currentPhotoPath = image.getAbsolutePath();
    return image;
  }

  public static Bitmap rotateBitmapByDegree(Bitmap bitmap, int degree) {
    Bitmap b = bitmap;
    switch (degree) {
      case 0:
        b = rotateBitmap(bitmap, ExifInterface.ORIENTATION_NORMAL);
        break;
      case 90:
        b = rotateBitmap(bitmap, ExifInterface.ORIENTATION_ROTATE_90);
        break;
      case 180:
        b = rotateBitmap(bitmap, ExifInterface.ORIENTATION_ROTATE_180);
        break;
      case 270:
        b = rotateBitmap(bitmap, ExifInterface.ORIENTATION_ROTATE_270);
        break;
    }
    return b;
  }

  public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
    Matrix matrix = new Matrix();
    switch (orientation) {
      case ExifInterface.ORIENTATION_NORMAL:
        return bitmap;
      case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
        matrix.setScale(-1, 1);
        break;
      case ExifInterface.ORIENTATION_ROTATE_180:
        matrix.setRotate(180);
        break;
      case ExifInterface.ORIENTATION_FLIP_VERTICAL:
        matrix.setRotate(180);
        matrix.postScale(-1, 1);
        break;
      case ExifInterface.ORIENTATION_TRANSPOSE:
        matrix.setRotate(90);
        matrix.postScale(-1, 1);
        break;
      case ExifInterface.ORIENTATION_ROTATE_90:
        matrix.setRotate(90);
        break;
      case ExifInterface.ORIENTATION_TRANSVERSE:
        matrix.setRotate(-90);
        matrix.postScale(-1, 1);
        break;
      case ExifInterface.ORIENTATION_ROTATE_270:
        matrix.setRotate(-90);
        break;
      default:
        return bitmap;
    }
    try {
      Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
      bitmap.recycle();
      return bmRotated;
    } catch (OutOfMemoryError e) {
      e.printStackTrace();
      return null;
    }
  }


  private void dispatchTakePictureIntent() {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    // Ensure that there's a camera activity to handle the intent
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      // Create the File where the photo should go
      File photoFile = null;
      try {
        photoFile = createImageFile();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      // Continue only if the File was successfully created
      if (photoFile != null) {
        Uri photoURI = FileProvider.getUriForFile(this,
                "com.example.bookaye.fileprovider",
                photoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
      }
    }
  }

  private void galleryAddPic() {
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    File f = new File(currentPhotoPath);
    Uri contentUri = Uri.fromFile(f);
    mediaScanIntent.setData(contentUri);
    this.sendBroadcast(mediaScanIntent);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    System.out.println("ACTIVITY RESULT: CODE: " + requestCode + "\t RESULT CODE: " + resultCode);
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      galleryAddPic();
      ExifInterface exifObject;
      try {
        exifObject = new ExifInterface(currentPhotoPath);
        int orientation = exifObject.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap imageRotate = rotateBitmap(BitmapFactory.decodeFile(currentPhotoPath), orientation);
        final String original = getIntent().getStringExtra("bookName");
        final Paint red = new Paint();
        red.setColor(Color.RED);
        red.setStyle(Paint.Style.STROKE);
        red.setStrokeWidth(15);
        final Paint yellow = new Paint();
        yellow.setColor(Color.YELLOW);
        yellow.setStyle(Paint.Style.STROKE);
        yellow.setStrokeWidth(15);
        final List<Bitmap> rotateds = new ArrayList<>();
        final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        List<Task<FirebaseVisionText>> tasks = new ArrayList<>();
        for (int i = 0; i < 360; i += 90) {
          Bitmap mutableBitmap = imageRotate.copy(Bitmap.Config.ARGB_8888, true);
          Bitmap rotated = rotateBitmapByDegree(mutableBitmap, i);
          rotateds.add(rotated);
          FirebaseVisionImage fvimage = FirebaseVisionImage.fromBitmap(rotated);
          FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                  .getOnDeviceTextRecognizer();
          Task<FirebaseVisionText> task = detector.processImage(fvimage);
          tasks.add(task);
        }
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
          @Override
          public void onSuccess(List<Object> objects) {
            int minLeven = 0;
            Bitmap finalBitMap = null;
            int i = 0;
            for (Object obj : objects) {
              FirebaseVisionText result = (FirebaseVisionText) obj;

              Optional<FirebaseVisionText.TextBlock> block = result.getTextBlocks().stream().min(new Comparator<FirebaseVisionText.TextBlock>() {
                @Override
                public int compare(FirebaseVisionText.TextBlock b1, FirebaseVisionText.TextBlock b2) {
                  return levenshteinDistance.apply(original.toLowerCase(), b1.getText().toLowerCase()) - levenshteinDistance.apply(original.toLowerCase(), b2.getText().toLowerCase());
                }
              });
              if (block.isPresent()) {
                Canvas canvas = new Canvas(rotateds.get(i));
                /*for (FirebaseVisionText.TextBlock bl : result.getTextBlocks()) {
                  canvas.drawRect(bl.getBoundingBox(), yellow);
                }*/ //I want to skip drawing the yellow boxes
                canvas.drawRect(block.get().getBoundingBox(), red);
                if (finalBitMap == null) {
                  minLeven = levenshteinDistance.apply(original.toLowerCase(), block.get().getText().toLowerCase());
                  finalBitMap = rotateBitmapByDegree(rotateds.get(i), (360 - i * 90) % 360);
                } else if (minLeven > levenshteinDistance.apply(original.toLowerCase(), block.get().getText().toLowerCase())) {
                  minLeven = levenshteinDistance.apply(original.toLowerCase(), block.get().getText().toLowerCase());
                  finalBitMap = rotateBitmapByDegree(rotateds.get(i), (360 - i * 90) % 360);
                }
              }
              i += 1;
            }
            image.setImageBitmap(finalBitMap);
          }
        });
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      finish();
    }
  }

  private ImageView image;
  private EditText text;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_3);
        /*//Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);*/
        /*fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }*/

//        if (refresh == 1) {
//            refresh--;
//            openActivity1();
//        }
    text = findViewById(R.id.textView3);
    image = findViewById(R.id.image);
    //refresh++;
    dispatchTakePictureIntent();

//        image = findViewById(R.id.image);
//        Button scan = findViewById(R.id.buttonScan);
//        scan.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                dispatchTakePictureIntent();
//            }
//        });


  }

  // opens activity 1
  public void openActivity1() {
    Intent i = new Intent(this, BookAye.class);
    startActivity(i);
  }

}
