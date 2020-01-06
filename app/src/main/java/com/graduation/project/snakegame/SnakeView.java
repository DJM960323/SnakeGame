package com.graduation.project.snakegame;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Random;


public class SnakeView extends TileView{

    static int mMoveDelay = 500;   //控制蛇的移动速度，值越小移动越快
    private long mLastMove;

    private static final int HEAD = 1;
    private static final int FOOD = 2;
    private static final int WALL = 3;

    private static final int UP = 1;
    private static final int DOWN = 2;
    private static final int RIGHT = 3;
    private static final int LEFT = 4;
    static int mDirection = RIGHT;
    static int mNextDirection = RIGHT;

    public static final int PAUSE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;
    public static final int LOSE = 3;
    public static final int QUIT = 4;
    public int mMode = READY;
    public int newMode;

    private TextView mStatusText;
    public long mScore = 0;

    private ArrayList<Coordinate> mSnakeTrail = new ArrayList<Coordinate>(); // 蛇的所有（点）tile的坐标数组
    private ArrayList<Coordinate> mAppleList = new ArrayList<Coordinate>();  // 苹果的所有（点）tile的坐标数组

    private static final Random RNG = new Random();
    //开启线程，不断调用更新和重绘
    MyHandler handler=new MyHandler();
    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            SnakeView.this.update();
            SnakeView.this.invalidate();    //请求重绘，不断调用ondraw方法
        }
        //调用sleep后,在一定时间后再sendmessage进行UI更新
        public void sleep(int delayMillis) {
            this.removeMessages(0);         //清空消息队列
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }
    public SnakeView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
        initNewGame();

    }
    public SnakeView(Context context, AttributeSet attrs){
        super(context,attrs);
        setFocusable(true);
        initNewGame();

    }
    public SnakeView(Context context){
        super(context);

    }
    private void addRandomApple() {
        Coordinate newCoord = null;
        boolean found = false;
        while (!found) {
            // apple生成的位置的坐标.mXT=24,mTY=35
            int newX = 1 + RNG.nextInt(27-2);
            int newY = 1 + RNG.nextInt(20);
            newCoord = new Coordinate(newX, newY);

            boolean collision = false;
            int snakelength = mSnakeTrail.size();
            //遍历snake, 看新添加的apple是否在snake体内, 如果是,重新生成
            for (int index = 0; index < snakelength; index++) {
                if (mSnakeTrail.get(index).equals(newCoord)) {
                    collision = true;
                }
            }
            found = !collision;
        }
        mAppleList.add(newCoord);
    }
    //绘制边界的墙
    private void updateWalls() {
        for (int x = 0; x < mXTileCount; x++) {
            setTile(WALL, x, 0);
            setTile(WALL, x, mYTileCount - 20);
        }
        for (int y = 0; y < mYTileCount - 20; y++) {
            setTile(WALL, 0, y);
            setTile(WALL, mXTileCount - 1, y);
        }
    }
    private void updateSnake(){
        boolean growSnake = false;
        Coordinate head = mSnakeTrail.get(0);
        Coordinate newHead = new Coordinate(1, 1);

        mDirection = mNextDirection;
        switch (mDirection) {
            case RIGHT: {
                newHead = new Coordinate(head.x + 1, head.y);
                break;
            }
            case LEFT: {
                newHead = new Coordinate(head.x - 1, head.y);
                break;
            }
            case UP: {
                newHead = new Coordinate(head.x, head.y - 1);
                break;
            }
            case DOWN: {
                newHead = new Coordinate(head.x, head.y + 1);
                break;
            }
        }

        //检测是否撞墙
        if ((newHead.x < 1) || (newHead.y < 1) || (newHead.x > mXTileCount - 2)
                || (newHead.y > mYTileCount - 21)) {
            setMode(LOSE);
            return;
        }
        //检测蛇头是否撞到自己
        int snakelength = mSnakeTrail.size();
        for (int snakeindex = 0; snakeindex < snakelength; snakeindex++) {
            Coordinate c = mSnakeTrail.get(snakeindex);
            if (c.equals(newHead)) {
                setMode(LOSE);
                return;
            }
        }
        //检测蛇是否吃到苹果
        int applecount = mAppleList.size();
        for (int appleindex = 0; appleindex < applecount; appleindex++) {
            Coordinate c = mAppleList.get(appleindex);
            if (c.equals(newHead)) {
                mAppleList.remove(c);
                addRandomApple();
                mScore++;
                mMoveDelay *= 0.95;
                growSnake = true;
            }
        }
        mSnakeTrail.add(0,newHead);
        if(!growSnake) {
            mSnakeTrail.remove(mSnakeTrail.size() - 1);
        }
        //蛇头和蛇身分别设置图片
        int index=0;
        for(Coordinate c:mSnakeTrail) {

            if(index == 0) {
                switch (mDirection) {
                    case RIGHT: {
                        setTile(HEAD, c.x, c.y);
                        break;
                    }
                    case LEFT: {
                        setTile(HEAD, c.x, c.y);
                        break;
                    }
                    case UP: {
                        setTile(HEAD, c.x, c.y);
                        break;
                    }
                    case DOWN: {
                        setTile(HEAD, c.x, c.y);
                        break;
                    }
                }
            } else {
                setTile(FOOD,c.x,c.y);
            }
            index++;
        }
    }
    private void updateApples() {
        for (Coordinate c : mAppleList) {
            setTile(FOOD, c.x, c.y);
        }
    }
    public void update(){
        if(mMode == RUNNING) {
            long now = System.currentTimeMillis();
            if (now - mLastMove > mMoveDelay) {
                clearTiles();
                updateWalls();
                updateSnake();
                updateApples();
                mLastMove = now;
            }
            handler.sleep(mMoveDelay);
        }
    }
    //图像初始化
    private void initSnakeView() {
        setFocusable(true);
        Resources r = this.getContext().getResources();
        //添加几种不同的tile
        resetTiles(4);
        //从文件中加载图片
        loadTile(HEAD, r.getDrawable(R.drawable.head_right));
        loadTile(FOOD, r.getDrawable(R.drawable.yellowstar));
        loadTile(WALL, r.getDrawable(R.drawable.greenstar));
        update();
    }
    public void initNewGame() {
        mSnakeTrail.clear();
        mAppleList.clear();
        //snake初始状态时的个数和位置,方向
        mSnakeTrail.add(new Coordinate(8, 6));
        mSnakeTrail.add(new Coordinate(7, 6));
        mSnakeTrail.add(new Coordinate(6, 6));
        mSnakeTrail.add(new Coordinate(5, 6));
        mSnakeTrail.add(new Coordinate(4, 6));
        mSnakeTrail.add(new Coordinate(3, 6));
        mDirection = RIGHT;
        mNextDirection = RIGHT;
        addRandomApple();
        mScore=0;
    }
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        Paint paint=new Paint();
        initSnakeView();
        //遍历地图绘制界面
        for (int x = 0; x < mXTileCount; x++) {
            for (int y = 0; y < mYTileCount; y++) {
                if (mTileGrid[x][y] > 0) {
                    canvas.drawBitmap(mTileArray[mTileGrid[x][y]], mXOffset + x * mTileSize, mYOffset + y * mTileSize, paint);
                }
            }
        }
    }
    //把蛇和苹果各点对应的坐标储存起来
    private int[] coordArrayListToArray(ArrayList<Coordinate> cvec) {
        int count = cvec.size();
        int[] rawArray = new int[count * 2];
        for (int index = 0; index < count; index++) {
            Coordinate c = cvec.get(index);
            rawArray[2 * index] = c.x;
            rawArray[2 * index + 1] = c.y;
        }
        return rawArray;
    }
    //将当前所有的游戏数据全部保存
    public Bundle saveState() {
        Bundle map = new Bundle();

        map.putIntArray("mAppleList", coordArrayListToArray(mAppleList));
        map.putInt("mDirection", Integer.valueOf(mDirection));
        map.putInt("mNextDirection", Integer.valueOf(mNextDirection));
        map.putInt("mMoveDelay", Integer.valueOf(mMoveDelay));
        map.putLong("mScore", Long.valueOf(mScore));
        map.putIntArray("mSnakeTrail", coordArrayListToArray(mSnakeTrail));

        return map;
    }
    //是coordArrayListToArray()的逆过程，用来读取保存在Bundle中的数据
    private ArrayList<Coordinate> coordArrayToArrayList(int[] rawArray) {
        ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();
        int coordCount = rawArray.length;
        for (int index = 0; index < coordCount; index += 2) {
            Coordinate c = new Coordinate(rawArray[index], rawArray[index + 1]);
            coordArrayList.add(c);
        }
        return coordArrayList;
    }
    //saveState()的逆过程,用于恢复游戏数据
    public void restoreState(Bundle icicle) {
        setMode(PAUSE);

        mAppleList = coordArrayToArrayList(icicle.getIntArray("mAppleList"));
        mDirection = icicle.getInt("mDirection");
        mNextDirection = icicle.getInt("mNextDirection");
        mMoveDelay = icicle.getInt("mMoveDelay");
        mScore = icicle.getLong("mScore");
        mSnakeTrail = coordArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
            if(mDirection != DOWN) {
                mNextDirection = UP;
            }
            return (true);
        }
        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
            if(mDirection != UP) {
                mNextDirection = DOWN;
            }
            return (true);
        }
        if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
            if(mDirection != LEFT) {
                mNextDirection = RIGHT;
            }
            return (true);
        }
        if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
            if(mDirection != RIGHT) {
                mNextDirection = LEFT;
            }
            return (true);
        }
        return super.onKeyDown(keyCode,event);
    }

    public void setTextView(TextView newView) {
        mStatusText = newView;
    }

    public void setMode(int newMode) {
        this.newMode=newMode;
        int oldMode = mMode;
        mMode = newMode;
        if (newMode == RUNNING & oldMode != RUNNING) {
            mStatusText.setVisibility(INVISIBLE);
            update();
            return;
        }

        Resources res = getContext().getResources();
        CharSequence str = "";
        if (newMode == PAUSE) {
            str = res.getText(R.string.mode_pause);
        }
        if (newMode == READY) {
            str = res.getText(R.string.mode_ready);
        }
        if (newMode == LOSE) {
            str = res.getString(R.string.mode_lose_prefix) + mScore
                    + res.getString(R.string.mode_lose_suffix);
        }
        if (newMode == QUIT){
            str = res.getText(R.string.mode_quit);
        }

        mStatusText.setText(str);
        mStatusText.setVisibility(VISIBLE);
    }

    //记录坐标位置
    private class Coordinate {
        public int x;
        public int y;

        public Coordinate(int newX, int newY) {
            x = newX;
            y = newY;
        }

        //触碰检测，看蛇是否吃到苹果
        public boolean equals(Coordinate other) {
            if (x == other.x && y == other.y) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Coordinate: [" + x + "," + y + "]";
        }
    }
}

