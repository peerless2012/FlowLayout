package com.peerless2012.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MyTest extends AbsListView {

	public MyTest(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MyTest(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyTest(Context context) {
		super(context);
	}

	@Override
	public ListAdapter getAdapter() {
		ListView listView;
		return null;
	}

	@Override
	public void setSelection(int position) {
		
	}

	
}
