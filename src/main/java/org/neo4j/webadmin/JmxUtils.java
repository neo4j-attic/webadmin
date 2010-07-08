package org.neo4j.webadmin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.management.ObjectName;

import org.neo4j.webadmin.rest.WebUtils;

/**
 * Utilities for finding management classes made available by neo4j.
 * 
 * @author Anders Nawroth, Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@SuppressWarnings( "restriction" )
public class JmxUtils
{

    public static String mBean2Url( ObjectName obj )
    {
        try
        {
            return URLEncoder.encode( obj.toString(), WebUtils.UTF8 ).replace(
                    "%3A", "/" );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException( "Could not encode string as UTF-8", e );
        }
    }
}
