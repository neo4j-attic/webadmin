package org.neo4j.webadmin.gremlin;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * A wrapper thread for a given gremlin instance. Webadmin spawns one of these
 * threads for each client that uses the gremlin console.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@SuppressWarnings( "restriction" )
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
     * The scriptengine error and out streams are directed into this string
     * writer.
     */
    protected StringWriter scriptOutput = new StringWriter();

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
                    scriptEngine.getContext().setWriter( scriptOutput );
                    scriptEngine.getContext().setErrorWriter( scriptOutput );
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

            // Handle output data
            List<String> outputLines = new ArrayList<String>();

            // Handle eval() result
            if ( resultLines == null
                 || resultLines.size() == 0
                 || ( resultLines.size() == 1 && resultLines.get( 0 ).toString().length() == 0 ) )
            {
                outputLines.add( "" );
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
        catch ( ScriptException e )
        {
            return exceptionToResultList( e );
        }
        catch ( RuntimeException e )
        {
            e.printStackTrace();
            return exceptionToResultList( e );
        }
        // TODO: Make sure RuntimeExceptions are wrapped by ScriptExceptionscd
    }

    protected List<String> exceptionToResultList( Exception e )
    {
        ArrayList<String> resultList = new ArrayList<String>();

        resultList.add( e.getMessage() );

        return resultList;
    }

}
