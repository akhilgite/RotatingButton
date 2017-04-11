/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013 Triggertrap Ltd
 * Author Neil Davies
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.ag.rotatingbutton.seekbar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ag.rotatingbutton.R;
import com.ag.rotatingbutton.RoundKnobButton;

/**
 * 
 * SeekArc.java
 * 
 * This is a class that functions much like a SeekBar but
 * follows a circle path instead of a straight line.
 * 
 * @author Neil Davies
 * 
 */
public class SeekArc extends View implements GestureDetector.OnGestureListener{
	Context mContext;
	private GestureDetector gestureDetector;
	private float 				mAngleDown , mAngleUp;
	private ImageView ivRotor;
	private Bitmap 				bmpRotorOn , bmpRotorOff;

	/*mState = on/off state of knob*/
	private boolean 			mState = false;
	private int					m_nWidth = 0, m_nHeight = 0;


	private static final String TAG = SeekArc.class.getSimpleName();
	private static int INVALID_PROGRESS_VALUE = -1;
	// The initial rotational offset -90 means we start at 12 o'clock
	private final int mAngleOffset = -90;

	/**
	 * The Drawable for the seek arc thumbnail
	 */
	private Drawable mThumb;
	
	/**
	 * The Maximum value that this SeekArc can be set to
	 */
	private int mMax = 100;
	
	/**
	 * The Current value that the SeekArc is set to
	 */
	private int mProgress = 0;
		
	/**
	 * The width of the progress line for this SeekArc
	 */
	private int mProgressWidth = 4;
	
	/**
	 * The Width of the background arc for the SeekArc 
	 */
	private int mArcWidth = 2;
	
	/**
	 * The Angle to start drawing this Arc from
	 */
	private int mStartAngle = 0;
	
	/**
	 * The Angle through which to draw the arc (Max is 360)
	 */
	private int mSweepAngle = 360;

	private  MyCountDownTimer myCountDownTimer;
	
	/**
	 * The rotation of the SeekArc- 0 is twelve o'clock
	 */
	private int mRotation = 0;
	
	/**
	 * Give the SeekArc rounded edges
	 */
	private boolean mRoundedEdges = false;
	
	/**
	 * Enable touch inside the SeekArc
	 */
	private boolean mTouchInside = true;
	
	/**
	 * Will the progress increase clockwise or anti-clockwise
	 */
	private boolean mClockwise = true;


	/**
	 * is the control enabled/touchable
 	 */
	private boolean mEnabled = true;

	// Internal variables
	private int mArcRadius = 0;
	private float mProgressSweep = 0;
	private RectF mArcRect = new RectF();
	private Paint mArcPaint;
	private Paint mProgressPaint;
	private int mTranslateX;
	private int mTranslateY;
	private int mThumbXPos;
	private int mThumbYPos;
	private double mTouchAngle;
	private float mTouchIgnoreRadius;
	private OnSeekArcChangeListener mOnSeekArcChangeListener;
	private int mProgressPercent=0;
	Bitmap mBaseBitmap;

	@Override
	public boolean onDown(MotionEvent motionEvent) {
		/*onStartTrackingTouch();
		updateOnTouch(motionEvent);*/
		return true;
	}

