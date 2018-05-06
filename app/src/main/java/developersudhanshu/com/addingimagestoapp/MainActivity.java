package developersudhanshu.com.addingimagestoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView userPic;
    Button changePic;
    AlertDialog dialog;
    private final static int PICK_IMAGE_CODE = 123;
    private final static int TAKE_PICTURE_CODE = 133;
    private final static int STORAGE_PERMISSION_CODE = 135;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userPic = (ImageView) findViewById(R.id.img_view);
        changePic = (Button) findViewById(R.id.btn_change_pic);

        changePic.setOnClickListener(this);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_change_pic:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater layoutInflater = getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.dialog_image_chooser, null);
                builder.setView(view);
                Button viewPic, pickFromGallery, clickFromCamera;
                viewPic = (Button) view.findViewById(R.id.btn_img_pick_dialog_view);
                clickFromCamera = (Button) view.findViewById(R.id.btn_img_pick_dialog_camera);
                pickFromGallery = (Button) view.findViewById(R.id.btn_img_pick_dialog_gallery);

                viewPic.setOnClickListener(this);
                clickFromCamera.setOnClickListener(this);
                pickFromGallery.setOnClickListener(this);

                dialog = builder.create();
                dialog.show();
                break;
            case R.id.btn_img_pick_dialog_view:
                if(dialog.isShowing()){
                    dialog.cancel();
                }
                Intent profileViewActivity = new Intent(MainActivity.this, ImageViewActivity.class);
                startActivity(profileViewActivity);
                break;
            case R.id.btn_img_pick_dialog_camera:
                if(dialog.isShowing()){
                    dialog.cancel();
                }
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    if(cameraIntent.resolveActivity(getPackageManager()) != null){
                        File photoFile = null;
                        try {
                                photoFile = createImageFile();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // If the file was created successfully
                        if (photoFile != null) {
                            Uri photoUri = FileProvider.getUriForFile(this,
                                    Constants.APP_AUTHORITY,
                                    photoFile);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                            startActivityForResult(cameraIntent, TAKE_PICTURE_CODE);
                        }
                    }
                }else{
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                }
                break;
            case R.id.btn_img_pick_dialog_gallery:
                if(dialog.isShowing()){
                    dialog.cancel();
                }
                // Note: To pick up the image from the gallery we need the
                // EXTERNAL_STORAGE permission else it will not be able fetch the image.
                // Also check that the permission is granted at runtime or not.
                Intent galleryImagePickIntent = new Intent();
                galleryImagePickIntent.setType("image/*");
                galleryImagePickIntent.setAction(Intent.ACTION_PICK);
                startActivityForResult(galleryImagePickIntent, PICK_IMAGE_CODE);
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "Profile_Image";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK){
            String imagePath = getAbsolutePath(data.getData());
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                    300, 250, false);
            userPic.setImageBitmap(scaledBitmap);
        }
        if (requestCode == TAKE_PICTURE_CODE && resultCode == RESULT_OK){
            // Note: data.getData() will always be null as ACTION_IMAGE_CAPTURE
            // does not return a Uri. So to fetch the image we will use the Uri
            // which we already passed in as EXTRA_OUTPUT.
            // Note: If EXTRA_OUTPUT is not present then a small sized image is returned
            // as a Bitmap object in the extra field.
            // Refer the following links: https://developer.android.com/reference/android/provider/MediaStore#ACTION_IMAGE_CAPTURE
            // https://stackoverflow.com/questions/34119483/android-camera-photo-comes-back-null
//            if(data != null) {
//                String path = getAbsolutePath(data.getData());
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath),
                        300, 250, false);
                userPic.setImageBitmap(scaledBitmap);
//            }else{
//                Toast.makeText(this, "No image Uri fetched", Toast.LENGTH_SHORT).show();
//            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STORAGE_PERMISSION_CODE && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    public String getAbsolutePath(Uri uri) {
        String[] projection = { MediaStore.MediaColumns.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }
}
