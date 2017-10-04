
name := "iText4s"

scalaVersion := "2.12.3"

organization := "io.github.morgaroth"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

bintrayVcsUrl := Some("https://github.com/Morgaroth/iText4s")

val itextVersion = "5.5.12"

libraryDependencies ++= Seq(
  "com.itextpdf" % "itextpdf" % itextVersion,
  "joda-time" % "joda-time" % "2.9.9",
  "org.joda" % "joda-convert" % "1.8.2"
)