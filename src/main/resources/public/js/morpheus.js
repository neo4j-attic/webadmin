/**
 * This is the entry point file for Morpheus.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 */

//
// MORPHEUS CORE
//

/**
 * Morpheus core
 */
var morpheus = ( function( $, undefined )
{

    var me = morpheus || {};

    // 
    // PRIVATE
    //

    me.PROPERTIES_URL = "/manage/properties/";

    me.initiated = false;

    me.components = {};
    me.propertyCache = {};
    
    me.displayedErrors = {};

    me.init = function()
    {
        if ( me.initiated === false )
        {
            me.initiated = true;
            
            // Load UI
            morpheus.ui.MainMenu.init();
            morpheus.ui.Pages.init();

            // Trigger init event
            morpheus.event.trigger( "morpheus.init" );
        }
    };

    me.property = function( key, value, cb )
    {
        if ( typeof ( value ) === "function" )
        {
            if ( typeof ( me.propertyCache[key] ) === "undefined" )
            {
                neo4j.Web.get( me.PROPERTIES_URL + key, ( function( key, cb )
                {
                    return function( data )
                    {
                        var value = data === "undefined" ? undefined : typeof(data) === "string" ? JSON.parse(data) : data;
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
        	if( typeof( value ) !== "string") {
        		value = JSON.stringify(value);
        	}
        	
            neo4j.Web.post( me.PROPERTIES_URL + key, value, ( function( cb, key, value )
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
        prop : me.property,
    	
    	/**
    	 * Display an error message until timout time passes.
    	 * @param error is the error string
    	 * @param timeout is the time in milliseconds to show the error, default is 5000
    	 */
    	showError : function(error, timeout) {
    		var timeout = timeout || 5000;
    		
    		if( typeof(me.displayedErrors[error]) !== "undefined" ) {
    			var errObj = me.displayedErrors[error];
    			clearTimeout(errObj.timeout);
    			
    			$('.mor_error_count', errObj.elem).html("(" + (++errObj.count) + ")");
    			$('.mor_error_count', errObj.elem).show();
    			
    			errObj.timeout = setTimeout( (function(error) { return function() {me.api.hideError(error);};})(error), timeout );
    			
    		} else {
    			me.displayedErrors[error] = {
    					msg : error,
    					count : 1,
    					elem : $("<li>"+ error +"<span class='mor_error_count' style='display:none;'>(1)</span></li>"),
    					timeout : setTimeout( (function(error) { return function() {me.api.hideError(error);};})(error), timeout )
    			};
    			
    			$("#mor_errors").append(me.displayedErrors[error].elem);
    		}
    	},
    	
    	hideError : function(error) {
    		if( typeof(me.displayedErrors[error]) !== "undefined" ) {
    			me.displayedErrors[error].elem.remove()
    			delete(me.displayedErrors[error]);
    		}
    	}
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
//FOLDOUT HELP
//

$("a.mor_module_foldout_trigger").live("click",function(ev) {
    ev.preventDefault();
    $(".mor_module_foldout_content",$(ev.target).closest(".mor_module_foldout")).toggleClass("visible");
});
