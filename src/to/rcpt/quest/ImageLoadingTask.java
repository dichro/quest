package to.rcpt.quest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

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

public abstract class ImageLoadingTask extends AsyncTask<Uri, Integer, Bitmap> {
	private static final String TAG = ImageLoadingTask.class.getName();
	private static final String[] CONTENT_PROJECTION = new String[] {
			Media.DATA, ImageColumns.ORIENTATION };
	private final Toaster toast;
	private final WeakReference<Context> context;
	private double maxDimension;

	public ImageLoadingTask(Context ctx, double maxDimension) {
		this.toast = new Toaster(ctx);
		this.context = new WeakReference<Context>(ctx);
		this.maxDimension = maxDimension;
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
		return loadFile(path, orientation);
	}

	private Bitmap loadFile(String path, int orientation) {
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
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new BufferedInputStream(
					new FileInputStream(f)), null, o);
			Log.i(TAG, path + ": " + o.outWidth + "x" + o.outHeight);
			if (o.outWidth == 0 || o.outHeight == 0) {
				toast.s("Failed to load image");
				return null;
			}
			int scaleFactor = (int) Math.pow(
					2,
					Math.ceil(Math.log(Math.max(o.outWidth / maxDimension,
							o.outHeight / maxDimension)) / Math.log(2)));
			Log.i(TAG, "Downsampling " + o.outWidth + "x" + o.outHeight
					+ " image by " + scaleFactor);
			o.inJustDecodeBounds = false;
			o.inSampleSize = scaleFactor;
			// TODO(dichro): pass in a flag to determine if bitmap should be
			// mutable; set below appropriately; remove bitmap.copy.
			o.inPurgeable = true;
			o.inInputShareable = true;
			Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(
					new FileInputStream(f)), null, o);
			if (orientation > 0) {
				Matrix rotate = new Matrix();
				rotate.postRotate(orientation);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
						bitmap.getHeight(), rotate, true);
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

	private Bitmap loadFile(Uri uri) {
		// TODO(dichro): EXIF orientation?
		return loadFile(uri.getPath(), 0);
	}

	protected abstract void onPostExecute(Bitmap bm);

	public static class HasBitmap extends ImageLoadingTask {
		private final WeakReference<ImageHandoffTask.HasBitmap> hasBitmap;

		public HasBitmap(Context ctx, ImageHandoffTask.HasBitmap hasBitmap,
				double maxDimension) {
			super(ctx, maxDimension);
			this.hasBitmap = new WeakReference<ImageHandoffTask.HasBitmap>(
					hasBitmap);
		}

		@Override
		protected void onPostExecute(Bitmap bm) {
			if (bm == null) {
				return;
			}
			ImageHandoffTask.HasBitmap hb = hasBitmap.get();
			if (hb != null) {
				hb.setBitmap(bm);
			}
		}
	}
}
