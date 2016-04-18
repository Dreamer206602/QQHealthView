package com.tq.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

/**
 * Created by boobooL on 2016/4/18 0018
 * Created 邮箱 ：boobooMX@163.com
 */
public class ViewUtils {


    //转换dp为px
    public static int dp2px(Context context,int dip){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dip*scale+0.5f*(dip>=0?1:-1));
    }

    //转换px为dp
    public static int px2dp(Context context,int px){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(px/scale+0.5*(px>=0?1:-1));
    }

    //转换sp为px
    public static  int sp2px(Context context,float spValue){
        float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        return (int)(spValue*fontScale+0.5f);
    }

    //转换px为sp
    public static int px2sp(Context context,float pxValue){
        float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        return (int)(pxValue/fontScale+0.5f);
    }

    /**
     * 获的系统的主题颜色
     * @param context
     * @return
     */
    public static int getThemeColorPrimary(Context context){
        TypedValue typedValue=new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.theme,typedValue,true);
        int[] attribute=new int[]{android.R.attr.colorPrimary};
        TypedArray array=context.obtainStyledAttributes(typedValue.resourceId,attribute);
        int color=array.getColor(0,-1);
        array.recycle();
        return color;
    }


}
