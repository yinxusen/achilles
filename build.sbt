name := "Achilles"
 
version := "0.1"
 
scalaVersion := "2.10.2"
 
resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-kernel" % "2.2.3",
  "org.scalanlp" % "chalk" % "1.3.0",
  "org.scalanlp" % "breeze_2.10" % "0.5.2",
  "org.apache.lucene" % "lucene-analyzers-smartcn" % "4.5.1",
  "commons-dbutils" % "commons-dbutils" % "1.5",
  "mysql" % "mysql-connector-java" % "5.1.26"
)
