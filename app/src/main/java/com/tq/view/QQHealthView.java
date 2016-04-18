package com.tq.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.tq.R;
import com.tq.Utils.ViewUtils;

/**
 * Created by boobooL on 2016/4/18 0018
 * Created 邮箱 ：boobooMX@163.com
 */
public class QQHealthView extends View{
    public static final String TAG="QQHealthView";
    private int mWidth;//自定义View高
    private int mHeight;//自定义View宽
    private  int mBackgroundCorner;//背景四角的弧度
    private int mArcCenterX;
    private int mArcCenterY;
    /**
     * 1、精度不一样，Rect是使用int类型作为数值，RectF是使用float类型作为数值
     2、两个类型提供的方法也不是完全一致
     Rect：
     equals(Object obj)   (for some reason it as it's own implementation of equals)
     exactCenterX()
     exactCenterY()
     flattenToString()
     toShortString()
     unflattenFromString(String str)
     RectF：
     round(Rect dst)
     roundOut(Rect dst)
     set(Rect src)
     */
    private RectF mArcRectF;
    private Paint mBackgroundPaint;
    private Paint mArcPaint;//最上面弧度的画笔
    private Paint mTextPaint;//文字的画笔
    private Paint mDashLinePaint;//虚线的画笔
    private Paint mBarPaint;//竖条的画笔

    private int[]mSteps;//步数的数组
    private float mRatio;


    private Context mContext;
    private int mDefaultThemeColor;//主题色
    private int mDefaultUpBackgroundColor;//上层默认的背景色
    private int mThemeColor;//主题的颜色
    private int mUpBackgroundColor;
    private  float mArcWidth;
    private float mBarWidth;
    private int mAverageStep;//平均的步数
    private int mMaxStep;//最大的步数
    private int mTotalSteps;//总共的步数
    private Paint mAvatarPaint;

    public QQHealthView(Context context) {
        this(context,null);
    }

