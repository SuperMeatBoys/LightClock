package com.gesuper.lightclock.view;

import com.gesuper.lightclock.R;
import com.gesuper.lightclock.model.AlertItemModel;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

public class AlertListView extends ListView implements OnScrollListener {

	public static final String TAG = "AlertListView";
	public static final int PULL_DOWN = 0;
	public static final int RELEASE_UP = 1;
	public static final int REFRESH_DONE = 2;
	
	private MainView mMainView;
	
	private boolean isRecored;
	private boolean isRefreshable;
	private int firstItemIndex;
	private int startY;
	private int status;
	
	private int RATIO = 1;
	private AlertItemView headView;
	private int headContentHeight;
	private TextView mTextView;
	
	public AlertListView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    // TODO Auto-generated constructor stub
	    this.initResource();
	}

	public void OnDraw(Canvas canvas){
		super.onDraw(canvas);
	}
	
	private void initResource(){
		//hide scroll bar
		this.setVerticalScrollBarEnabled(true);
		this.isRecored = false;
		this.isRefreshable = true;
		this.status = REFRESH_DONE;
		AlertItemModel mItemModel = new AlertItemModel();
		mItemModel.setId((long) -2);
		
		headView = new AlertItemView(this.getContext());
		headView.setModel(mItemModel);
		mTextView = (TextView)headView.findViewById(R.id.tv_content);
		this.measureView(headView);
		this.headContentHeight = this.headView.getMeasuredHeight();
		Log.d(TAG, "headContentHeight" + this.headContentHeight);
		this.headView.setPadding(0, -1 * this.headContentHeight, 0, 0);
		this.headView.invalidate();
		this.addHeaderView(headView);

		this.setOnScrollListener(this);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		int paddingTop;
		if(isRefreshable){
			switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				if(firstItemIndex == 0 && !isRecored){
					isRecored = true;
					startY = (int)event.getY();
					this.headView.setRandBgColor();
				}
				break;
			case MotionEvent.ACTION_UP:
				if(status == RELEASE_UP){
					status = REFRESH_DONE;
					changeHeaderViewByStatus();
					createNewAlert();
				}else{
					status = REFRESH_DONE;
					changeHeaderViewByStatus();
					this.headView.setPadding(0, -this.headContentHeight, 0, 0);
				}
				
				isRecored = false;
				break;
			case MotionEvent.ACTION_MOVE:
				int tmpY = (int)event.getY();
				if(!isRecored && firstItemIndex == 0){
					isRecored = true;
					startY = tmpY;
				}
				if(this.status == RELEASE_UP){
					setSelection(0);
					if(tmpY-startY < 0){
						this.status = REFRESH_DONE;
						changeHeaderViewByStatus();
					}
					else if(tmpY-startY < headContentHeight){
						this.status = PULL_DOWN;
						changeHeaderViewByStatus();
					} else if(tmpY-startY > 4 * headContentHeight){
						break;
					}
					paddingTop = (tmpY - startY) - headContentHeight;
					headView.setPadding(0, paddingTop / this.RATIO, 
							0, 0);
				}
				else if(this.status == PULL_DOWN){
					setSelection(0);
					if(tmpY - startY >= headContentHeight){
						this.status = RELEASE_UP;
						changeHeaderViewByStatus();
					}else if(tmpY - startY <= 0){
						this.status = REFRESH_DONE;
						changeHeaderViewByStatus();
					}
					paddingTop = (tmpY - startY) - headContentHeight;
					headView.setPadding(0, paddingTop / this.RATIO, 
							0, 0);
				}
				else if(this.status == REFRESH_DONE){
					if(tmpY - startY > 0){
						this.status = PULL_DOWN;
						changeHeaderViewByStatus();
					}
				}
				break;
			}
		}
		return super.onTouchEvent(event);
	}

	private void createNewAlert() {
		// TODO Auto-generated method stub
		this.headView.setPadding(0, -this.headContentHeight, 0, 0);
		this.mMainView.addNewItem(this.headView.getModel().getBgColorId());
	}

	private void changeHeaderViewByStatus() {
		// TODO Auto-generated method stub
		switch(status){
		case PULL_DOWN:
			this.mTextView.setHint("pull down to create");
			this.RATIO = 1;
			break;
		case RELEASE_UP:
			this.RATIO = 4;
			this.mTextView.setHint("release to create");
			break;
		case REFRESH_DONE:
			this.RATIO = 1;
			this.mTextView.setHint("click to edit");
			break;
		}
	}
	
	public void onScroll(AbsListView view, int firstVisibleItem, 
			int visibleItemCount, int totalItemCount){
		this.firstItemIndex = firstVisibleItem;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}
	
	public View getItemAt(int index){
		return super.getChildAt(index+1);
	}



	public void setMainView(MainView mainView) {
		// TODO Auto-generated method stub
		this.mMainView = mainView;
	}
	
	public boolean isRecored(){
		return this.isRecored;
	}
	
	@SuppressWarnings("deprecation")
	public void measureView(View child){
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if(p == null){
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);  
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if(lpHeight > 0){
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		}else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}
}
