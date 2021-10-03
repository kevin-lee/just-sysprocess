# just-sysprocess

Just SysProcess

[![Build Status](https://github.com/Kevin-Lee/just-sysprocess/workflows/Build-All/badge.svg)](https://github.com/Kevin-Lee/just-sysprocess/actions?workflow=Build-All)
[![Release Status](https://github.com/Kevin-Lee/just-sysprocess/workflows/Release/badge.svg)](https://github.com/Kevin-Lee/just-sysprocess/actions?workflow=Release)

[![Latest version](https://index.scala-lang.org/kevin-lee/just-sysprocess/latest.svg)](https://index.scala-lang.org/kevin-lee/just-sysprocess)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kevinlee/just-sysprocess_2.13/badge.svg)](https://search.maven.org/artifact/io.kevinlee/just-sysprocess_2.13)


```scala
libraryDependencies += "io.kevinlee" %% "just-sysprocess" % "1.0.0"
```

## Example

### Scala 2.11 ~ 2.13
Run on Scastie: https://scastie.scala-lang.org/qb8MfQbxTCql9ZhTR1POCw
```scala
import just.sysprocess._

val sysProcess = SysProcess.singleSysProcess(None, "ls")

val result: Either[ProcessError, ProcessResult] = sysProcess.run()
result match {
  case Right(ProcessResult(result)) =>
    println(result.mkString("Files: \n  -", "\n  -", "\n"))
  
  case Left(ProcessError.Failure(code, error)) =>
    println(s"[ERROR] Failed: code: $code, ${error.mkString("\n")}")
  
  case Left(ProcessError.FailureWithNonFatal(nonFatalThrowable)) =>
    println(s"[ERROR] ${nonFatalThrowable.getMessage}")
}
```

### Scala 3 (Dotty) 
Run on Scastie: https://scastie.scala-lang.org/fe7qYl9uTkSOX9VpyHZzHw

```scala
import just.sysprocess._

@main def runApp: Unit = {
  val sysProcess = SysProcess.singleSysProcess(None, "ls")

  val result: Either[ProcessError, ProcessResult] = sysProcess.run()
  result match {
    case Right(ProcessResult(result)) =>
      println(result.mkString("Files: \n  -", "\n  -", "\n"))
    case Left(ProcessError.Failure(code, error)) =>
      println(s"[ERROR] Failed: code: $code, ${error.mkString("\n")}")
    case Left(ProcessError.FailureWithNonFatal(nonFatalThrowable)) =>
      println(s"[ERROR] ${nonFatalThrowable.getMessage}")
  }
}
```
