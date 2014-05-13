tweets = []
plotData = [0, 0]

$ ->
  ws = new WebSocket $("body").data("ws-url")
  ws.onmessage = (event) ->
    message = JSON.parse event.data
    switch message.type
      when "stockhistory"
        populateStockHistory(message)
      when "stockupdate"
        updateStockData(message)
        #updateStockPlot()
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
  Math.min.apply(Math, data) * 0.999
getAxisMax = (data) ->
  Math.max.apply(Math, data) * 1.001

  
  
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
  plot = chart.plot([plotData], getChartOptions(message.history)).data("plot")
  
  #console.log("mesage history", message.history)
  #console.log("data", plot.getData()[0].data)
#compteur de messages recus
window.kl = 0



root = exports ? this

#global variable containing plot size
nDataInPlot = 100;


@updatePlotSize = updatePlotSize = () ->
	inputNData = document.getElementsByName('textboxplotsize')[0].value
	if (inputNData >= 1) and (inputNData <= 3000)
	  nDataInPlot = inputNData
	  #we have to resize the data array if it is bigger (if smaller will grow automatically), keeping the last values
	  while (plotData.length >= nDataInPlot)
	    plotData.shift()
	  #redraw graph
	  updateStockPlot()
	else
	  alert "invalid value, outside of : [1,3000]"
  
updateStockData = (message) ->


  if ($("#chart").size() > 0)
    plot = $("#chart").data("plot")
    
    
    data = getPricesFromArray(plotData)
    data.shift()
    data.push(message.price)
    #plot.setData([getChartArray(data)])   data was used before, without timestamps
    
    #console.log("seconds", message.seconds)
    #console.log("price", message.price)
    
    #we do not get the data from the plot itself
    #data2 = plot.getData()[0].data
  
  #check if we need to increase the plot size  
  if (plotData.length == 1) or (plotData.length >= nDataInPlot)
    plotData.shift()
	
  #trying to do something with the time
  timestamp = message.time
  timestamp = timestamp % (3600*24)
  
  window.kl++
  plotData.push([timestamp, message.price])
  
  plotData.sort()
  
  plot.setData([plotData])


#redraws the plot every second, regardless of data pushed (to prevent freezes)  
setInterval ( ->
  updateStockPlot()
), 1000
  
#method for redrawing the plot and axis
updateStockPlot = () ->
  if ($("#chart").size() > 0)
    plot = $("#chart").data("plot")
    #data2 = plot.getData()[0].data
    data = getPricesFromArray(plotData)
  #setting the x axis
  
  xaxes = plot.getOptions().xaxes[0]
  xaxes.min = getXAxisMin(plotData)
  xaxes.max = getXAxisMax(plotData)
  plot.setupGrid()
    
    # update the yaxes if either the min or max is now out of the acceptable range
  yaxes = plot.getOptions().yaxes[0]
  #if ((getAxisMin(data) < yaxes.min) || (getAxisMax(data) > yaxes.max))
    # reseting yaxes
  yaxes.min = getAxisMin(data)*1
  yaxes.max = getAxisMax(data)*1
  plot.setupGrid()
  # redraw the chart
  plot.draw()
  #console.log("data", data)
    
  

 
	
 #tweet array 
#max tweets
updatePrice = (message) ->
	document.getElementById('price').innerHTML =  '$' + message.price.toFixed(2)
    
showTweets = () ->
	formattedTweets = (showtweet(tweet) for tweet in tweets).reduceRight (x, y) -> x + "\n" + y
	document.getElementById('tweetList').innerHTML = formattedTweets
    
showtweet = (message) ->
	sentiment = message.sentiment
	str = 
	if sentiment == -1
		str = '<div class="negtweet" id="clickable">'
	if sentiment == 1
		str = '<div class="postweet" id="clickable">'
	
	str += message.symbol
	str += '<i class="icon-info-sign" id="info">click</i></div>'
	return str

    
