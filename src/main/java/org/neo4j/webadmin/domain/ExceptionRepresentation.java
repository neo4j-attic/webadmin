package org.neo4j.webadmin.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.rest.domain.Representation;

public class ExceptionRepresentation implements Representation
{

    private Exception exception;

    public ExceptionRepresentation( Exception e )
    {
        this.exception = e;
    }

    public Object serialize()
    {
        Map<String, Object> repr = new HashMap<String, Object>();

        repr.put( "exception", exception.getClass().getSimpleName() );
        repr.put( "message", exception.getMessage() );

        List<String> trace = new ArrayList<String>();
        for ( StackTraceElement elem : exception.getStackTrace() )
        {
            trace.add( elem.toString() );
        }

        repr.put( "trace", trace );

        return repr;
    }

}
