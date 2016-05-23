package com.peerless2012.flowlayout;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class MainActivity extends Activity {

	private FlowLayout mFlowLayout;
	
	private FlowViewsAdapter mFlowViewsAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mFlowLayout = (FlowLayout) findViewById(R.id.flow_layout);
		String[] keyWords = getResources().getStringArray(R.array.FlowViews);
		mFlowViewsAdapter = new FlowViewsAdapter(keyWords);
		mFlowLayout.setAdapter(mFlowViewsAdapter);
	}
}
