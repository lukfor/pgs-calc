function createPlot(selectedData) {
  return [{
    x: selectedData,
    type: 'histogram',
    name: 'Distribution',
    orientation: 'v',
    marker: {
      color: '#007bff',
      line: {
        color: 'white',
        width: 1
      }
    }
  }];
};

function createPlotLayout() {
  return {
    dragmode: 'select',
    hovermode: 'x',
    margin: {
      l: 50,
      r: 50,
      b: 50,
      t: 0,
      pad: 4
    },
    xaxis: {
      title: 'Score'
    },
    yaxis: {
      title: 'Count'
    },
    bargap: 0
  };
};

function updateSelection(eventData) {
  var selection = 0;
  var table = $('#selection-table tbody');
  var rows = '';
  eventData.points.forEach(function(pt) {
    selection += pt.pointIndices.length;
    for (var i = 0; i < pt.pointIndices.length; i++) {
      var index = pt.pointIndices[i];
      rows += '<tr><td>' + samples[index] + '</td><td>' + selectedData[index] + '</td></tr>';
    }
  });
  table.html(rows);
  $('#selection-header').html('Selection (' + selection + ')');
};

function updateScore(e) {
  var score = $(this).data('score');
  selectedData = data[score];
  if (selectedData) {
    $('#row-plots').show();
    //clear table
    var table = $('#selection-table tbody');
    table.html('');
    $('#selection-header').html('Selection (' + 0 + ')');
    var plotData = createPlot(selectedData);
    var layout = createPlotLayout();
    Plotly.react('plot', plotData, layout, {
      displayModeBar: false
    });

  } else {
    $('#row-plots').hide();
  }
}

function filterScores(e) {
  var input = $(this).val()
  var filter = input.toUpperCase()
  $('.list-group .list-group-item').each(function() {
    var anchor = $(this)
    if (anchor.data('meta').toUpperCase().indexOf(filter) > -1) {
      anchor.removeClass('d-none')
    } else {
      anchor.addClass('d-none');
    }
  });
}

$(document).ready(function() {

  $('#s').on('input', filterScores);
  $('.list-group-item').on('click', updateScore);

  selectedData = data['score1'];
  var plotData = createPlot(selectedData);
  var layout = createPlotLayout();
  Plotly.newPlot('plot', plotData, layout, {
    displayModeBar: false
  });

  var myPlot = document.getElementById('plot');
  var hoverInfo = myPlot.on('plotly_selected', updateSelection);

});
