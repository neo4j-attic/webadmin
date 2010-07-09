package org.neo4j.webadmin.domain;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.openmbean.CompositeData;

import org.neo4j.rest.domain.Representation;

@SuppressWarnings( "restriction" )
public class JmxAttributeRepresentation implements Representation
{

    protected ObjectName objectName;
    protected MBeanAttributeInfo attrInfo;
    protected MBeanServer jmxServer = ManagementFactory.getPlatformMBeanServer();

    public JmxAttributeRepresentation( ObjectName objectName,
            MBeanAttributeInfo attrInfo )
    {
        this.objectName = objectName;
        this.attrInfo = attrInfo;
    }

    public Object serialize()
    {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put( "name", attrInfo.getName() );
        data.put( "description", attrInfo.getDescription() );
        data.put( "type", attrInfo.getType() );

        data.put( "isReadable", attrInfo.isReadable() ? "true" : "false" );
        data.put( "isWriteable", attrInfo.isWritable() ? "true" : "false" );
        data.put( "isIs", attrInfo.isIs() ? "true" : "false" );

        try
        {
            Object value = jmxServer.getAttribute( objectName,
                    attrInfo.getName() );

            if ( value == null )
            {
                data.put( "value", null );
            }
            else if ( value.getClass().isArray() )
            {
                ArrayList<Object> values = new ArrayList<Object>();

                for ( Object subValue : (Object[]) value )
                {
                    if ( subValue instanceof CompositeData )
                    {
                        values.add( ( new JmxCompositeDataRepresentation(
                                (CompositeData) subValue ) ).serialize() );
                    }
                    else
                    {
                        values.add( subValue.toString() );
                    }
                }

                data.put( "value", values );
            }
            else
            {
                data.put( "value", value.toString() );
            }

        }
        catch ( AttributeNotFoundException e )
        {
            e.printStackTrace();
            data.put( "value", "N/A" );
        }
        catch ( InstanceNotFoundException e )
        {
            e.printStackTrace();
            data.put( "value", "N/A" );
        }
        catch ( MBeanException e )
        {
            e.printStackTrace();
            data.put( "value", "N/A" );
        }
        catch ( ReflectionException e )
        {
            e.printStackTrace();
            data.put( "value", "N/A" );
        }
        catch ( RuntimeMBeanException e )
        {
            data.put( "value", "N/A" );
        }
        catch ( ClassCastException e )
        {
            e.printStackTrace();
            data.put( "value", "N/A" );
        }

        return data;

    }
}
