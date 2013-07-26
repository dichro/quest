package to.rcpt.quest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

public class SolutionImageActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_solution_image);
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
