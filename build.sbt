
name := "iText4s"

version := "0.1.0"

scalaVersion := "2.12.3"

organization := "io.github.morgaroth"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

val itextVersion = "5.5.12"

libraryDependencies ++= Seq(
  "com.itextpdf" % "itextpdf" % itextVersion,
  "joda-time" % "joda-time" % "2.9.9",
  "org.joda" % "joda-convert" % "1.8.2"
)