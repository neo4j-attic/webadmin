package org.neo4j.webadmin.task;

/**
 * This is used primarily to perform long-running tasks without having to keep
 * web clients waiting, or to perform some task that requires the server to be
 * shut down.
 * 
 * The current implementation spawns one thread per deferred task instance,
 * which may not be desirable if you end up spawning lots of tasks. This is a
 * KISS implementation, extend it with some task queueing magic if the need
 * arises.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class DeferredTask implements Runnable
{

    protected Runnable task;
    protected long timeout;

    public static void defer( Runnable task )
    {
        defer( task, 0 );
    }

    public static void defer( Runnable task, long timeout )
    {

        Thread runner = new Thread( new DeferredTask( task, timeout ),
                "Deferred task" );
        runner.start();
    }

    //
    // CONSTRUCT
    //

    protected DeferredTask( Runnable task, long timeout )
    {
        this.timeout = timeout;
        this.task = task;
    }

    //
    // PUBLIC
    //

    public void run()
    {
        try
        {
            Thread.sleep( timeout );
            this.task.run();
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
    }
}
