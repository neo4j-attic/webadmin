package org.neo4j.webadmin.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;

import org.neo4j.rest.domain.Representation;

@SuppressWarnings( "restriction" )
public class JmxCompositeDataRepresentation implements Representation
{

    protected CompositeData data;

    public JmxCompositeDataRepresentation( CompositeData data )
    {
        this.data = data;
    }

    public Object serialize()
    {
        Map<String, Object> serialData = new HashMap<String, Object>();

        serialData.put( "type", data.getCompositeType().getTypeName() );
        serialData.put( "description", data.getCompositeType().getDescription() );

        ArrayList<Object> values = new ArrayList<Object>();
        for ( String key : data.getCompositeType().keySet() )
        {
            Map<String, Object> value = new HashMap<String, Object>();
            value.put( "name", key );

            Object rawValue = data.get( key );
            if ( rawValue instanceof CompositeData )
            {
                value.put( "value", ( new JmxCompositeDataRepresentation(
                        (CompositeData) rawValue ) ).serialize() );
            }
            else
            {
                value.put( "value", rawValue.toString() );
            }

            values.add( value );
        }

        serialData.put( "value", values );

        return serialData;
    }
}
