package com.xiaosong.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

public class FragmentAdapter extends FragmentStatePagerAdapter{

	List<Fragment> list ;
	
	public FragmentAdapter(FragmentManager fm,List<Fragment> list) {
		super(fm);
		
		this.list = list;
	}

	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}
	
	@Override
	public int getItemPosition(Object object) {
	        return PagerAdapter.POSITION_NONE;
	}
	

}
