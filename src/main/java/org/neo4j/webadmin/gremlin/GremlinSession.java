package org.neo4j.webadmin.gremlin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.tinkerpop.gremlin.statements.EvaluationException;
import com.tinkerpop.gremlin.statements.SyntaxException;

/**
 * A wrapper thread for a given gremlin instance. Webadmin spawns one of these
 * threads for each client that uses the gremlin console.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class GremlinSession implements Runnable
{

    public static final int MAX_COMMANDS_WAITING = 128;

    /**
     * Keep track of the last time this was used.
     */
    protected Date lastTimeUsed = new Date();

    /**
     * The gremlin evaluator instance beeing wrapped.
     */
    protected ScriptEngine scriptEngine;

    /**
     * Commands waiting to be executed. Number of waiting commands is capped at
     * 128, since this is meant to be used by a single client.
     */
    protected BlockingQueue<GremlinEvaluationJob> jobQueue = new ArrayBlockingQueue<GremlinEvaluationJob>(
            MAX_COMMANDS_WAITING );

    /**
     * Should I shut down?
     */
    protected boolean sepukko = false;

    /**
     * Mama thread.
     */
    protected Thread runner = new Thread( this, "GremlinSession" );

    //
    // CONSTRUCT
    //

    public GremlinSession()
    {
        runner.start();
    }

    //
    // PUBLIC
    //

    public void run()
    {
        GremlinEvaluationJob job;
        try
        {
            while ( true )
            {
                if ( scriptEngine == null )
                {
                    scriptEngine = GremlinFactory.createGremlinScriptEngine();
                }

                job = jobQueue.take();
                job.setResult( performEvaluation( job.getScript() ) );

                if ( sepukko )
                {
                    break;
                }
            }
        }
        catch ( InterruptedException e )
        {
            // Exit
        }
    }

    /**
     * Take some gremlin script, evaluate it in the context of this gremlin
     * session, and return the result.
     * 
     * @param script
     * @return
     */
    public List<String> evaluate( String script )
    {
        try
        {
            GremlinEvaluationJob job = new GremlinEvaluationJob( script );
            jobQueue.add( job );

            while ( !job.isComplete() )
            {
                Thread.sleep( 10 );
            }

            return job.getResult();
        }
        catch ( InterruptedException e )
        {
            return new ArrayList<String>();
        }
    }

    /**
     * Destroy the internal gremlin evaluator and replace it with a clean slate.
     */
    public synchronized void reset()
    {
        // #run() will pick up on this and create a new script engine. This
        // ensures it is instantiated in the correct thread context.
        this.scriptEngine = null;
    }

    /**
     * Get the number of milliseconds this worker has been idle.
     */
    public long getIdleTime()
    {
        return ( new Date() ).getTime() - lastTimeUsed.getTime();
    }

    public void die()
    {
        this.sepukko = true;
    }

    //
    // INTERNALS
    //

    /**
     * Internal evaluate implementation. This actually interprets a gremlin
     * statement.
     */
    @SuppressWarnings( "unchecked" )
    protected List<String> performEvaluation( String line )
    {
        try
        {
            this.lastTimeUsed = new Date();
            List<Object> resultLines = (List<Object>) scriptEngine.eval( line );

            List<String> outputLines = new ArrayList<String>();
            if ( resultLines == null )
            {
                outputLines.add( "null" );
            }
            else
            {
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
            return exceptionToResultList( e );
        }
        catch ( EvaluationException e )
        {
            return exceptionToResultList( e );
        }
        catch ( ScriptException e )
        {
            return exceptionToResultList( e );
        }
    }

    protected List<String> exceptionToResultList( Exception e )
    {
        ArrayList<String> resultList = new ArrayList<String>();

        resultList.add( e.getMessage() );

        for ( StackTraceElement stackTraceElement : e.getStackTrace() )
        {
            resultList.add( stackTraceElement.toString() );
        }

        return resultList;
    }

}
