package org.neo4j.webadmin.backup;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.rest.domain.Representation;

public class BackupLogEntry implements Representation
{

    public static final int NEED_FOUNDATION_CODE = 100;

    public static final String DATE_KEY = "timestamp";
    public static final String TYPE_KEY = "type";
    public static final String MESSAGE_KEY = "message";
    public static final String CODE_KEY = "code";
    public static final String JOB_ID_KEY = "jobId";

    public enum Type
    {
        ERROR,
        SUCCESSFUL_BACKUP,
        INFO
    }

    private Date date;
    private Type type;
    private String message;
    private Integer jobId;
    private Integer code;

    public BackupLogEntry( Date date, Type type, String message, Integer jobId,
            Integer code )
    {
        this.date = date;
        this.type = type;
        this.message = message;
        this.jobId = jobId;
        this.code = code;
    }

    public Object serialize()
    {
        Map<String, Object> serial = new HashMap<String, Object>();

        serial.put( DATE_KEY, date.getTime() );
        serial.put( TYPE_KEY, type );
        serial.put( MESSAGE_KEY, message );
        serial.put( JOB_ID_KEY, jobId );
        serial.put( CODE_KEY, code );

        return serial;
    }

    public static BackupLogEntry deserialize( Map<String, Object> data )
    {
        Date date = new Date( (Long) data.get( DATE_KEY ) );
        String message = (String) data.get( MESSAGE_KEY );
        Type type = getType( (String) data.get( TYPE_KEY ) );
        Integer jobId = (Integer) data.get( JOB_ID_KEY );

        Integer code = -1;
        if ( data.containsKey( CODE_KEY ) )
        {
            code = (Integer) data.get( CODE_KEY );
        }

        return new BackupLogEntry( date, type, message, jobId, code );
    }

    private static Type getType( String typeString )
    {
        if ( typeString.equalsIgnoreCase( Type.SUCCESSFUL_BACKUP.toString() ) )
        {
            return Type.SUCCESSFUL_BACKUP;
        }
        else if ( typeString.equalsIgnoreCase( Type.INFO.toString() ) )
        {
            return Type.INFO;
        }
        else
        {
            return Type.ERROR;
        }
    }

}
