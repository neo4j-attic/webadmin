package org.neo4j.webadmin.domain;

import java.util.Map;

import org.neo4j.webadmin.properties.ValueDefinition;

/**
 * A specific type of server property that is defined and fancy, is exposed via
 * the REST configuration API and is changeable just like its parent.
 * 
 * The only difference is that settings defined with this class do not show up
 * in the admin configuration UI.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class HiddenServerPropertyRepresentation extends
        ServerPropertyRepresentation
{

    public HiddenServerPropertyRepresentation( String key, String value,
            PropertyType type )
    {
        super( key, value, type );
    }

    public HiddenServerPropertyRepresentation( String key, String displayName,
            String value, PropertyType type )
    {
        super( key, displayName, value, type );
    }

    public HiddenServerPropertyRepresentation( String key, String displayName,
            String value, PropertyType type, ValueDefinition valueDefinition )
    {
        super( key, displayName, value, type, valueDefinition );
    }

    public Object serialize()
    {
        @SuppressWarnings( "unchecked" ) Map<String, Object> map = (Map<String, Object>) super.serialize();
        map.put( "hidden", true );
        return map;
    }

}
