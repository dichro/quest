package to.rcpt.quest;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

public class IngestPhotoActivity extends Activity {
	private static final String TAG = "IngestPhotoActivity";
	private static final String[] PATH = new String[] { Media.DATA,
			ImageColumns.ORIENTATION };
	private ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ingest_photo);
		imageView = (ImageView) findViewById(R.id.imageView);
	}

	private class ImageHandler extends AsyncTask<Uri, Integer, Bitmap> {
		@Override
		protected Bitmap doInBackground(Uri... uris) {
			Uri uri = uris[0];
			Log.i(TAG, "Got " + uri);
			// TODO(dichro): handle things other than "content://"
			Cursor cursor = getContentResolver().query(uri, PATH, null, null,
					null);
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
				toast("Failed to load image", Toast.LENGTH_SHORT);
				return null;
			}
			// constraints: ImageView can only render 2048x2048 bitmaps.
			// inScaleFactor is rounded down to the nearest power of two.
			int scaleFactor = (int) Math.pow(
					Math.ceil(Math.log(Math.max(o.outWidth / 2048.0,
							o.outHeight / 2048.0)) / Math.log(2)), 2);
			o.inJustDecodeBounds = false;
			o.inSampleSize = scaleFactor;
			o.inPurgeable = true;
			o.inInputShareable = true;
			Bitmap bm = BitmapFactory.decodeFile(path, o);
			if (orientation > 0) {
				Matrix rotate = new Matrix();
				rotate.postRotate(orientation);
				bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
						bm.getHeight(), rotate, true);
			}
			return bm;
		}

		@Override
		protected void onPostExecute(Bitmap bm) {
			if (bm != null) {
				Log.i(TAG, "rendering " + bm.getWidth() + "x" + bm.getHeight());
				imageView.setImageBitmap(bm);
			}
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
