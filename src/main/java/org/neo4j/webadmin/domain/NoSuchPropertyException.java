package org.neo4j.webadmin.domain;

/**
 * Thrown when trying to set a non-existant property.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class NoSuchPropertyException extends RuntimeException
{
    /**
     * Serial #
     */
    private static final long serialVersionUID = -7766801456616236010L;

    public NoSuchPropertyException()
    {
        super();
    }

    public NoSuchPropertyException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NoSuchPropertyException( String message )
    {
        super( message );
    }

    public NoSuchPropertyException( Throwable cause )
    {
        super( cause );
    }
}
