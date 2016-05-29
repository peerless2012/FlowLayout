package com.peerless2012.flowlayout;

import java.util.Random;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FlowViewsAdapter extends BaseAdapter {

	private String[] mKeyWords ;
	
	private Random mRandom = new Random();
	
	private LayoutInflater mLayoutInflater;
	
	public FlowViewsAdapter(String[] keyWords) {
		super();
		mKeyWords = keyWords;
	}

	public void addAll(String[] newDatas) {
		int newSize = newDatas == null ? 0: newDatas.length;
		if (newSize == 0) return;
		int preSize = mKeyWords == null ? 0: mKeyWords.length;
		if (preSize == 0) {
			mKeyWords = newDatas;
			notifyDataSetChanged();
			return;
		}
		String[] preDatas = mKeyWords;
		mKeyWords = new String[newSize + preSize];
		System.arraycopy(preDatas, 0, mKeyWords, 0, preDatas.length);
		System.arraycopy(newDatas, 0, mKeyWords, preDatas.length, newDatas.length);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mKeyWords == null ? 0 : mKeyWords.length;
	}

	@Override
	public String getItem(int position) {
		return mKeyWords[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			if (mLayoutInflater == null) {
				mLayoutInflater = LayoutInflater.from(parent.getContext());
			}
			convertView = mLayoutInflater.inflate(R.layout.flow_item_view, parent, false);
		}else {
			
		}
		TextView textView = (TextView) convertView;
		textView.setText(getItem(position));
		convertView.setBackgroundColor(Color.argb(100, mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255)));
		return convertView;
	}

}
