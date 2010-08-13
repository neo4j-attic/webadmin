morpheus.provide( "morpheus.ui" );

//$.require( "js/vend/jquery.simplemodal.js" );

/**
 * Morpheus user interface. Builds the base ui, keeps track of registered
 * components and provides an API to inject new UI parts.
 */
morpheus.ui = ( function( $ )
{

    var me = {};

    // 
    // PRIVATE
    //

    me.DEFAULT_PAGE = "morpheus.server.monitor";

    me.pages = {};
    me.currentPage = null;
    me.pageRoot = null;

    /**
     * Set up the user interface.
     */
    me.init = function()
    {

        // Init pages
        me.pageRoot = $( "#mor_pages" );

        for ( key in me.pages )
        {
            me.pageRoot.append( me.pages[key].element );
            me.pages[key].element.hide();
        }

        $( window ).bind( "hashchange", me.hashchange );

        me.api.mainmenu.init();

        me.showPage($.bbq.getState( "p" ));

    };

    me.addPage = function( key, page )
    {

        me.pages[key] =
        {
            obj : page,
            element : page.getPage()
        };

        if ( me.pageRoot !== null )
        {
            me.pageRoot.append( me.pages[key].element );
            me.pages[key].element.hide();
        }

    };

    me.showPage = function( key )
    {
        if ( typeof ( me.pages[key] ) === "undefined" )
        {
            // Selected page does not exist.
            if ( key !== me.DEFAULT_PAGE )
            {
                $.bbq.pushState(
                {
                    p : me.DEFAULT_PAGE
                } );
            }
        }
        else
        {
            if ( me.currentPage === key ) { return; }

            if ( me.currentPage !== null )
            {
                me.pages[me.currentPage].element.hide();
            }

            me.currentPage = key;
            me.pages[key].element.show();
            me.api.mainmenu.setCurrentPage( key );
            
            morpheus.event.trigger("morpheus.ui.page.changed", key);
        }
    };

    /**
     * Called whenever the url hash changes.
     */
    me.hashchange = function( event )
    {

        var pageKey = $.bbq.getState( "p" );
        me.showPage( pageKey );

    };

    //
    // PUBLIC INTERFACE
    //

    me.api =
    {
        init : me.init,
        addPage : me.addPage
    };

    return me.api;

} )( jQuery );

//
// MAIN MENU
//

