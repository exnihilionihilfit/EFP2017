var paths = ["/upload", "/edit_config" , "/about"];


$(document).ready(function(){

  //Set current path navbar button active
  paths.forEach(function(path){
    if(window.location.pathname == path) $("#" + path.replace("/", "")).addClass("active");
  });

  if(window.location.pathname == "/save_config") $("#edit_config").addClass("active");


  //Add click handler for export in validation view
  $("#exportValidationResult").click(function () {
    var doc = new jsPDF();

    doc.addHTML($("#validationResult")[0], 15, 15, {
    }, function() {
      doc.save("validation_result.pdf");
    });
  });

});
