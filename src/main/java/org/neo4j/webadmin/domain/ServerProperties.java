package org.neo4j.webadmin.domain;

import java.util.ArrayList;

import org.neo4j.rest.domain.Representation;
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
public enum ServerProperties implements Representation
{
    INSTANCE;

    /*
     * TODO: I've only added a few test properties here, still need
     * to add fetching of actual values from running db etc, as well
     * as adding all the values we'd like to modify here.
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

        // CONFIG FILE ARGS

        properties.add( new ServerPropertyRepresentation(
                "config.keep_logical_logs", "Enable logical logs", "true",
                PropertyType.CONFIG_PROPERTY ) );

        properties.add( new ServerPropertyRepresentation(
                "config.enable_remote_shell", "Enable remote shell", "true",
                PropertyType.CONFIG_PROPERTY ) );

        // DB CREATION ARGS

        properties.add( new ServerPropertyRepresentation(
                "config.array_block_size", "Array block size", "133",
                PropertyType.DB_CREATION_PROPERTY ) );

        properties.add( new ServerPropertyRepresentation(
                "config.string_block_size", "String block size", "133",
                PropertyType.DB_CREATION_PROPERTY ) );

        return properties;

    }

    protected ArrayList<ServerPropertyRepresentation> properties = createProperties();

    public Object serialize()
    {
        ArrayList<Object> serial = new ArrayList<Object>();
        for ( ServerPropertyRepresentation prop : properties )
        {
            serial.add( prop.serialize() );
        }
        return serial;
    }
}
