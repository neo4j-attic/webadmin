package org.neo4j.webadmin.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.rest.domain.Representation;
import org.neo4j.webadmin.domain.ServerPropertyRepresentation.PropertyType;

/**
 * Allows changing two out of the threee available types of properties that
 * affects a neo4j instance. Ie. the properties file and the JVM arguments. This
 * may allow for changing db creation settings in the future, but currently
 * throws operationnotsupportedexception if you try.
 * 
 * This class also contains a hard-coded definition of what settings are to be
 * available to the client, and how to handle them.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class ServerProperties implements Representation
{
    public static final String SERVICE_CONFIG_PATH = "./conf/wrapper.conf";
    public static final String FALLBACK_MAX_HEAP = "512M";
    public static final String FALLBACK_MIN_HEAP = "512M";

    /**
     * This is the hard-coded settings that will be exposed to the user.
     * 
     * Each property should have a unique key, for neo4j config file and server
     * creation properties this should be the property name. For JVM args it is
     * irrellevant, but it is recommended to name them something like
     * "jvm.myproperty".
     * 
     * A note on how values are fetched:
     * 
     * For CONFIG_PROPERTY properties, the value will be fetched from the neo4j
     * config file. If no property with a matching key is found, the default
     * value you speciSizefy will be used.
     * 
     * For JVM_ARGUMENT and DB_CREATION_PROPERTY the value will be fetched from
     * a separate config file, startup.properties in the neo4j database folder.
     * If no property with a matching key is found, the default value you
     * specify will be used.
     * 
     */
    protected static ArrayList<ServerPropertyRepresentation> createProperties()
    {
        ArrayList<ServerPropertyRepresentation> properties = new ArrayList<ServerPropertyRepresentation>();

        // JVM ARGS

        properties.add( new ServerPropertyRepresentation(
                "jvm.garbagecollector", "Garbage collector",
                "-XX:+UseSerialGC", PropertyType.JVM_ARGUMENT ) );

        properties.add( new ServerPropertyRepresentation( "jvm.heapsize",
                "Heap size", "-Xmx512m", PropertyType.JVM_ARGUMENT ) );

        properties.add( new ServerPropertyRepresentation( "web.root",
                "Web root", "-DwebRoot=../public", PropertyType.JVM_ARGUMENT ) );

        // CONFIG FILE ARGS

        properties.add( new ServerPropertyRepresentation( "keep_logical_logs",
                "Enable logical logs", "false", PropertyType.CONFIG_PROPERTY ) );

        properties.add( new ServerPropertyRepresentation(
                "enable_remote_shell", "Enable remote shell", "false",
                PropertyType.CONFIG_PROPERTY ) );

        // DB CREATION ARGS

        properties.add( new ServerPropertyRepresentation(
                "create.array_block_size", "Array block size", "133",
                PropertyType.DB_CREATION_PROPERTY ) );

        properties.add( new ServerPropertyRepresentation(
                "create.string_block_size", "String block size", "133",
                PropertyType.DB_CREATION_PROPERTY ) );

        // GENERAL SETTINGS

        properties.add( new ServerPropertyRepresentation(
                "general.backup.path", "Backup path", "",
                PropertyType.GENERAL_PROPERTY ) );

        return properties;

    }

    /**
     * Get database config file, creating one if it does not exist.
     * 
     * @return
     * @throws IOException
     */
    public static File getDbConfigFile() throws IOException
    {
        File configFile = new File( new File( DatabaseLocator.DB_PATH ),
                "neo4j.properties" );

        if ( !configFile.exists() && !configFile.createNewFile() )
        {
            throw new IllegalStateException( configFile.toString() );
        }

        return configFile;
    }

    /**
     * Get startup config file, creating one if it does not exist.
     * 
     * @return
     * @throws IOException
     */
    public static File getGeneralConfigFile() throws IOException
    {

        File configFile = new File( new File( DatabaseLocator.DB_PATH ),
                "startup.properties" );

        if ( !configFile.exists() && !configFile.createNewFile() )
        {
            throw new IllegalStateException( configFile.toString() );
        }

        return configFile;
    }

    /**
     * Get file that stores JVM startup arguments during development.
     * 
     * @return
     * @throws IOException
     */
    public static File getDevelopmentJvmArgsFile() throws IOException
    {

        File configFile = new File( new File( DatabaseLocator.DB_PATH ),
                "jvmargs" );

        if ( !configFile.exists() && !configFile.createNewFile() )
        {
            throw new IllegalStateException( configFile.toString() );
        }

        return configFile;
    }

    /**
     * Get the service configuration file, this is where JVM args are changed
     * when running in production. This method is slightly different from the
     * above, it does not try to create a file if one does not exist. It assumes
     * that if there is no service config file, the environment we're running in
     * is not the production service env.
     * 
     * @return the service file or null if no file exists.
     * @throws IOException
     */
    public static File getServiceConfigFile() throws IOException
    {

        File configFile = new File( SERVICE_CONFIG_PATH );

        if ( !configFile.exists() )
        {
            return null;
        }

        return configFile;
    }

    /**
     * Somewhat cumbersome singleton implementation to allow IOException to
     * bubble up nicely.
     * 
     * @return
     * @throws IOException
     */
    public static ServerProperties getInstance() throws IOException
    {
        if ( INSTANCE == null )
        {
            INSTANCE = new ServerProperties();
        }
        return INSTANCE;
    }

    /**
     * This is the properties file that is read and used by the underlying neo4j
     * instance.
     */
    protected static volatile Properties dbConfig;

    /**
     * This property file stores configuration used when launching the JVM and
     * when creating a new database.
     */
    protected static volatile Properties generalConfig;

    protected static ServerProperties INSTANCE;

    protected ArrayList<ServerPropertyRepresentation> properties = createProperties();

    //
    // CONSTRUCT
    //

    protected ServerProperties() throws IOException
    {
        if ( dbConfig == null )
        {
            dbConfig = new Properties();
            FileInputStream in = new FileInputStream( getDbConfigFile() );
            dbConfig.load( in );
            in.close();
        }

        if ( generalConfig == null )
        {
            generalConfig = new Properties();
            FileInputStream in = new FileInputStream( getGeneralConfigFile() );
            generalConfig.load( in );
            in.close();
        }

        // Go through properties and set them to their current values
        for ( ServerPropertyRepresentation prop : properties )
        {
            switch ( prop.getType() )
            {
            case CONFIG_PROPERTY:
                if ( dbConfig.containsKey( prop.getKey() ) )
                {
                    prop.setValue( dbConfig.getProperty( prop.getKey() ) );
                }
            default:
                if ( generalConfig.containsKey( prop.getKey() ) )
                {
                    prop.setValue( generalConfig.getProperty( prop.getKey() ) );
                }
            }
        }
    }

    //
    // PUBLIC
    //

    public Object serialize()
    {
        ArrayList<Object> serial = new ArrayList<Object>();
        for ( ServerPropertyRepresentation prop : properties )
        {
            serial.add( prop.serialize() );
        }
        return serial;
    }

    /**
     * Change a setting. This applies immediately, but does not restart any
     * running server instance.
     * 
     * @param key
     * @param value
     * @throws IOException if writing changes to disk fails
     * @throws NoSuchPropertyException if no property with the given key exists.
     * @throws IllegalArgumentException if trying to set an invalid value
     */
    public void set( String key, String value ) throws IOException
    {
        ServerPropertyRepresentation prop = this.get( key );

        if ( !prop.isValidValue( value ) )
        {
            throw new IllegalArgumentException(
                    "'" + value + "' is not a valid value for property '" + key
                            + "'." );
        }

        // Update the value in property object
        prop.setValue( value );

        switch ( prop.getType() )
        {
        case CONFIG_PROPERTY:
            dbConfig.put( key, value );
            saveProperties( dbConfig, getDbConfigFile() );
            break;
        default:
            generalConfig.put( key, value );
            saveProperties( generalConfig, getGeneralConfigFile() );
            writeJvmArgs();
        }
    }

    /**
     * Get a property by key.
     * 
     * @param key
     * @return
     * @throws NoSuchPropertyException if no property with the given key exists.
     */
    public ServerPropertyRepresentation get( String key )
    {
        for ( ServerPropertyRepresentation prop : properties )
        {
            if ( prop.key.equals( key ) )
            {
                return prop;
            }
        }

        throw new NoSuchPropertyException( "Property with key '" + key
                                           + "' does not exist." );
    }

    //
    // INTERNALS
    //

    protected void saveProperties( Properties prop, File file )
            throws IOException
    {
        FileOutputStream out = new FileOutputStream( file );
        prop.store( out, "--Changed via admin gui--" );
        out.close();
    }

    /**
     * Update the file with extra args passed to the JVM to match the currently
     * set startup args.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    protected synchronized void writeJvmArgs() throws FileNotFoundException,
            IOException
    {
        File serviceConfig = getServiceConfigFile();

        if ( serviceConfig != null )
        {
            // PRODUCTION MODE

            // So uh. This is not very nice.
            // The reason for this is to a) conserve config file formatting and
            // b) config file is not a "normal" properties file, and escaping
            // special
            // chars etc. that is done by Properties will screw up running the
            // service. So we have to do some manual labour here.

            // This will contain the end-result config file
            StringBuilder configFileBuilder = new StringBuilder();

            // We need to keep track of heap settings, b/c they are for some
            // reason not passed as pure command-line arguments in the config
            // file
            String maxHeap = null, minHeap = null;

            // Read the whole config file, discard any pre-existing JVM args
            FileReader in = new FileReader( serviceConfig );
            BufferedReader br = new BufferedReader( in );
            String line;
            while ( ( line = br.readLine() ) != null )
            {
                if ( !line.startsWith( "wrapper.java.additional" ) )
                {

                    if ( line.startsWith( "wrapper.java.maxmemory" ) )
                    {
                        maxHeap = line.substring( 23 );
                    }
                    else if ( line.startsWith( "wrapper.java.initmemory" ) )
                    {
                        minHeap = line.substring( 23 );
                    }
                    else
                    {

                        configFileBuilder.append( line );
                        configFileBuilder.append( "\n" );
                    }
                }
            }

            in.close();

            // Add admin-defined jvm arguments
            int argNo = 1;
            for ( ServerPropertyRepresentation prop : properties )
            {
                if ( prop.getType() == ServerPropertyRepresentation.PropertyType.JVM_ARGUMENT )
                {
                    // Heap memory size is a special case parameter in the
                    // service config file.
                    if ( prop.getValue().toLowerCase().startsWith( "-xmx" ) )
                    {
                        maxHeap = prop.getValue().substring( 4 );
                    }
                    else if ( prop.getValue().toLowerCase().startsWith( "-xms" ) )
                    {
                        minHeap = prop.getValue().substring( 4 );
                    }
                    else
                    {
                        // Write normal JVM arg
                        configFileBuilder.append( "wrapper.java.additional." );
                        configFileBuilder.append( ( argNo++ ) );
                        configFileBuilder.append( "=" );
                        configFileBuilder.append( prop.getValue() );
                        configFileBuilder.append( "\n" );
                    }
                }
            }

            // Handle heap special cases
            maxHeap = ( maxHeap == null ) ? FALLBACK_MAX_HEAP : maxHeap;
            minHeap = ( minHeap == null ) ? FALLBACK_MIN_HEAP : minHeap;

            configFileBuilder.append( "wrapper.java.maxmemory=" + maxHeap
                                      + "\n" );
            configFileBuilder.append( "wrapper.java.initmemory=" + minHeap
                                      + "\n" );

            // Write changes to file.
            FileOutputStream out = new FileOutputStream( serviceConfig );
            out.write( configFileBuilder.toString().getBytes() );
            out.close();
        }
        else
        {
            // DEVELOPMENT MODE
            FileOutputStream out = new FileOutputStream(
                    getDevelopmentJvmArgsFile() );
            StringBuilder args = new StringBuilder();

            for ( ServerPropertyRepresentation prop : properties )
            {
                if ( prop.getType() == ServerPropertyRepresentation.PropertyType.JVM_ARGUMENT )
                {
                    args.append( prop.getValue() );
                    args.append( " " );
                }
            }

            out.write( args.toString().getBytes() );
            out.close();
        }
    }
}
