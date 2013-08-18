package to.rcpt.quest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

import to.rcpt.quest.ImageHandoffTask.HasBitmap;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.util.Log;

public class ImageLoadingTask extends AsyncTask<Uri, Integer, Bitmap> {
	private static final String TAG = ImageLoadingTask.class.getName();
	private static final String[] CONTENT_PROJECTION = new String[] {
			Media.DATA, ImageColumns.ORIENTATION };
	private final Toaster toast;
	private final WeakReference<ImageHandoffTask.HasBitmap> hasBitmap;
	private final WeakReference<Context> context;

	public ImageLoadingTask(Context ctx, ImageHandoffTask.HasBitmap hasBitmap) {
		this.toast = new Toaster(ctx);
		this.context = new WeakReference<Context>(ctx);
		this.hasBitmap = new WeakReference<ImageHandoffTask.HasBitmap>(
				hasBitmap);
	}

	@Override
	protected Bitmap doInBackground(Uri... uris) {
		Uri uri = uris[0];
		String scheme = uri.getScheme();
		if ("file".equals(scheme)) {
			return loadFile(uri);
		} else if ("content".equals(scheme)) {
			return loadContent(uri);
		}
		toast.s("Unknown URI scheme: " + scheme);
		return null;
	}

	private Bitmap loadContent(Uri uri) {
		Context ctx = context.get();
		if (ctx == null) {
			return null;
		}
		Cursor cursor = ctx.getContentResolver().query(uri, CONTENT_PROJECTION,
				null, null, null);
		cursor.moveToFirst();
		String path = cursor.getString(cursor.getColumnIndex(Media.DATA));
		int orientation = cursor.getInt(cursor
				.getColumnIndex(ImageColumns.ORIENTATION));
		cursor.close();
		// downsample
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, o);
		Log.i(TAG, path + ": " + o.outWidth + "x" + o.outHeight);
		if (o.outWidth == 0 || o.outHeight == 0) {
			toast.s("Failed to load image");
			return null;
		}
		// constraints: ImageView can only render bitmaps no larger than
		// 2048x2048. inScaleFactor is rounded down to the nearest power of
		// two by Bitmap.decodeFile.
		int scaleFactor = (int) Math.pow(
				2,
				Math.ceil(Math.log(Math.max(o.outWidth / 2048.0,
						o.outHeight / 2048.0)) / Math.log(2)));
		Log.i(TAG, "Downsampling " + o.outWidth + "x" + o.outHeight
				+ " image by " + scaleFactor);
		o.inJustDecodeBounds = false;
		o.inSampleSize = scaleFactor;
		o.inPurgeable = true;
		o.inInputShareable = true;
		Bitmap bm = BitmapFactory.decodeFile(path, o);
		if (orientation > 0) {
			Matrix rotate = new Matrix();
			rotate.postRotate(orientation);
			bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(),
					rotate, true);
		}
		return bm;
	}

	private Bitmap loadFile(Uri uri) {
		String path = uri.getPath();
		File f = new File(path);
		if (!f.exists()) {
			toast.s("Couldn't find file: " + path);
			return null;
		}
		if (!f.canRead()) {
			toast.s("Not readable: " + path);
			return null;
		}
		try {
			FileInputStream in = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(
					in));
			if (bitmap == null) {
				toast.s("Couldn't load image: " + path + " (" + f.length()
						+ " bytes)");
				return null;
			}
			if (!bitmap.isMutable()) {
				bitmap = bitmap.copy(bitmap.getConfig(), true);
				if (bitmap == null) {
					toast.s("Failed to make a mutable copy of bitmap");
					return null;
				}
			}
			return bitmap;
		} catch (FileNotFoundException e) {
			toast.s("File not found: " + path);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap bm) {
		if (bm == null) {
			return;
		}
		HasBitmap hb = hasBitmap.get();
		if (hb != null) {
			hb.setBitmap(bm);
		}
	}
}
