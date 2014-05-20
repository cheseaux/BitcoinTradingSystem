plotData = []
EMAValues = []
SMAValues = []
nDataInPlot = 2000;
sumSentiment = 0
totalSentiment = 0
blacklist = []
modalID = 0
fakePlot = [[1400076000, 434.5],[1400076500, 434.0],[1400077000, 434.5],[1400077500, 434.0],[1400078000, 434.5]]
transNumber = 0

$ ->

  $('#collapseOne').collapse("hide");
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
          if not isBlackListed(message.content)
            sumSentiment += message.sentiment
            totalSentiment += 1
            if sumSentiment > 10
              strSenti = 'Positive'
            else if sumSentiment < -10
              strSenti = 'Negative'
            else
              strSenti = 'Neutral'
            
            document.getElementById('senti').innerHTML =  strSenti
            document.getElementById('sentipercent').innerHTML =  'Index : ' + sumSentiment

            showtweet(message)
            $('body').append(getModalHTML(message))
            $('#btnNeg'+modalID).click -> 
              console.log("clicked neg")
              $.post( "http://jonathancheseaux.ch/saveCorrection.php", { tweet: message.content, sentiment: "negative" } );
            $('#btnNeu'+modalID).click -> 
              console.log("clicked neu")
              $.post( "http://jonathancheseaux.ch/saveCorrection.php", { tweet: message.content, sentiment: "neutral" } );
            $('#btnPos'+modalID).click -> 
              console.log("clicked pos")
              $.post( "http://jonathancheseaux.ch/saveCorrection.php", { tweet: message.content, sentiment: "positive" } );
            $('#btnIgn'+modalID).click -> 
              console.log("clicked spam " + message.content)
              blacklist.push(cleanTweet(message.content))
              console.log("added " + cleanTweet(message.content))
      when "gain"
        transNumber += 1
        console.log(message)
        totalGain = message.content
        document.getElementById('gain').innerHTML =  '$ ' + totalGain
        document.getElementById('transact').innerHTML =  transnumber + ' transactions'
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
    timezone: "browser"


getAxisMin = (data) ->
  Math.min.apply(Math, data) * 0.9999
getAxisMax = (data) ->
  Math.max.apply(Math, data) * 1.0001

  
  
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
    #console.log("EMA JSON Array", message.values)
    EMAValues = message.values
    EMAValues.sort()
    
updateSMAData = (message) ->
    #console.log("SMA JSON Array", message.values)
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
    
cleanTweet = (text) ->
    regex = /[-a-zA-Z0-9@:%_\+.~#?&//=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&//=]*)?/gi;
    cleanText = text.replace(regex, "")
    cleanText = cleanText.replace(/(^|\W+)\@([\w\-]+)/gm,'');
    return cleanText.replace /^\s+|\s+$/g, ""

isBlackListed = (text) ->
    return (blacklist.indexOf(cleanTweet(text)) != -1)
    
getModalHTML = (message) ->
    return '<div id="static'+modalID+'" class="modal fade" tabindex="-1" style="display: none;">'+
            '<div class="modal-header">'+
            '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>'+
            '<h4 class="modal-title">What is your sentiment about this tweet ?</h4>'+
            '</div><div class="modal-body">'+ formatTweet(message) +
            '<div class="modal-footer">' +
            '<button id="btnNeg'+modalID+'" type="button" data-dismiss="modal" class="btn btn-default">Negative</button>'+
            '<button id="btnNeu'+modalID+'" type="button" data-dismiss="modal" class="btn btn-default">Neutral</button>'+
            '<button id="btnPos'+modalID+'" type="button" data-dismiss="modal" class="btn btn-default">Positive</button>'+
            '<button id="btnIgn'+modalID+'" type="button" data-dismiss="modal" class="btn btn-default">Spam !</button>'+
            '<button type="button" data-dismiss="modal" class="btn btn-primary">Cancel</button>' + 
            '</div></div></div>'
            
formatTweet = (message) ->
    sentiment = message.sentiment
    if sentiment == -1
        strSentiment = 'negative'
    if sentiment == 1
        strSentiment = 'positive'
    return '<div class="root standalone-tweet ltr twitter-tweet not-touch" id="'+strSentiment+'">' +
    '<blockquote class="tweet subject expanded h-entry" class="btn btn-primary btn-lg" data-toggle="modal" href="#static'+modalID+'">' +    
    '<div class="header" style="padding-top: 0px;>' +
    '<div class="h-card p-author">'+
    '<a class="u-url profile">'+
    '<img class="u-photo avatar" src="'+message.imagesrc+'">'+
    '<span class="full-name">'+
    '<span class="p-name customisable-highlight">'+message.author+'</span>'+
    '</span>'+
    '<span class="p-nickname">'+message.content+'</span>'+
    '</div></div></blockquote></div>'

showtweet = (message) ->
  modalID += 1
  formatted = formatTweet(message)
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