    public QQHealthView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public QQHealthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;
        init();
    }

    private void init() {
        //下面这句是关闭硬件的加速，防某些4.0的设备虚线显示为实现的问题
        //可以在AndroidManifest.xml时的Application标签上加上android:hardwareAccelerated="false"
       //这样整个应用都关闭了硬件加速，虚线可以正常的显示，但是关闭硬件的加速对性能会有些影响
        setLayerType(View.LAYER_TYPE_HARDWARE,null);
        //自定义View的宽高的比例
        mRatio=450.f/525.f;
        //初始化一些默认的参数
        mBackgroundCorner= ViewUtils.dp2px(mContext,5);
        mDefaultThemeColor= Color.parseColor("#2EC3FD");
        mDefaultUpBackgroundColor=Color.WHITE;
        mThemeColor=mDefaultThemeColor;
        mUpBackgroundColor=mDefaultUpBackgroundColor;
        mSteps=new int[]{10050,15280,8900,9200,6500,5660,9450};
        calculateSteps();
        //背景画笔
        mBackgroundPaint=new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setColor(mThemeColor);

        //圆弧的画笔
        mArcPaint=new Paint();
        mArcPaint.setColor(mThemeColor);//画笔的颜色
        mArcPaint.setAntiAlias(true);//抗锯齿
        mArcPaint.setStyle(Paint.Style.STROKE);//空心
        mArcPaint.setDither(true);//防抖动
        mArcPaint.setStrokeJoin(Paint.Join.ROUND);//在画笔的连接处是圆滑的
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);//在画笔的起始处是圆滑的
        mArcPaint.setPathEffect(new CornerPathEffect(10));//画笔的效果
        //文字画笔
        mTextPaint=new Paint();
        mTextPaint.setAntiAlias(true);
        //虚线的画笔
        mDashLinePaint=new Paint();
        mDashLinePaint.setAntiAlias(true);
        mDashLinePaint.setColor(Color.parseColor("#C1C1C1"));
        mDashLinePaint.setStyle(Paint.Style.STROKE);//空心
        mDashLinePaint.setPathEffect(new DashPathEffect(new float[]{8,4},0));//画虚线
        //竖条画笔
        mBarPaint=new Paint();
        mBarPaint.setColor(mThemeColor);
        mBarPaint.setAntiAlias(true);
        mBarPaint.setStrokeCap(Paint.Cap.ROUND);
        //头像的画笔
        mAvatarPaint=new Paint();
        mAvatarPaint.setAntiAlias(true);

    }
    public void setThemeColor(int color){
        mThemeColor=color;
        mBackgroundPaint.setColor(mThemeColor);
        mArcPaint.setColor(mThemeColor);
        mBarPaint.setColor(mThemeColor);
        invalidate();
    }

    public void setSteps(int[] steps){
        if(steps==null||steps.length==0)
            throw  new IllegalArgumentException("非法参数");
        mSteps=steps;
        calculateSteps();
        invalidate();
    }

    /**
     * 将原始的图片转换为圆形的图片
     * @param bitmap
     * @return
     */
    public Bitmap toRoundBitmap(Bitmap bitmap){
        int width=bitmap.getWidth();
        int height=bitmap.getHeight();
        int r;
        if(width>height){
            r=height;
        }else{
            r=width;
        }
        Bitmap backgroundBitmap=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(backgroundBitmap);
        Paint paint=new Paint();
        paint.setAntiAlias(true);
        RectF rectF=new RectF(0,0,r,r);
        BitmapShader shader=new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        canvas.drawRoundRect(rectF,r/2,r/2,paint);
        return  backgroundBitmap;
    }


    /**
     * 计算步数
     */
    private void calculateSteps() {
        mTotalSteps=0;
        mMaxStep=0;
        mAverageStep=0;
        for (int i = 0; i <mSteps.length ; i++) {
            mTotalSteps+=mSteps[i];
            if(mMaxStep<mSteps[i])
                mMaxStep=mSteps[i];
        }
        mAverageStep=(int)(mTotalSteps*1.0f/mSteps.length);

    }

    //绘制最下层的背景
    public void  drawBelowBackground(int left,int  top,int right,int bottom,int radius,Canvas canvas,Paint paint){
        Path path=new Path();
        path.moveTo(left,top);

        path.lineTo(right-radius,top);
        path.quadTo(right,top,right,top+radius);

        path.lineTo(right,bottom-radius);
        path.quadTo(right,bottom,right-radius,bottom);

        path.lineTo(left+radius,bottom);
        path.quadTo(left,bottom,left,bottom-radius);

        path.lineTo(left,top+radius);
        path.quadTo(left,top,left+radius,top);
        canvas.drawPath(path,paint);
    }

    //绘制上层的背景
    public void drawUpBackground(int left,int top,int right,int bottom,int radius,Canvas canvas,Paint  paint){
        Path path=new Path();
        path.moveTo(left,top);

        path.lineTo(right-radius,top);
        path.quadTo(right,top,right,top+radius);

        path.lineTo(right,bottom);
        path.lineTo(left,bottom);

        path.lineTo(left,top+radius);
        path.quadTo(left,top,left+radius,top);

        canvas.drawPath(path,paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultWidth=Integer.MAX_VALUE;
        int width;
        int height;
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        if(widthMode==MeasureSpec.EXACTLY||widthMode==MeasureSpec.AT_MOST){
            width=widthSize;
        }else{
            width=defaultWidth;
        }
        int defaultHeight=(int)(width*1.f/mRatio);
        height=defaultHeight;
        setMeasuredDimension(width,height);
        Log.i(TAG,"width:"+width+"|height:"+height);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth=w;
        mHeight=h;
        mArcCenterX=(int)(mWidth/2.f);
        mArcCenterY=(int)(160.f/525.f*mHeight);
        mArcRectF=new RectF();
        mArcRectF.left=mArcCenterX-125.f/450.f*mWidth;
        mArcRectF.top=mArcCenterY-125.f/525.f*mHeight;
        mArcRectF.right=mArcCenterX+125.f/450.f*mWidth;
        mArcRectF.bottom=mArcCenterY+125.f/525.f*mHeight;

        mArcWidth=20.f/450.f*mWidth;
        mBarWidth=16.f/450.f*mWidth;

        //画笔的宽度一定要在这里设置才能自适应
        mArcPaint.setStrokeWidth(mArcWidth);
        mBarPaint.setStrokeWidth(mBarWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
       // super.onDraw(canvas);
        float startX,startY,stopX,stopY,xPos,yPos;
        //1.绘制最下层背景
        drawBelowBackground(0,0,mWidth,mHeight,mBackgroundCorner,canvas,mBackgroundPaint);
        //2.绘制上面的背景
        mBackgroundPaint.setColor(mUpBackgroundColor);
        drawUpBackground(0,0,mWidth,mWidth,mBackgroundCorner,canvas,mBackgroundPaint);

        //3.绘制圆弧
        canvas.drawArc(mArcRectF,120,300,false,mArcPaint);
        //4.绘制圆弧里面的文字
        xPos=mArcCenterX;
        yPos=(int)(mArcCenterY-40.f/525.f*mHeight);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(15.f/450.f*mWidth);
        mTextPaint.setColor(Color.parseColor("#C1C1C1"));
        canvas.drawText("截止22:50分已走",xPos,yPos,mTextPaint);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(42.f/450.f*mWidth);
        mTextPaint.setColor(mThemeColor);
        canvas.drawText(mSteps[mSteps.length-1]+"",mArcCenterX,mArcCenterY,mTextPaint);
        yPos=(int)(mArcCenterY+50.f/525.f*mHeight);
        mTextPaint.setColor(Color.parseColor("#C1C1C1"));
        mTextPaint.setTextSize(13.f/450.f*mWidth);
        canvas.drawText("好友平均5620步",mArcCenterX,yPos,mTextPaint);
        xPos=(int)(mArcCenterX-35.f/450.f*mWidth);
        yPos=(int)(mArcCenterY+120.f/525.f*mHeight);
        canvas.drawText("第",xPos,yPos,mTextPaint);
        xPos=(int)(mArcCenterX+35.f/450.f*mWidth);
        canvas.drawText("名",xPos,yPos,mTextPaint);
        mTextPaint.setColor(mThemeColor);
        mTextPaint.setTextSize(24.f/450.f*mWidth);
        canvas.drawText("10",mArcCenterX,yPos,mTextPaint);
        //5.绘制圆弧下面的文字
        xPos=(int)(25.f/450.f*mWidth);
        yPos=(int)(330.f/525.f*mHeight);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setTextSize(12.f/450.f*mWidth);
        canvas.drawText("最近7天",xPos,yPos,mTextPaint);
        xPos=(int)((450.f-25.f)/450.f*mWidth);
        yPos=(int)(330.f/525.f*mHeight);
        mTextPaint.setTextAlign(Paint.Align.RIGHT);
        mTextPaint.setColor(Color.parseColor("#C1C1C1"));
        mTextPaint.setTextSize(12.f/450.f*mWidth);
        canvas.drawText("平均"+mAverageStep+"步/天",xPos,yPos,mTextPaint);

        //6.画虚线
        xPos=(int)(25.f/450.f*mWidth);
        yPos=(int)(352.f/525.f*mHeight);
        stopX=xPos+(450.f-50.f)/450.f*mWidth;
        stopY=yPos;
        canvas.drawLine(xPos,yPos,stopX,stopY,mDashLinePaint);
        //7.绘制下面的竖条
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(10.f/450.f*mWidth);
        startY=388.f/525.f*mHeight;
        for (int i = 0; i <mSteps.length; i++) {
            float barHeight=mSteps[i]*1.f/mAverageStep*35.f/525.f*mHeight;
            startX=55.f/450.f*mWidth+i*(57.f/450.f*mWidth);
            stopX=startX;
            stopY=startY-barHeight;
            if(mSteps[i]<mAverageStep)
                mBarPaint.setColor(Color.parseColor("#C1C1C1"));
            else
                mBarPaint.setColor(mThemeColor);
            canvas.drawLine(startX,startY,stopX,stopY,mBarPaint);
            canvas.drawText("0"+(i+1)+"日",startX,startY+25.f/525.f*mHeight,mTextPaint);
        }

        //8.绘制蓝色层的文字以及头像
        yPos=(mHeight-mWidth)/2.f+mWidth+20.f/450.f*mWidth/2;
        xPos=80.f/450.f*mWidth;
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(20.f/450.f*mWidth);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("SOLID获的今日的冠军",xPos,yPos,mTextPaint);

        Bitmap bitmap= BitmapFactory.decodeResource(mContext.getResources(),R.drawable.avastar);
        Rect dst=new Rect();//头像绘制到的矩形
        int rectWidth=(int)(30.f/525.f*mHeight);//矩形的宽度
        dst.top=(int)((mHeight-mWidth)/2.f+mWidth-rectWidth/2.f);
        dst.left=(int)(xPos-40.f/450*mWidth);
        dst.bottom=(int)((mHeight-mWidth)/2.f+mWidth+rectWidth/2.f);
        dst.right=(int)(xPos-10.f/450*mWidth);
        bitmap=toRoundBitmap(bitmap);
        canvas.drawBitmap(bitmap,null,dst,mAvatarPaint);//绘制头像

        xPos=425.f/450.f*mWidth;
        mTextPaint.setTextAlign(Paint.Align.RIGHT);
        mTextPaint.setTextSize(15.f/450.f*mWidth);
        canvas.drawText("查看>",xPos,yPos,mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        RectF rectF=new RectF();
        rectF.top=mWidth;
        rectF.left=380.f/450.f*mWidth;
        rectF.right=mWidth;
        rectF.bottom=mHeight;
        if(rectF.contains(event.getX(),event.getY())){
            //当前点击的坐标在右下角的范围内
            //在这里可以做点击事件的监听
            Snackbar.make(this,"Click",Snackbar.LENGTH_SHORT).show();;
            return true;
        }else {
            return super.onTouchEvent(event);
        }
    }
}
