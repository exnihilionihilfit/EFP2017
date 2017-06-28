var paths = ["/upload", "/edit_config", "/download" , "/about"];


$(document).ready(function(){

  //Set current path navbar button active
  paths.forEach(function(path){
    if(window.location.pathname == path) $("#" + path.replace("/", "")).addClass("active");
  });

  if(window.location.pathname == "/save_config") $("#edit_config").addClass("active");

});
