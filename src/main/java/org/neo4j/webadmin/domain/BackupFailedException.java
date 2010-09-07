package org.neo4j.webadmin.domain;

public class BackupFailedException extends Exception
{
    /**
     * Serial #
     */
    private static final long serialVersionUID = -7766801456616236010L;

    public BackupFailedException()
    {
        super();
    }

    public BackupFailedException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public BackupFailedException( String message )
    {
        super( message );
    }

    public BackupFailedException( Throwable cause )
    {
        super( cause );
    }
}