morpheus.ui.mainmenu = ( function( $, undefined )
{

    var me = {};

    //
    // PRIVATE
    //

    me.initiated = false;
    me.container = null;

    me.currentPage = null;
    me.menuItems = [];
    
    me.sets = { root : { hidden:false } };

    me.init = function()
    {
        me.container = $( "#mor_mainmenu" );

        me.container.setTemplateURL( "templates/mainmenu.tp" );
        me.render();
        
        $( window ).bind( "hashchange", me.hashchange );

        me.initiated = true;
    };

    /**
     * Re-render the main menu
     */
    me.render = function()
    {
        var item, items = [];
        for( var key in me.menuItems ) {
            item = me.menuItems[key];
            if( me.sets[item.set].hidden === false ) {
                items.push(item);
            }
        }
        
        me.container.processTemplate(
        {
            items : items,
            urlAppend : me.getExtraUrlParams()
        });
    };

    /**
     * Add a new menu item
     */
    me.addItem = function( name, page, data, set, index )
    {
        var data = data || {};
        var set = set || "root";
        var index = index || 0;

        if( me.sets[set] === undefined ) {
            me.sets[set] = { hidden : true };
        }
        
        // Find the index where we should insert the new item
        var countdown = me.menuItems.length;
        
        while(me.menuItems[countdown - 1] != undefined && me.menuItems[countdown - 1].index > index) {
        	countdown--;
        }
        
        // Insert the new item
        me.menuItems.splice(countdown, 0,
        {
            name : name,
            page : page,
            data : data,
            set  : set,
            urlAppend : "",
            index : index
        } );

        if ( me.initiated )
        {
            me.render();
        }
    };

    /**
     * Set the current page beeing viewed.
     */
    me.setCurrentPage = function( currentPage )
    {

        for ( var i = 0, l = me.menuItems.length; i < l; i++ )
        {
            if ( me.menuItems[i].page === currentPage )
            {
                me.menuItems[i].isCurrent = true;
            }
            else
            {
                me.menuItems[i].isCurrent = false;
            }
        }

        if ( me.initiated )
        {
            me.render();
        }
    };
    
    me.update = function( name, update ) {
      
        for(var key in me.menuItems) {
            if(me.menuItems[key].name === name) {
                
                for(var param in update) {
                    me.menuItems[key][param] = update[param];
                }
                
            }
        }
        
        me.render();
        
    };

    me.getSetClass = function(set) {
        return "menuset-" + set;
    };
    
    me.showSet = function(set) {
        if( me.sets[set] === undefined ) {
            me.sets[set] = { hidden : false };
        }
        
        me.sets[set].hidden = false;
        me.render();
    };
    
    me.hideSet = function(set) {
        if( me.sets[set] === undefined ) {
            me.sets[set] = { hidden : false };
        }
        
        me.sets[set].hidden = true;
        me.render();
    };
    
    me.hashchange = function(ev) {
      
        me.render();
        
    };
    
    /**
     * Get a string of all url hash parameters, except the page one.
     * This is appended to the end of all menu links each time the hash changes.
     */
    me.getExtraUrlParams= function() {
      
        var data = $.deparam.fragment();
        delete(data.p);
        
        return "&" + $.param.fragment("", data).substring(1); 
        
    };
    
    //
    // PUBLIC API
    //

    me.api =
    {
        init : me.init,
        
        /**
         * Add a new menu item.
         * 
         * @param [string] name
         * @param [string] page is the page id string for the page to show when clicked
         * @param data is optional data to send to the page when showing it
         * @param [string] set is an optional set the button should belong to, sets can shown/hidden
         */
        add : me.addItem,
        
        /**
         * Update a menu item
         * 
         * @param [string] name of the menu item to update
         * @param [object] is a dictionary with keys corresponding to the parameters of the add method
         */
        update : me.update,
        
        setCurrentPage : me.setCurrentPage,
        
        hideSet : me.hideSet,
        showSet : me.showSet
    };

    return me.api;

} )( jQuery );

//
// DIALOG
//

morpheus.ui.dialog = (function($){
	var me = {};
	
	me.container = null;
	
	me.public = {
		show : function(title, body) {
			$("#mor_dialog_title").html(title);
			$("#mor_dialog_data").html(body);
			me.showImpl();
		},
		
		showUsingTemplate : function( title, templateUrl, templateContext ) {
			$("#mor_dialog_title").html(title);
			$("#mor_dialog_data").setTemplateURL(templateUrl);
			$("#mor_dialog_data").processTemplate(templateContext || {});
			me.showImpl();
		},
		
		close : function() {
			$.modal.close();
		}
	};
	
	me.showImpl = function() {
		$("#mor_dialog_content").modal({
			overlayId: 'mor_dialog_overlay',
			containerId: 'mor_dialog_container',
			closeHTML: null,
			minHeight: 80,
			opacity: 65, 
			position: ['0',],
			overlayClose: true,
			onOpen: me.open,
			onClose: me.close
		});
	};
	
	me.open = function (d) {
		me.container = d.container[0];
		d.overlay.fadeIn('slow', function () {
			$("#mor_dialog_content", me.container).show();
			var title = $("#mor_dialog_title", me.container);
			title.show();
			d.container.slideDown('slow', function () {
				setTimeout(function () {
					var h = $("#mor_dialog_data", me.container).height()
						+ title.height()
						+ 20; // padding
					d.container.animate(
						{height: h}, 
						200,
						function () {
							$("div.close", me.container).show();
							$("#mor_dialog_data", me.container).show();
						}
					);
				}, 300);
			});
		})
	};
	
	me.close = function (d) {
		d.container.animate(
			{top:"-" + (d.container.height() + 20)},
			500,
			function () {
				$.modal.close();
			}
		);
	};
	
	return me.public;
})(jQuery);

//
// FOLDOUT HELP
//

$("a.mor_module_foldout_trigger").live("click",function(ev) {
	ev.preventDefault();
	$(".mor_module_foldout_content",$(ev.target).closest(".mor_module_foldout")).toggleClass("visible");
});
