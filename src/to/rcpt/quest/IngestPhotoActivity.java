package to.rcpt.quest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class IngestPhotoActivity extends Activity {
	private static final String TAG = "IngestPhotoActivity";
	private View imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ingest_photo);
		imageView = findViewById(R.id.imageView);
	}

	private class ImageHandler extends AsyncTask<Uri, Integer, Integer> {
		@Override
		protected Integer doInBackground(Uri... uris) {
			Uri uri = uris[0];
			Log.i(TAG, "Got " + uri);
			// TODO(dichro): handle things other than "content://"

			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			String path = uri.getPath();
			BitmapFactory.decodeFile(path, o);
			Log.i(TAG, path + ": " + o.outWidth + "x" + o.outHeight);
			if (o.outWidth == 0 || o.outHeight == 0) {
				toast("Failed to load image", Toast.LENGTH_SHORT);
				return 1;
			}
			return 0;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		Log.i(TAG, "resume " + intent);
		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
			new ImageHandler().execute(uri);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_ingest_photo, menu);
		return true;
	}

	private final Handler toaster = new Handler() {
		// TODO(dichro): resolve this weakreference stuff
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(IngestPhotoActivity.this, (String) msg.obj, msg.what)
					.show();
		}
	};

	private void toast(String msg, int duration) {
		toaster.sendMessage(toaster.obtainMessage(duration, msg));
	}
}
