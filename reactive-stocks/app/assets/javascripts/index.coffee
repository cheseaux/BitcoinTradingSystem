tweets = []

$ ->
  ws = new WebSocket $("body").data("ws-url")
  ws.onmessage = (event) ->
    message = JSON.parse event.data
    switch message.type
      when "stockhistory"
        populateStockHistory(message)
      #when "stockupdate"
        #updateStockChart(message)
        #updatePrice(message)
      when "tweet"
      	if message.sentiment != 0 
       	  tweets.push message
          showTweets()
      else
        console.log(message)

	

  $("#addsymbolform").submit (event) ->
    event.preventDefault()
    # send the message to watch the stock
    ws.send(JSON.stringify({symbol: $("#addsymboltext").val()}))
    # reset the form
    $("#addsymboltext").val("")
    
    
getPricesFromArray = (data) ->
  (v[1] for v in data)
  
  
getChartArray = (data) ->
  ([i, v] for v, i in data)
  
  
getChartOptions = (data) ->
  series:
    shadowSize: 0
  yaxis:
    min: 400
    max: 410
  xaxis:
    show: true
    #mode: time
    #timeformat: "%Y/%m/%d"


getAxisMin = (data) ->
  Math.min.apply(Math, data) * 0.98
getAxisMax = (data) ->
  Math.max.apply(Math, data) * 1.02

  
  
getXAxisMin = (fulldata) ->
  a = (v[0] for v in fulldata)
  return Math.min.apply(Math, a)
  
getXAxisMax = (fulldata) ->
  a = (v[0] for v in fulldata)
  return Math.max.apply(Math, a)
  
  
populateStockHistory = (message) ->
  
  
  #populates with history, we do not want
  #plot = chart.plot([getChartArray(message.history)], getChartOptions(message.history)).data("plot")
  chart = $("#chart").addClass("chart")
  plot = chart.plot([[0, 0]], getChartOptions(message.history)).data("plot")
  
  #console.log("mesage history", message.history)
  #console.log("data", plot.getData()[0].data)
#compteur de messages recus
window.kl = 0
  
updateStockChart = (message) ->
  if ($("#chart").size() > 0)
    plot = $("#chart").data("plot")
    data = getPricesFromArray(plot.getData()[0].data)
    data.shift()
    data.push(message.price)
    #plot.setData([getChartArray(data)])   data was used before, without timestamps
    
    #console.log("seconds", message.seconds)
    #console.log("price", message.price)
    
    data2 = plot.getData()[0].data
    
  if (data2.length == 1) or (data2.length >= 100)
    data2.shift()
	
  window.kl++
  data2.push([message.time, message.price])
  
  data2.sort()
  
  plot.setData([data2])
  
  #setting the x axis
  
  xaxes = plot.getOptions().xaxes[0]
  xaxes.min = getXAxisMin(data2)
  xaxes.max = getXAxisMax(data2)
  plot.setupGrid()
    
    # update the yaxes if either the min or max is now out of the acceptable range
  yaxes = plot.getOptions().yaxes[0]
  #if ((getAxisMin(data) < yaxes.min) || (getAxisMax(data) > yaxes.max))
    # reseting yaxes
  yaxes.min = getAxisMin(data)*0.98
  yaxes.max = getAxisMax(data)*1.02
  plot.setupGrid()
  # redraw the chart
  plot.draw()
  #console.log("data", data)
    
    
    
  
root = exports ? this
 
	
 #tweet array 
#max tweets
updatePrice = (message) ->
	document.getElementById('price').innerHTML =  '<font color="FFFF33", font size=2>Price :  </font>' + message.price + '$'
    
showTweets = () ->
	formattedTweets = (showtweet(tweet) for tweet in tweets).reduceRight (x, y) -> x + "\n" + y
	document.getElementById('tweetList').innerHTML = formattedTweets
    
