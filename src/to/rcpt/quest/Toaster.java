package to.rcpt.quest;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
		super(Looper.getMainLooper());
		this.context = new WeakReference<Context>(context);
	}

	@Override
	public void handleMessage(Message msg) {
		Context ctx = context.get();
		if (ctx != null) {
			Toast.makeText(ctx, (String) msg.obj, msg.what).show();
		}
	}

	public void toast(int duration, String msg, String... more) {
		StringBuilder sb = new StringBuilder(msg);
		for (String s : more) {
			sb.append(" ").append(s);
		}
		sendMessage(obtainMessage(duration, sb.toString()));
	}

	public void s(String msg, String... more) {
		toast(Toast.LENGTH_SHORT, msg, more);
	}

	public void l(String msg, String... more) {
		toast(Toast.LENGTH_LONG, msg, more);
	}

	public static void s(Context context, String msg, String... more) {
		if (context != null) {
			new Toaster(context).s(msg, more);
		}
	}

	public static void l(Context context, String msg, String... more) {
		if (context != null) {
			new Toaster(context).l(msg, more);
		}
	}
}
