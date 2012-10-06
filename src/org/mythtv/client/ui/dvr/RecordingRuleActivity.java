/**
 *  This file is part of MythTV for Android
 * 
 *  MythTV for Android is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MythTV for Android is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MythTV for Android.  If not, see <http://www.gnu.org/licenses/>.
 *   
 * This software can be found at <https://github.com/MythTV-Android/mythtv-for-android/>
 *
 */
package org.mythtv.client.ui.dvr;

import org.mythtv.R;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

/**
 * @author Daniel Frey
 * 
 */
public class RecordingRuleActivity extends AbstractDvrActivity {

	private static final String TAG = RecordingRuleActivity.class.getSimpleName();
	private static final int EDIT_ID = Menu.FIRST + 2;
	
	public static final String EXTRA_RECORDING_RULE_KEY = "org.mythtv.client.ui.dvr.recordingRule.EXTRA_RECORDING_RULE_KEY";

	private RecordingRuleFragment recordingRuleFragment = null;

	private Integer recordingRuleId;
	
	// ***************************************
	// Activity methods
	// ***************************************

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		Log.v( TAG, "onCreate : enter" );
		super.onCreate( savedInstanceState );

		Bundle extras = getIntent().getExtras(); 
		recordingRuleId = extras.getInt( EXTRA_RECORDING_RULE_KEY );
		
		setContentView( R.layout.fragment_dvr_recording_rule );

		recordingRuleFragment = (RecordingRuleFragment) getSupportFragmentManager().findFragmentById( R.id.fragment_dvr_recording_rule );
		recordingRuleFragment.loadRecordingRule( recordingRuleId );
		
		Log.v( TAG, "onCreate : exit" );
	}
	
}
