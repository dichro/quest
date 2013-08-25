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
			Toaster.s(originContext.get(), "Couldn't create:",
					dir.getAbsolutePath());
			return null;
		}
		Context ctx = originContext.get();
		if (ctx == null) {
			return null;
		}
		Metadata.Helper helper = new Metadata.Helper(ctx);
		long dbId = getDbId(helper);
		File file = new File(dir, dbId + "-" + fileName + ".png");
		try {
			if (!b.compress(Bitmap.CompressFormat.PNG, 100,
					new FileOutputStream(file))) {
				Toaster.s(originContext.get(), "PNG conversion failed");
				return null;
			}
		} catch (FileNotFoundException e) {
			Toaster.s(originContext.get(), "File/directory not found:",
					file.getAbsolutePath());
			return null;
		}
		Uri uri = Uri.fromFile(file);
		updateDb(helper, dbId, uri);
		return Pair.create(uri, dbId);
	}

	protected abstract void updateDb(Metadata.Helper helper, long dbId, Uri uri);

	protected abstract long getDbId(Metadata.Helper helper);

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
