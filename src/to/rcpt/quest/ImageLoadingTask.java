package to.rcpt.quest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

public class ImageLoadingTask extends AsyncTask<Uri, Integer, Bitmap> {
	private final Toaster toast;
	private final ImageHandoffTask.HasBitmap hasBitmap;

	public ImageLoadingTask(Toaster toast, ImageHandoffTask.HasBitmap hasBitmap) {
		this.toast = toast;
		this.hasBitmap = hasBitmap;
	}

	@Override
	protected Bitmap doInBackground(Uri... uris) {
		Uri uri = uris[0];
		String scheme = uri.getScheme();
		if ("file".equals(scheme)) {
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
				Bitmap bitmap = BitmapFactory
						.decodeStream(new BufferedInputStream(in));
				if (bitmap == null) {
					toast.s("Couldn't load image: " + path + " (" + f.length()
							+ " bytes)");
					return null;
				}
				return bitmap.copy(bitmap.getConfig(), true);
			} catch (FileNotFoundException e) {
				toast.s("File not found: " + path);
			}
		} else {
			toast.s("Unknown URI scheme: " + scheme);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap uri) {
		if (uri == null) {
			return;
		}
		hasBitmap.setBitmap(uri);
	}
}
