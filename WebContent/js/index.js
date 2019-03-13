var cogWheels = '<span class="glyphicons glyphicons-cogwheels x1" aria-hidden="true"></span>';
var upload = '<span class="glyphicons glyphicons-upload x1" aria-hidden="true"></span>';
var play = '<span class="glyphicons glyphicons-play x1" aria-hidden="true"></span>';
var playlist = '<span class="glyphicons glyphicons-playlist x1" aria-hidden="true"></span>';
var columns = {
  "sentence": {field:"sentence", title: "Sentence", valign:"middle", align: "left", rowspan: 2, colspan: 1, formatter: "", sortable: true},
  "speaker": {field:"speaker", title: "Speaker", valign:"middle", align: "center", rowspan: 2, colspan: 1, formatter: "", sortable: true},
  "timestamp": {field:"timestamp", title: "Timestamp", valign:"middle", align: "center", rowspan: 2, colspan: 1, formatter: "", sortable: true},

  "chats": {field:"chats", title: "Customer Engagement", valign:"middle", align: "center", rowspan: 1, colspan: 7, formatter: "", sortable: false},

  "excited": {field:"excited", title: "Excited", valign:"middle", align: "center", rowspan: 1, colspan: 1, formatter: "", sortable: true},
  "frustrated": {field:"frustrated", title: "Frustrated", valign:"middle", align: "center", rowspan: 1, colspan: 1, formatter: "", sortable: true},
  "impolite": {field:"impolite", title: "Impolite", valign:"middle", align: "center", rowspan: 1, colspan: 1, formatter: "", sortable: true},
  "polite": {field:"polite", title: "Polite", valign:"middle", align: "center", rowspan: 1, colspan: 1, formatter: "", sortable: true},
  "sad": {field:"sad", title: "Sad", valign:"middle", align: "center", rowspan: 1, colspan: 1, formatter: "", sortable: true},
  "satisfied": {field:"satisfied", title: "Satisfied", valign:"middle", align: "center", rowspan: 1, colspan: 1, formatter: "", sortable: true},
  "sympathetic": {field:"sympathetic", title: "Sympathetic", valign:"middle", align: "center", rowspan: 1, colspan: 1, formatter: "", sortable: true},
}

var taCols = [];
var taRow0 = [];
taRow0.push(columns.sentence);
taRow0.push(columns.speaker);
taRow0.push(columns.timestamp);
taRow0.push(columns.chats);
var taRow1 = [];
taRow1.push(columns.excited);
taRow1.push(columns.frustrated);
taRow1.push(columns.impolite);
taRow1.push(columns.polite);
taRow1.push(columns.sad);
taRow1.push(columns.satisfied);
taRow1.push(columns.sympathetic);

taCols.push(taRow0);
taCols.push(taRow1);

$(document)
  .ready(function() {
    buildTable(taCols);
    // $('#uploadFile').shake();
    $('#playSound').focus();
    // var options = {"effect": "bounce", "duration": 5000};
    $('#playSound').effect("bounce",{times:20,distance:50},5000);
    $('#chooseSound').effect("bounce",{times:20,distance:50},5000);
    $('#uploadFile').effect("bounce",{times:20,distance:50},5000);

    var msg = 'First step, choose one of the following options:<ul>';
    msg += '<li>Upload an audio/mp3 conversation by clicking ' + upload + '</li>';
    msg += '<li>Listen to the sample conversation by clicking ' + play + '</li>';
    msg += '<li>Load a sample conversation from playlist by clicking ' + playlist + '</li>';
    msg += '</ul>'

    ShowAlert("Welcome to Speech to Customer Engagement.", msg, "alert-success", 15000);
  })
  .ajaxStart(function(){
      $("div#Loading").addClass('show');
  })
  .ajaxStop(function(){
      $("div#Loading").removeClass('show');
  });

$('.modal').on('shown.bs.modal', function() {
  $(this).find('[autofocus]').focus();
});

