morpheus.provide( "morpheus.ui" );

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

    me.DEFAULT_PAGE = "morpheus.overview";

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

        $( window ).trigger( "hashchange" );

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

morpheus.ui.mainmenu = ( function( $ )
{

    var me = {};

    //
    // PRIVATE
    //

    me.initiated = false;
    me.container = null;

    me.currentPage = null;
    me.menuItems = [];

    me.init = function()
    {
        me.container = $( "#mor_mainmenu" );

        me.container.setTemplateURL( "templates/mainmenu.tp" );

        me.render();

        me.initiated = true;
    };

    /**
     * Re-render the main menu
     */
    me.render = function()
    {
        me.container.processTemplate(
        {
            items : me.menuItems
        } );
    };

    /**
     * Add a new menu item
     */
    me.addItem = function( name, page, data )
    {
        var data = data || {};
        me.menuItems.push(
        {
            name : name,
            page : page,
            data : data
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

    //
    // PUBLIC API
    //

    me.api =
    {
        init : me.init,
        addItem : me.addItem,
        setCurrentPage : me.setCurrentPage
    };

    return me.api;

} )( jQuery );