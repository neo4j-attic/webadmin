package org.neo4j.webadmin.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.rest.domain.Representation;
import org.neo4j.webadmin.NoSuchPropertyException;
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

    public static final String SERVICE_CONFIG_PATH = "../conf/wrapper.conf";

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
    public static File getStartupConfigFile() throws IOException
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
     * value you specify will be used.
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
                "Enable logical logs", "true", PropertyType.CONFIG_PROPERTY ) );

        properties.add( new ServerPropertyRepresentation(
                "enable_remote_shell", "Enable remote shell", "true",
                PropertyType.CONFIG_PROPERTY ) );

        // DB CREATION ARGS

        properties.add( new ServerPropertyRepresentation(
                "create.array_block_size", "Array block size", "133",
                PropertyType.DB_CREATION_PROPERTY ) );

        properties.add( new ServerPropertyRepresentation(
                "create.string_block_size", "String block size", "133",
                PropertyType.DB_CREATION_PROPERTY ) );

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

    /**
     * This is the properties file that is read and used by the underlying neo4j
     * instance.
     */
    protected static volatile Properties dbConfig;

    /**
     * This property file stores configuration used when launching the JVM and
     * when creating a new database.
     */
    protected static volatile Properties startupConfig;

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

        if ( startupConfig == null )
        {
            startupConfig = new Properties();
            FileInputStream in = new FileInputStream( getStartupConfigFile() );
            startupConfig.load( in );
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
                if ( startupConfig.containsKey( prop.getKey() ) )
                {
                    prop.setValue( startupConfig.getProperty( prop.getKey() ) );
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
            startupConfig.put( key, value );
            saveProperties( startupConfig, getStartupConfigFile() );
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

            // This is loaded here, since it is fairly likely the user, if she
            // has made changes, has made them while the JVM has been running.
            // This way we don't risk undoing any of her changes (except for
            // changes to JVM args).
            Properties serviceProperties = new Properties();
            FileInputStream in = new FileInputStream( serviceConfig );
            serviceProperties.load( in );
            in.close();

            // Find config-file defined JVM args
            String strKey;
            ArrayList<Object> toRemove = new ArrayList<Object>();
            for ( Object objKey : serviceProperties.keySet() )
            {
                strKey = (String) objKey;
                if ( strKey.startsWith( "wrapper.java.additional" ) )
                {
                    toRemove.add( objKey );
                }
            }

            // Remove config-file args, not done above because there is no
            // iterator available for Properties. This avoids
            // concurrent modification problems.
            for ( Object key : toRemove )
            {
                serviceProperties.remove( key );
            }

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
                        serviceProperties.put( "wrapper.java.maxmemory",
                                prop.getValue().substring( 4 ) );
                    }
                    else if ( prop.getValue().toLowerCase().startsWith( "-xms" ) )
                    {
                        serviceProperties.put( "wrapper.java.minmemory",
                                prop.getValue().substring( 4 ) );
                    }
                    else
                    {
                        // Write normal JVM arg
                        serviceProperties.put( "wrapper.java.additional."
                                               + ( argNo++ ), prop.getValue() );
                    }
                }
            }

            // Write changes to disc
            saveProperties( serviceProperties, serviceConfig );

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
