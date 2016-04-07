package com.dashengz.facialrecognitiontest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.kairos.Kairos;
import com.kairos.KairosListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ACCESS = 1;
    private static final int REQUEST_REGISTER = 2;
    KairosListener listener;
    Kairos myKairos;
    private ImageView imageView;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.pic);

        // listener
        listener = new KairosListener() {
            @Override
            public void onSuccess(String response) {
                processResponse(response);
                Log.d("KAIROS DEMO", response);
            }

            @Override
            public void onFail(String response) {
                Log.d("KAIROS DEMO", response);
            }
        };

        myKairos = new Kairos();

        String app_id = "app_id";
        String api_key = "api_key";
        myKairos.setAuthentication(this, app_id, api_key);

        try {
            myKairos.listGalleries(listener);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void processResponse(String response) {
        try {
            JSONObject responseJson = new JSONObject(response);

            // Reset Action
            if (responseJson.has("status"))
                if (responseJson.getString("status").equals("Complete"))
                    Toast.makeText(MainActivity.this, responseJson.getString("message"), Toast.LENGTH_SHORT).show();

            // No face found
            JSONArray errorArray = responseJson.optJSONArray("Errors");
            if (errorArray != null) {
                int errorCode = errorArray.getJSONObject(0).getInt("ErrCode");
                if (errorCode == 5002) {
                    Toast.makeText(MainActivity.this,
                            errorArray.getJSONObject(0).getString("Message"), Toast.LENGTH_SHORT).show();
                }
            }

            // Found a face, but is it enroll (register) or recognize or failure?
            JSONArray imagesArray = responseJson.optJSONArray("images");
            if (imagesArray != null) {
                for (int i = 0; i < imagesArray.length(); i++) {
                    JSONObject transaction = imagesArray.getJSONObject(i).optJSONObject("transaction");
                    // Check status
                    String status = transaction.getString("status");
                    if (status.equals("failure"))
                        Toast.makeText(MainActivity.this, transaction.getString("message"), Toast.LENGTH_SHORT).show();
                    else {
                        // Recognize
                        if (transaction.has("subject")) {
                            String recognizedSubject = transaction.getString("subject");
                            if (recognizedSubject.equals("Admin")) {
                                Intent intent = new Intent(MainActivity.this, Secret.class);
                                startActivity(intent);
                            }
                        }
                        // Register (Enroll)
                        if (transaction.has("subject_id")) {
                            String registeredSubject = transaction.getString("subject_id");
                            Toast.makeText(MainActivity.this,
                                    registeredSubject + " is successfully registered", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void access(View view) {
        takePhotoForResult(REQUEST_ACCESS);
    }

    public void register(View view) {
        takePhotoForResult(REQUEST_REGISTER);
    }

    public void reset(View view) {
        try {
            myKairos.deleteSubject("Admin", "users", listener);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void takePhotoForResult(int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = createImageFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivityForResult(intent, requestCode);
    }

    private File createImageFile() {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(storageDir.getAbsolutePath(), imageFileName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == REQUEST_ACCESS) {
            try {
                Bitmap image = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                String galleryId = "users";
                String selector = "FULL";
                String threshold = "0.75";
                myKairos.recognize(image, galleryId, selector, threshold, null, null, listener);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } finally {
                setPic();
            }
        } else if (requestCode == REQUEST_REGISTER) {
            try {
                Bitmap image = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                String subjectId = "Admin";
                String galleryId = "users";
                String selector = "FULL";
                String multipleFaces = "false";
                myKairos.enroll(image, subjectId, galleryId, selector, multipleFaces, null, listener);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } finally {
                setPic();
            }
        }
    }

    private void setPic() {
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
        imageView.setImageBitmap(bitmap);
    }
}