showtweet = (message) ->
<<<<<<< HEAD
	randomnumber = Math.random()
	sentiment = message.sentiment
	if sentiment = -1
		tweets.push '<font color="FF0000">'
	else if sentiment = 1
		tweets.push '<font color="00FF00">'
	else 
		tweets.push '<font color="000000">'
	
	tweets.push message.symbol
	tweets.push '</font>'
	tweets.push '<hr>'
	
=======
	sentiment = message.sentiment
	str = 
	if sentiment == -1
		str = '<div class="negtweet">'
	if sentiment == 1
		str = '<div class="postweet">'
>>>>>>> 1b5a9a77421179e84d17e4cebee7f29460974fd7
	
	str += message.symbol
	#str += '</font>'
	str += '</div>'
	return str

    
<<<<<<< HEAD
handleFlip = (container) ->
  if (container.hasClass("flipped"))
    container.removeClass("flipped")
    container.find(".details-holder").empty()
  else
    container.addClass("flipped")
    # fetch stock details and tweet
    $.ajax
      url: "/sentiment/" + container.children(".flipper").attr("data-content")
      dataType: "json"
      context: container
      success: (data) ->
        detailsHolder = $(this).find(".details-holder")
        detailsHolder.empty()
        switch data.label
          when "pos"
            detailsHolder.append($("<h4>").text("The tweets say BUY!"))
            detailsHolder.append($("<img>").attr("src", "/assets/images/buy.png"))
          when "neg"
            detailsHolder.append($("<h4>").text("The tweets say SELL!"))
            detailsHolder.append($("<img>").attr("src", "/assets/images/sell.png"))
          else
            detailsHolder.append($("<h4>").text("The tweets say HOLD!"))
            detailsHolder.append($("<img>").attr("src", "/assets/images/hold.png"))
      error: (jqXHR, textStatus, error) ->
        detailsHolder = $(this).find(".details-holder")
        detailsHolder.empty()
        detailsHolder.append($("<h2>").text("Error: " + JSON.parse(jqXHR.responseText).error))
    # display loading info
    detailsHolder = container.find(".details-holder")
    detailsHolder.append($("<h4>").text("Determing whether you should buy or sell based on the sentiment of recent tweets..."))
    detailsHolder.append($("<div>").addClass("progress progress-striped active").append($("<div>").addClass("bar").css("width", "100%")))

`
$(function() {

		// We use an inline data source in the example, usually data would
		// be fetched from a server

		var data = [],
			totalPoints = 300;

		function getRandomData() {

			if (data.length > 0)
				data = data.slice(1);

			// Do a random walk

			while (data.length < totalPoints) {

				var prev = data.length > 0 ? data[data.length - 1] : 50,
					y = prev + Math.random() * 10 - 5;

				if (y < 0) {
					y = 0;
				} else if (y > 100) {
					y = 100;
				}

				data.push(y);
			}

			// Zip the generated y values with the x values

			var res = [];
			for (var i = 0; i < data.length; ++i) {
				res.push([i, data[i]])
			}

			return res;
		}

		// Set up the control widget

		var updateInterval = 30;
		$("#updateInterval").val(updateInterval).change(function () {
			var v = $(this).val();
			if (v && !isNaN(+v)) {
				updateInterval = +v;
				if (updateInterval < 1) {
					updateInterval = 1;
				} else if (updateInterval > 2000) {
					updateInterval = 2000;
				}
				$(this).val("" + updateInterval);
			}
		});

		var plot = $.plot("#placeholder", [ getRandomData() ], {
			series: {
				shadowSize: 0	// Drawing is faster without shadows
			},
			yaxis: {
				min: 0,
				max: 100
			},
			xaxis: {
				show: false
			}
		});

		function update() {

			plot.setData([getRandomData()]);

			// Since the axes don't change, we don't need to call plot.setupGrid()

			plot.draw();
			setTimeout(update, updateInterval);
		}

		update();

		// Add the Flot version string to the footer

		$("#footer").prepend("Flot " + $.plot.version + " &ndash; ");
	});
`
=======
>>>>>>> 1b5a9a77421179e84d17e4cebee7f29460974fd7
