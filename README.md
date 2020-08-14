# just-sysprocess

Just SysProcess

[![Build Status](https://github.com/Kevin-Lee/just-sysprocess/workflows/Build-All/badge.svg)](https://github.com/Kevin-Lee/just-sysprocess/actions?workflow=Build-All)
[![Release Status](https://github.com/Kevin-Lee/just-sysprocess/workflows/Release/badge.svg)](https://github.com/Kevin-Lee/just-sysprocess/actions?workflow=Release)

[![Latest version](https://index.scala-lang.org/kevin-lee/just-sysprocess/latest.svg)](https://index.scala-lang.org/kevin-lee/just-sysprocess)
[![Download](https://api.bintray.com/packages/kevinlee/maven/just-sysprocess/images/download.svg)](https://bintray.com/kevinlee/maven/just-sysprocess/_latestVersion) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kevinlee/just-sysprocess_2.13/badge.svg)](https://search.maven.org/artifact/io.kevinlee/just-sysprocess_2.13)


> **NOTE: `just-sysprocess` has not been released yet.**

```scala
libraryDependencies += "io.kevinlee" %% "just-sysprocess" % "0.1.0"
```

```scala
import just.sysprocess._

val sysProcess = SysProcess.singleSysProcess(None, "ls")

SysProcess.run(sysProcess)
  .toEither {
      case ProcessResult.Success(result) =>
        println(s"Success: ${result.mkString("\n")}")
      
      case ProcessResult.Failure(code, error) =>
        println(s"Failed: code: $code, ${error.mkString("\n")}")
    
      case ProcessResult.FailureWithNonFatal(nonFatalThrowable) =>
        println(nonFatalThrowable.getMessage)
    }
```
