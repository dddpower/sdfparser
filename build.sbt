name := "sdfparser_renewal_laptop"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5";
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test";
libraryDependencies += "org.scala-graph" %% "graph-core" % "1.12.5";
libraryDependencies += "org.scala-graph" %% "graph-dot" % "1.12.1";
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0";
libraryDependencies += "org.scala-lang" % "scala-compiler" % "scalaVersion";
libraryDependencies += "com.eed3si9n" %% "treehugger" % "0.4.3";
resolvers += Resolver.sonatypeRepo("public");
