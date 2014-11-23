(function($) {
  
  $.fn.collapsibleCheckboxTree = function(options) {
    
    var defaults = {
      checkParents : true, // When checking a box, all parents are checked
      checkChildren : true, // When checking a box, all children are checked
      shiftClickEffectChildren : true, // When shift-clicking a box, all children are checked or unchecked.
      uncheckChildren : true, // When unchecking a box, all children are unchecked
      includeButtons : false, // Include buttons to expand or collapse all above list
      initialState : 'default' // Options - 'expand' (fully expanded), 'collapse' (fully collapsed) or default
    };
      
    var options = $.extend(defaults, options); 
 
    this.each(function() {
               
      var $root = this;
               
      if (defaults.includeButtons) {
        $(this).before('<button id="expand">Expand All</button><button id="collapse">Collapse All</button><button id="default">Default</button>');
      }

      $("ul", $(this)).addClass('hide');
      if (defaults.checkParents) {
        $("input:checked").parents("li").find("input[type='checkbox']:first").attr('checked', true);
      }
      if (defaults.checkChildren) {
        $("input:checked").parent("li").find("input[type='checkbox']").attr('checked', true);
      }
      $("li:has(input:checked) > ul", $(this)).removeClass('hide');
      $("li", $(this)).prepend('<span>&nbsp;</span>');
      $("li:has(> ul:not(.hide)) > span", $(this)).addClass('expanded').html('-');
      $("li:has(> ul.hide) > span", $(this)).addClass('collapsed').html('+');

      $("input[type='checkbox']", $(this)).click(function(ev){
        if ($(this).is(":checked")) {
          $("> ul", $(this).parent("li")).removeClass('hide');
          $("> span.collapsed", $(this).parent("li")).removeClass("collapsed").addClass("expanded").html('-');

          if (defaults.checkParents) {
            $(this).parents("li").find("input[type='checkbox']:first").attr('checked', true);
          }

          if (defaults.checkChildren || (defaults.shiftClickEffectChildren && ev.shiftKey)) {
            $(this).parent("li").find("input[type='checkbox']").attr('checked', true);
            $("ul", $(this).parent("li")).removeClass('hide');
            $("span.collapsed", $(this).parent("li")).removeClass("collapsed").addClass("expanded").html('-');
          }
        } else {
          if (defaults.uncheckChildren || (defaults.shiftClickEffectChildren && ev.shiftKey)) {
            $(this).parent("li").find("input[type='checkbox']").attr('checked', false);
            $("ul", $(this).parent("li")).addClass('hide');
            $("span.expanded", $(this).parent("li")).removeClass("expanded").addClass("collapsed").html('+');
          }
        }
        
      });

      $("li:has(> ul) span", $(this)).click(function(){
        if ($(this).is(".collapsed")) {
          $("> ul", $(this).parent("li")).removeClass('hide');
          $(this).removeClass("collapsed").addClass("expanded").html('-');
        
        } else if ($(this).is(".expanded")) {
          
          $("> ul", $(this).parent("li")).addClass('hide');
          $(this).removeClass("expanded").addClass("collapsed").html('+');
        }
        
      });
      
      // Button functions
      
      $("#expand").click(function () {
        $("ul", $root).removeClass('hide');
        $("li:has(> ul) > span", $root).removeClass("collapsed").addClass("expanded").html('-');
        return false;
      });
      $("#collapse").click(function () {
        $("ul", $root).addClass('hide');
        $("li:has(> ul) > span", $root).removeClass("expanded").addClass("collapsed").html('+');
        return false;
      });
      $("#default").click(function () {
        $("ul", $root).addClass('hide');
        $("li:has(input:checked) > ul", $root).removeClass('hide');
        $("li:has(> ul:not(.hide)) > span", $root).removeClass('collapsed').addClass('expanded').html('-');
        $("li:has(> ul.hide) > span", $root).removeClass('expanded').addClass('collapsed').html('+');
        return false;
      });
      
      switch(defaults.initialState) {
        case 'expand':
          $("#expand").trigger('click');
          break;
        case 'collapse':
          $("#collapse").trigger('click');
          break;
      }
      
    });
    
    return this;
    
  };
  
})(jQuery);
