package to.rcpt.quest;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class IngestPhotoActivity extends Activity {

    private static final String TAG = "IngestPhotoActivity";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingest_photo);
    }

	private class ImageHandler extends AsyncTask<Uri, Integer, Integer> {
		@Override
		protected Integer doInBackground(Uri... uri) {
			Log.i(TAG, "Got " + uri[0]);			
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
    
}
