BitcoinTradingSystem
====================

Bitcoin pricing prediction and trading simulation through time series and sentiment analysis.


## Prerequisits
To compile and run the software, a JVM, Scala and SBT must be installed.

SBt need the following plugin line in plugins.sbt

    addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.4.0")


## Compilation
There is compile script in the base directory.

    $ ./setup.sh


## Run
Execute the following commands the run the software and the user interface


    $ ./run_crawler.sh
    $ sleep 30s
    $ ./run_ui.sh

20 seconds after ui was run, navigate to localhost:9000 with you browser.
