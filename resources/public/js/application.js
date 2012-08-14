$(function(){
  $(".fighter").click(function(){
    location.href = $(this).find("a").attr("href")
    return false;
  })
})
