package org.neo4j.webadmin.properties;

import java.util.ArrayList;
import java.util.TreeMap;

import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.webadmin.AdminServer;
import org.neo4j.webadmin.domain.HiddenServerPropertyRepresentation;
import org.neo4j.webadmin.domain.ServerPropertyRepresentation;
import org.neo4j.webadmin.domain.ServerPropertyRepresentation.PropertyType;
import org.neo4j.webadmin.utils.PlatformUtils;

/**
 * This is the hard-coded settings that will be exposed to the user.
 * 
 * This class defines a static list of property definitions. Each entry in the
 * list defines:
 * 
 * <ul>
 * <li>A unique key for the property</li>
 * <li>The type of the property, see {@link PropertyType}</li>
 * <li>A user-readable name of the property</li>
 * <li>A default value</li>
 * <li>A value definition, containing meta data about how to operate on the
 * actual property value, see {@link ValueDefinition}</li>
 * </ul>
 * 
 * Each property should have a unique key, for neo4j config file and server
 * creation properties this should be the property name. For JVM args it is
 * irrelevant, but it is recommended to name them something like
 * "jvm.myproperty".
 * 
 * A note on how values are fetched:
 * 
 * For CONFIG_PROPERTY properties, the value will be fetched from the neo4j
 * config file. If no property with a matching key is found, the default value
 * you specify will be used.
 * 
 * For JVM_ARGUMENT, APP_ARGUMENT and DB_CREATION_PROPERTY the value will be
 * fetched from a separate config file, startup.properties in the neo4j database
 * folder. If no property with a matching key is found, the default value you
 * specify will be used.
 * 
 */
public class ServerPropertyDefinitions
{

    public static ArrayList<ServerPropertyRepresentation> getPropertyDefinitions()
    {

        ArrayList<ServerPropertyRepresentation> properties = new ArrayList<ServerPropertyRepresentation>();

        //
        // JVM ARGS
        //

        if ( DatabaseLocator.isLocalDatabase() )
        {

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
            properties.add( new ServerPropertyRepresentation(
                    "jvm.min_heap_size", "Min heap size", "512m",
                    PropertyType.JVM_ARGUMENT, new ValueDefinition( "-Xms", "" ) ) );

            // Max heap size
            properties.add( new ServerPropertyRepresentation(
                    "jvm.max_heap_size", "Max heap size", "512m",
                    PropertyType.JVM_ARGUMENT, new ValueDefinition( "-Xmx", "" ) ) );

            // JVM server mode
            if ( PlatformUtils.jvmServerModeIsAvailable() )
            {
                properties.add( new ServerPropertyRepresentation( "jvm.server",
                        "JVM server mode", "", PropertyType.JVM_ARGUMENT,
                        new ValueDefinition( "", "", "-server" ) ) );
            }
        }

        //
        // APP ARGS
        //

        // Static web content folder
        properties.add( new HiddenServerPropertyRepresentation( "web.root",
                "Web root", AdminServer.INSTANCE.getStaticPath(),
                PropertyType.APP_ARGUMENT,
                new ValueDefinition( "-webRoot=", "" ) ) );

        // Database folder
        properties.add( new ServerPropertyRepresentation( "db.root",
                "Database location", DatabaseLocator.getDatabaseLocation(),
                PropertyType.APP_ARGUMENT, new ValueDefinition( "-dbPath=", "" ) ) );

        //
        // CONFIG FILE ARGS
        //

        if ( DatabaseLocator.isLocalDatabase() )
        {

            // Logical logs
            properties.add( new ServerPropertyRepresentation(
                    "keep_logical_logs", "Enable logical logs", "false",
                    PropertyType.CONFIG_PROPERTY, new ValueDefinition( "", "",
                            "true", "false" ) ) );

            // Remote shell
            properties.add( new ServerPropertyRepresentation(
                    "enable_remote_shell", "Enable remote shell", "false",
                    PropertyType.CONFIG_PROPERTY, new ValueDefinition( "", "",
                            "true", "false" ) ) );

            // DB CREATION ARGS

            properties.add( new ServerPropertyRepresentation(
                    "create.array_block_size", "Array block size", "133",
                    PropertyType.DB_CREATION_PROPERTY ) );

            properties.add( new ServerPropertyRepresentation(
                    "create.string_block_size", "String block size", "133",
                    PropertyType.DB_CREATION_PROPERTY ) );
        }
        else
        {

            // JMX URI
            properties.add( new ServerPropertyRepresentation(
                    "general.jmx.uri", "JMX URI", "",
                    PropertyType.GENERAL_PROPERTY ) );

        }

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

}
