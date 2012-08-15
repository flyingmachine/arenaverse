$(function(){
  $(".fighter").click(function(){
    location.href = $(this).find("a").attr("href")
    return false;
  })

  $("#admin img").error(function () {
    $(this).unbind("error").attr("src", "/img/_ui/placeholder-amazon.png");
  });
})
