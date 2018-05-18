/**
 * 
 */

$(document).ready(function(){
	// find all items that has a class item-qty
	$('.item-qty').on('input', function(){
		
		var id = this.id.substring(4);
		
		$('#update-'+id).css("display", "inline-block")
	})
	
})

