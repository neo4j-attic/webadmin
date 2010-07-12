package org.neo4j.webadmin.domain;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.neo4j.rest.domain.Representation;
import org.neo4j.webadmin.utils.JmxUtils;

@SuppressWarnings( "restriction" )
public class JmxMBeanRepresentation implements Representation
{

    protected ObjectName beanName;
    protected MBeanServer jmxServer = ManagementFactory.getPlatformMBeanServer();

    public JmxMBeanRepresentation( ObjectName beanInstance )
    {
        this.beanName = beanInstance;
    }

    public Object serialize()
    {

        Map<String, Object> data = new HashMap<String, Object>();

        data.put( "name", beanName.toString() );
        data.put( "url", "" );

        try
        {
            MBeanInfo beanInfo = jmxServer.getMBeanInfo( beanName );

            data.put( "description", beanInfo.getDescription() );
            data.put( "url", JmxUtils.mBean2Url( beanName ) );

            ArrayList<Object> attributes = new ArrayList<Object>();
            for ( MBeanAttributeInfo attrInfo : beanInfo.getAttributes() )
            {
                attributes.add( ( new JmxAttributeRepresentation( beanName,
                        attrInfo ) ).serialize() );
            }

            data.put( "attributes", attributes );

        }
        catch ( IntrospectionException e )
        {
            e.printStackTrace();
        }
        catch ( InstanceNotFoundException e )
        {
            e.printStackTrace();
        }
        catch ( ReflectionException e )
        {
            e.printStackTrace();
        }

        return data;
    }
}
