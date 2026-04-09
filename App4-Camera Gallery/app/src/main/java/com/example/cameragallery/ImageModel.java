package com.example.cameragallery;

import android.content.Context;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model class to represent an image file using DocumentFile for SAF support
 */
public class ImageModel {
    private Uri uri;
    private String name;
    private String path;
    private long size;
    private long dateModified;
    private boolean isDocumentFile;

    /**
     * Constructor for DocumentFile (SAF)
     */
    public ImageModel(DocumentFile documentFile) {
        this.uri = documentFile.getUri();
        this.name = documentFile.getName();
        this.path = uri.toString();
        this.size = documentFile.length();
        this.dateModified = documentFile.lastModified();
        this.isDocumentFile = true;
    }

    // Getters
    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public String getSizeFormatted() {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return (size / 1024) + " KB";
        } else {
            return (size / (1024 * 1024)) + " MB";
        }
    }

    public long getDateModified() {
        return dateModified;
    }

    public String getDateFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(dateModified));
    }

    public boolean isDocumentFile() {
        return isDocumentFile;
    }
}