tweets = []
plotData = []

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
#taille d'affichage du plot
window.ps = 0


root = exports ? this

#global variable containing plot size
nDataInPlot = 2000;

#begin and end times for plot graph
beginTime = 1400043400
endTime = 1500064800

@updatePlotTimeRange = updatePlotTimeRange = () ->
	inputBeginTime = document.getElementsByName('textboxbegintime')[0].value
	inputEndTime = document.getElementsByName('textboxendtime')[0].value
	beginTime = inputBeginTime
	endTime = inputEndTime
	drawValuesInRange(beginTime, endTime)
	#now we have to old values with shifting
	#while (plotData[0][0])


@updatePlotSize = updatePlotSize = () ->
	inputNData = document.getElementsByName('textboxplotsize')[0].value
	if (inputNData >= 1) and (inputNData <= 3000)
	  nDataInPlot = inputNData
	  
	  #redraw graph
	  drawLastValues(nDataInPlot)
	else
	  alert "invalid value, outside of : [1,3000]"
	window.ps = plotData.length

  
updateStockData = (message) ->
	plotData.push([message.time, message.price])


#redraws the plot every second, regardless of data pushed (to prevent freezes)  
setInterval ( ->
  drawLastValues(nDataInPlot)
  #drawValuesInRange(beginTime, endTime)
), 1000

    
drawLastValues = (numberOfValues) ->
	plotData.sort()
	#copying the array
	lastPlotData = clone(plotData)
	#resizing dataset locally
	while (lastPlotData.length >= numberOfValues)
	  lastPlotData.shift()
	  
	if ($("#chart").size() > 0)
      plot = $("#chart").data("plot")
      plot.setData([lastPlotData])
      #data2 = plot.getData()[0].data
      data = getPricesFromArray(lastPlotData)
	#setting the x axis
  
	xaxes = plot.getOptions().xaxes[0]
	xaxes.min = getXAxisMin(lastPlotData)
	xaxes.max = getXAxisMax(lastPlotData)
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


drawValuesInRange = (beginRange, endRange) ->
	
	#copying the array
	rangePlotData = clone(plotData)
	#resizing dataset locally
	while (rangePlotData[0][0] <= beginRange)
	  rangePlotData.shift()
	while (rangePlotData[rangePlotData.length - 1][0] >= endRange)
	  rangePlotData.pop()
	console.log("range from", beginRange)
	console.log("range to", endRange)  
	console.log("n. of vals to draw for range", rangePlotData.length)
	  
	if ($("#chart").size() > 0)
      plot = $("#chart").data("plot")
      plot.setData([rangePlotData])
      #data2 = plot.getData()[0].data
      data = getPricesFromArray(rangePlotData)
	#setting the x axis
	xaxes = plot.getOptions().xaxes[0]
	xaxes.min = getXAxisMin(rangePlotData)
	xaxes.max = getXAxisMax(rangePlotData)
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
	`$.getScript('http://platform.twitter.com/widgets.js');`
	$( ".root" ).remove();
	document.getElementById('tweetList').innerHTML = formattedTweets
	

    
showtweet = (message) ->
	sentiment = message.sentiment
	if sentiment == -1
		strSentiment = 'negative'
	if sentiment == 1
		strSentiment = 'positive'
		
	name = 'Jonathan Cheseaux'
	username = '@cheseaux'
	tweetURL = 'example.com'
	date = '22 December 1920'
	
	return '<blockquote class="twitter-tweet"><p>Currently testing: jQuery and CSS animations: fly-in - <a href="http://t.co/8sFm5wFM" title="http://jsfiddle.net/gabrieleromanato/km3TE/">jsfiddle.net/gabrieleromana…</a> for web apps</p>&mdash; Gabriele Romanato (@gabromanato) <a href="https://twitter.com/gabromanato/status/275673554408837120" data-datetime="2012-12-03T18:51:11+00:00">December 3, 2012</a></blockquote>
'

clone = (obj) ->
  if not obj? or typeof obj isnt 'object'
    return obj

  if obj instanceof Date
    return new Date(obj.getTime()) 

  if obj instanceof RegExp
    flags = ''
    flags += 'g' if obj.global?
    flags += 'i' if obj.ignoreCase?
    flags += 'm' if obj.multiline?
    flags += 'y' if obj.sticky?
    return new RegExp(obj.source, flags) 

  newInstance = new obj.constructor()

  for key of obj
    newInstance[key] = clone obj[key]

  return newInstance   
