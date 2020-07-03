# just-sysprocess
Just SysProcess

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
