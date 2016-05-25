package com.peerless2012.flowlayout;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Build;
import android.support.v4.util.LongSparseArray;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewGroupCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.ListAdapter;

/**
* @Author peerless2012
* @Email peerless2012@126.com
* @DateTime 2016年5月14日 上午12:07:59
* @Version V1.0
* @Description: 自定义流式布局
*/
public class FlowLayout extends ViewGroup {

	private ListAdapter mListAdapter;
	
	private ListDataSetObserver mListDataSetObserver;
	
	private ArrayList<View> mChilds;
	
	private boolean isDirty = true;
	
	private RecycleBin mRecycler;
	
	 /**
     * Running count of how many items are currently checked
     */
    int mCheckedItemCount;

    /**
     * Running state of which positions are currently checked
     */
    SparseBooleanArray mCheckStates;

    /**
     * Running state of which IDs are currently checked.
     * If there is a value for a given key, the checked state for that ID is true
     * and the value holds the last known position in the adapter for that id.
     */
    LongSparseArray<Integer> mCheckedIdStates;
    
    /**
     * If mAdapter != null, whenever this is true the adapter has stable IDs.
     */
    boolean mAdapterHasStableIds;
    
    /**
     * Controls if/how the user may choose/check items in the list
     */
    int mChoiceMode = CHOICE_MODE_NONE;
    
    /**
     * Normal list that does not indicate choices
     */
    public static final int CHOICE_MODE_NONE = 0;

    /**
     * The list allows up to one choice
     */
    public static final int CHOICE_MODE_SINGLE = 1;

    /**
     * The list allows multiple choices
     */
    public static final int CHOICE_MODE_MULTIPLE = 2;

    /**
     * The list allows multiple choices in a modal selection mode
     */
    public static final int CHOICE_MODE_MULTIPLE_MODAL = 3;
    
	public FlowLayout(Context context) {
		this(context,null);
	}

	public FlowLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs);
	}

	public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context,attrs);
	}

	/*-----------------------------------------系统方法区---------------------------------------------*/
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int gLeft = 0,gTop = 0;
		int viewWidth =MeasureSpec.getSize(widthMeasureSpec);
		int viewHeight = MeasureSpec.getSize(heightMeasureSpec);
		if (mListAdapter == null || mListAdapter.getCount() == 0) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}else {
			int childCount = getChildCount();
			View child = null;
			int height = 0,width = 0;
			for (int i = 0; i < childCount; i++) {
				child = getChildAt(i);
				child.measure(MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.AT_MOST)
						, MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.AT_MOST));
				LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
				width = child.getMeasuredWidth();
				height = child.getMeasuredHeight();
				gLeft += width;
				Log.i("FlowLayout", "Measure  width = "+ width +"   height = "+height);
				if (gLeft >= viewWidth) {//需要折行
					Log.i("FlowLayout", "折行");
					gTop += height;
					gLeft = 0;
				}
				layoutParams.setPosition(gLeft - width, gTop, gLeft, gTop + height);
			}
			isDirty = false;
		}
		setMeasuredDimension(viewWidth, 1000);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mChilds == null) {
			return;
		}
		int childCount = getChildCount();
		View child;
		LayoutParams layoutParams;
		for (int i = 0; i < childCount; i++) {
			child = getChildAt(i);
			layoutParams = (LayoutParams) child.getLayoutParams();
			Log.i("FlowLayout", "onLayout : left = " + layoutParams.left +",   top = " + layoutParams.top +" ,   right = " + layoutParams.right+" ,   bottom = " + layoutParams.bottom);
			child.layout(layoutParams.left, t + layoutParams.top, layoutParams.right, t + layoutParams.bottom);
		}
	}

	Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
