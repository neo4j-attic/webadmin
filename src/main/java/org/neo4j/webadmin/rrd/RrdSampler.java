package org.neo4j.webadmin.rrd;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.management.Kernel;
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
@SuppressWarnings( "restriction" )
public class RrdSampler
{

    //
    // SINGLETON IMPLEMENTATION
    //

    public static final RrdSampler INSTANCE = new RrdSampler();

    // JMX bean names
    private static final String JMX_NEO4J_PRIMITIVE_COUNT = "Primitive count";
    private static final String JMX_NEO4J_STORE_FILE_SIZES = "Store file sizes";
    private static final String JMX_NEO4J_MEMORY_MAPPING = "Memory Mapping";
    private static final String JMX_NEO4J_TRANSACTIONS = "Transactions";
    private static final String JMX_NEO4J_KERNEL = "Kernel";
    private static final String JMX_NEO4J_LOCKING = "Locking";
    private static final String JMX_NEO4J_CACHE = "Cache";
    private static final String JMX_NEO4J_CONFIGURATION = "Configuration";
    private static final String JMX_NEO4J_XA_RESOURCES = "XA Resources";

    // DATA SOURCE HANDLES

    /**
     * The current sampling object. This is created when calling #start().
     */
    private Sample sample;

    /**
     * Update task. This is is triggered on a regular interval to record new
     * data points.
     */
    private TimerTask updateTask = new TimerTask()
    {
        public void run()
        {
            if ( !running )
            {
                this.cancel();
            }
            else
            {
                updateSample( sample );
            }
        }
    };

    private EmbeddedGraphDatabase db = GraphDatabaseUtils.getLocalDatabase();

    // MANAGEMENT BEANS

    private MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private MBeanInfo primitivesMBean;
    private MBeanInfo storeSizesMBean;
    private MBeanInfo transactionsMBean;
    private MBeanInfo memoryMappingMBean;
    private MBeanInfo kernelMBean;
    private MBeanInfo lockingMBean;
    private MBeanInfo cacheMBean;
    private MBeanInfo configurationMBean;
    private MBeanInfo xaResourcesMBean;

    /**
     * Keep track of whether to run the update task or not.
     */
    private boolean running = false;

    //
    // CONSTRUCTOR
    //

    @SuppressWarnings( { "restriction" } )
    protected RrdSampler()
    {
        try
        {
            // Grab relevant jmx management beans

            ObjectName neoQuery = db.getManagementBean( Kernel.class ).getMBeanQuery();

            for ( ObjectName objectName : server.queryNames( neoQuery, null ) )
            {
                if ( objectName.getKeyProperty( "name" ).equals(
                        JMX_NEO4J_PRIMITIVE_COUNT ) )
                {
                    primitivesMBean = server.getMBeanInfo( objectName );
                }
                else if ( objectName.getKeyProperty( "name" ).equals(
                        JMX_NEO4J_STORE_FILE_SIZES ) )
                {
                    storeSizesMBean = server.getMBeanInfo( objectName );
                }
                
                transactionsMBean;
                private MBeanInfo memoryMappingMBean;
                private MBeanInfo kernelMBean;
                private MBeanInfo lockingMBean;
                private MBeanInfo cacheMBean;
                private MBeanInfo configurationMBean;
                private MBeanInfo xaResourcesMBean;
            }
        }
        catch ( IntrospectionException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( InstanceNotFoundException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( ReflectionException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //
    // PUBLIC
    //

    /**
     * Start the data collecting, creating a central round-robin database if one
     * does not exist.
     */
    public void start()
    {
        try
        {
            if ( running == false )
            {
                running = true;
                sample = RrdManager.getRrdDB().createSample();
                Timer timer = new Timer( "rrd" );

                timer.scheduleAtFixedRate( updateTask, 0, 3000 );
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
    public void stop()
    {
        running = false;
    }

    //
    // INTERNALS
    //

    /**
     * This method is called each time we want a snapshot of the current system
     * state. Data sources to work with are defined in
     * {@link RrdManager#getRrdDB()}
     */
    private void updateSample( Sample sample )
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
