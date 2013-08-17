package to.rcpt.quest;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;

public class QuestListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private SimpleCursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, null, new String[] {
						Metadata.Images.ORIGINAL, Metadata.Images.LINEARIZED },
				new int[] { android.R.id.text1, android.R.id.text2 }, 0);
		setListAdapter(adapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new Metadata.Helper(this).getLoader();
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.changeCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.changeCursor(null);
	}
}
