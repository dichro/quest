package to.rcpt.quest;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageThresholdEdgeDetection;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

public class LinearizeActivity extends Activity implements
		ImageHandoffTask.HasBitmap {
	private static final String TAG = LinearizeActivity.class.getName();
	private GPUImageView imageView;
	private GPUImageThresholdEdgeDetection filter = new GPUImageThresholdEdgeDetection();
	private Uri originalUri;

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

	public void finishImage(View view) {
		new ImageHandoffTask(this, SolutionImageActivity.class, this, "base") {
			@Override
			protected long updateDb(Metadata.Helper helper, Uri uri) {
				return helper.newImage(originalUri, uri);
			}
		}.execute();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			originalUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		} else if (Intent.ACTION_SENDTO.equals(intent.getAction())) {
			originalUri = intent.getData();
		} else {
			Toaster.s(this, "Unknown intent received");
			return;
		}
		new ImageLoadingTask.HasBitmap(this, this, 2048).execute(originalUri);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_ingest_photo, menu);
		return true;
	}

	@Override
	public Bitmap getBitmap() {
		try {
			return imageView.capture(512, 512);
		} catch (InterruptedException e) {
			Toaster.s(this, "Image save interrupted");
			return null;
		}
	}

	public void setBitmap(Bitmap bm) {
		imageView.setImage(bm);
	}
}
