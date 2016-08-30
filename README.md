# IMCE Third-Party Other Scala Libraries

This project builds an aggregate of selected Scala-related libraries published
by several organizations:

- [org.scalaz](https://github.com/scalaz/scalaz) An extension of the core Scala library for functional programming.

- [com.typesafe](https://github.com/typesafehub/config) Typesafe's Configuration library for JVM languages.

- [org.scalacheck](https://scalacheck.org) Property-based testing for Scala.

- [org.scalatest](https://scalatest.org) Flexible & productive testing for the Scala ecosystem.

- [org.specs2](https://etorreborre.github.io/specs2/) Software specifications for Scala.

- [org.parboiled](https://github.com/sirthias/parboiled) Elegant parsing in Java & Scala.

- [com.typesafe.akka](http://akka.io) Toolkit & runtime for building highly concurrent, distributed & resilient message-driven applications on the JVM.

- [io.spray](http://spray.io) Elegant, high-performance HTTP for Akka actors.

- [com.typesafe.play](https://www.playframework.com) High velocity web framework forJava & Scala

[![Build Status](https://travis-ci.org/JPL-IMCE/imce.third_party.other_scala_libraries.svg?branch=master)](https://travis-ci.org/JPL-IMCE/imce.third_party.other_scala_libraries)
[ ![Download](https://api.bintray.com/packages/jpl-imce/gov.nasa.jpl.imce/imce.third_party.other_scala_libraries/images/download.svg) ](https://bintray.com/jpl-imce/gov.nasa.jpl.imce/imce.third_party.other_scala_libraries/_latestVersion)
 
## Usage

```
    resolvers += Resolver.bintrayRepo("jpl-imce", "gov.nasa.jpl.imce"),

    libraryDependencies += 
      "gov.nasa.jpl.imce" %% "imce.third_party.other_scala_libraries" % "<version>"
        artifacts
        Artifact("imce.third_party.other_scala_libraries", "zip", "zip", Some("resource"), Seq(), None, Map())
```