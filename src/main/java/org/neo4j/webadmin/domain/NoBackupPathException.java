package org.neo4j.webadmin.domain;

public class NoBackupPathException extends RuntimeException
{
    /**
     * Serial #
     */
    private static final long serialVersionUID = -7766432456616236010L;

    public NoBackupPathException()
    {
        super();
    }

    public NoBackupPathException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NoBackupPathException( String message )
    {
        super( message );
    }

    public NoBackupPathException( Throwable cause )
    {
        super( cause );
    }
}