$('#save').click(function (){
  if($('#answers').bootstrapTable('getData').length == 0){
    ShowAlert("No matching records found.", "There's nothing to save.", "alert-info");
  }
  else{
    ($('#answers').tableExport({
      type:'csv',
      csvSeparator: ';',
      fileName: 'Watson-Tone-Analyzer-analyses'
    }));
  }
})

$("#chooseSound").click(function (){

  $.ajax({
    type: 'POST',
    url: "LPL",
    dataType: 'json',

    success: function(data) {
      showPlaylist(data);
    },
    error: function(data) {
      console.log(data);
    }
  });

})

$('#soundFile').change(function (){


  var fileInput = document.getElementById('soundFile');
  var file = fileInput.files[0];
  var fd = new FormData();
  fd.append('file', file, 'sound.mp3');
  console.log(JSON.stringify(fd));

  $.ajax({
        url: "US",
        type: "POST",
        data: fd,
        enctype: 'multipart/form-data',
        dataType: false,
        processData: false,  // tell jQuery not to process the data
        contentType: false,   // tell jQuery not to set contentType
        success: function(data) {
          if(data.STATUS == "OK"){
            console.log(data);
            var fileURL = "sounds/sound.mp3";
          	var audio = $("#player");
          	$("#sound").attr("src", fileURL);
          	audio[0].pause();
          	audio[0].load();

            ShowAlert("Upload successfull.", 'Next step : send upload to Watson by clicking ' + cogWheels , "alert-success");
            $('#send').focus();
            $('#send').effect("bounce",{times:20,distance:50},5000);
          }
          else{
            ShowAlert("Upload failed.", data.MESSAGE, "alert-danger");
          }
    		}
  });

})

$("#send").click(function (){

  $.ajax({
    type: 'POST',
    url: "RA",
    dataType: 'json',

    success: function(data) {
      if(data.STATUS == "OK"){

        $.ajax({
          type: 'POST',
          url: "ACET",
          dataType: 'json',

          success: function(data) {
              // console.log(JSON.stringify(data));
              if(data.STATUS == "OK" && data.ANSWER){
                $('#answers').bootstrapTable('load', loadDatas(data));
              }
              else{
                ShowAlert("Watson did not answered correctly.", data.ANSWER, "alert-danger");
              }
          },
          error: function(data) {
            console.log(data);
            ShowAlert("Watson did not answered correctly.", data.responseText + '.<br>Give a chance to another sound file...', "alert-danger");
          }

        });
      }

    },
    error: function(data) {
      console.log(data);
      ShowAlert("Watson did not answered correctly.", data.responseText + '.<br>Give a chance to another sound file...', "alert-danger");
    }

  });


})

$("#stopSound").click(function (){

  var audio = $("#player");
  audio[0].pause();
  audio[0].currentime = 0;
  $("#sound").attr("src", "");

});


$("#playSound").click(function (){
  playSample();
});

function showPlaylist(data){

  $modal = $('#dynamicModal');
  $modal.find('.modal-header').find('.modal-title').empty();
  $modal.find('.modal-body').empty();
  $modal.find('.modal-footer').empty();

  var openTitle = "<h4>Playlist...</h4>"

  var openBody = '<div class="container-fluid"><div class="row"><form role="form"><div class="form-group">';
  openBody += '<input id="searchinput" class="form-control" type="search" placeholder="Search..." autofocus/></div>';
  openBody += '<div id="searchlist" class="list-group">';

  $.each(Object.values(data.ANSWER), function(i, obj){
    openBody += '<a href="#" id="' + obj.path +'" class="list-group-item list-group-item-action"><span>' + obj.name + '</span></a>';
  });
  openBody += '</div></form></div></div><script>';
  openBody += '$("#searchlist").btsListFilter("#searchinput", {itemChild: "span", initial: false, casesensitive: false,});';
  openBody += '$(".list-group a").click(function(){loadPLTrack($(this).attr("id"));});';
  openBody += '</script>';

  $modal.find('.modal-header').find('.modal-title').append(openTitle);
  $modal.find('.modal-body').append(openBody);

  $('#dynamicModal').modal('toggle');

}

