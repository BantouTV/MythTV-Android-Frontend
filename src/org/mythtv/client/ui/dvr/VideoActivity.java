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
 * @author Daniel Frey <dmfrey at gmail dot com>
 * 
 * This software can be found at <https://github.com/dmfrey/mythtv-for-android/>
 *
 */
package org.mythtv.client.ui.dvr;


import org.mythtv.R;
import org.mythtv.client.ui.BaseActivity;
import org.mythtv.services.api.content.LiveStreamInfo;
import org.mythtv.services.api.dvr.Program;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * @author John Baab
 * 
 */
public class VideoActivity extends BaseActivity {

	private static final String TAG = VideoActivity.class.getSimpleName();

	public static final String EXTRA_PROGRAM_GROUP_KEY = "org.mythtv.client.ui.dvr.programGroup.EXTRA_PROGRAM_GROUP_KEY";
	private LiveStreamInfo info = null;

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

	    setContentView(R.layout.activity_video);
	    
	    new CreateStreamTask().execute();
	    
	    // replace me with the real url once the return is working
	    String url = "http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8";
	    VideoView videoView = (VideoView)findViewById(R.id.videoView);
	    videoView.setVideoURI(Uri.parse(url));
	    videoView.setMediaController(new MediaController(this));
	    videoView.requestFocus();
	    videoView.start();
		
		Log.v( TAG, "onCreate : exit" );
	}
	
	private void exceptionDialolg( Throwable t ) {
		AlertDialog.Builder builder = new AlertDialog.Builder( this );

		builder
			.setTitle( R.string.exception )
			.setMessage( t.toString() )
			.setPositiveButton( R.string.close, null )
				.show();
	}
	
	public void setLiveStreamInfo( LiveStreamInfo info ) {
		Log.v( TAG, "setLiveStreamInfo : enter" );

		this.info = info;

		Log.v( TAG, "setLiveStreamInfo : exit" );
	}
	
	
	private class CreateStreamTask extends AsyncTask<Void, Void, LiveStreamInfo> {

		private Exception e = null;

		@Override
		protected LiveStreamInfo doInBackground( Void... params ) {
			Log.v( TAG, "doInBackground : enter" );

			LiveStreamInfo lookup = null;

			try {
				Log.v( TAG, "doInBackground : lookup" );
				Program program = getApplicationContext().getCurrentProgram();
				
				//lookup = getApplicationContext().getMythServicesApi().contentOperations().
				//	addRecordingLiveStream(Integer.valueOf(program.getChannelInfo().getChannelId()), program.getStartTime(), 
				//			-1, -1, -1, -1, -1, -1);
				
				lookup = getApplicationContext().getMythServicesApi().contentOperations().
						addLiveStream(null, program.getFilename(), null, -1, -1, -1, -1, -1, -1);
			} catch( Exception e ) {
				Log.v( TAG, "doInBackground : error" );

				this.e = e;
			}

			Log.v( TAG, "doInBackground : exit" );
			return lookup;
		}

		@Override
		protected void onPostExecute( LiveStreamInfo result ) {
			Log.v( TAG, "onPostExecute : enter" );

			if( null == e ) {
				setLiveStreamInfo( result );
			} else {
				Log.e( TAG, "error creating live stream", e );
				exceptionDialolg( e );
			}

			Log.v( TAG, "onPostExecute : exit" );
		}
	}

}
