package com.dankstudio.android.fingerdj;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by admin on 2016/12/24.
 */
public class FileUtils {
    public static String getPath(Context context, Uri uri) {

        String result = null;

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection,null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    result =  cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            result =  uri.getPath();
        }

        return result;
    }
}
