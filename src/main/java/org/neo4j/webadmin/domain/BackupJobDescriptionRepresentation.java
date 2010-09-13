package org.neo4j.webadmin.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.rest.domain.Representation;
import org.neo4j.webadmin.backup.BackupJobDescription;
import org.neo4j.webadmin.backup.BackupLog;
import org.neo4j.webadmin.backup.BackupLogEntry;
import org.neo4j.webadmin.backup.BackupManager;

public class BackupJobDescriptionRepresentation implements Representation
{

    public static final String ID_KEY = "id";
    public static final String NAME_KEY = "name";
    public static final String CRON_EXPRESSION_KEY = "cronExpression";
    public static final String AUTO_FOUNDATION_KEY = "autoFoundation";
    public static final String BACKUP_PATH_KEY = "backupPath";
    public static final String LOG_KEY = "log";
    public static final String LOG_ENTRIES_KEY = "entries";
    public static final String LATEST_SUCCESS_LOG_KEY = "latestSuccess";

    private BackupJobDescription wrapped;

    public BackupJobDescriptionRepresentation( BackupJobDescription jobDesc )
    {
        this.wrapped = jobDesc;
    }

    public Object serialize()
    {
        return serialize( true );
    }

    public Object serialize( boolean includeLog )
    {

        Map<String, Object> data = new HashMap<String, Object>();

        data.put( ID_KEY, wrapped.getId() );
        data.put( NAME_KEY, wrapped.getName() );
        data.put( CRON_EXPRESSION_KEY, wrapped.getCronExpression() );
        data.put( AUTO_FOUNDATION_KEY, wrapped.isAutoFoundation() );
        data.put( BACKUP_PATH_KEY, wrapped.getPath() );

        // Add log data

        if ( includeLog )
        {
            BackupLog log = BackupManager.INSTANCE.getLog();
            ArrayList<BackupLogEntry> logEntries = log.getLog( wrapped.getId() );
            BackupLogEntry latestSuccessfulLogEntry = log.getLatestSuccessful( wrapped.getId() );
            ArrayList<Object> serializedLog = new ArrayList<Object>();
            for ( BackupLogEntry entry : logEntries )
            {
                serializedLog.add( entry.serialize() );
            }

            Map<String, Object> logData = new HashMap<String, Object>();
            logData.put( LATEST_SUCCESS_LOG_KEY,
                    latestSuccessfulLogEntry == null ? null
                            : latestSuccessfulLogEntry.serialize() );

            logData.put( LOG_ENTRIES_KEY, serializedLog );

            data.put( LOG_KEY, logData );
        }

        return data;
    }

    public static BackupJobDescription deserialize( Map<String, Object> data )
    {
        BackupJobDescription desc = new BackupJobDescription();

        if ( data.containsKey( ID_KEY ) )
        {
            try
            {
                try
                {
                    desc.setId( (Integer) data.get( ID_KEY ) );
                }
                catch ( ClassCastException e )
                {
                    desc.setId( Integer.valueOf( (String) data.get( ID_KEY ) ) );
                }
            }
            catch ( Exception e )
            {
                desc.setId( null );
            }
        }
        else
        {
            desc.setId( null );
        }

        desc.setName( (String) data.get( NAME_KEY ) );
        desc.setCronExpression( (String) data.get( CRON_EXPRESSION_KEY ) );
        desc.setPath( (String) data.get( BACKUP_PATH_KEY ) );
        desc.setAutoFoundation( (Boolean) data.get( AUTO_FOUNDATION_KEY ) );

        return desc;
    }
}
