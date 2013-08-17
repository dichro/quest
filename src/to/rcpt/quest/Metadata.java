package to.rcpt.quest;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public final class Metadata {
	public static String TABLE_NAME = "Metadata";
	private static String FILE_NAME = "metadata.db";

	private Metadata() {
	}

	/** Database columns for image URIs */
	public static abstract class Images {
		/** Original source imagge */
		public static final String ORIGINAL = "original";
		/** Edge-detected image */
		public static final String LINEARIZED = "linearized";
		/** Solution image */
		public static final String SOLUTION = "solution";
		/** Clue image */
		public static final String CLUE = "clue";
	}

	// TODO(dichro): just use reflection?
	private static String[] SETUP_COMMANDS = { new SQLBuilder()
			.createTable(TABLE_NAME).textColumn(Images.ORIGINAL)
			.textColumn(Images.LINEARIZED).textColumn(Images.SOLUTION)
			.textColumn(Images.CLUE).toString(), };

	public static class Helper extends SQLiteOpenHelper {
		private static final String TAG = ImageHandoffTask.class.getName();

		public Helper(Context ctx) {
			super(ctx, FILE_NAME, null, SETUP_COMMANDS.length);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			executeSetupCommands(db, 0, SETUP_COMMANDS.length - 1);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			executeSetupCommands(db, oldVersion + 1, newVersion - 1);
		}

		private void executeSetupCommands(SQLiteDatabase db, int from, int to) {
			for (int i = from; i <= to; i++) {
				Log.i(TAG, "Executing: " + SETUP_COMMANDS[i]);
				db.execSQL(SETUP_COMMANDS[i]);
			}
		}

		public long newImage(Uri uri) {
			ContentValues values = new ContentValues();
			values.put(Images.ORIGINAL, uri.toString());
			return getWritableDatabase().insert(TABLE_NAME, null, values);
		}
	}

	static class SQLBuilder {
		protected final StringBuilder leading, trailing;

		SQLBuilder() {
			leading = new StringBuilder();
			trailing = new StringBuilder();
		}

		@Override
		public String toString() {
			return leading.append(trailing.toString()).toString();
		}

		SQLBuilder createTable(String tableName) {
			leading.append("CREATE TABLE ").append(tableName).append(" (");
			trailing.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY")
					.append(")");
			return this;
		}

		SQLBuilder textColumn(String columnName) {
			leading.append(columnName).append(" TEXT,");
			return this;
		}
	}
}
