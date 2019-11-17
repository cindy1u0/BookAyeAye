package com.example.bookaye;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

//import com.google.android.gms.vision.text.TextRecognizer;

public class Activity4 extends AppCompatActivity {

  ImageView image;
  Button pick;
  String path;

  private static final int PICK_CODE = 1000;
  private static final int PERMISSION = 1001;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_4);

    // View
    image = findViewById(R.id.image_view);
    pick = findViewById(R.id.buttonPic);

    // handle clicking action
        /*pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checks runtime permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission((Manifest.permission.READ_EXTERNAL_STORAGE)) == PackageManager.PERMISSION_DENIED) {
                        // Request permission
                        String[] perm = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        // Pop-up window to show permission denied
                        requestPermissions(perm, PERMISSION);
                    } else {
                        // Permission granted
                        pickFromGallery();
                    }
                } else {
                    // pick the picture from the gallery
                    pickFromGallery();
                }
            }
        });*/


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (checkSelfPermission((Manifest.permission.READ_EXTERNAL_STORAGE)) == PackageManager.PERMISSION_DENIED) {
        // Request permission
        String[] perm = {Manifest.permission.READ_EXTERNAL_STORAGE};
        // Pop-up window to show permission denied
        requestPermissions(perm, PERMISSION);
      } else {
        // Permission granted
        pickFromGallery();
      }
    } else {
      // pick the picture from the gallery
      pickFromGallery();
    }
  }

  public void pickFromGallery() {
    // pick an image
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType("image/*");
    startActivityForResult(intent, PICK_CODE);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case PERMISSION:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          pickFromGallery();
        } else {
          // permission denied
          Toast.makeText(this, "Sorry no permission!", Toast.LENGTH_SHORT).show();
        }

    }
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

  private String getRealPathFromURI(Uri contentURI) {
    String result;
    String[] projection = {  MediaStore.Images.Media.DATA};
    Cursor cursor = getContentResolver().query(contentURI, projection, null, null, null);
    if (cursor == null) { // Source is Dropbox or other similar local file path
      result = contentURI.getPath();
    } else {
      int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
      cursor.moveToFirst();
      result = cursor.getString(idx);
      cursor.close();
      return result;
    }
    return result;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK && requestCode == PICK_CODE && image != null) {
//            // set image to image view
//            image.setImageURI(data.getData());
//        }
//        else {
//            finish();
//        }
    if (resultCode == RESULT_OK && requestCode == PICK_CODE && image != null) {
      image.setImageURI(data.getData());
      path = getRealPathFromURI(data.getData());
      ExifInterface exifObject;
      try {
        exifObject = new ExifInterface(path);
        int orientation = exifObject.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap imageRotate = rotateBitmap(BitmapFactory.decodeFile(path), orientation);
        final String original = getIntent().getStringExtra("bookName");
        final Paint red = new Paint();
        red.setColor(Color.RED);
        red.setStyle(Paint.Style.STROKE);
        red.setStrokeWidth(10);
        final Paint yellow = new Paint();
        yellow.setColor(Color.YELLOW);
        yellow.setStyle(Paint.Style.STROKE);
        yellow.setStrokeWidth(10);
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
                for (FirebaseVisionText.TextBlock bl : result.getTextBlocks()) {
                  canvas.drawRect(bl.getBoundingBox(), yellow);
                }
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
}