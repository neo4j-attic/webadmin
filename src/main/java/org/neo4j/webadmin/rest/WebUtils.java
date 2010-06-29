package org.neo4j.webadmin.rest;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * Static helpers for web services. Mostly copied from neo4-rest.
 *
 */
public class WebUtils {

	public static final String UTF8 = "UTF-8";
	
	/**
	 * Add necessary headers and ensure content is UTF-8 encoded.
	 * @param builder
	 * @return builder with content length and utf-8 headers.
	 */
	public static ResponseBuilder addHeaders( ResponseBuilder builder )
    {
        String entity = (String) builder.clone().build().getEntity();
        byte[] entityAsBytes;
        try
        {
            entityAsBytes = entity.getBytes( UTF8 );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException( "Could not encode string as UTF-8", e );
        }
        builder = builder.entity( entityAsBytes );
        builder = builder.header( HttpHeaders.CONTENT_LENGTH,
                String.valueOf( entityAsBytes.length ) );
        builder = builder.header( HttpHeaders.CONTENT_ENCODING, UTF8 );
        return builder;
    }
	
}
