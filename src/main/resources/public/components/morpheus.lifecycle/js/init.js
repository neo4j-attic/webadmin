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

    me.template = template || "components/morpheus.lifecycle/templates/widget.tp";
    
    me.element = false;

    me.status = "UNKOWN";
    me.watingForResponse = false;
    me.url = server ? server.urls.admin : false;

    me.getWidget = function()
    {
        return me.element;
    };

    me.start = function( ev )
    {
        if ( me.status !== "RUNNING" )
        {
            me.serverAction( me.url + "start", "Starting server.." );
        }
        if ( ev ) ev.preventDefault();
    };

    me.stop = function( ev )
    {
        if ( me.status !== "STOPPED" )
        {
            me.serverAction( me.url + "stop", "Stopping server.." );
        }
        if ( ev ) ev.preventDefault();
    };

    me.restart = function( ev )
    {
        me.serverAction( me.url + "restart", "Restarting server.." );
        if ( ev ) ev.preventDefault();
    };

    me.check = function( ev )
    {
        me.serverAction( me.url + "status", "Checking server status..", "GET" );
        if ( ev ) ev.preventDefault();
    };

    me.disable = function()
    {
        me.buttons.start.hide();
        me.buttons.stop.hide();
        me.buttons.restart.hide();
    };

    me.serverAction = function( url, message, type )
    {
        var type = type || "POST";
        if ( !me.watingForResponse )
        {
            me.statusElement.html( message );
            me.watingForResponse = true;

            // Allow UI update
            setTimeout( function()
            {
                $.ajax(
                {
                    url : url,
                    success : function( data )
                    {
                        me.watingForResponse = false;
                        me.setStatus( data.current_status );
                    },
                    failure : function()
                    {
                        me.watingForResponse = false;
                        me.setStatus( "Connection error" );
                    },
                    dataType : "json",
                    type : type
                } );
            }, 0 );
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

    // Check server status
    if ( me.url )
    {
        me.check();
    }
    else
    {
        // No server connected
        me.disable();
        me.statusElement.html( "N/A" );
    }

    //
    // PUBLIC INTERFACE
    //

    me.api =
    {
        init : me.init,
        getWidget : me.getWidget
    };

    return me.api;

};
