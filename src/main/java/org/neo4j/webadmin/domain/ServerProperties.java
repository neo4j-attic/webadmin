package org.neo4j.webadmin.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.rest.domain.Representation;
import org.neo4j.webadmin.NoSuchPropertyException;
import org.neo4j.webadmin.domain.ServerPropertyRepresentation.PropertyType;

/**
 * Static, hard-coded definition of what properties are available.
 * 
 * This implementation is a bit hacky right now, and will probably change quite
 * a bit.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class ServerProperties implements Representation
{

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
        String userHome = System.getProperty( "user.home" );
        if ( userHome == null )
        {
            throw new IllegalStateException( "user.home==null" );
        }

        // Make sure settings directory exists
        File home = new File( userHome );
        File settingsDirectory = new File( home, ".neo4jwebadmin" );
        if ( !settingsDirectory.exists() )
        {
            if ( !settingsDirectory.mkdir() )
            {
                throw new IllegalStateException( settingsDirectory.toString() );
            }
        }

        File configFile = new File( settingsDirectory, "startup.properties" );

        if ( !configFile.exists() && !configFile.createNewFile() )
        {
            throw new IllegalStateException( configFile.toString() );
        }

        return configFile;
    }

    /*
     * TODO: I've only added a few test properties here, still need
     * to add fetching of actual values from running db etc, as well
     * as adding all the values we'd like to modify here.
     */
    protected static ArrayList<ServerPropertyRepresentation> createProperties()
    {
        ArrayList<ServerPropertyRepresentation> properties = new ArrayList<ServerPropertyRepresentation>();

        // JVM ARGS

        // properties.add( new ServerPropertyRepresentation(
        // "jvm.garbagecollector", "Garbage collector",
        // "-XX:+UseSerialGC", PropertyType.JVM_ARGUMENT ) );
        //
        // properties.add( new ServerPropertyRepresentation( "jvm.heapsize",
        // "Heap size", "-Xmx512m", PropertyType.JVM_ARGUMENT ) );

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

        switch ( prop.getType() )
        {
        case CONFIG_PROPERTY:
            dbConfig.put( key, value );
            saveProperties( dbConfig, getDbConfigFile() );
            break;
        default:
            startupConfig.put( key, value );
            saveProperties( startupConfig, getStartupConfigFile() );
        }

        // Update the value in property object
        prop.setValue( value );
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
}
