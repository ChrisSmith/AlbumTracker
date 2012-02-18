package org.collegelabs.albumtracker.views;

import org.collegelabs.albumtracker.R;
import org.collegelabs.library.bitmaploader.views.AsyncImageView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

public class SquareImage extends AsyncImageView {

	public static enum FixedAlong{
		width,
		height
	}
	
	private FixedAlong fixedAlong = FixedAlong.width;
	
	public SquareImage(Context context) {
		super(context);
	}
	
	public SquareImage(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SquareImage, 0, 0);
		
		fixedAlong = FixedAlong.valueOf(array.getString(R.styleable.SquareImage_fixedAlong));
        if (fixedAlong == null) fixedAlong = FixedAlong.width;
		
		array.recycle();
	}
	
	public SquareImage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	int squareDimen = 1;
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int square = (fixedAlong == FixedAlong.width) ? getMeasuredWidth() : getMeasuredHeight();

		if(square > squareDimen){
			squareDimen = square;
		}
		
		setMeasuredDimension(squareDimen, squareDimen);
	}
	
	

}
