import sbt.Keys._
import sbt._
import xerial.sbt._

import gov.nasa.jpl.imce.sbt._

useGpg := true

developers := List(
  Developer(
    id="rouquett",
    name="Nicolas F. Rouquette",
    email="nicolas.f.rouquette@jpl.nasa.gov",
    url=url("https://gateway.jpl.nasa.gov/personal/rouquett/default.aspx")))

val resourceArtifact = settingKey[Artifact]("Specifies the project's resource artifact")

def IMCEThirdPartyProject(projectName: String, location: String): Project =
  Project(projectName, file("."))
    .enablePlugins(IMCEGitPlugin)
    .enablePlugins(IMCEReleasePlugin)
    .settings(IMCEReleasePlugin.packageReleaseProcessSettings)
    .settings(
      IMCEKeys.targetJDK := IMCEKeys.jdk18.value,
      IMCEKeys.licenseYearOrRange := "2015-2016",
      IMCEKeys.organizationInfo := IMCEPlugin.Organizations.thirdParty,
      git.baseVersion := Versions.version,
      scalaVersion := Versions.scala_version
    )
    .settings(

      // disable publishing the main jar produced by `package`
      publishArtifact in(Compile, packageBin) := false,

      // disable publishing the main API jar
      publishArtifact in(Compile, packageDoc) := false,

      // disable publishing the main sources jar
      publishArtifact in(Compile, packageSrc) := false,

      // disable publishing the jar produced by `test:package`
      publishArtifact in(Test, packageBin) := false,

      // disable publishing the test API jar
      publishArtifact in(Test, packageDoc) := false,

      // disable publishing the test sources jar
      publishArtifact in(Test, packageSrc) := false,

      // name the '*-resource.zip' in the same way as other artifacts
      com.typesafe.sbt.packager.Keys.packageName in Universal :=
        normalizedName.value + "_" + scalaBinaryVersion.value + "-" + version.value,

      resourceArtifact := Artifact((name in Universal).value, "zip", "zip", Some("resource"), Seq(), None, Map()),

      artifacts += resourceArtifact.value,

      // contents of the '*-resource.zip' to be produced by 'universal:packageBin'
      mappings in Universal <++= (
        appConfiguration,
        classpathTypes,
        update,
        streams) map {
        (appC, cpT, up, s) =>

          def getFileIfExists(f: File, where: String)
          : Option[(File, String)] =
            if (f.exists()) Some((f, s"$where/${f.getName}")) else None

          val ivyHome: File =
            Classpaths
              .bootIvyHome(appC)
              .getOrElse(sys.error("Launcher did not provide the Ivy home directory."))

          val libDir = location + "/lib/"
          val srcDir = location + "/lib.sources/"
          val docDir = location + "/lib.javadoc/"

          s.log.info(s"====== $projectName =====")

          val providedOrganizationArtifacts: Set[String] = (for {
            cReport <- up.configurations
            if Configurations.Provided.name == cReport.configuration
            oReport <- cReport.details
            mReport <- oReport.modules
            (artifact, file) <- mReport.artifacts
            if "jar" == artifact.extension
          } yield {
            s.log.info(s"provided: ${oReport.organization}, ${file.name}")
            s"{oReport.organization},${oReport.name}"
          }).to[Set]

          val fileArtifacts = for {
            cReport <- up.configurations
            if Configurations.Compile.name == cReport.configuration
            oReport <- cReport.details
            organizationArtifactKey = s"{oReport.organization},${oReport.name}"
            if !providedOrganizationArtifacts.contains(organizationArtifactKey)
            mReport <- oReport.modules
            (artifact, file) <- mReport.artifacts
            if "jar" == artifact.extension
          } yield (oReport.organization, oReport.name, file, artifact)

          val fileArtifactsByType = fileArtifacts.groupBy { case (_, _, _, a) =>
            a.`classifier`.getOrElse(a.`type`)
          }
          val jarArtifacts = fileArtifactsByType("jar")
          val srcArtifacts = fileArtifactsByType("sources")
          val docArtifacts = fileArtifactsByType("javadoc")

          val jars = jarArtifacts.map { case (o, _, jar, _) =>
            s.log.info(s"* jar: $o/${jar.name}")
            jar -> (libDir + jar.name)
          }
          val srcs = srcArtifacts.map { case (o, _, jar, _) =>
            s.log.info(s"* src: $o/${jar.name}")
            jar -> (srcDir + jar.name)
          }
          val docs = docArtifacts.map { case (o, _, jar, _) =>
            s.log.info(s"* doc: $o/${jar.name}")
            jar -> (docDir + jar.name)
          }

          jars ++ srcs ++ docs
      },

      artifacts <+= (name in Universal) { n => Artifact(n, "zip", "zip", Some("resource"), Seq(), None, Map()) },
      packagedArtifacts <+= (packageBin in Universal, name in Universal) map { (p, n) =>
        Artifact(n, "zip", "zip", Some("resource"), Seq(), None, Map()) -> p
      }
    )

