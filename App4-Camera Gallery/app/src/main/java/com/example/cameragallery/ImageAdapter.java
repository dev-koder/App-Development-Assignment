package com.example.cameragallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.InputStream;
import java.util.List;

/**
 * Custom adapter for GridView to display images from Uris
 */
public class ImageAdapter extends BaseAdapter {
    private Context context;
    private List<ImageModel> imageList;
    private LayoutInflater inflater;

    public ImageAdapter(Context context, List<ImageModel> imageList) {
        this.context = context;
        this.imageList = imageList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int position) {
        return imageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.gridImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageModel image = imageList.get(position);
        
        // Use a placeholder or clear while loading
        holder.imageView.setImageResource(android.R.color.darker_gray);
        
        // In a real app, use Glide or Picasso. Here we load simple thumbnail.
        Bitmap bitmap = loadThumbnail(image.getUri(), 200, 200);
        if (bitmap != null) {
            holder.imageView.setImageBitmap(bitmap);
        }

        return convertView;
    }

    private Bitmap loadThumbnail(Uri uri, int width, int height) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            input.close();

            options.inSampleSize = calculateInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;
            
            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            input.close();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static class ViewHolder {
        ImageView imageView;
    }
}