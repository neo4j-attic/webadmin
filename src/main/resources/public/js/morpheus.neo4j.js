morpheus.provide( "morpheus.neo4j" );

/**
 * Contains resources required to connect to a running neo4j instance.
 */
morpheus.neo4j = function( )
{

    var me = {};

    //
    // PRIVATE
    //
    
    me.trusted_urls = [];
    
    me.url = url;

    //
    // CONSTRUCT
    //

    //
    // PUBLIC API
    //

    me.api = {};

    return me.api;

};