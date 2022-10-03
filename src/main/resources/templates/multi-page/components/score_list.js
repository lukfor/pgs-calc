activeItem = $('#sample-item');

function setActive(e) {

  if (activeItem){
    activeItem.removeClass("active");
  }
  $(this).addClass("active");
  activeItem = $(this);
}


$(document).ready(function() {

  //event handler
  $('.list-group-item').on('click', setActive);

});
