/**
 * This is the entry point file for Morpheus.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 */

//
// MORPHEUS CORE
//
$.require( "js/vend/json2.js" );
$.require( "js/vend/jquery-jtemplates.js" );
$.require( "js/vend/jquery.bbq.js" );

$.require( "js/morpheus.ui.js" );
$.require( "js/morpheus.event.js" );
$.require( "js/morpheus.neo4j.js" );

$.require( "components.js" );

/**
 * Morpheus core
 */
var morpheus = ( function( $, undefined )
{

    var me = morpheus || {};

    // 
    // PRIVATE
    //

    me.COMPONENT_PATH = "components/";
    me.COMPONENT_INIT_FILE = "/js/init.js";

    me.PROPERTIES_URL = "/admin/properties/";

    me.initiated = false;

    me.components = {};
    me.propertyCache = {};

    me.init = function()
    {
        if ( me.initiated === false )
        {
            me.initiated = true;
            me.loadComponents();
        }
    };

    /**
     * Load components listed in components.js
     */
    me.loadComponents = function( cb )
    {
        me.pendingComponents = me.api.componentList.length;
        
        for ( var i = 0, l = me.api.componentList.length; i < l; i++ )
        {

            // Import the module init.js file

            me.components[me.api.componentList[i]] =
            {
                name : me.api.componentList[i],
                loaded : false
            };

            $.require( me.COMPONENT_PATH + me.api.componentList[i]
                    + me.COMPONENT_INIT_FILE, ( function( componentName )
            {
                return function()
                {
                    me.componentLoaded( componentName );
                };
            } )( me.api.componentList[i] ) );

        }

        setTimeout( me.checkComponentsLoaded, 13 );
    };

    /**
     * Triggered for each component that is loaded.
     */
    me.componentLoaded = function( componentName )
    {
        me.components[componentName].loaded = true;
    };

    /**
     * Check if components have been loaded at given intervals, trigger full
     * application start once all components are loaded.
     */
    me.checkComponentsLoaded = function()
    {
        for ( var key in me.components )
        {
            if ( me.components[key].loaded === false )
            {
                setTimeout( me.checkComponentsLoaded, 13 );
                return;
            }
        }

        // All components loaded, load UI
        morpheus.ui.init();

        // Trigger init event
        morpheus.event.trigger( "morpheus.init" );
    };

    me.property = function( key, value, cb )
    {
        if ( typeof ( value ) === "function" )
        {
            if ( typeof ( me.propertyCache[key] ) === "undefined" )
            {
                $.get( me.PROPERTIES_URL + key, ( function( key, cb )
                {
                    return function( data )
                    {
                        var value = data === "undefined" ? undefined : JSON.parse(data);
                        me.propertyCache[key] = value;
                        cb( key, value );
                    };
                } )( key, value ) );
            }
            else
            {
                value( me.propertyCache[key] );
            }
        }
        else
        {
            $.post( me.PROPERTIES_URL + key,
            {
                value : JSON.stringify( value )
            }, ( function( cb, key, value )
            {
                return function()
                {
                    if ( typeof ( cb ) === "function" )
                    {
                        me.propertyCache[key] = value;
                        cb();
                    }
                };
            } )( cb, key, value ) );
        }
    };

    //
    // PUBLIC INTERFACE
    //

    me.api =
    {
        init : me.init,

        /**
         * Persistent backend key-value store for simple properties.
         * 
         * morpheus.prop("myProperty","a value", function() { alert("Value
         * set!"); });
         * 
         * morpheus.prop("myProperty", function(key, value) { alert(key + " is " +
         * value); });
         * 
         * @param {String}
         *            key is a unique property key
         * @param [obj]
         *            defines a value to set the property to. If omitted, this
         *            method will instead fetch the value and call the callback
         *            with it.
         * @param {Function}
         *            cb is a callback that will be called with key, value if no
         *            value was passed (used to get a parameter value), or with
         *            only the key if a value was passed and successfully set.
         */
        prop : me.property
    };

    return me.api;

} )( jQuery );

//
// GLOBAL UTILS
//

/**
 * Quick-n-dirty provide implementation, shortens down boilerplate like:
 * 
 * morpheus.something = morpheus.something || {};
 * morpheus.something.somethingelse = morpheus.something.somethingelse || {};
 * 
 * to:
 * 
 * morpheus.provide("morpheus.something.somethingelse")
 */
morpheus.provide = function( path )
{

    var parts = path.split( "." );
    parts.pop(); // Last one is class / module name, and should not be
    // defined here.

    var currentScope = window;
    for ( var index in parts )
    {
        currentScope[parts[index]] = currentScope[parts[index]] || {};
        currentScope = currentScope[parts[index]];
    }
};

//
// BOOT
//

$( function()
{
    morpheus.init();
} );