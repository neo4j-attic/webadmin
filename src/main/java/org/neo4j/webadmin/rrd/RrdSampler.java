package org.neo4j.webadmin.rrd;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.management.Cache;
import org.neo4j.kernel.management.Kernel;
import org.neo4j.kernel.management.LockManager;
import org.neo4j.kernel.management.MemoryMapping;
import org.neo4j.kernel.management.Primitives;
import org.neo4j.kernel.management.StoreFile;
import org.neo4j.kernel.management.TransactionManager;
import org.neo4j.kernel.management.XaManager;
import org.neo4j.webadmin.utils.GraphDatabaseUtils;
import org.rrd4j.core.Sample;

/**
 * Manages sampling the state of the database and storing the samples in a round
 * robin database instance.
 * 
 * To add other data points, or change how the sampling is done, look at
 * {@link #updateSample(Sample)}.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class RrdSampler
{
    // DATA SOURCE HANDLES

    /**
     * The current sampling object. This is created when calling #start().
     */
    private static Sample SAMPLE;

    /**
     * Update task. This is is triggered on a regular interval to record new
     * data points.
     */
    private static TimerTask UPDATE_TASK = new TimerTask()
    {
        public void run()
        {
            if ( !RUNNING )
            {
                this.cancel();
            }
            else
            {
                updateSample( SAMPLE );
            }
        }
    };

    private static EmbeddedGraphDatabase db = GraphDatabaseUtils.getLocalDatabase();

    // MANAGEMENT BEANS

    private static Cache cacheBean = db.getManagementBean( Cache.class );
    private static Primitives primitivesBean = db.getManagementBean( Primitives.class );
    private static Kernel kernelBean = db.getManagementBean( Kernel.class );
    private static LockManager lockManagerBean = db.getManagementBean( LockManager.class );
    private static MemoryMapping memoryMappingBean = db.getManagementBean( MemoryMapping.class );
    private static StoreFile storFileBean = db.getManagementBean( StoreFile.class );
    private static TransactionManager transactionManagerBean = db.getManagementBean( TransactionManager.class );
    private static XaManager xaManagerBean = db.getManagementBean( XaManager.class );

    /**
     * Keep track of whether to run the update task or not.
     */
    private static boolean RUNNING = false;

    /**
     * Start the data collecting, creating a central round-robin database if one
     * does not exist.
     */
    public static void start()
    {
        try
        {
            if ( RUNNING == false )
            {
                RUNNING = true;
                SAMPLE = RrdManager.getRrdDB().createSample();
                Timer timer = new Timer( "rrd" );

                timer.scheduleAtFixedRate( UPDATE_TASK, 0, 3000 );
            }
        }
        catch ( IOException e )
        {
            throw new RuntimeException(
                    "IO Error trying to access round robin database path. See nested exception.",
                    e );
        }
    }

    /**
     * Stop the data collecting.
     */
    public static void stop()
    {
        RUNNING = false;
    }

    //
    // INTERNALS
    //

    /**
     * This method is called each time we want a snapshot of the current system
     * state. Data sources to work with are defined in
     * {@link RrdManager#getRrdDB()}
     */
    private static void updateSample( Sample sample )
    {
        try
        {
            sample.setTime( new Date().getTime() );

            sample.setValue( RrdManager.NODE_CACHE_SIZE,
                    cacheBean.getNodeCacheSize() );

            sample.setValue( RrdManager.NODE_COUNT,
                    primitivesBean.getNumberOfNodeIdsInUse() );

            sample.setValue( RrdManager.RELATIONSHIP_COUNT,
                    primitivesBean.getNumberOfRelationshipIdsInUse() );

            sample.setValue( RrdManager.PROPERTY_COUNT,
                    primitivesBean.getNumberOfPropertyIdsInUse() );

            sample.update();
        }
        catch ( IOException e )
        {
            throw new RuntimeException(
                    "IO Error trying to access round robin database path. See nested exception.",
                    e );
        }
    }
}
