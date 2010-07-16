package org.neo4j.webadmin.domain;

public class BackupAlreadyRunningException extends RuntimeException
{
    /**
     * Serial #
     */
    private static final long serialVersionUID = -7766801456616236010L;

    public BackupAlreadyRunningException()
    {
        super();
    }

    public BackupAlreadyRunningException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public BackupAlreadyRunningException( String message )
    {
        super( message );
    }

    public BackupAlreadyRunningException( Throwable cause )
    {
        super( cause );
    }
}
