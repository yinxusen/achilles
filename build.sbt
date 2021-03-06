name := "Achilles"

version := "0.1"

scalaVersion := "2.10.4"

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype Snapshots 1" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Snapshots 2" at "https://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-kernel" % "2.2.3",
  "com.typesafe.akka" %% "akka-remote" % "2.2.3",
  "org.scalanlp" % "chalk" % "1.3.0",
  "org.scalanlp" %% "breeze" % "0.4",
  "org.apache.lucene" % "lucene-analyzers-smartcn" % "4.5.1",
  "commons-dbutils" % "commons-dbutils" % "1.5",
  "mysql" % "mysql-connector-java" % "5.1.26",
  "commons-codec" % "commons-codec" % "1.4",
  "tomcat" % "commons-logging" % "4.0.6",
  "javax.mail" % "mail" % "1.4",
  "tomcat" % "tomcat-juli" % "5.5.12",
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "org.codehaus.jackson" % "jackson-jaxrs" % "1.8.5",
  "org.apache.tomcat" % "tomcat-jdbc" % "7.0.47",
  "org.slf4j" % "slf4j-log4j12" % "1.7.5"
)
