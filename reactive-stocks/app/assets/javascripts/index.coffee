tweets = []
plotData = []
EMAValues = []
SMAValues = []
nDataInPlot = 2000;
sumSentiment = 0
blacklist = ["USA Government trying to shutdown Bitcoin network read more here:"]



$ ->
  ws = new WebSocket $("body").data("ws-url")
  ws.onmessage = (event) ->
    message = JSON.parse event.data
    switch message.type
      when "stockhistory"
        populateStockHistory(message)
      when "stockupdate"
        updateStockData(message)
        updatePrice(message)
      when "EMA"
        updateEMAData(message)
      when "SMA"
        updateSMAData(message)
      when "tweet"
      	if message.sentiment != 0
      	  if not 0 #isBlackListed(message.content)
            sumSentiment += message.sentiment
            tweets.push message
            showtweet(message)
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
    mode: "time"
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
  plot = chart.plot([plotData, EMAValues, SMAValues], getChartOptions(message.history)).data("plot")
  
  #console.log("mesage history", message.history)
  #console.log("data", plot.getData()[0].data)

#compteur de messages recus
window.kl = 0
#taille d'affichage du plot
window.ps = 0


root = exports ? this



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
	if (inputNData >= 1) and (inputNData <= plotData.length-1)
	  nDataInPlot = inputNData
	  
	  #redraw graph
	  drawLastValues(nDataInPlot)
	else
	  alert "invalid value, max value is " + (plotData.length - 1)
	window.ps = plotData.length

  
updateStockData = (message) ->
	plotData.push([message.time, message.price])

updateEMAData = (message) ->
	console.log("EMA JSON Array", message.values)
	EMAValues = message.values
	EMAValues.sort()
	
updateSMAData = (message) ->
	console.log("SMA JSON Array", message.values)
	SMAValues = message.values
	SMAValues.sort()

#redraws the plot every second, regardless of data pushed (to prevent freezes)  
setInterval ( ->
  drawLastValues(nDataInPlot)
  #drawValuesInRange(beginTime, endTime)
), 1000


#plots last values for real price, but plots all of ema values    
drawLastValues = (numberOfValues) ->
	plotData.sort()
	#copying the array
	lastPlotData = clone(plotData)
	#resizing dataset locally
	while (lastPlotData.length >= numberOfValues)
	  lastPlotData.shift()
	  
	if ($("#chart").size() > 0)
      plot = $("#chart").data("plot")
      plot.setData([lastPlotData, EMAValues, SMAValues])
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
    

isBlackListed = (text) ->
	regex = /[-a-zA-Z0-9@:%_\+.~#?&//=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&//=]*)?/gi;
	cleanText = text.replace(regex, "")
	cleanText = cleanText.replace(/(^|\W+)\@([\w\-]+)/gm,'');
	cleanText = cleanText.replace /^\s+|\s+$/g, ""
	return (blacklist.indexOf(cleanText) != -1)

showtweet = (message) ->
	sentiment = message.sentiment
	if sentiment == -1
		strSentiment = 'negative'
	if sentiment == 1
		strSentiment = 'positive'

	formatted = '<div class="root standalone-tweet ltr twitter-tweet not-touch" id="'+strSentiment+'">' +
	'<blockquote class="tweet subject expanded h-entry" >' +
	'<div class="header" style="padding-top: 0px;>' +
	'<div class="h-card p-author">'+
	'<a class="u-url profile" href="https://twitter.com/'+message.author+'">'+
	'<img class="u-photo avatar" src="'+message.imagesrc+'">'+
	'<span class="full-name">'+
	'<span class="p-name customisable-highlight">'+message.author+'</span>'+
	'</span>'+
	'<span class="p-nickname">'+message.content+'</span>'+
	'</div></div></blockquote></div>'

	$('#tweetList').prepend(formatted)


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
