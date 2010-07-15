package org.neo4j.webadmin.gremlin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tinkerpop.gremlin.GremlinEvaluator;
import com.tinkerpop.gremlin.statements.EvaluationException;
import com.tinkerpop.gremlin.statements.SyntaxException;

/**
 * A wrapper that connects a given gremlin instance to some specific web client.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class GremlinSession
{

    /**
     * Keep track of the last time this was used.
     */
    protected Date lastTimeUsed = new Date();

    /**
     * The gremlin evaluator instance beeing wrapped.
     */
    protected GremlinEvaluator gremlin = GremlinFactory.createGremlinEvaluator();

    //
    // PUBLIC
    //

    /**
     * Evaluate a gremlin statement.
     */
    @SuppressWarnings( "unchecked" )
    public synchronized List<String> evaluate( String line )
    {
        try
        {
            this.lastTimeUsed = new Date();
            List<Object> resultLines = gremlin.evaluate( line );

            
            List<String> outputLines = new ArrayList<String>();
            if( resultLines == null) {
                outputLines.add("null");
            } else {
                // Make sure all lines are strings
                for ( Object resultLine : resultLines )
                {
                    outputLines.add( resultLine.toString() );
                }
            }

            return outputLines;
        }
        catch ( SyntaxException e )
        {
            return exceptionToResultList(e);
        }
        catch ( EvaluationException e )
        {
            return exceptionToResultList(e);
        }
    }

    /**
     * Destroy the internal gremlin evaluator and replace it with a clean slate.
     */
    public synchronized void reset()
    {
        this.gremlin = GremlinFactory.createGremlinEvaluator();
    }

    /**
     * Get the number of milliseconds this worker has been idle.
     */
    public long getIdleTime()
    {
        return ( new Date() ).getTime() - lastTimeUsed.getTime();
    }
    
    //
    // INTERNALS
    //
    
    protected List<String> exceptionToResultList(Exception e) {
        ArrayList<String> resultList = new ArrayList<String>();
        
        resultList.add(e.getMessage());
        
        for(StackTraceElement stackTraceElement : e.getStackTrace()) {
            resultList.add(stackTraceElement.toString());
        }
        
        return resultList;
    }

}
