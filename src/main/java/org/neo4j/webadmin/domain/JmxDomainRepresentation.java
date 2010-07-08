package org.neo4j.webadmin.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.neo4j.rest.domain.Representation;

@SuppressWarnings( "restriction" )
public class JmxDomainRepresentation implements Representation
{

    protected ArrayList<JmxMBeanRepresentation> beans = new ArrayList<JmxMBeanRepresentation>();
    protected String domainName;

    public JmxDomainRepresentation( String name )
    {
        this.domainName = name;
    }

    public void addBean( ObjectName bean )
    {
        beans.add( new JmxMBeanRepresentation( bean ) );
    }

    public Object serialize()
    {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put( "domain", this.domainName );

        ArrayList<Object> serialBeans = new ArrayList<Object>();
        for ( JmxMBeanRepresentation bean : beans )
        {
            serialBeans.add( bean.serialize() );
        }
        data.put( "beans", serialBeans );

        return data;
    }

}
