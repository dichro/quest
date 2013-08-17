package to.rcpt.quest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

/**
 * An {@link AsyncTask} to send a {@link Bitmap} via an {@link Intent} to a new
 * {@link Activity}.
 * 
 * @author Miki Habryn <dichro@rcpt.to>
 */
public class ImageHandoffTask extends AsyncTask<Object, Integer, Uri> {
	public static interface HasBitmap {
		Bitmap getBitmap();

		void setBitmap(Bitmap b);
	}

	private static final String TAG = ImageHandoffTask.class.getName();
	private static final String SUBDIR = ImageHandoffTask.class.getPackage()
			.getName();
	private final HasBitmap bitmapSource;
	private final Toaster toast;
	private final Context originContext;
	private final Class<?> destinationClass;
	private final String fileName;

	public ImageHandoffTask(Context originContext,
			Class<? extends Activity> destinationClass, HasBitmap bitmapSource,
			String fileName) {
		this.originContext = originContext;
		this.destinationClass = destinationClass;
		this.bitmapSource = bitmapSource;
		this.fileName = fileName;
		this.toast = new Toaster(originContext);
	}

	@Override
	protected Uri doInBackground(Object... args) {
		Bitmap b = bitmapSource.getBitmap();
		File dir = new File(Environment.getExternalStorageDirectory(), SUBDIR);
		if (!dir.exists() && !dir.mkdirs()) {
			toast.s("Couldn't create: " + dir.getAbsolutePath());
			return null;
		}
		File file = new File(dir, fileName + ".png");
		try {
			if (!b.compress(Bitmap.CompressFormat.PNG, 100,
					new FileOutputStream(file))) {
				toast.s("PNG conversion failed");
				return null;
			}
		} catch (FileNotFoundException e) {
			toast.s("File/directory not found: " + file.getAbsolutePath());
			return null;
		}
		return Uri.fromFile(file);
	}

	@Override
	protected void onPostExecute(Uri uri) {
		if (uri == null) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_SENDTO, uri, originContext,
				destinationClass);
		originContext.startActivity(i);
	}
}
