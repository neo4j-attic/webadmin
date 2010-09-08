morpheus.provide( "morpheus.forms" );

morpheus.forms = (function($){
	
	var me = {};
	
	me.validators = {
		'not_empty' : function(value) {
			return typeof(value) === "string" && value.length > 0;
		}
	};
	
	me.public = {
		
		validateField : function( field, validator, errorMessage ) {
			
			field = $(field);
			validator = typeof(validator) === "function" ? validator : me.validators[validator];
			
			if( ! validator(field.val(), field) ) {
				field.addClass("error");
				return false;
			} else {
				field.removeClass("error");
				return true;
			}
		},
		
		validateFields : function(def) {
			var success = true;
			
			for( var i = 0, l = def.length; i<l; i++) {
				if(!me.public.validateField(def[i].field, def[i].validator, def[i].errorMessage)) {
					success = false;
				}
			}
			
			return success;
			
		}
			
	};
	
	return me.public;
	
})(jQuery);