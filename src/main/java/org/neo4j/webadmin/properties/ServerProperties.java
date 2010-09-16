package org.neo4j.webadmin.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeMap;

import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.rest.domain.Representation;
import org.neo4j.webadmin.AdminServer;
import org.neo4j.webadmin.domain.NoSuchPropertyException;
import org.neo4j.webadmin.domain.ServerPropertyRepresentation;
import org.neo4j.webadmin.domain.ServerPropertyRepresentation.PropertyType;
import org.neo4j.webadmin.utils.PlatformUtils;

/**
 * Definition of properties that affect the neo4j server. This contains both the
 * definition and the load/save code.
 * 
 * This class also contains a hard-coded definition of what settings are to be
 * available to the client, and how to handle them.
 * 
 * TODO: This has far outgrown its initial purpose and is in need of
 * refactoring.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class ServerProperties implements Representation
{

    public static final String FALLBACK_MAX_HEAP = "512M";
    public static final String FALLBACK_MIN_HEAP = "512M";

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

    /**
     * This is the hard-coded settings that will be exposed to the user.
     * 
     * Each property should have a unique key, for neo4j config file and server
     * creation properties this should be the property name. For JVM args it is
     * irrelevant, but it is recommended to name them something like
     * "jvm.myproperty".
     * 
     * A note on how values are fetched:
     * 
     * For CONFIG_PROPERTY properties, the value will be fetched from the neo4j
     * config file. If no property with a matching key is found, the default
     * value you speciSizefy will be used.
     * 
     * For JVM_ARGUMENT, APP_ARGUMENT and DB_CREATION_PROPERTY the value will be
     * fetched from a separate config file, startup.properties in the neo4j
     * database folder. If no property with a matching key is found, the default
     * value you specify will be used.
     * 
     */
    protected static ArrayList<ServerPropertyRepresentation> createProperties()
    {
        ArrayList<ServerPropertyRepresentation> properties = new ArrayList<ServerPropertyRepresentation>();

        //
        // JVM ARGS
        //

        // Garbage collector
        TreeMap<String, String> gcs = new TreeMap<String, String>();
        gcs.put( "Serial GC", "-XX:+UseSerialGC" );
        gcs.put( "Throughput GC", "-XX:+UseParallelGC" );
        gcs.put( "Concurrent Low Pause GC", "-XX:+UseConcMarkSweepGC" );
        gcs.put( "Incremental Low Pause GC", "-Xincgc" );

        properties.add( new ServerPropertyRepresentation(
                "jvm.garbagecollector", "Garbage collector",
                "-XX:+UseSerialGC", PropertyType.JVM_ARGUMENT,
                new ValueDefinition( "", "", gcs ) ) );

        // Min heap size
        properties.add( new ServerPropertyRepresentation( "jvm.min_heap_size",
                "Min heap size", "512m", PropertyType.JVM_ARGUMENT,
                new ValueDefinition( "-Xms", "" ) ) );

        // Max heap size
        properties.add( new ServerPropertyRepresentation( "jvm.max_heap_size",
                "Max heap size", "512m", PropertyType.JVM_ARGUMENT,
                new ValueDefinition( "-Xmx", "" ) ) );

        // JVM server mode
        if ( PlatformUtils.jvmServerModeIsAvailable() )
        {
            properties.add( new ServerPropertyRepresentation( "jvm.server",
                    "JVM server mode", "", PropertyType.JVM_ARGUMENT,
                    new ValueDefinition( "", "", "-server" ) ) );
        }

        //
        // APP ARGS
        //

        // Static web content folder
        properties.add( new ServerPropertyRepresentation( "web.root",
                "Web root", AdminServer.INSTANCE.getStaticPath(),
                PropertyType.APP_ARGUMENT,
                new ValueDefinition( "-webRoot=", "" ) ) );

        // Database folder
        properties.add( new ServerPropertyRepresentation( "db.root",
                "Neo4j path", DatabaseLocator.DB_PATH,
                PropertyType.APP_ARGUMENT, new ValueDefinition( "-dbPath=", "" ) ) );

        //
        // CONFIG FILE ARGS
        //

        // Logical logs
        properties.add( new ServerPropertyRepresentation( "keep_logical_logs",
                "Enable logical logs", "false", PropertyType.CONFIG_PROPERTY,
                new ValueDefinition( "", "", "true", "false" ) ) );

        // Remote shell
        properties.add( new ServerPropertyRepresentation(
                "enable_remote_shell", "Enable remote shell", "false",
                PropertyType.CONFIG_PROPERTY, new ValueDefinition( "", "",
                        "true", "false" ) ) );

        // This is commented out, waiting for authentication scheme to be
        // switched over to OAuth.

        // properties.add( new ServerPropertyRepresentation(
        // "rest_enable_authentication", "Enable authentication", "false",
        // PropertyType.CONFIG_PROPERTY ) );
        //
        // properties.add( new ServerPropertyRepresentation( "rest_username",
        // "Authentication username", "", PropertyType.CONFIG_PROPERTY ) );
        //
        // properties.add( new ServerPropertyRepresentation( "rest_password",
        // "Authentication password", "", PropertyType.CONFIG_PROPERTY ) );

        // DB CREATION ARGS

        properties.add( new ServerPropertyRepresentation(
                "create.array_block_size", "Array block size", "133",
                PropertyType.DB_CREATION_PROPERTY ) );

        properties.add( new ServerPropertyRepresentation(
                "create.string_block_size", "String block size", "133",
                PropertyType.DB_CREATION_PROPERTY ) );

        //
        // GENERAL SETTINGS
        // Used directly by webadmin

        // Backup path
        properties.add( new ServerPropertyRepresentation(
                "general.backup.path", "Backup path", "",
                PropertyType.GENERAL_PROPERTY ) );

        // Properties to list in data browser
        properties.add( new ServerPropertyRepresentation(
                "general.data.listfields", "Data browser list fields", "name",
                PropertyType.GENERAL_PROPERTY ) );

        return properties;

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

    //
    // CONSTRUCT
    //

    protected ServerProperties() throws IOException
    {
        if ( dbConfig == null )
        {
            dbConfig = new Properties();
            FileInputStream in = new FileInputStream(
                    ConfigFileFactory.getDbConfigFile() );
            dbConfig.load( in );
            in.close();
        }

        if ( generalConfig == null )
        {
            generalConfig = new Properties();
            FileInputStream in = new FileInputStream(
                    ConfigFileFactory.getGeneralConfigFile() );
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
                    prop.setFullValue( dbConfig.getProperty( prop.getKey() ) );
                }
            default:
                if ( generalConfig.containsKey( prop.getKey() ) )
                {
                    prop.setFullValue( generalConfig.getProperty( prop.getKey() ) );
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
            dbConfig.put( key, prop.getFullValue() );
            saveProperties( dbConfig, ConfigFileFactory.getDbConfigFile() );
            break;
        default:
            generalConfig.put( key, prop.getFullValue() );
            saveProperties( generalConfig,
                    ConfigFileFactory.getGeneralConfigFile() );
            writeJvmAndAppArgs();
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
            if ( prop.getKey().equals( key ) )
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
     * Update the file with extra args passed to the JVM and to the application
     * itself to match the currently set startup args.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    protected synchronized void writeJvmAndAppArgs()
            throws FileNotFoundException, IOException
    {
        File serviceConfig = ConfigFileFactory.getServiceConfigFile();

        if ( serviceConfig != null )
        {
            // PRODUCTION MODE

            // So uh. This is not very nice.
            // The reason for this is to a) conserve config file formatting and
            // b) config file is not a "normal" properties file, and escaping
            // special
            // chars etc. that is done by Properties will screw up running the
            // service. So we have to do some manual labor here.

            // This will contain the end-result config file
            StringBuilder configFileBuilder = new StringBuilder();

            // Read the whole config file, discard any pre-existing JVM args
            FileReader in = new FileReader( serviceConfig );
            BufferedReader br = new BufferedReader( in );
            String line;
            while ( ( line = br.readLine() ) != null )
            {
                if ( !line.startsWith( "wrapper.java.additional" ) )
                {
                    configFileBuilder.append( line );
                    configFileBuilder.append( "\n" );
                }
            }

            in.close();

            // JVM Args
            configFileBuilder.append( propertiesToJSWConfigString(
                    "wrapper.java.additional.",
                    ServerPropertyRepresentation.PropertyType.JVM_ARGUMENT ) );

            // App args
            configFileBuilder.append( propertiesToJSWConfigString(
                    "wrapper.app.parameter.",
                    ServerPropertyRepresentation.PropertyType.APP_ARGUMENT ) );

            // Write changes to file.
            FileOutputStream out = new FileOutputStream( serviceConfig );
            out.write( configFileBuilder.toString().getBytes() );
            out.close();
        }
        else
        {
            // DEVELOPMENT MODE

            // Write jvm args

            FileOutputStream jvmArgs = new FileOutputStream(
                    ConfigFileFactory.getDevelopmentJvmArgsFile() );
            jvmArgs.write( propertiesToSpaceSeparatedString(
                    ServerPropertyRepresentation.PropertyType.JVM_ARGUMENT ).getBytes() );
            jvmArgs.close();

            // Write app args

            FileOutputStream appArgs = new FileOutputStream(
                    ConfigFileFactory.getDevelopmentAppArgsFile() );
            appArgs.write( propertiesToSpaceSeparatedString(
                    ServerPropertyRepresentation.PropertyType.APP_ARGUMENT ).getBytes() );
            appArgs.close();

        }
    }

    /**
     * Write a string with space-separated properties of a given type.
     * 
     * @param type
     * @return
     */
    private String propertiesToSpaceSeparatedString(
            ServerPropertyRepresentation.PropertyType type )
    {
        StringBuilder args = new StringBuilder();

        for ( ServerPropertyRepresentation prop : properties )
        {
            if ( prop.getType() == type )
            {
                args.append( prop.getFullValue() );
                args.append( " " );
            }
        }

        String out = args.toString();
        return out.substring( 0, out.length() - 1 );
    }

    private String propertiesToJSWConfigString( String prepend,
            ServerPropertyRepresentation.PropertyType type )
    {
        StringBuilder builder = new StringBuilder();

        int argNo = 1;
        for ( ServerPropertyRepresentation prop : properties )
        {
            if ( prop.getType() == type )
            {
                builder.append( prepend );
                builder.append( ( argNo++ ) );
                builder.append( "=" );
                builder.append( prop.getFullValue() );
                builder.append( "\n" );
            }
        }

        return builder.toString();
    }
}