lazy val otherLibs = IMCEThirdPartyProject("other-scala-libraries", "otherLibs")
  .settings(
    resolvers += new MavenRepository("bintray-pchiusano-scalaz-stream", "http://dl.bintray.com/pchiusano/maven"),
    libraryDependencies ++= Seq(
      "gov.nasa.jpl.imce.thirdParty" %% "scala-libraries" % Versions.scala_libraries % "provided" artifacts
        Artifact("scala-libraries", "zip", "zip", Some("resource"), Seq(), None, Map()),

      "org.scalaz" %% "scalaz-core" % Versions.scalaz % "compile" withSources() withJavadoc(),
      "org.scalaz" %% "scalaz-effect" % Versions.scalaz % "compile" withSources() withJavadoc(),
      "org.scalaz" %% "scalaz-concurrent" % Versions.scalaz % "compile" withSources() withJavadoc(),
      "org.scalaz" %% "scalaz-iteratee" % Versions.scalaz % "compile" withSources() withJavadoc(),
      "org.scalaz" %% "scalaz-scalacheck-binding" % Versions.scalaz % "compile" withSources() withJavadoc(),
      "org.scalaz" %% "scalaz-xml" % Versions.scalaz_xml % "compile" withSources() withJavadoc(),

      "com.typesafe" % "config" % Versions.config % "compile" withSources() withJavadoc(),

      "org.scalacheck" %% "scalacheck" % Versions.scalaCheck % "compile" withSources() withJavadoc(),

      "org.scalatest" %% "scalatest" % Versions.scalaTest % "compile" withSources() withJavadoc(),

      "org.specs2" %% "specs2-core" % Versions.specs2 % "compile" withSources() withJavadoc(),

      "org.scalaz.stream" %% "scalaz-stream" % Versions.scalaz_stream % "compile" withSources() withJavadoc(),

      "org.parboiled" %% "parboiled" % Versions.parboiled % "compile" withSources() withJavadoc(),

      "com.typesafe.akka" %% "akka-actor" % Versions.akka % "compile" withSources() withJavadoc(),
      "com.typesafe.akka" %% "akka-testkit" % Versions.akka % "compile" withSources() withJavadoc(),

      "io.spray" %% "spray-can" % Versions.spray % "compile" withSources() withJavadoc(),
      "io.spray" %% "spray-routing-shapeless2" % Versions.spray_routing_shapeless % "compile" withSources() withJavadoc(),
      "io.spray" %% "spray-testkit" % Versions.spray % "compile" withSources() withJavadoc(),

      "com.typesafe.play" %% "play" % Versions.play % "compile" withSources(),
      "com.typesafe.play" %% "play-iteratees" % Versions.play % "compile" withSources(),
      "com.typesafe.play" %% "play-json" % Versions.play % "compile" withSources(),
      "com.typesafe.play" %% "play-functional" % Versions.play % "compile" withSources()
    ),
    libraryDependencies ~= {
      _ map {
        case m if m.organization == "com.typesafe.play" =>
          m.
            exclude("commons-codec", "commons-codec").
            exclude("commons-logging", "commons-logging").
            exclude("com.typesafe", "config").
            exclude("com.typesafe.play", "sbt-link").
            exclude("org.slf4j", "slf4j-api").
            exclude("org.slf4j", "slf4j-nop").
            exclude("org.slf4j", "jcl-over-slf4j").
            exclude("ch.qos.logback", "logback-classic")
        case m => m
      }
    })
