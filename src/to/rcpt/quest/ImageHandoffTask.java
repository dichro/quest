package to.rcpt.quest;

import java.io.ByteArrayOutputStream;

import jp.co.cyberagent.android.gpuimage.GPUImageView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

/**
 * An {@link AsyncTask} to extract a {@link Bitmap} from a {@link GPUImageView}
 * and send it via an {@link Intent} to a new {@link Activity}.
 * 
 * @author Miki Habryn <dichro@rcpt.to>
 */
public class ImageHandoffTask extends
		AsyncTask<Object, Integer, ByteArrayOutputStream> {
	private static final String TAG = "ImageHandoffTask";
	private final GPUImageView imageView;
	private final Toaster toast;
	private final Context originContext;
	private final Class<?> destinationClass;

	public ImageHandoffTask(Context originContext,
			Class<? extends Activity> destinationClass, Toaster toast,
			GPUImageView imageView) {
		this.originContext = originContext;
		this.destinationClass = destinationClass;
		this.toast = toast;
		this.imageView = imageView;
	}

	@Override
	protected ByteArrayOutputStream doInBackground(Object... arg0) {
		try {
			Bitmap b = imageView.capture(512, 512);
			// TODO(dichro): save it somewhere instead
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			if (!b.compress(Bitmap.CompressFormat.PNG, 100, bs)) {
				toast.s("PNG conversion failed");
				return null;
			}
			return bs;
		} catch (InterruptedException e) {
			toast.l("Image save interrupted");
			return null;
		}
	}

	protected void onPostExecute(ByteArrayOutputStream bs) {
		byte[] ba = bs.toByteArray();
		Log.i(TAG, "Compressed PNG bytes: " + ba.length);
		toast.s(ba.length + " bytes");
		Intent i = new Intent(originContext, destinationClass);
		i.putExtra("image", ba);
		originContext.startActivity(i);
	}
}