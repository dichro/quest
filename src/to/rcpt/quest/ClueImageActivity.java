package to.rcpt.quest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;

public class ClueImageActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_clue_image);
		final ErasingView erasingView = (ErasingView) findViewById(R.id.erasingView);
		CompoundButton drawState = (CompoundButton) findViewById(R.id.drawState);
		drawState.setOnCheckedChangeListener(erasingView);
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
}
