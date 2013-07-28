package to.rcpt.quest;

import org.metalev.multitouch.controller.MultiTouchController;
import org.metalev.multitouch.controller.MultiTouchController.MultiTouchObjectCanvas;
import org.metalev.multitouch.controller.MultiTouchController.PointInfo;
import org.metalev.multitouch.controller.MultiTouchController.PositionAndScale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ErasingView extends View implements MultiTouchObjectCanvas<Bitmap> {
	private static final String TAG = "ErasingView";
	private final MultiTouchController<Bitmap> multiTouch;
	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Path mPath;
	private Paint mBitmapPaint;
	private Paint mPaint;

	public ErasingView(Context c, AttributeSet attrs) {
		super(c, attrs);

		mPath = new Path();
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0xFFFFFFFF);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(12);
		mBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		multiTouch = new MultiTouchController<Bitmap>(this);
	}

	public void setBitmap(Bitmap b) {
		mBitmap = b;
		mCanvas = new Canvas(mBitmap);
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

		canvas.drawPath(mPath, mPaint);
	}

	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;

	private void touch_start(float x, float y) {
		mPath.reset();
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
	}

	private void touch_up() {
		mPath.lineTo(mX, mY);
		// commit the path to our offscreen
		mCanvas.drawPath(mPath, mPaint);
		// kill this so we don't double draw
		mPath.reset();
	}

	private boolean erasing = false;

	public void setErasing(boolean erasing) {
		this.erasing = erasing;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!erasing) {
			multiTouch.onTouchEvent(event);
			return true;
		}

		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touch_start(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			touch_move(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			touch_up();
			invalidate();
			break;
		}
		return true;
	}

	@Override
	public Bitmap getDraggableObjectAtPoint(PointInfo touchPoint) {
		Log.i(TAG, "gDOAP");
		return mBitmap;
	}

	float xOffset = 0, yOffset = 0, zoom = 1;

	@Override
	public void getPositionAndScale(Bitmap obj,
			PositionAndScale objPosAndScaleOut) {
		Log.i(TAG, "gPAS");
		objPosAndScaleOut.set(xOffset, yOffset, true, zoom, false, 0, 0, false,
				0);
	}

	@Override
	public boolean setPositionAndScale(Bitmap obj,
			PositionAndScale newObjPosAndScale, PointInfo touchPoint) {
		Log.i(TAG,
				"sPAS " + newObjPosAndScale.getXOff() + ", "
						+ newObjPosAndScale.getYOff() + " x "
						+ newObjPosAndScale.getScale());
		return false;
	}

	@Override
	public void selectObject(Bitmap obj, PointInfo touchPoint) {
		Log.i(TAG, "sO");
	}
}