//		mPaint .setColor(Color.RED);
//		mPaint.setStyle(Style.FILL);
//		mPaint.setStrokeWidth(20);
//		canvas.drawCircle(200, 200, 100, mPaint);
	}
	
	/*-----------------------------------------自定义方法区---------------------------------------------*/
	private void generateViews() {
		mChilds.clear();
		if (mListAdapter != null) {
			removeAllViewsInLayout();
			int count = mListAdapter.getCount();
			View child = null;
			for (int i = 0; i < count; i++) {
				// 后面牵涉到复用的话第二个参数就不能总是传空了
				child = mListAdapter.getView(i, null, this);
				child.setId(generateViewIdComp());
				mChilds.add(child);
				addView(child);
				requestLayout();
//				invalidate();
			}
		}else {
			removeAllViews();
		}
	}
	
	
	private void init(Context context, AttributeSet attrs) {
		mListDataSetObserver = new ListDataSetObserver();
		mChilds = new ArrayList<View>();
		mRecycler = new RecycleBin();
		setWillNotDraw(false);
	}
	
	
	public void setAdapter(ListAdapter listAdapter) {
		mListAdapter = listAdapter;
		mListAdapter.registerDataSetObserver(mListDataSetObserver);
		mRecycler.setViewTypeCount(listAdapter.getViewTypeCount());
		mAdapterHasStableIds = listAdapter.hasStableIds();
		if (mChoiceMode != CHOICE_MODE_NONE && mAdapterHasStableIds &&
                mCheckedIdStates == null) {
            mCheckedIdStates = new LongSparseArray<Integer>();
        }
		if (mCheckStates != null) {
            mCheckStates.clear();
        }

        if (mCheckedIdStates != null) {
            mCheckedIdStates.clear();
        }
        generateViews();
	}
	
	
	class ListDataSetObserver extends DataSetObserver{

		@Override
		public void onChanged() {
			super.onChanged();
			//刷新界面
			generateViews();
		}

		@Override
		public void onInvalidated() {
			super.onInvalidated();
			generateViews();
		}
		
	}
	
	/**
     * An {@code int} value that may be updated atomically.
     */
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * 动态生成View ID
     * API LEVEL 17 以上View.generateViewId()生成
     * API LEVEL 17 以下需要手动生成
     */
    @SuppressLint("NewApi")
	public static int generateViewIdComp() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }
	
	/*-----------------------------------------声明区---------------------------------------------*/
	
	/**
	* @Author peerless2012
	* @Email peerless2012@126.com
	* @DateTime 2016年5月18日 下午4:29:08
	* @Version V1.0
	* @Description: 对齐方式
	* <p>{@link FlowLayout.Alignment#LEFT LEFT} 左对齐</p>
	* <p>{@link FlowLayout.Alignment#RIGHT RIGHT} 右对齐</p>
	* <p>{@link FlowLayout.Alignment#DISTRIBUTE DISTRIBUTE} 分散对齐</p>
	*/
	public enum Alignment{
		/**
		 * 左对齐
		 */
		LEFT(0x00)
		/**
		 * 右对齐
		 */
		,RIGHT(0x01)
		/**
		 * 分散对齐
		 */
		,DISTRIBUTE(0x02);
		
		private int mValue;
		
		Alignment(int value) {
			mValue = value;
		}
		
		public int getmValue() {
			return mValue;
		}
	}
	
	/**
	* @Author peerless2012
	* @Email peerless2012@126.com
	* @DateTime 2016年5月18日 下午4:25:50
	* @Version V1.0
	* @Description: 选中的模式
	* <p>{@link FlowLayout.CheckMode#NONE NONE} 无选中效果</p>
	* <p>{@link FlowLayout.CheckMode#SINGLE SINGLE} 单选效果</p>
	* <p>{@link FlowLayout.CheckMode#MULTI MULTI} 多选效果</p>
	*/
	public enum CheckMode{
		/**
		 * 无选中效果 
		 */
		NONE(0x00)
		/**
		 * 单选效果
		 */
		,SINGLE(0x01)
		/**
		 * 多选效果
		 */
		,MULTI(0x02);
		
		private int mValue;
		CheckMode(int value) {
			mValue = value;
		}
		
		public int getmValue() {
			return mValue;
		}
	}
	 @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new FlowLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FlowLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof FlowLayout.LayoutParams;
    }
	 /**
     * Get a view and have it show the data associated with the specified
     * position. This is called when we have already discovered that the view is
     * not available for reuse in the recycle bin. The only choices left are
     * converting an old view or making a new one.
     *
     * @param position The position to display
     * @param isScrap Array of at least 1 boolean, the first entry will become true if
     *                the returned view was taken from the scrap heap, false if otherwise.
     *
     * @return A view displaying the data associated with the specified position
     */
    View obtainView(int position, boolean[] isScrap) {

        isScrap[0] = false;
        View scrapView = mRecycler.getScrapView(position);

        View child;
        if (scrapView != null) {
            child = mListAdapter.getView(position, scrapView, this);
            if (child != scrapView) {
                mRecycler.addScrapView(scrapView, position);
            } else {
                isScrap[0] = true;
            }
        } else {
            child = mListAdapter.getView(position, null, this);
        }

        if (mAdapterHasStableIds) {
            final ViewGroup.LayoutParams vlp = child.getLayoutParams();
            LayoutParams lp;
            if (vlp == null) {
                lp = (LayoutParams) generateDefaultLayoutParams();
            } else if (!checkLayoutParams(vlp)) {
                lp = (LayoutParams) generateLayoutParams(vlp);
            } else {
                lp = (LayoutParams) vlp;
            }
            lp.itemId = mListAdapter.getItemId(position);
            child.setLayoutParams(lp);
        }

        return child;
    }
    
    /**
     * FlowLayout extends LayoutParams to provide a place to hold the view type.
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {
        /**
         * View type for this view, as returned by
         * {@link android.widget.Adapter#getItemViewType(int) }
         */
        int viewType;

        /**
         * When this boolean is set, the view has been added to the AbsListView
         * at least once. It is used to know whether headers/footers have already
         * been added to the list view and whether they should be treated as
         * recycled views or not.
         */
        @ViewDebug.ExportedProperty(category = "list")
        boolean recycledHeaderFooter;

        /**
         * When an AbsListView is measured with an AT_MOST measure spec, it needs
         * to obtain children views to measure itself. When doing so, the children
         * are not attached to the window, but put in the recycler which assumes
         * they've been attached before. Setting this flag will force the reused
         * view to be attached to the window rather than just attached to the
         * parent.
         */
        @ViewDebug.ExportedProperty(category = "list")
        boolean forceAdd;

        /**
         * The position the view was removed from when pulled out of the
         * scrap heap.
         * @hide
         */
        int scrappedFromPosition;

        /**
         * The ID the view represents
         */
        long itemId = -1;

        int left = 0,top = 0,right = 0,bottom= 0;
        
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(int w, int h, int viewType) {
            super(w, h);
            this.viewType = viewType;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
        
        public void setPosition(int left,int top ,int right ,int bottom) {
			this.left = left;
			this.top = top;
			this.right =right;
			this.bottom = bottom;
		}
    }
    
	class RecycleBin{
		private ArrayList<View>[] mScrapViews;
		private int mViewTypeCount;
        private ArrayList<View> mCurrentScrap;
		public void setViewTypeCount(int viewTypeCount) {
            if (viewTypeCount < 1) {
                throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
            }
            ArrayList<View>[] scrapViews = null;
            if (viewTypeCount > 1) {
            	scrapViews = new ArrayList[viewTypeCount];
            	for (int i = 0; i < viewTypeCount; i++) {
            		scrapViews[i] = new ArrayList<View>();
            	}
			}
            mViewTypeCount = viewTypeCount;
            //如果ViewTypeCount只有1，则没必要new mScrapViews数组。
            mCurrentScrap = scrapViews == null ? new ArrayList<View>() : scrapViews[0];
            mScrapViews = scrapViews;
        }
		
		public void markChildrenDirty() {
            if (mViewTypeCount == 1) {
                final ArrayList<View> scrap = mCurrentScrap;
                final int scrapCount = scrap.size();
                for (int i = 0; i < scrapCount; i++) {
                    scrap.get(i).forceLayout();
                }
            } else {
                final int typeCount = mViewTypeCount;
                for (int i = 0; i < typeCount; i++) {
                    final ArrayList<View> scrap = mScrapViews[i];
                    final int scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        scrap.get(j).forceLayout();
                    }
                }
            }
        }
		
		/**
         * @return A view from the ScrapViews collection. These are unordered.
         */
        View getScrapView(int position) {
            if (mViewTypeCount == 1) {
                return retrieveFromScrap(mCurrentScrap, position);
            } else {
                int whichScrap = mListAdapter.getItemViewType(position);
                if (whichScrap >= 0 && whichScrap < mScrapViews.length) {
                    return retrieveFromScrap(mScrapViews[whichScrap], position);
                }else {
					throw new IllegalArgumentException("The type of ItemView must be lagger than or equals to 0,and smaller than viewTypeCount!");
				}
            }
        }
        /**
         * Puts a view into the list of scrap views.
         * <p>
         * If the list data hasn't changed or the adapter has stable IDs, views
         * with transient state will be preserved for later retrieval.
         *
         * @param scrap The view to add
         * @param position The view's position within its parent
         */
        void addScrapView(View scrap, int position) {
            final FlowLayout.LayoutParams lp = (FlowLayout.LayoutParams) scrap.getLayoutParams();
            if (lp == null) {
                return;
            }

            lp.scrappedFromPosition = position;

            // Remove but don't scrap header or footer views, or views that
            // should otherwise not be recycled.
            final int viewType = lp.viewType;
            if (!shouldRecycleViewType(viewType)) {
                return;
            }

            if (mViewTypeCount == 1) {
                mCurrentScrap.add(scrap);
            } else {
                mScrapViews[viewType].add(scrap);
            }

            scrap.setAccessibilityDelegate(null);
        }
        
        
        public boolean shouldRecycleViewType(int viewType) {
            return viewType >= 0;
        }
	}
	
	static View retrieveFromScrap(ArrayList<View> scrapViews, int position) {
        int size = scrapViews.size();
        if (size > 0) {
            return scrapViews.remove(size - 1);
        } else {
            return null;
        }
    }
}
