package to.rcpt.quest;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * Utility wrapper for {@link Toast}.
 * 
 * @author Miki Habryn <dichro@rcpt.to>
 */
public class Toaster extends Handler {
	private final WeakReference<Context> context;

	public Toaster(Context context) {
		this.context = new WeakReference<Context>(context);
	}

	@Override
	public void handleMessage(Message msg) {
		Context ctx = context.get();
		if (ctx != null) {
			Toast.makeText(ctx, (String) msg.obj, msg.what).show();
		}
	}

	public void toast(int duration, String msg) {
		// TODO(dichro): consider passing in a Context?
		sendMessage(obtainMessage(duration, msg));
	}

	public void s(String msg) {
		// TODO(dichro): add an Object... args param and do something clever
		// with it?
		toast(Toast.LENGTH_SHORT, msg);
	}

	public void l(String msg) {
		toast(Toast.LENGTH_LONG, msg);
	}
}
