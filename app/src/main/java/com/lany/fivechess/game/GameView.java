package com.lany.fivechess.game;

import com.lany.fivechess.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * ������Ϸ����ʾ����Ϸ���߼��ж���Game.java��
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "GameView";
	SurfaceHolder mSurfaceHolder;
	// ���ӻ���
	private Paint chessPaint = new Paint();
	// ���̻���
	private Paint boardPaint = new Paint();
	private int boardColor = 0;
	private float boardWidth = 0.0f;
	private float anchorWidth = 0.0f;

	// ��������
	Paint clear = new Paint();

	public int[][] mChessArray = null;

	Bitmap mBlack = null;
	Bitmap mBlackNew = null;
	Bitmap mWhite = null;
	Bitmap mWhiteNew = null;

	int mChessboardWidth = 0;
	int mChessboardHeight = 0;
	int mChessSize = 0;

	Context mContext;

	private Game mGame;

	private Coordinate focus;
	private boolean isDrawFocus;
	private Bitmap bFocus;

	public GameView(Context context) {
		this(context, null);
	}

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		boardColor = Color.BLACK;
		boardWidth = getResources().getDimensionPixelSize(R.dimen.boardWidth);
		anchorWidth = getResources().getDimensionPixelSize(R.dimen.anchorWidth);
		focus = new Coordinate();
		init();
	}

	private void init() {
		mSurfaceHolder = this.getHolder();
		mSurfaceHolder.addCallback(this);
		// ����͸��
		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		setZOrderOnTop(true);
		chessPaint.setAntiAlias(true);
		boardPaint.setStrokeWidth(boardWidth);
		boardPaint.setColor(boardColor);
		clear.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		setFocusable(true);
	}

	/**
	 * ������Ϸ
	 * 
	 * @param game
	 */
	public void setGame(Game game) {
		mGame = game;
		requestLayout();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// ���ø߶�����һ��
		int width = MeasureSpec.getSize(widthMeasureSpec);
		if (mGame != null) {
			if (width % mGame.getWidth() == 0) {
				float scale = ((float) mGame.getHeight()) / mGame.getWidth();
				int height = (int) (width * scale);
				setMeasuredDimension(width, height);
			} else {
				width = width / mGame.getWidth() * mGame.getWidth();
				float scale = ((float) mGame.getHeight()) / mGame.getWidth();
				int height = (int) (width * scale);
				setMeasuredDimension(width, height);
			}
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mGame != null) {
			mChessboardWidth = mGame.getWidth();
			mChessboardHeight = mGame.getHeight();
			mChessSize = (right - left) / mChessboardWidth;
			Log.d(TAG, "mChessSize=" + mChessSize + " mChessboardWidth="
					+ mChessboardWidth + " mChessboardHeight"
					+ mChessboardHeight);
		}
	}

	/**
	 * ������Ϸ����
	 */
	public void drawGame() {
		Canvas canvas = mSurfaceHolder.lockCanvas();
		if (mSurfaceHolder == null || canvas == null) {
			Log.d(TAG, "mholde=" + mSurfaceHolder + "  canvas=" + canvas);
			return;
		}
		// ���� ���Ƿ���Բ�����������˫���弼��ʵ��
		canvas.drawPaint(clear);
		drawChessBoard(canvas);
		drawChess(canvas);
		drawFocus(canvas);
		mSurfaceHolder.unlockCanvasAndPost(canvas);
	}

	/**
	 * ����һ������
	 * 
	 * @param x
	 *            �����
	 * @param y
	 *            �����
	 */
	public void addChess(int x, int y) {
		if (mGame == null) {
			Log.d(TAG, "game can not be null");
			return;
		}
		mGame.addChess(x, y);
		drawGame();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			focus.x = (int) (x / mChessSize);
			focus.y = (int) (y / mChessSize);
			isDrawFocus = true;
			drawGame();
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			isDrawFocus = false;
			int newx = (int) (x / mChessSize);
			int newy = (int) (y / mChessSize);
			if (canAdd(newx, newy, focus)) {
				addChess(focus.x, focus.y);
			} else {
				drawGame();
			}
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * �ж��Ƿ�ȡ��˴�����
	 * 
	 * @param x
	 *            xλ��
	 * @param y
	 *            yλ��
	 * @param down
	 *            ���µĵ�λ
	 * @return
	 */
	private boolean canAdd(float x, float y, Coordinate focus) {
		return x < focus.x + 3 && x > focus.x - 3 && y < focus.y + 3
				&& y > focus.y - 3;
	}

	/**
	 * ��������
	 * 
	 * @param width
	 *            VIEW�Ŀ��
	 * @param height
	 *            VIEW�ĸ߶�
	 * @param type
	 *            ���͡������ӻ����
	 * @return Bitmap
	 */
	private Bitmap createChess(int width, int height, int type) {
		int tileSize = width / 15;
		Bitmap bitmap = Bitmap.createBitmap(tileSize, tileSize,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Drawable d = null;
		if (type == 0) {
			d = getResources().getDrawable(R.drawable.black);
		} else if (type == 1) {
			d = getResources().getDrawable(R.drawable.white);
		} else if (type == 2) {
			d = getResources().getDrawable(R.drawable.black_new);
		} else if (type == 3) {
			d = getResources().getDrawable(R.drawable.white_new);
		} else if (type == 4) {
			d = getResources().getDrawable(R.drawable.focus);
		}
		d.setBounds(0, 0, tileSize, tileSize);
		d.draw(canvas);
		return bitmap;
	}

	// �����̱���
	private void drawChessBoard() {
		Canvas canvas = mSurfaceHolder.lockCanvas();
		if (mSurfaceHolder == null || canvas == null) {
			return;
		}
		drawChessBoard(canvas);
		mSurfaceHolder.unlockCanvasAndPost(canvas);
	}

	// �����̱���
	private void drawChessBoard(Canvas canvas) {
		// ����ê��(���ĵ�)
		int startX = mChessSize / 2;
		int startY = mChessSize / 2;
		int endX = startX + (mChessSize * (mChessboardWidth - 1));
		int endY = startY + (mChessSize * (mChessboardHeight - 1));
		// draw ��ֱ��
		for (int i = 0; i < mChessboardWidth; ++i) {
			canvas.drawLine(startX + (i * mChessSize), startY, startX
					+ (i * mChessSize), endY, boardPaint);
		}
		// draw ˮƽ��
		for (int i = 0; i < mChessboardHeight; ++i) {
			canvas.drawLine(startX, startY + (i * mChessSize), endX, startY
					+ (i * mChessSize), boardPaint);
		}
		// ����ê��(���ĵ�)
		int circleX = startX + mChessSize * (mChessboardWidth / 2);
		int circleY = startY + mChessSize * (mChessboardHeight / 2);
		canvas.drawCircle(circleX, circleY, anchorWidth, boardPaint);
		// ����ê��(�������Ͻǵĵ�)
		int aX = startX + mChessSize * (mChessboardWidth / 4);
		int aY = startY + mChessSize * (mChessboardHeight / 4);
		canvas.drawCircle(aX, aY, anchorWidth, boardPaint);
		// ����ê��(�������Ͻǵĵ�)
		int bX = startX + mChessSize
				* (mChessboardWidth / 4 + mChessboardWidth / 2 + 1);
		int bY = startY + mChessSize * (mChessboardHeight / 4);
		canvas.drawCircle(bX, bY, anchorWidth, boardPaint);
		// ����ê��(�������½ǵĵ�)
		int cX = startX + mChessSize * (mChessboardWidth / 4);
		int cY = startY + mChessSize
				* (mChessboardHeight / 4 + mChessboardHeight / 2 + 1);
		canvas.drawCircle(cX, cY, anchorWidth, boardPaint);
		// ����ê��(�������½ǵĵ�)
		int dX = startX + mChessSize
				* (mChessboardWidth / 4 + mChessboardWidth / 2 + 1);
		int dY = startY + mChessSize
				* (mChessboardHeight / 4 + mChessboardHeight / 2 + 1);
		canvas.drawCircle(dX, dY, anchorWidth, boardPaint);
	}

	// ������
	private void drawChess(Canvas canvas) {
		int[][] chessMap = mGame.getChessMap();
		for (int x = 0; x < chessMap.length; ++x) {
			for (int y = 0; y < chessMap[0].length; ++y) {
				int type = chessMap[x][y];
				if (type == Game.BLACK) {
					canvas.drawBitmap(mBlack, x * mChessSize, y * mChessSize,
							chessPaint);
				} else if (type == Game.WHITE) {
					canvas.drawBitmap(mWhite, x * mChessSize, y * mChessSize,
							chessPaint);
				}
			}
		}
		// �������µ�һ������
		if (mGame.getActions() != null && mGame.getActions().size() > 0) {
			Coordinate last = mGame.getActions().getLast();
			int lastType = chessMap[last.x][last.y];
			if (lastType == Game.BLACK) {
				canvas.drawBitmap(mBlackNew, last.x * mChessSize, last.y
						* mChessSize, chessPaint);
			} else if (lastType == Game.WHITE) {
				canvas.drawBitmap(mWhiteNew, last.x * mChessSize, last.y
						* mChessSize, chessPaint);
			}
		}
	}

	/**
	 * ����ǰ��
	 * 
	 * @param canvas
	 */
	private void drawFocus(Canvas canvas) {
		if (isDrawFocus) {
			canvas.drawBitmap(bFocus, focus.x * mChessSize, focus.y
					* mChessSize, chessPaint);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (mBlack != null) {
			mBlack.recycle();
		}
		if (mWhite != null) {
			mWhite.recycle();
		}
		mWhite = createChess(width, height, 1);
		mBlack = createChess(width, height, 0);
		mBlackNew = createChess(width, height, 2);
		mWhiteNew = createChess(width, height, 3);
		bFocus = createChess(width, height, 4);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// ��ʼ������
		drawChessBoard();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {

	}

}