/**
 * Shows a loading message that blocks the UI.
 */
wa.ui.Loading = (function($){
    var me = {};
    
    me.container = null;
    
    me.showImpl = function() {
        $("#mor_loading_content").modal({
            overlayId: 'mor_loading_overlay',
            containerId: 'mor_loading_container',
            closeHTML: null,
            minHeight: 80,
            opacity: 65, 
            position: ['400',],
            overlayClose: false
            //onOpen: me.open,
            //onClose: me.close
        });
    };
    
    me.open = function (d) {
        me.container = d.container[0];
        d.overlay.fadeIn(50, function () {
            
            $("#mor_dialog_content", me.container).show();
            var title = $("#mor_loading_title", me.container);
            title.show();
            
            d.container.slideDown(50, function () {
                setTimeout(function () {
                    var h = $("#mor_loading_message", me.container).height()
                        + title.height()
                        + 20; // padding
                    
                    d.container.animate(
                        {height: h},
                        100,
                        function () {
                            $("#mor_loading_message", me.container).show();
                            
                            if( typeof(me.cb) === "function" ) {
                                me.cb(true);
                            }
                            
                        }
                    );
                }, 100);
            });
        })
    };
    
    me.close = function (d) {
        d.container.animate(
            {top:"-" + (d.container.height() + 20)},
            200,
            function () {
                $.modal.close();
            }
        );
    };
    
    return {
        show : function(title, message, cb) {
            me.cb = cb;
            $("#mor_loading_title").html(title);
            $("#mor_loading_message").html(message);
            me.showImpl();
        },
        
        hide : function() {
            $.modal.close();
        }
    };
})(jQuery);
