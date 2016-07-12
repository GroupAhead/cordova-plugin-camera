package org.apache.cordova.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by Kalyan Vishnubhatla on 7/11/16.
 */
public class GalleryImageHelper {
    private static final String TAG = GalleryImageHelper.class.getSimpleName();


    /**
     * Gets the current orientation of the image
     * @param context
     * @param uri
     * @return rotation
     */
    public int getImageOrientation(final Context context, final Uri uri) {
        int cursorBasedRotate = 0;
        String[] cols = { MediaStore.Images.Media.ORIENTATION };
        try {
            Cursor cursor = context.getContentResolver().query(uri, cols, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    cursorBasedRotate = cursor.getInt(0);
                }
                cursor.close();
            }

            // Some phones (Galaxy S6/S5 etc) do not return the right orientation. This is a double check.
            String path = getRealPathFromURI(context, uri);
            ExifInterface exif = new ExifInterface(path);
            int exifBasedRotate = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (exifBasedRotate != cursorBasedRotate) {
                switch (exifBasedRotate) {
                    case ExifInterface.ORIENTATION_ROTATE_90: {
                        return 90;
                    }

                    case ExifInterface.ORIENTATION_ROTATE_180: {
                        return 180;
                    }

                    case ExifInterface.ORIENTATION_ROTATE_270: {
                        return 270;
                    }

                    default:
                    case ExifInterface.ORIENTATION_NORMAL: {
                        return 0;
                    }
                }
            }

        } catch (Exception e) {
            // You can get an IllegalArgumentException if ContentProvider doesn't support querying for orientation.
            Log.i(TAG, e.getMessage());
        }
        return cursorBasedRotate;
    }

    /**
     * Gets the real path from file
     * @param context
     * @param contentUri
     * @return path
     */
    public String getRealPathFromURI(Context context, Uri contentUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return getPathForV19AndUp(context, contentUri);
        } else {
            return getPathForPreV19(context, contentUri);
        }
    }

    /**
     * Handles pre V19 uri's
     * @param context
     * @param contentUri
     * @return
     */
    public String getPathForPreV19(Context context, Uri contentUri) {
        String res = null;

        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();

        return res;
    }

    /**
     * Handles V19 and up uri's
     * @param context
     * @param contentUri
     * @return path
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String getPathForV19AndUp(Context context, Uri contentUri) {
        String wholeID = DocumentsContract.getDocumentId(contentUri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];
        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{ id }, null);

        String filePath = "";
        int columnIndex = cursor.getColumnIndex(column[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();
        return filePath;
    }

}
