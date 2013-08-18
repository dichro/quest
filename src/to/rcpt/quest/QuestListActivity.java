package to.rcpt.quest;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;

public class QuestListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private SimpleCursorAdapter adapter;
	private Toaster toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		toast = new Toaster(this);
		getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(this, R.layout.list_item_querylist,
				null, new String[] { Metadata.Images.ORIGINAL,
						Metadata.Images.LINEARIZED, Metadata.Images.SOLUTION,
						Metadata.Images.CLUE }, new int[] { R.id.original,
						R.id.linearized, R.id.solution, R.id.clue }, 0);
		setListAdapter(adapter);
	}

	public void editImage(View v) {
		toast.s("editImage " + v.getId());
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
