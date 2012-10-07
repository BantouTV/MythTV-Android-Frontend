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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.mythtv.service.MythtvService;
import org.mythtv.service.dvr.cache.RecordedLruMemoryCache;
import org.mythtv.service.util.UrlUtils;
import org.mythtv.services.api.ETagInfo;
import org.mythtv.services.api.dvr.Program;
import org.mythtv.services.api.dvr.ProgramList;
import org.mythtv.services.api.dvr.Programs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import android.content.Intent;
import android.util.Log;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * @author Daniel Frey
 *
 */
public class ProgramGroupRecordedDownloadService extends MythtvService {

	private static final String TAG = ProgramGroupRecordedDownloadService.class.getSimpleName();

	public static final String RECORDED_FILE = "-recorded.json";
	
    public static final String ACTION_DOWNLOAD = "org.mythtv.background.programGroupRecordedDownload.ACTION_DOWNLOAD";
    public static final String ACTION_PROGRESS = "org.mythtv.background.programGroupRecordedDownload.ACTION_PROGRESS";
    public static final String ACTION_COMPLETE = "org.mythtv.background.programGroupRecordedDownload.ACTION_COMPLETE";

    public static final String EXTRA_PROGRESS = "PROGRESS";
    public static final String EXTRA_PROGRESS_TITLE = "PROGRESS_TITLE";
    public static final String EXTRA_PROGRESS_ERROR = "PROGRESS_ERROR";
    public static final String EXTRA_COMPLETE = "COMPLETE";

    private RecordedLruMemoryCache cache;
    
	public ProgramGroupRecordedDownloadService() {
		super( "ProgramGroupRecordedDownloadService" );
		
		cache = new RecordedLruMemoryCache( this );
	}
	
	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent( Intent intent ) {
		Log.d( TAG, "onHandleIntent : enter" );
		super.onHandleIntent( intent );
		
        if ( intent.getAction().equals( ACTION_DOWNLOAD ) ) {
    		Log.i( TAG, "onHandleIntent : DOWNLOAD action selected" );

    		download();
        }
		
		Log.d( TAG, "onHandleIntent : exit" );
	}

	// internal helpers
	
	private void download() {
//		Log.v( TAG, "download : enter" );
		
		boolean newDataDownloaded = false;
		
		DateTime start = new DateTime();
		start = start.withTime( 0, 0, 0, 0 );
//		Log.d( TAG, "download : start="+ dateTimeFormatter.print( start ) );
		
		List<String> programGroups = new ArrayList<String>();
		Programs programs = cache.get( RecordedDownloadService.RECORDED_FILE );
		if( null != programs ) {
			for( Program program : programs.getPrograms() ) {
				if( !programGroups.contains( program.getTitle() ) ) {
					programGroups.add( program.getTitle() );
				}
			}
			
			File programCache = mFileHelper.getProgramDataDirectory();
			if( null != programCache && programCache.exists() ) {
				
				for( String title : programGroups ) {
					title = UrlUtils.encodeUrl( title );
					
					ETagInfo etag = ETagInfo.createEmptyETag();
					ResponseEntity<ProgramList> responseEntity = mMainApplication.getMythServicesApi().dvrOperations().getFiltererRecordedList( true, 1, 999, title, null, null, etag );
					if( responseEntity.getStatusCode().equals( HttpStatus.OK ) ) {
						
						Intent progressIntent = new Intent( ACTION_PROGRESS );

						File existing = new File( programCache, title + RECORDED_FILE );
						if( existing.exists() ) {
							existing.delete();
						}

						try {
							ProgramList programList = responseEntity.getBody();
							
							mObjectMapper.writeValue( new File( programCache, title + RECORDED_FILE ), programList.getPrograms() );

							newDataDownloaded = true;

							progressIntent.putExtra( EXTRA_PROGRESS, "downloaded file for " + title + "-recorded'"  );
							progressIntent.putExtra( EXTRA_PROGRESS_TITLE, title );
						} catch( JsonGenerationException e ) {
							Log.e( TAG, "download : JsonGenerationException - error downloading file for 'recorded'", e );

							progressIntent.putExtra( EXTRA_PROGRESS_ERROR, "error downloading file for 'recorded': " + e.getLocalizedMessage() );
						} catch( JsonMappingException e ) {
							Log.e( TAG, "download : JsonGenerationException - error downloading file for 'recorded'", e );

							progressIntent.putExtra( EXTRA_PROGRESS_ERROR, "error downloading file for 'recorded': " + e.getLocalizedMessage() );
						} catch( IOException e ) {
							Log.e( TAG, "download : JsonGenerationException - error downloading file for 'recorded'", e );

							progressIntent.putExtra( EXTRA_PROGRESS_ERROR, "IOException - error downloading file for 'recorded': " + e.getLocalizedMessage() );
						}

						sendBroadcast( progressIntent );

					}
				}
				
			}
			
		}
		
		if( newDataDownloaded ) {
			Intent completeIntent = new Intent( ACTION_COMPLETE );
			completeIntent.putExtra( EXTRA_COMPLETE, "Program Group Recorded Download Service Finished" );
			sendBroadcast( completeIntent );
		}
		
//		Log.v( TAG, "download : exit" );
	}
	
}
