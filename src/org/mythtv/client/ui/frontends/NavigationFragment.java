package org.mythtv.client.ui.frontends;


import org.mythtv.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class NavigationFragment extends AbstractFrontendFragment implements OnClickListener  {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		//inflate fragment layout
		View view = inflater.inflate(R.layout.fragment_mythmote_navigation, container, false);
		
		//set onclick listener for each button
		((ImageButton)view.findViewById(R.id.imageButton_nav_info)).setOnClickListener(this);
		((ImageButton)view.findViewById(R.id.imageButton_nav_up)).setOnClickListener(this);
		((ImageButton)view.findViewById(R.id.imageButton_nav_tvguide)).setOnClickListener(this);
		((ImageButton)view.findViewById(R.id.imageButton_nav_left)).setOnClickListener(this);
		((ImageButton)view.findViewById(R.id.imageButton_nav_select)).setOnClickListener(this);
		((ImageButton)view.findViewById(R.id.imageButton_nav_right)).setOnClickListener(this);
		((ImageButton)view.findViewById(R.id.imageButton_nav_cancel)).setOnClickListener(this);
		((ImageButton)view.findViewById(R.id.imageButton_nav_down)).setOnClickListener(this);
		((ImageButton)view.findViewById(R.id.imageButton_nav_menu)).setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onClick(View v) {
		
		final FrontendsFragment frontends = (FrontendsFragment) getFragmentManager().findFragmentById( R.id.frontends_fragment );
		final Frontend fe = frontends.getSelectedFrontend();
		
		//exit if we don't have a frontend
		if(null == fe) return;
		
		switch(v.getId()){
		case R.id.imageButton_nav_info:
			new SendActionTask().execute(fe.getUrl(), "INFO");
			break;
			
		case R.id.imageButton_nav_up:
			new SendActionTask().execute(fe.getUrl(), "UP");
			break;
			
		case R.id.imageButton_nav_tvguide:
			new SendActionTask().execute(fe.getUrl(), "GUIDE");
			break;
			
		case R.id.imageButton_nav_left:
			new SendActionTask().execute(fe.getUrl(), "LEFT");
			break;
			
		case R.id.imageButton_nav_select:
			new SendActionTask().execute(fe.getUrl(), "SELECT");
			break;
			
		case R.id.imageButton_nav_right:
			new SendActionTask().execute(fe.getUrl(), "RIGHT");
			break;
			
		case R.id.imageButton_nav_cancel:
			new SendActionTask().execute(fe.getUrl(), "ESCAPE");
			break;
			
		case R.id.imageButton_nav_down:
			new SendActionTask().execute(fe.getUrl(), "DOWN");
			break;
			
		case R.id.imageButton_nav_menu:
			new SendActionTask().execute(fe.getUrl(), "MENU");
			break;
		};
		
	}
	
	
	
	
	
}
