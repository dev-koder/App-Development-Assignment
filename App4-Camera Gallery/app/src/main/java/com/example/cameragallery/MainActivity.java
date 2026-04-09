package com.example.cameragallery;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button captureBtn, refreshBtn, deleteAllBtn, selectFolderBtn;
    private GridView galleryGrid;
    private TextView imageCount;

    private ImageAdapter adapter;
    private List<ImageModel> imageList;

    private Uri selectedFolderUri;
    private Uri tempPhotoUri;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int FOLDER_PICKER_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        captureBtn = findViewById(R.id.captureBtn);
        refreshBtn = findViewById(R.id.refreshBtn);
        deleteAllBtn = findViewById(R.id.deleteAllBtn);
        selectFolderBtn = findViewById(R.id.selectFolderBtn);
        galleryGrid = findViewById(R.id.galleryGrid);
        imageCount = findViewById(R.id.imageCount);

        imageList = new ArrayList<>();
        adapter = new ImageAdapter(this, imageList);
        galleryGrid.setAdapter(adapter);
    }

    private void setupClickListeners() {
        captureBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            } else if (selectedFolderUri == null) {
                Toast.makeText(this, "Please select a folder first", Toast.LENGTH_SHORT).show();
                openDirectoryPicker();
            } else {
                openCamera();
            }
        });

        selectFolderBtn.setOnClickListener(v -> openDirectoryPicker());
        refreshBtn.setOnClickListener(v -> loadImagesFromFolder());
        deleteAllBtn.setOnClickListener(v -> deleteAllImages());

        galleryGrid.setOnItemClickListener((parent, view, position, id) -> {
            showImageDetails(imageList.get(position));
        });
    }

    private void openDirectoryPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, FOLDER_PICKER_CODE);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // Use a temp file in external files dir (more reliable for FileProvider)
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File tempFile = new File(storageDir, "temp_photo.jpg");
        
        try {
            if (tempFile.exists()) tempFile.delete();
            tempFile.createNewFile();
        } catch (IOException e) { 
            e.printStackTrace();
            Toast.makeText(this, "Failed to create temp file", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            tempPhotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", tempFile);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(this, "FileProvider error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempPhotoUri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        // Grant permission to all apps that can handle the intent
        List<android.content.pm.ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (android.content.pm.ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            grantUriPermission(packageName, tempPhotoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (selectedFolderUri == null) {
                    Toast.makeText(this, "Camera permission granted. Please select a folder.", Toast.LENGTH_SHORT).show();
                    openDirectoryPicker();
                } else {
                    openCamera();
                }
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == FOLDER_PICKER_CODE && data != null) {
                selectedFolderUri = data.getData();
                getContentResolver().takePersistableUriPermission(selectedFolderUri, 
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                loadImagesFromFolder();
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                saveTempPhotoToFolder();
            }
        }
    }

    private void saveTempPhotoToFolder() {
        if (selectedFolderUri == null || tempPhotoUri == null) return;

        try {
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, selectedFolderUri);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timeStamp + ".jpg";
            
            DocumentFile newFile = pickedDir.createFile("image/jpeg", fileName);
            if (newFile != null) {
                InputStream is = getContentResolver().openInputStream(tempPhotoUri);
                OutputStream os = getContentResolver().openOutputStream(newFile.getUri());
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                is.close();
                os.close();
                
                Toast.makeText(this, "Saved: " + fileName, Toast.LENGTH_SHORT).show();
                loadImagesFromFolder();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImagesFromFolder() {
        if (selectedFolderUri == null) return;

        imageList.clear();
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, selectedFolderUri);
        DocumentFile[] files = pickedDir.listFiles();

        for (DocumentFile file : files) {
            if (file.getType() != null && file.getType().startsWith("image/")) {
                imageList.add(new ImageModel(file));
            }
        }
        
        imageList.sort((a, b) -> Long.compare(b.getDateModified(), a.getDateModified()));
        adapter.notifyDataSetChanged();
        imageCount.setText("Images: " + imageList.size() + " in chosen folder");
    }

    private void showImageDetails(ImageModel image) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialogue_image_details, null);
        builder.setView(dialogView);

        ImageView dialogImage = dialogView.findViewById(R.id.dialogImage);
        TextView fileName = dialogView.findViewById(R.id.dialogFileName);
        TextView filePath = dialogView.findViewById(R.id.dialogFilePath);
        TextView fileSize = dialogView.findViewById(R.id.dialogFileSize);
        TextView fileDate = dialogView.findViewById(R.id.dialogFileDate);
        Button deleteBtn = dialogView.findViewById(R.id.dialogDeleteBtn);
        Button closeBtn = dialogView.findViewById(R.id.dialogCloseBtn);

        try {
            InputStream is = getContentResolver().openInputStream(image.getUri());
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            dialogImage.setImageBitmap(bitmap);
            is.close();
        } catch (Exception e) { e.printStackTrace(); }

        fileName.setText(image.getName());
        filePath.setText("URI: " + image.getUri().toString());
        fileSize.setText(image.getSizeFormatted());
        fileDate.setText(image.getDateFormatted());

        AlertDialog dialog = builder.create();

        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Image?")
                    .setMessage("Are you sure you want to delete this photo?")
                    .setPositiveButton("Yes", (d, w) -> {
                        DocumentFile fileToDelete = DocumentFile.fromSingleUri(this, image.getUri());
                        if (fileToDelete != null && fileToDelete.delete()) {
                            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                            loadImagesFromFolder();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void deleteAllImages() {
        if (imageList.isEmpty() || selectedFolderUri == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete All?")
                .setMessage("Delete all " + imageList.size() + " images in this folder?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    for (ImageModel image : imageList) {
                        DocumentFile.fromSingleUri(this, image.getUri()).delete();
                    }
                    loadImagesFromFolder();
                })
                .setNegativeButton("No", null)
                .show();
    }
}