	@Override
	public void onShowPress(MotionEvent motionEvent) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent motionEvent) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {


		isMoved=true;
		updateOnTouch(motionEvent1);

		return true;
	}

	@Override
	public void onLongPress(MotionEvent motionEvent) {

	}

	@Override
	public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
		return false;
	}


	public interface RoundKnobButtonListener {
		public void onStateChange(boolean newstate) ;
		public void onRotate(int percentage);
	}

	private RoundKnobButton.RoundKnobButtonListener m_listener;

	public void SetListener(RoundKnobButton.RoundKnobButtonListener l) {
		m_listener = l;
	}

	public void SetState(boolean state) {
		mState = state;
		ivRotor.setImageBitmap(state?bmpRotorOn:bmpRotorOff);
	}

	public interface OnSeekArcChangeListener {

		/**
		 * Notification that the progress level has changed. Clients can use the
		 * fromUser parameter to distinguish user-initiated changes from those
		 * that occurred programmatically.
		 * 
		 * @param seekArc
		 *            The SeekArc whose progress has changed
		 * @param progress
		 *            The current progress level. This will be in the range
		 *            0..max where max was set by
		 *            {@link ProgressArc#setMax(int)}. (The default value for
		 *            max is 100.)
		 * @param fromUser
		 *            True if the progress change was initiated by the user.
		 */
		void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser);

		/**
		 * Notification that the user has started a touch gesture. Clients may
		 * want to use this to disable advancing the seekbar.
		 * 
		 * @param seekArc
		 *            The SeekArc in which the touch gesture began
		 */
		void onStartTrackingTouch(SeekArc seekArc);

		/**
		 * Notification that the user has finished a touch gesture. Clients may
		 * want to use this to re-enable advancing the seekarc.
		 * 
		 * @param seekArc
		 *            The SeekArc in which the touch gesture began
		 */
		void onStopTrackingTouch(SeekArc seekArc);
	}

	public SeekArc(Context context) {
		super(context);
		init(context, null, 0);

		m_nWidth = getArcWidth();
		m_nHeight = getArcWidth();

		// set initial state
		SetState(mState);
		// enable gesture detector


	}

	public SeekArc(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, R.attr.seekArcStyle);
	}

	public SeekArc(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	Vibrator mVibrator;
	private void init(Context context, AttributeSet attrs, int defStyle) {
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

		Log.d(TAG, "Initialising SeekArc");
		final Resources res = getResources();
		float density = context.getResources().getDisplayMetrics().density;

		// Defaults, may need to link this into theme settings
		int arcColor = res.getColor(R.color.progress_gray);
		int progressColor = res.getColor(R.color.default_blue_light);
		int thumbHalfheight = 0;
		int thumbHalfWidth = 0;
		mThumb = res.getDrawable(R.drawable.seek_arc_control_selector);
		// Convert progress width to pixels for current density
		mProgressWidth = (int) (mProgressWidth * density);

		
		if (attrs != null) {
			// Attribute initialization
			final TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.SeekArc, defStyle, 0);

			Drawable thumb = a.getDrawable(R.styleable.SeekArc_thumb);
			if (thumb != null) {
				mThumb = thumb;
			}
			
			thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
			thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
			mThumb.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
					thumbHalfheight);

			mMax = a.getInteger(R.styleable.SeekArc_max, mMax);
			mProgress = a.getInteger(R.styleable.SeekArc_progress, mProgress);
			mProgressWidth = (int) a.getDimension(
					R.styleable.SeekArc_progressWidth, mProgressWidth);
			mArcWidth = (int) a.getDimension(R.styleable.SeekArc_arcWidth,
					mArcWidth);
			mStartAngle = a.getInt(R.styleable.SeekArc_startAngle, mStartAngle);
			mSweepAngle = a.getInt(R.styleable.SeekArc_sweepAngle, mSweepAngle);
			mRotation = a.getInt(R.styleable.SeekArc_rotation, mRotation);
			mRoundedEdges = a.getBoolean(R.styleable.SeekArc_roundEdges,
					mRoundedEdges);
			mTouchInside = a.getBoolean(R.styleable.SeekArc_touchInside,
					mTouchInside);
			mClockwise = a.getBoolean(R.styleable.SeekArc_clockwise,
					mClockwise);
			mEnabled = a.getBoolean(R.styleable.SeekArc_enabled, mEnabled);

			arcColor = a.getColor(R.styleable.SeekArc_arcColor, arcColor);
			progressColor = a.getColor(R.styleable.SeekArc_progressColor,
					progressColor);

			a.recycle();
		}

		mProgress = (mProgress > mMax) ? mMax : mProgress;
		mProgress = (mProgress < 0) ? 0 : mProgress;

		mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
		mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

		mProgressSweep = (float) mProgress / mMax * mSweepAngle;

		mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
		mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

		mArcPaint = new Paint();
		mArcPaint.setColor(arcColor);
		mArcPaint.setAntiAlias(true);
		mArcPaint.setStyle(Paint.Style.STROKE);
		mArcPaint.setStrokeWidth(mArcWidth);
		//mArcPaint.setAlpha(45);

		mProgressPaint = new Paint();
		mProgressPaint.setColor(progressColor);
		mProgressPaint.setAntiAlias(true);
		mProgressPaint.setStyle(Paint.Style.STROKE);
		mProgressPaint.setStrokeWidth(mProgressWidth);

		if (mRoundedEdges) {
			mArcPaint.setStrokeCap(Paint.Cap.ROUND);
			mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
		}

		/*scale image*/
