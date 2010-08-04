morpheus.provide( "morpheus.components.Lifecycle" );

/**
 * A widget for starting, stopping and restarting the neo4j backend.
 */
morpheus.components.Lifecycle = function( server, template )
{

    var me = {};

    // 
    // PRIVATE
    //

    me.template = template || "components/morpheus.server.lifecycle/templates/widget.tp";
    
    me.element = false;

    me.status = "UNKOWN";
    me.watingForResponse = false;
    me.server = server ? server : false;

    me.getWidget = function()
    {
        return me.element;
    };

    me.start = function( ev )
    {
        if ( me.status !== "RUNNING" )
        {
            me.serverAction( "start", "Starting server.." );
        }
        if ( ev && ev.preventDefault ) ev.preventDefault();
    };

    me.stop = function( ev )
    {
        if ( me.status !== "STOPPED" )
        {
            me.serverAction( "stop", "Stopping server.." );
        }
        if ( ev ) ev.preventDefault();
    };

    me.restart = function( ev )
    {
        me.serverAction( "restart", "Restarting server.." );
        if ( ev ) ev.preventDefault();
    };

    me.check = function( ev )
    {
        if ( me.server )
        {
            me.enable();
            me.serverAction( "status", me.statusElement.html(), "GET" );
        }
        else
        {
            // No server connected
            me.disable();
            me.statusElement.html( "N/A" );
        }
        
        if ( ev && ev.preventDefault ) ev.preventDefault();
    };

    me.disable = function()
    {
        me.buttons.start.hide();
        me.buttons.stop.hide();
        me.buttons.restart.hide();
    };

    me.enable = function() {
        me.buttons.start.show();
        me.buttons.stop.show();
        me.buttons.restart.show();
        
    };
    
    me.serverAction = function( resource, message, type )
    {
        var type = type || "POST";
        if ( !me.watingForResponse )
        {
            me.statusElement.html( message );
            me.watingForResponse = true;

            // Allow UI update
            var method = (type === "POST") ? me.server.admin.post : me.server.admin.get;
            method( resource,
                    function( data ) {
                        me.watingForResponse = false;
                        me.setStatus( data.current_status );
                    });
        }
    };

    me.setStatus = function( status )
    {
        me.status = status;
        if ( me.statusActions[status] )
        {
            me.statusActions[status]();
        }
    };

    me.statusActions =
    {
        RUNNING : function()
        {
            me.statusElement.html( "Running" );
        },

        STOPPED : function()
        {
            me.statusElement.html( "Stopped" );
        }
    };
    
    //
    // CONSTRUCT
    //

    // Create UI
    var tmpElement = $( "<div></div>" );
    tmpElement.setTemplateURL( me.template );
    tmpElement.processTemplate({domain:server.domain});
    
    me.element = tmpElement.children();

    // Cache element lookups
    me.buttons = {};
    me.buttons.start = $( ".mor_lifecycle_start", me.element );
    me.buttons.restart = $( ".mor_lifecycle_restart", me.element );
    me.buttons.stop = $( ".mor_lifecycle_stop", me.element );

    me.statusElement = $( ".mor_lifecycle_status", me.element );

    // Event listeners
    me.buttons.start.click( me.start );
    me.buttons.restart.click( me.restart );
    me.buttons.stop.click( me.stop );

    // Check server status at regular intervals
    me.check();
    
    //
    // PUBLIC INTERFACE
    //

    me.api =
    {
        init      : me.init,
        check     : me.check,
        stop      : me.stop,
        start     : me.start,
        restart   : me.restart,
        getWidget : me.getWidget,
        render    : me.getWidget
    };

    return me.api;

};
