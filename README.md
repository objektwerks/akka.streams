Akka Streams
------------
>Akka Stream feature tests.

Test
----
1. sbt clean test

Run
---
>Runs StreamingChartApp, which dynamically updates a time series chart via an AkkaStream source and Akka scheduler.
1. sbt clean compile run
>**Warning:** The app fails to terminate completely due to an Sbt conflict.
>Use Control-C from commandline. Or select the Java app and Quit menu item.