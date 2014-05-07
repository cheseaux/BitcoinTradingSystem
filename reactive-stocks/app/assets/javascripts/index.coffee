tweets = []

$ ->
  ws = new WebSocket $("body").data("ws-url")
  ws.onmessage = (event) ->
    message = JSON.parse event.data
    switch message.type
      when "stockhistory"
        populateStockHistory(message)
      when "stockupdate"
        updateStockChart(message)
        updatePrice(message)
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
populateStockHistory = (message) ->
  chart = $("#chart").addClass("chart")
  plot = chart.plot([getChartArray(message.history)], getChartOptions(message.history)).data("plot")

  
  
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
    data2.shift()
    data2.push([message.seconds, message.price])
    plot.setData([data2])
    
    
    # update the yaxes if either the min or max is now out of the acceptable range
    yaxes = plot.getOptions().yaxes[0]
    if ((getAxisMin(data) < yaxes.min) || (getAxisMax(data) > yaxes.max))
      # reseting yaxes
      yaxes.min = getAxisMin(data)
      yaxes.max = getAxisMax(data)
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
	sentiment = message.sentiment
	str = 
	if sentiment == -1
		str = '<div class="negtweet">'
	if sentiment == 1
		str = '<div class="postweet">'
	
	str += message.symbol
	#str += '</font>'
	str += '</div>'
	return str

    
