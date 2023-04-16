Akka Streams
------------
>Akka Stream feature tests.

Note
----
>Akka Streams does not always work well with Scala 3.
>In particular, using the GraphDSL is problematic.

Test
----
1. sbt clean test

Run
---
>Runs StreamingChartApp, which dynamically updates a time series chart via an Akka Stream source and Akka scheduler.
1. sbt clean run