//		reDrawnBitmap=BitmapFactory.decodeResource(getResources(),R.drawable.rotoron);
		/*float scaleWidth = ((float) mArcRect.width()) / bmp.getWidth();
		float scaleHeight = ((float) mArcRect.height()) / bmp.getHeight();
		Matrix matrix1 = new Matrix();
		matrix1.postScale(scaleWidth, scaleHeight);


		reDrawnBitmap=Bitmap.createBitmap(bmp,0,0,bmp.getWidth(),bmp.getHeight(),matrix1,true);*/

//		reDrawnBitmap=bmp;
		myCountDownTimer  = new MyCountDownTimer(100,10);
		gestureDetector = new GestureDetector(context, this);
	}



	@Override
	protected void onDraw(Canvas canvas) {
		/*if(isVibrating)
			return;*/
		if(!mClockwise) {
			canvas.scale(-1, 1, mArcRect.centerX(), mArcRect.centerY() );
		}

		int bmp_width1=mBaseBitmap.getWidth();
		int bmp_height1=mBaseBitmap.getHeight();

		float center_x1=mArcRect.centerX();
		float center_y1=mArcRect.centerY();

		float img_x1=center_x1-bmp_width1/2;
		float img_y1=center_y1-bmp_height1/2;

		canvas.drawBitmap(mBaseBitmap,img_x1,img_y1,mProgressPaint);
		
		// Draw the arcs
		final int arcStart = mStartAngle + mAngleOffset + mRotation;
		final int arcSweep = mSweepAngle;
		//canvas.drawRect(mArcRect,mArcPaint);
		//canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaint);



		if (mProgress==0)
			mProgressSweep=1;
		canvas.drawArc(mArcRect, arcStart, mProgressSweep, false,
				mProgressPaint);


//		Bitmap bmp= BitmapFactory.decodeResource(getResources(),R.drawable.rotoroff);
		int bmp_width=reDrawnBitmap.getWidth();
		int bmp_height=reDrawnBitmap.getHeight();

		float center_x=mArcRect.centerX();
		float center_y=mArcRect.centerY();

		float img_x=center_x-bmp_width/2;
		float img_y=center_y-bmp_height/2;
		Log.e(TAG,"Bmp Width w ,h "+bmp_width+" "+bmp_height);



		//canvas.drawBitmap(bmp,mArcRect,mArcRect,mProgressPaint);
		//Rect rectF=new Rect((int)mArcRect.left+10,(int)mArcRect.top+10,(int)mArcRect.right,(int)mArcRect.bottom);
//		canvas.translate(img_x,img_y);
//		canvas.drawRect(new RectF(img_x,img_y,bmp_width,bmp_height),mArcPaint);
//		canvas.translate(-img_x,-img_y);0

		//Bitmap.createBitmap(reDrawnBitmap,0,0,reDrawnBitmap.getWidth(),reDrawnBitmap.getHeight(),matrix1,true);
		canvas.drawBitmap(reDrawnBitmap,img_x,img_y,mProgressPaint);



		//canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bsae2),mArcRect.left-15,mArcRect.top-15,mProgressPaint);

		Paint textPaint = new Paint();
		textPaint.setARGB(200, 0, 0, 0);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTextSize(100);
		int xPos = (canvas.getWidth() / 2);
		int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)) ;
		//((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.

		//canvas.drawText(""+mProgressPercent, xPos, yPos, textPaint);

		if(mEnabled) {
			// Draw the thumb nail
			canvas.translate(mTranslateX - mThumbXPos, mTranslateY - mThumbYPos);
//			mThumb.draw(canvas);
			//canvas.translate(-(mTranslateX - mThumbXPos), -(mTranslateY - mThumbYPos));
		}



	}

	int mWidth;
	int mHeight;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		final int height = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		final int width = getDefaultSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int min = Math.min(width, height);
		float top = 0;
		float left = 0;
		int arcDiameter = 0;

		mWidth=width;
		mHeight=height;

		mTranslateX = (int) (width * 0.5f);
		mTranslateY = (int) (height * 0.5f);
		
		arcDiameter = min - getPaddingLeft();
		mArcRadius = arcDiameter / 2;
		top = height / 2 - (arcDiameter / 2);
		left = width / 2 - (arcDiameter / 2);
		mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);
	
		int arcStart = (int)mProgressSweep + mStartAngle  + mRotation + 90;
		mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart)));
		mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart)));
		
		setTouchInSide(mTouchInside);

