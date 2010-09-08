package org.neo4j.webadmin.domain;

/**
 * Thrown when the application has explicitly prohibited database access. This
 * is done when the application does something where it needs to be sure the
 * database stays turned off during operation.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class DatabaseBlockedException extends RuntimeException
{

    /**
     * Serial version
     */
    private static final long serialVersionUID = -2181905163252962004L;

    public DatabaseBlockedException( String message )
    {
        super( message );
    }

}
