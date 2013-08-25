package to.rcpt.quest;

import to.rcpt.quest.Metadata.Helper;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;

public class SolutionImageActivity extends Activity {
	private ErasingView erasingView;
	private long dbId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_solution_image);
		erasingView = (ErasingView) findViewById(R.id.erasingView);
		CompoundButton drawState = (CompoundButton) findViewById(R.id.drawState);
		drawState.setOnCheckedChangeListener(erasingView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		dbId = intent.getLongExtra(BaseColumns._ID, -1);
		Uri uri = intent.getData();
		if (uri == null) {
			Toaster.s(this, "No URI received?");
			return;
		}
		new ImageLoadingTask.HasBitmap(this, erasingView, 2048).execute(uri);
	}

	public void goNext(View v) {
		new ImageHandoffTask(this, ClueImageActivity.class, erasingView,
				"solution") {
			@Override
			protected long updateDb(Helper helper, Uri uri) {
				helper.setSolutionImage(dbId, uri);
				return dbId;
			}

			@Override
			protected long getDbId(Helper helper) {
				return dbId;
			}
		}.execute();
	}
}
