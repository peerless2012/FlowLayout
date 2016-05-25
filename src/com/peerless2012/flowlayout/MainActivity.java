package com.peerless2012.flowlayout;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class MainActivity extends Activity implements OnClickListener{

	private FlowLayout mFlowLayout;
	
	private Random mRandom = new Random();
	
	private FlowViewsAdapter mFlowViewsAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mFlowLayout = (FlowLayout) findViewById(R.id.flow_layout);
		String[] keyWords = getResources().getStringArray(R.array.FlowViews);
		mFlowViewsAdapter = new FlowViewsAdapter(keyWords);
		mFlowLayout.setAdapter(mFlowViewsAdapter);
		findViewById(R.id.add_data).setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		int nextInt = mRandom.nextInt(5) + 5;
		String [] newDatas = new String[nextInt];
		for (int i = 0; i < newDatas.length; i++) {
			newDatas[i] = getRandomJianHan(mRandom.nextInt(5) + 1);
		}
		mFlowViewsAdapter.addAll(newDatas);
	}
	
	 /**
     * 获取指定长度随机简体中文
     * @param len int
     * @return String
     */
    public static String getRandomJianHan(int len)
    {
    	Random random = new Random();
        String ret="";
          for(int i=0;i<len;i++){
              String str = null;
              int hightPos, lowPos; // 定义高低位
              hightPos = (176 + Math.abs(random.nextInt(39))); //获取高位值
              lowPos = (161 + Math.abs(random.nextInt(93))); //获取低位值
              byte[] b = new byte[2];
              b[0] = (Integer.valueOf(hightPos).byteValue());
              b[1] = (Integer.valueOf(lowPos).byteValue());
              try
              {
                  str = new String(b, "GBk"); //转成中文
              }
              catch (UnsupportedEncodingException ex)
              {
                  ex.printStackTrace();
              }
               ret+=str;
          }
      return ret;
    }
}
