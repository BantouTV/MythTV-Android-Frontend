/**
 * This file is part of MythTV Android Frontend
 *
 * MythTV Android Frontend is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MythTV Android Frontend is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MythTV Android Frontend.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This software can be found at <https://github.com/MythTV-Clients/MythTV-Android-Frontend/>
 */
package org.mythtv.service.dvr;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.mythtv.service.MythtvService;
import org.mythtv.service.util.UrlUtils;
import org.mythtv.services.api.ETagInfo;
import org.mythtv.services.api.dvr.Program;
import org.mythtv.services.api.dvr.Programs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * @author Daniel Frey
 *
 */
public class CoverartDownloadService extends MythtvService {

	private static final String TAG = CoverartDownloadService.class.getSimpleName();

	public static final String COVERART_INETREF = "INETREF";
	public static final String COVERART_TITLE = "TITLE";
	public static final String COVERART_FILE = "coverart.png";
	public static final String COVERART_FILE_NA = "coverart.na";
	
    public static final String ACTION_DOWNLOAD = "org.mythtv.background.coverartDownload.ACTION_DOWNLOAD";
    public static final String ACTION_COMPLETE = "org.mythtv.background.coverartDownload.ACTION_COMPLETE";

    public static final String EXTRA_COMPLETE = "COMPLETE";
    public static final String EXTRA_COMPLETE_FILENAME = "COMPLETE_FILENAME";

	private File programGroupsDirectory = null;

	public CoverartDownloadService() {
		super( "CoverartDownloadService" );
	}
	
	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent( Intent intent ) {
		Log.d( TAG, "onHandleIntent : enter" );
		super.onHandleIntent( intent );
		
		programGroupsDirectory = mFileHelper.getProgramGroupsDataDirectory();
		if( null == programGroupsDirectory || !programGroupsDirectory.exists() ) {
			Intent completeIntent = new Intent( ACTION_COMPLETE );
			completeIntent.putExtra( EXTRA_COMPLETE, "Program group location can not be found" );
			sendBroadcast( completeIntent );

			Log.d( TAG, "onHandleIntent : exit, programGroupsDirectory does not exist" );
			return;
		}

		ResponseEntity<String> hostname = mMainApplication.getMythServicesApi().mythOperations().getHostName();
		if( null == hostname || "".equals( hostname ) ) {
			Intent completeIntent = new Intent( ACTION_COMPLETE );
			completeIntent.putExtra( EXTRA_COMPLETE, "Master Backend unreachable" );
			sendBroadcast( completeIntent );

			Log.d( TAG, "onHandleIntent : exit, Master Backend unreachable" );
			return;
		}

		if ( intent.getAction().equals( ACTION_DOWNLOAD ) ) {
    		Log.i( TAG, "onHandleIntent : DOWNLOAD action selected" );

    		try {
    			download( intent );
    		} catch( Exception e ) {
    			Log.e( TAG, "onHandleIntent : error", e );
    		} finally {
    			Intent completeIntent = new Intent( ACTION_COMPLETE );
    			completeIntent.putExtra( EXTRA_COMPLETE, "Coverart Download Service Finished" );
    			sendBroadcast( completeIntent );
    		}
		}
		
		Log.d( TAG, "onHandleIntent : exit" );
	}

	// internal helpers
	
	private void download( Intent intent ) throws Exception {
		Log.v( TAG, "download : enter" );
		
		String inetref = intent.getStringExtra( COVERART_INETREF );
		String title = intent.getStringExtra( COVERART_TITLE );
		String encodedTitle = UrlUtils.encodeUrl( title );
		
		File programGroupDirectory = mFileHelper.getProgramGroupDirectory( title );

		File coverartExists = new File( programGroupDirectory, COVERART_FILE );
		if( coverartExists.exists() ) {
			Log.v( TAG, "download : exit, coverart exists" );
			
			return;
		}
		
		boolean coverartNotAvailable = false;
		File checkImageNA = new File( programGroupDirectory, COVERART_FILE_NA );
		if( checkImageNA.exists() ) {
			coverartNotAvailable = true;
		}

		File programGroupJson = new File( programGroupDirectory, encodedTitle + ProgramGroupRecordedDownloadService.RECORDED_FILE );

		Programs programs = null; 
		InputStream is = null;
		try {
			is = new BufferedInputStream( new FileInputStream( programGroupJson ), 8192 );
			programs = mMainApplication.getObjectMapper().readValue( is, Programs.class );
		} catch( FileNotFoundException e ) {
			Log.e( TAG, "onProgramGroupSelected : error, json could not be found", e );
		} catch( JsonParseException e ) {
			Log.e( TAG, "onProgramGroupSelected : error, json could not be parsed", e );
		} catch( JsonMappingException e ) {
			Log.e( TAG, "onProgramGroupSelected : error, json could not be mapped", e );
		} catch( IOException e ) {
			Log.e( TAG, "onProgramGroupSelected : error, io exception reading file", e );
		}

		Map<Integer, String> coverarts = new HashMap<Integer, String>();
		coverarts.put( -1, "" );
		
		for( Program program : programs.getPrograms() ) {
			if( null != program.getSeason() && !"".equals( program.getSeason() ) ) {
				if( Integer.parseInt( program.getSeason() ) > 0 ) {
					coverarts.put( Integer.parseInt( program.getSeason() ), "s" + program.getSeason() + "_" );
				}
			}
		}
		
		for( int season : coverarts.keySet() ) {
			String filename = coverarts.get( season );
			
			File coverart = new File( programGroupDirectory, filename + COVERART_FILE );
			if( !coverart.exists() && !coverartNotAvailable ) {
					
				try {
					ETagInfo eTag = ETagInfo.createEmptyETag();
					ResponseEntity<byte[]> responseEntity = mMainApplication.getMythServicesApi().contentOperations().getRecordingArtwork( "Coverart", inetref, season, -1, -1, eTag );
					if( responseEntity.getStatusCode().equals( HttpStatus.OK ) ) {
						byte[] bytes = responseEntity.getBody();
						Bitmap bitmap = BitmapFactory.decodeByteArray( bytes, 0, bytes.length );

						String name = coverart.getAbsolutePath();
						FileOutputStream fos = new FileOutputStream( name );
						bitmap.compress( Bitmap.CompressFormat.PNG, 100, fos );
						fos.flush();
						fos.close();
						
						Log.v( TAG, "download : downloaded coverart file '" + coverart.getName() + "' in program group '" + title + "'" );
					}
				} catch( Exception e ) {
					Log.e( TAG, "download : error creating image file", e );

					File bannerNA = new File( programGroupDirectory, filename + COVERART_FILE_NA );
					if( !bannerNA.exists() ) {
						try {
							bannerNA.createNewFile();
						} catch( IOException e1 ) {
							Log.e( TAG, "download : error creating image na file", e1 );
							
							throw new Exception( e1 );
						}
					}
				}

			}
		}

		Log.v( TAG, "download : exit" );
	}
	
}
