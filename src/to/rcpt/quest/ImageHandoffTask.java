package to.rcpt.quest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Pair;

/**
 * An {@link AsyncTask} to send a {@link Bitmap} via an {@link Intent} to a new
 * {@link Activity}.
 * 
 * @author Miki Habryn <dichro@rcpt.to>
 */
public abstract class ImageHandoffTask extends
		AsyncTask<Object, Integer, Pair<Uri, Long>> {
	public static interface HasBitmap {
		Bitmap getBitmap();

		void setBitmap(Bitmap b);
	}

	private static final String TAG = ImageHandoffTask.class.getName();
	private static final String SUBDIR = ImageHandoffTask.class.getPackage()
			.getName();
	private final WeakReference<HasBitmap> bitmapSource;
	private final Toaster toast;
	private final WeakReference<Context> originContext;
	private final Class<?> destinationClass;
	private final String fileName;

	public ImageHandoffTask(Context originContext,
			Class<? extends Activity> destinationClass, HasBitmap bitmapSource,
			String fileName) {
		this.originContext = new WeakReference<Context>(originContext);
		this.destinationClass = destinationClass;
		this.bitmapSource = new WeakReference<HasBitmap>(bitmapSource);
		this.fileName = fileName;
		this.toast = new Toaster(originContext);
	}

	@Override
	protected Pair<Uri, Long> doInBackground(Object... args) {
		HasBitmap bs = bitmapSource.get();
		if (bs == null) {
			return null;
		}
		Bitmap b = bs.getBitmap();
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
		Uri uri = Uri.fromFile(file);
		Context ctx = originContext.get();
		if (ctx == null) {
			return null;
		}
		return Pair.create(uri, updateDb(new Metadata.Helper(ctx), uri));
	}

	protected abstract long updateDb(Metadata.Helper helper, Uri uri);

	@Override
	protected void onPostExecute(Pair<Uri, Long> uri) {
		if (uri == null) {
			return;
		}
		Context ctx = originContext.get();
		if (ctx == null) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_SENDTO, uri.first, ctx,
				destinationClass);
		i.putExtra(BaseColumns._ID, uri.second);
		ctx.startActivity(i);
	}
}
