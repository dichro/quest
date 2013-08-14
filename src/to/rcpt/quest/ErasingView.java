package to.rcpt.quest;

import org.metalev.multitouch.controller.MultiTouchController;
import org.metalev.multitouch.controller.MultiTouchController.MultiTouchObjectCanvas;
import org.metalev.multitouch.controller.MultiTouchController.PointInfo;
import org.metalev.multitouch.controller.MultiTouchController.PositionAndScale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * A scrollable, zoomable, modal view for erasing portions of a bitmap.
 * 
 * @author Miki Habryn <dichro@rcpt.to>
 */
public class ErasingView extends View implements
		MultiTouchObjectCanvas<Bitmap>, ImageHandoffTask.HasBitmap {
	private static final String TAG = "ErasingView";
	private final MultiTouchController<Bitmap> multiTouch;
	/** defines what part of the bitmap is currently displayed */
	private Matrix zoomMatrix;
	/** the bitmap being edited by this view */
	private Bitmap editableBitmap;
	/** the (off-screen) canvas that edits the bitmap */
	private Canvas editableCanvas;
	private Path mPath;
	/** renders the bitmap onto the View's canvas */
	private Paint renderPaint;
	/** shows the path to be erased by user action */
	private Paint pathPaint;
	/** erases from the bitmap */
	private Paint erasePaint;

	public ErasingView(Context c, AttributeSet attrs) {
		super(c, attrs);

		mPath = new Path();
		renderPaint = new Paint(Paint.DITHER_FLAG);
		erasePaint = new Paint();
		configurePaint(erasePaint);
		erasePaint.setColor(0xFFFFFFFF);
		pathPaint = new Paint();
		configurePaint(pathPaint);
		pathPaint.setColor(0x80FF0000);
		pathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		// temporary bitmap, will be replaced by setBitmap()
		editableBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
		editableCanvas = new Canvas(editableBitmap);
		multiTouch = new MultiTouchController<Bitmap>(this);
		zoomMatrix = new Matrix();
		zoomMatrix.reset();
		setBackgroundColor(0xFFFFFFFF);
	}

	private void configurePaint(Paint p) {
		p.setAntiAlias(true);
		p.setDither(true);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeJoin(Paint.Join.ROUND);
		p.setStrokeCap(Paint.Cap.ROUND);
		p.setStrokeWidth(24);
	}

	public void setBitmap(Bitmap b) {
		editableBitmap = b;
		editableCanvas = new Canvas(editableBitmap);
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w == 0 || h == 0) {
			return;
		}
		if (!zoomMatrix.setRectToRect(new RectF(0, 0,
				editableBitmap.getWidth(), editableBitmap.getHeight()),
				new RectF(0, 0, w, h), Matrix.ScaleToFit.CENTER)) {
			zoomMatrix.reset();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(editableBitmap, zoomMatrix, renderPaint);
		canvas.drawPath(mPath, pathPaint);
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
		// calculate transform from screen co-ordinates to bitmap co-ordinates
		Matrix inverse = new Matrix();
		if (!zoomMatrix.invert(inverse)) {
			inverse.reset();
		}
		// set the transform before we draw the path, which is in screen
		// co-ordinates
		editableCanvas.setMatrix(inverse);
		// commit the path to our offscreen
		editableCanvas.drawPath(mPath, erasePaint);
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
		return editableBitmap;
	}

	@Override
	public void getPositionAndScale(Bitmap obj,
			PositionAndScale objPosAndScaleOut) {
		float[] values = new float[9];
		zoomMatrix.getValues(values);
		Log.i(TAG, "gPAS " + values[Matrix.MTRANS_X] + ", "
				+ values[Matrix.MTRANS_Y] + " x " + values[Matrix.MSCALE_X]);
		objPosAndScaleOut.set(values[Matrix.MTRANS_X], values[Matrix.MTRANS_Y],
				true, values[Matrix.MSCALE_X], false, 0, 0, false, 0);
	}

	@Override
	public boolean setPositionAndScale(Bitmap obj,
			PositionAndScale newObjPosAndScale, PointInfo touchPoint) {
		float xOff = newObjPosAndScale.getXOff();
		float yOff = newObjPosAndScale.getYOff();
		float scale = newObjPosAndScale.getScale();
		Log.i(TAG, "sPAS " + xOff + ", " + yOff + " x " + scale);
		zoomMatrix.setScale(scale, scale);
		zoomMatrix.postTranslate(xOff, yOff);
		float[] values = new float[9];
		zoomMatrix.getValues(values);
		Log.i(TAG, "?? " + values[Matrix.MTRANS_X] + ", "
				+ values[Matrix.MTRANS_Y] + " x " + values[Matrix.MSCALE_X]);
		invalidate();
		return true;
	}

	@Override
	public void selectObject(Bitmap obj, PointInfo touchPoint) {
		Log.i(TAG, "sO");
	}

	@Override
	public Bitmap getBitmap() {
		return editableBitmap;
	}
}