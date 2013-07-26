package to.rcpt.quest;

import java.io.ByteArrayOutputStream;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageThresholdEdgeDetection;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

public class LinearizeActivity extends Activity {
	private static final String TAG = "IngestPhotoActivity";
	private static final String[] PATH = new String[] { Media.DATA,
			ImageColumns.ORIENTATION };
	private GPUImageView imageView;
	private GPUImageThresholdEdgeDetection filter = new GPUImageThresholdEdgeDetection();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_linearize);
		imageView = (GPUImageView) findViewById(R.id.gpuImageView);
		imageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
		SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
		// TODO(dichro): filter defaults to a threshold of 0.9, but the current
		// value isn't extractable. Fix it; use it.
		seekBar.setProgress(90);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				filter.setThreshold(progress / (float) seekBar.getMax());
				imageView.requestRender();
			}
		});
	}

	public void edgeDetect(View view) {
		imageView.setFilter(filter);
	}

	private class HandoffTask extends
			AsyncTask<Object, Integer, ByteArrayOutputStream> {
		@Override
		protected ByteArrayOutputStream doInBackground(Object... arg0) {
			try {
				Bitmap b = imageView.capture(512, 512);
				// TODO(dichro): save it somewhere instead
				ByteArrayOutputStream bs = new ByteArrayOutputStream();
				if (!b.compress(Bitmap.CompressFormat.PNG, 100, bs)) {
					toast("PNG conversion failed", false);
					return null;
				}
				return bs;
			} catch (InterruptedException e) {
				toast("Image save interrupted", false);
				return null;
			}
		}

		protected void onPostExecute(ByteArrayOutputStream bs) {
			byte[] ba = bs.toByteArray();
			Log.i(TAG, "Compressed PNG bytes: " + ba.length);
			Intent i = new Intent(LinearizeActivity.this,
					SolutionImageActivity.class);
			i.putExtra("image", ba);
			startActivity(i);
		}
	}

	public void finishImage(View view) {
		new HandoffTask().execute();
	}

	private class ImageHandler extends AsyncTask<Uri, Integer, Bitmap> {
		@Override
		protected Bitmap doInBackground(Uri... uris) {
			Uri uri = uris[0];
			Log.i(TAG, "Received URI " + uri);
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
			// constraints: ImageView can only render bitmaps no larger than
			// 2048x2048. inScaleFactor is rounded down to the nearest power of
			// two by Bitmap.decodeFile.
			int scaleFactor = (int) Math.pow(
					2,
					Math.ceil(Math.log(Math.max(o.outWidth / 2048.0,
							o.outHeight / 2048.0)) / Math.log(2)));
			Log.i(TAG, "Downsampling " + o.outWidth + "x" + o.outHeight
					+ " image by " + scaleFactor);
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
				imageView.setImage(bm);
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
			Toast.makeText(LinearizeActivity.this, (String) msg.obj, msg.what)
					.show();
		}
	};

	private void toast(String msg, boolean longDuration) {
		if (longDuration) {
			toast(msg, Toast.LENGTH_LONG);
		} else {
			toast(msg, Toast.LENGTH_SHORT);
		}
	}

	private void toast(String msg, int duration) {
		toaster.sendMessage(toaster.obtainMessage(duration, msg));
	}
}
