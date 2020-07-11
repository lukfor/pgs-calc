function createPlot(selectedData, highlightedSamples) {

  //histogram of score
  var plotData = [];
  plotData.push({
    x: selectedData,
    type: 'histogram',
    name: 'Distribution Score',
    orientation: 'v',
    marker: {
      color: '#007bff',
      line: {
        color: 'white',
        width: 1
      }
    }
  });

  //scatter plot of selected samples
  if (highlightedSamples && highlightedSamples.length > 0) {
    var x = [];
    var y = [];
    var text = [];
    for (var i = 0; i < highlightedSamples.length; i++) {
      x.push(selectedData[highlightedSamples[i]]);
      y.push(0);
      text.push(samples[highlightedSamples[i]]);
    }
    plotData.push({
      x: x,
      y: y,
      type: 'scatter',
      mode: 'markers',
      name: 'Highlighted Samples',
      text: text,
      marker: {
        color: 'red'
      }
    });
  }

  return plotData;

};

function createPlotLayout() {
  return {
    showlegend: false,
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
  selectedSamples = [];
  eventData.points.forEach(function(pt) {
    if (pt.pointIndices) {
      selection += pt.pointIndices.length;
      for (var i = 0; i < pt.pointIndices.length; i++) {
        var index = pt.pointIndices[i];
        selectedSamples.push(index);
        rows += '<tr><td>' + samples[index] + '</td><td>' + selectedData[index] + '</td></tr>';
      }
    }
  });
  table.html(rows);
  $('#selection-header').html('Selection (' + selection + ')');
};

function highlightAllSelectedSamples() {
  for (var i = 0; i < selectedSamples.length; i++) {
    var sample = selectedSamples[i];
    if (!highlightedSamples.includes(sample)) {
      highlightedSamples.push(sample);
    }
  }
  updatePlots();
  updateHighlightSample();
}

function unHighlightAllSelectedSamples() {
  for (var i = 0; i < selectedSamples.length; i++) {
    var sample = selectedSamples[i];
    var index = highlightedSamples.indexOf(sample);
    if (index > -1) {
      highlightedSamples.splice(index, 1);
    }
  }
  updatePlots();
  updateHighlightSample();
}

function clearHighlightedSamples() {
  highlightedSamples = [];
  updatePlots();
  updateHighlightSample();
}

function addHighlightedSamples() {

  bootbox.prompt({
    title: 'Samples',
    message: '<p><small class="text-muted">To specify more than one sample ID, please separate the IDs with a new line.</small></p>',
    inputType: 'textarea',
    callback: function(result) {
      if (result) {
        var lines = result.match(/[^\r\n]+/g);
        for (var i = 0; i < lines.length; i++) {
          var sample = lines[i];
          var index = samples.indexOf(sample);
          if (index > -1) {
            if (!highlightedSamples.includes(sample)) {
              highlightedSamples.push(index);
            }
          }
        }
        updatePlots();
        updateHighlightSample();
      }
    }
  });

}

function updateHighlightSample() {
  $('#highlight-samples-header').html('Highlighted Samples (' + highlightedSamples.length + ')');
}

function updateScore(e) {
  var score = $(this).data('score');
  selectedData = data[score];
  if (selectedData) {
    $('#row-plots').show();
    //clear table
    var table = $('#selection-table tbody');
    table.html('');
    selectedSamples = [];
    $('#selection-header').html('Selection (' + 0 + ')');
    updatePlots();

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

function updatePlots() {
  var plotData = createPlot(selectedData, highlightedSamples);
  var layout = createPlotLayout();
  Plotly.react('plot', plotData, layout, {
    displayModeBar: false
  });
}

$(document).ready(function() {

  //event handler
  $('#s').on('input', filterScores);
  $('.list-group-item').on('click', updateScore);
  $('#highlight-selection-button').on('click', highlightAllSelectedSamples);
  $('#unhighlight-selection-button').on('click', unHighlightAllSelectedSamples);
  $('#clear-highlighted-samples-button').on('click', clearHighlightedSamples);
  $('#add-highlighted-samples-button').on('click', addHighlightedSamples);

  highlightedSamples = [];
  updateHighlightSample();

  selectedSamples = [];
  selectedData = data['score1'];
  var plotData = createPlot(selectedData, highlightedSamples);
  var layout = createPlotLayout();
  Plotly.newPlot('plot', plotData, layout, {
    displayModeBar: false
  });

  var myPlot = document.getElementById('plot');
  var hoverInfo = myPlot.on('plotly_selected', updateSelection);

});