function onPause(){
  ShowAlert("Sound is ready for analysis.", 'Next step : send upload to Watson by clicking ' + cogWheels , "alert-success");
  $('#send').focus();
  $('#send').effect("bounce",{times:20,distance:50},5000);
}

function loadPLTrack(file){
  var fileURL = file;
  var track = {};
  track.track = fileURL;

  $.ajax({
    type: 'POST',
    url: "LPLT",
    dataType: 'json',
    data: JSON.stringify(track),
    success: function(data) {
    },
    error: function(data) {
      console.log(data);
    }
  });

  var audio = $("#player");
  $("#sound").attr("src", fileURL);
  audio[0].pause();
  audio[0].load();
  $('#dynamicModal').modal('toggle');
  onPause();

}

function playSample(){
  var fileURL = "sounds/sound.mp3";
	var audio = $("#player");
	$("#sound").attr("src", fileURL);
	audio[0].pause();
	audio[0].load();
  audio[0].oncanplaythrough = audio[0].play();
}


function loadDatas(data){

  var rows = [];

  var objects = Object.values(data.ANSWER);

  $.each(objects, function(index, object){

    if(object.tones){
      rows.push({
        sentence: object.sentence,
        speaker: object.speaker,
        timestamp: object.when,
        excited: object.tones.excited,
        frustrated: object.tones.frustrated,
        impolite: object.tones.impolite,
        polite: object.tones.polite,
        sad: object.tones.sad,
        satisfied: object.tones.satisfied,
        sympathetic: object.tones.sympathetic
      });
    }

  })

  return rows;
}

function buildTable(cols){

  $('#answers').bootstrapTable({
      columns: cols,
      // url: url,
      // data: data,
      showToggle: false,
      search: true,
      onEditableInit: function(){
        //Fired when all columns was initialized by $().editable() method.
      },
      onEditableShown: function(editable, field, row, $el){
        //Fired when an editable cell is opened for edits.
      },
      onEditableHidden: function(field, row, $el, reason){
        //Fired when an editable cell is hidden / closed.
      },
      onEditableSave: function (field, row, oldValue, editable) {
        //Fired when an editable cell is saved.
      },
      onClickCell: function (field, value, row, $element){
      }
  });

}

function ShowAlert(title, message, alertType, timeout, area) {

    $('#alertmsg').remove();

    if(timeout == undefined){
      var timeout = 5000;
    }

    if(area == undefined){
      area = "bottom";
    }
    if(alertType.match('alert-warning')){
      area = "bottom";
      timeout = 20000;
    }
    if(alertType.match('alert-danger')){
      area = "bottom";
      timeout = 120000;
    }

    var $newDiv;

    if(alertType.match('alert-success|alert-info')){
      $newDiv = $('<div/>')
       .attr( 'id', 'alertmsg' )
       .html(
          '<h4>' + title + '</h4>' +
          '<p>' +
          message +
          '</p>'
        )
       .addClass('alert ' + alertType + ' flyover flyover-' + area);
    }
    else{
      $newDiv = $('<div/>')
       .attr( 'id', 'alertmsg' )
       .html(
          '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
          '<h4>' + title + '</h4>' +
          '<p>' +
          '<strong>' + message + '</strong>' +
          '</p>'
        )
       .addClass('alert ' + alertType + ' alert-dismissible flyover flyover-' + area);
    }

    $('#alert').append($newDiv);

    if ( !$('#alertmsg').is( '.in' ) ) {
      $('#alertmsg').addClass('in');

      setTimeout(function() {
         $('#alertmsg').removeClass('in');
      }, timeout);
    }
}

$('#logout').click(function (){
  logout();
})

function logout(){

  $('#modLogout').modal('toggle');

  return;
}
