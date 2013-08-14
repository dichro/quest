package to.rcpt.quest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;

public class SolutionImageActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_solution_image);
		final ErasingView erasingView = (ErasingView) findViewById(R.id.erasingView);
		CompoundButton drawState = (CompoundButton) findViewById(R.id.drawState);
		drawState
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton view,
							boolean erasing) {
						erasingView.setErasing(erasing);
					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
		ErasingView erasingView = (ErasingView) findViewById(R.id.erasingView);
		Intent i = getIntent();
		byte[] bytes = i.getByteArrayExtra("image");
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		erasingView.setBitmap(bitmap.copy(bitmap.getConfig(), true));
	}

	public void goNext(View v) {

	}
}
