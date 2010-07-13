package org.neo4j.webadmin.gremlin;

import org.neo4j.webadmin.gremlin.functions.LoadDbFunction;

import com.tinkerpop.gremlin.functions.FunctionLibrary;

/**
 * Library of extra-feature functions available to the WebAdmin console.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class WebAdminFunctions extends FunctionLibrary
{

    public WebAdminFunctions()
    {
        this.addFunction( "webadmin", new LoadDbFunction() );
    }

}
