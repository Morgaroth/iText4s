name := "iText4s"

scalaVersion := "2.12.6"

organization := "io.morgaroth"

val itextVersion = "5.5.12"

libraryDependencies ++= Seq(
  "com.itextpdf" % "itextpdf" % itextVersion,
  "joda-time" % "joda-time" % "2.9.9",
  "org.joda" % "joda-convert" % "1.8.2"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

bintrayVcsUrl := Some("https://gitlab.com/morgaroth/iText4s")

releaseTagComment := s"Releasing ${(version in ThisBuild).value} [skip ci]"

releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value} [skip ci]"