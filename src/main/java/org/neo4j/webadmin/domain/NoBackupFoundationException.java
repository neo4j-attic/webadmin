package org.neo4j.webadmin.domain;

public class NoBackupFoundationException extends RuntimeException
{
    /**
     * Serial #
     */
    private static final long serialVersionUID = -1166432456616236010L;

    public NoBackupFoundationException()
    {
        super();
    }

    public NoBackupFoundationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NoBackupFoundationException( String message )
    {
        super( message );
    }

    public NoBackupFoundationException( Throwable cause )
    {
        super( cause );
    }
}
