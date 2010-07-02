package org.neo4j.webadmin.domain;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.rest.domain.Representation;

/**
 * Represents a server configuration setting. This is an abstraction of the
 * three types of settings that are possible:
 * 
 * <ul>
 * <li>Configuration file settings</li>
 * <li>JVM directives</li>
 * <li>Database creation settings</li>
 * </ul>
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class ServerPropertyRepresentation implements Representation
{

    public enum PropertyType
    {
        CONFIG_PROPERTY,
        JVM_ARGUMENT,
        DB_CREATION_PROPERTY
    }

    protected String key;
    protected String displayName;
    protected PropertyType type;
    protected String value;

    //
    // CONSTRUCTORS
    //

    public ServerPropertyRepresentation( String key, String value, PropertyType type )
    {
        this( key, key, value, type );
    }

    public ServerPropertyRepresentation( String key, String displayName,
            String value, PropertyType type )
    {
        this.key = key;
        this.displayName = displayName;
        this.value = value;
        this.type = type;
    }

    //
    // PUBLIC
    //

    public Object serialize()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put( "key", this.key );
        map.put( "display_name", this.displayName );
        map.put( "type", this.type );
        map.put( "value", this.value );
        return map;
    }

}