//		if(reDrawnBitmap == null) {
//			Bitmap original = BitmapFactory.decodeResource(getResources(),R.drawable.rotoron);
//			reDrawnBitmap = Bitmap.createScaledBitmap(original,(int)(mArcRect.width()-10),(int)(mArcRect.height()-10),false);
//
//		}

		/*scale image*/
		/*Bitmap bmp=BitmapFactory.decodeResource(getResources(),R.drawable.rotoron);
		int scaleWidth = ((int) mWidth) / bmp.getWidth();
		int scaleHeight = ((int) mHeight) / bmp.getHeight();
		Matrix matrix1 = new Matrix();
		matrix1.postScale(scaleWidth, scaleHeight);
		reDrawnBitmap=Bitmap.createBitmap(bmp,0,0,bmp.getWidth(),bmp.getHeight(),matrix1,true);*/

		//reDrawnBitmap=Bitmap.createScaledBitmap(reDrawnBitmap,(int)scaleWidth,(int)scaleHeight,false);

		Bitmap bmp=BitmapFactory.decodeResource(getResources(),R.drawable.bsae2);
		Matrix matrix2 = new Matrix();
		// resize the bit map

		int newWidth = (int)mArcRect.width() +15;
		int newHeight = (int)mArcRect.height() +15 ;

		// calculate the scale - in this case = 0.4f
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		matrix2.postScale(scaleWidth, scaleHeight);
		mBaseBitmap = Bitmap.createBitmap(bmp, 0, 0,
				bmp.getWidth(), bmp.getHeight(), matrix2, true);

		updateRotation(210);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}


	private boolean isMoved=false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/*if (mEnabled) {
			gestureDetector.onTouchEvent(event);
			this.getParent().requestDisallowInterceptTouchEvent(true);

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:


					break;
				case MotionEvent.ACTION_MOVE:
					isMoved=true;
					updateOnTouch(event);
					break;
				case MotionEvent.ACTION_UP:
					if (isMoved) {
						*//*onStopTrackingTouch();
						setPressed(false);
						this.getParent().requestDisallowInterceptTouchEvent(false);
						isMoved=false;*//*
					}
					break;
				case MotionEvent.ACTION_CANCEL:
					onStopTrackingTouch();
					setPressed(false);
					this.getParent().requestDisallowInterceptTouchEvent(false);
					break;
			}
			return true;
		}else {
			if (gestureDetector.onTouchEvent(event)) return true;
			else return super.onTouchEvent(event);
		}*/

		if (isVibrating) {
			Log.e(TAG, "onTouchEvent: isVibrating true" + mProgress);
			return true;
		}
		if (gestureDetector.onTouchEvent(event)) return true;
		else return super.onTouchEvent(event);
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mThumb != null && mThumb.isStateful()) {
			int[] state = getDrawableState();
			mThumb.setState(state);
		}
		invalidate();
	}

	private void onStartTrackingTouch() {
		if (mOnSeekArcChangeListener != null) {
			mOnSeekArcChangeListener.onStartTrackingTouch(this);
		}
	}

	private void onStopTrackingTouch() {
		if (mOnSeekArcChangeListener != null) {
			mOnSeekArcChangeListener.onStopTrackingTouch(this);
		}
	}
	int oldProgress;
	private void updateOnTouch(MotionEvent event) {
		boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
		if (ignoreTouch) {
			return;
		}
		setPressed(true);
		mTouchAngle = getTouchDegrees(event.getX(), event.getY());

		Log.e(TAG, "updateOnTouch: TouchAngle " + mTouchAngle);



		int progress = getProgressForAngle(mTouchAngle);
		if (oldProgress!=progress)
			vibrate();
		if (progress==-1)
			return;

		float x = event.getX() / ((float) getWidth());
		float y = event.getY() / ((float) getHeight());
		mAngleDown = cartesianToPolar(1 - x, 1 - y);


		onProgressRefresh(progress, true);
		//updateRotation(mAngleDown);
	}

	private float cartesianToPolar(float x, float y) {
		return (float) -Math.toDegrees(Math.atan2(x - 0.5f, y - 0.5f));
	}

	private boolean ignoreTouch(float xPos, float yPos) {
		boolean ignore = false;
		float x = xPos - mTranslateX;
		float y = yPos - mTranslateY;

		float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
		if (touchRadius < mTouchIgnoreRadius) {
			ignore = true;
		}

		/*int currentProgress=getProgress();
		if (xPos<currentProgress+15 &&xPos>currentProgress-15 && yPos<currentProgress+15&& yPos>currentProgress-15){

		}else {
			ignore=true;
		}*/


		return ignore;
	}

	private double getTouchDegrees(float xPos, float yPos) {
		float x = xPos - mTranslateX;
		float y = yPos - mTranslateY;
		//invert the x-coord if we are rotating anti-clockwise
		x= (mClockwise) ? x:-x;
		// convert to arc Angle
		double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2)
				- Math.toRadians(mRotation));
		if (angle < 0) {
			angle = 360 + angle;
		}
		angle -= mStartAngle;
		return angle;
	}

	private int getProgressForAngle(double angle) {
		int touchProgress = (int) Math.round(valuePerDegree() * angle);

		touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE
				: touchProgress;
		touchProgress = (touchProgress > mMax) ? INVALID_PROGRESS_VALUE
				: touchProgress;
		return touchProgress;
	}

	private float valuePerDegree() {
		return (float) mMax / mSweepAngle;
	}

	private void onProgressRefresh(int progress, boolean fromUser) {
		updateProgress(progress, fromUser);
	}

	private void updateThumbPosition() {
		int thumbAngle = (int) (mStartAngle + mProgressSweep + mRotation + 90);
		mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(thumbAngle)));
		mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(thumbAngle)));

		updateRotation(mProgressSweep + mRotation+mStartAngle);
	}

	private void updateProgress(int progress, boolean fromUser) {

		if (progress == INVALID_PROGRESS_VALUE) {
			return;
		}

		progress = (progress > mMax) ? mMax : progress;
		progress = (progress < 0) ? 0 : progress;
		mProgress = progress;
		oldProgress=progress;


		if (mOnSeekArcChangeListener != null) {
			mOnSeekArcChangeListener
					.onProgressChanged(this, progress, fromUser);
		}

		/*if(progress%10==0){
			mProgressPercent=mProgress;
			vibrate();
		}*/
		mProgressPercent=mProgress;

		mProgressSweep = (float) progress / mMax * mSweepAngle;
		updateThumbPosition();

		invalidate();
	}

	boolean isVibrating=false;

	private void vibrate(){
		mVibrator.vibrate(10);
		isVibrating=true;
		myCountDownTimer.start();
	}

	/**
	 * Sets a listener to receive notifications of changes to the SeekArc's
	 * progress level. Also provides notifications of when the user starts and
	 * stops a touch gesture within the SeekArc.
	 * 
	 * @param l
	 *            The seek bar notification listener
	 * 
	 * @see SeekArc.OnSeekBarChangeListener
	 */
	public void setOnSeekArcChangeListener(OnSeekArcChangeListener l) {
		mOnSeekArcChangeListener = l;
	}

	public void setProgress(int progress) {
		updateProgress(progress, false);
	}

	public int getProgress() {
		return mProgress;
	}

	public int getProgressWidth() {
		return mProgressWidth;
	}

	public void setProgressWidth(int mProgressWidth) {
		this.mProgressWidth = mProgressWidth;
		mProgressPaint.setStrokeWidth(mProgressWidth);
	}
	
	public int getArcWidth() {
		return mArcWidth;
	}

	public void setArcWidth(int mArcWidth) {
		this.mArcWidth = mArcWidth;
		mArcPaint.setStrokeWidth(mArcWidth);
	}
	public int getArcRotation() {
		return mRotation;
	}

	public void setArcRotation(int mRotation) {
		this.mRotation = mRotation;
		updateThumbPosition();
	}

	public int getStartAngle() {
		return mStartAngle;
	}

	public void setStartAngle(int mStartAngle) {
		this.mStartAngle = mStartAngle;
		updateThumbPosition();
	}

	public int getSweepAngle() {
		return mSweepAngle;
	}

	public void setSweepAngle(int mSweepAngle) {
		this.mSweepAngle = mSweepAngle;
		updateThumbPosition();
	}
	
	public void setRoundedEdges(boolean isEnabled) {
		mRoundedEdges = isEnabled;
		if (mRoundedEdges) {
			mArcPaint.setStrokeCap(Paint.Cap.ROUND);
			mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
		} else {
			mArcPaint.setStrokeCap(Paint.Cap.SQUARE);
			mProgressPaint.setStrokeCap(Paint.Cap.SQUARE);
		}
	}
	
	public void setTouchInSide(boolean isEnabled) {
		int thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
		int thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
		mTouchInside = isEnabled;
		if (mTouchInside) {
			mTouchIgnoreRadius = (float) mArcRadius / 4;
		} else {
			// Don't use the exact radius makes interaction too tricky
			mTouchIgnoreRadius = mArcRadius
					- Math.min(thumbHalfWidth, thumbHalfheight);
		}
	}
	
	public void setClockwise(boolean isClockwise) {
		mClockwise = isClockwise;
	}

	public boolean isClockwise() {
		return mClockwise;
	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public void setEnabled(boolean enabled) {
		this.mEnabled = enabled;
	}

	public int getProgressColor() {
		return mProgressPaint.getColor();
	}

	public void setProgressColor(int color) {
		mProgressPaint.setColor(color);
		invalidate();
	}

	public int getArcColor() {
		return mArcPaint.getColor();
	}

	public void setArcColor(int color) {
		mArcPaint.setColor(color);
		invalidate();
	}

	public int getMax() {
		return mMax;
	}

	public void setMax(int mMax) {
		this.mMax = mMax;
	}

	Bitmap reDrawnBitmap;
	private void updateRotation(double deg){
		/*float newRot=new Float(rot);
		Bitmap bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.rotoron);
		Matrix matrix=new Matrix();
		matrix.postRotate(newRot,bitmap.getWidth(),bitmap.getHeight());
		reDrawnBitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
		invalidate();*/
		Log.e(TAG,"Degree "+ deg);
		if (deg < 0) {
//			Log.e(TAG, "onScroll: rotDegrees < 0::"+rotDegrees);
			deg = 360 + deg;
		}
		if (deg >= 210 || deg <= 150) {
			if (deg > 180) deg = deg - 360;
//			Matrix matrix=new Matrix();
//			Bitmap bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.rotoron);
//			matrix.postRotate((float) deg, getWidth()/2, getHeight()/2); // 210/2,210/2);//
			//matrix.setRectToRect(new RectF(0, 0, reDrawnBitmap.getWidth(), reDrawnBitmap.getHeight()), new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight()), Matrix.ScaleToFit.CENTER);
//			matrix.postRotate((float) deg);
//			reDrawnBitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
//			reDrawnBitmap = Bitmap.createScaledBitmap(reDrawnBitmap,(int)mArcRect.width(),(int)mArcRect.height(),false);
			/*int origWidth = reDrawnBitmap.getWidth();
			int origHeight = reDrawnBitmap.getHeight();

			final int destWidth = (int) (mArcRect.width()-mArcWidth);
			if(origWidth > destWidth){
				// picture is wider than we want it, we calculate its target height
				int destHeight = origHeight/( origWidth / destWidth ) ;
				// we create an scaled bitmap so it reduces the image, not just trim it
				Bitmap b2 = Bitmap.createScaledBitmap(reDrawnBitmap, destWidth, destHeight, false);
				reDrawnBitmap=b2;
			}*/


			/*Bitmap bmp=reDrawnBitmap;
			float scaleWidth = ((float) mArcRect.width()) / bmp.getWidth();
			float scaleHeight = ((int) mArcRect.height()) / bmp.getHeight();
			Matrix matrix1 = new Matrix();
			matrix1.postScale(scaleWidth, scaleHeight);
			//reDrawnBitmap=Bitmap.createBitmap(bmp,0,0,bmp.getWidth(),bmp.getHeight(),matrix1,true);
			reDrawnBitmap=Bitmap.createScaledBitmap(reDrawnBitmap,Math.round(scaleWidth),Math.round(scaleHeight),false);*/

			//reDrawnBitmap=Bitmap.createScaledBitmap(reDrawnBitmap, getMeasuredWidth(), getMeasuredHeight(), false);
			//ivRotor.setImageMatrix(matrix);



			Bitmap bitmapOrg=BitmapFactory.decodeResource(getResources(),R.drawable.knob1);
			int width = bitmapOrg.getWidth();
			int height = bitmapOrg.getHeight();
			Log.e(TAG,"Original Bitmap: "+ width+ " "+ height);
			int newWidth = (int)mArcRect.width() - 3;
			int newHeight = (int)mArcRect.height() - 3;

			// calculate the scale - in this case = 0.4f
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;

			// createa matrix for the manipulation
			Matrix matrix = new Matrix();
			// resize the bit map
			matrix.postScale(scaleWidth, scaleHeight);
			// rotate the Bitmap
//			matrix.preScale(scaleWidth,scaleHeight);
			matrix.postRotate((float) deg);

			// recreate the new Bitmap
			reDrawnBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
					width, height, matrix, true);



			invalidate();
		}

	}

	private  class MyCountDownTimer extends CountDownTimer {


		public MyCountDownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long l) {

		}

		@Override
		public void onFinish() {
			isVibrating = false;
			Log.e(TAG, "onFinish: Timer completed");
		}
	}
}
