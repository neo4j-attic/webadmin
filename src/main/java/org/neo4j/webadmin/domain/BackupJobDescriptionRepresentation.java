package org.neo4j.webadmin.domain;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.rest.domain.Representation;
import org.neo4j.webadmin.backup.BackupJobDescription;

public class BackupJobDescriptionRepresentation implements Representation
{

    public static final String NAME_KEY = "name";
    public static final String CRON_EXPRESSION_KEY = "cronExpression";
    public static final String AUTO_FOUNDATION_KEY = "autoFoundation";
    public static final String BACKUP_PATH_KEY = "backupPath";

    BackupJobDescription wrapped;

    public BackupJobDescriptionRepresentation( BackupJobDescription jobDesc )
    {
        this.wrapped = jobDesc;
    }

    public Object serialize()
    {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put( NAME_KEY, wrapped.getName() );
        data.put( CRON_EXPRESSION_KEY, wrapped.getCronExpression() );
        data.put( AUTO_FOUNDATION_KEY, wrapped.isAutoFoundation() );
        data.put( BACKUP_PATH_KEY, wrapped.getPath() );

        return data;
    }

    public static BackupJobDescription deserialize( Map<String, Object> data )
    {
        BackupJobDescription desc = new BackupJobDescription();

        desc.setName( (String) data.get( NAME_KEY ) );
        desc.setCronExpression( (String) data.get( CRON_EXPRESSION_KEY ) );
        desc.setPath( (String) data.get( BACKUP_PATH_KEY ) );
        desc.setAutoFoundation( data.get( AUTO_FOUNDATION_KEY ).equals( "true" ) );

        return desc;
    }
}
