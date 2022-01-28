package just.sysprocess

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.sys.process.ProcessLogger
import scala.util.control.NonFatal

/** Copied from sbt-devoops and modified.
  *
  * @author Kevin Lee
  * @since 2019-01-01
  */
enum SysProcess derives CanEqual {
  case SingleSysProcess(
    baseDir: Option[File],
    command: String,
    commands: List[String]
  )
}

object SysProcess {

  def singleSysProcess(baseDir: Option[File], command: String, commands: String*): SysProcess =
    SysProcess.SingleSysProcess(baseDir, command, commands.toList)

  def processResult(code: Int, resultCollector: ResultCollector): Either[ProcessError, ProcessResult] =
    if (code == 0) {
      /* Why concatenate outputs and errors in success?
       * Sometimes 'errors' has some part of success result. :(
       */
      Right(ProcessResult(resultCollector.outputs ++ resultCollector.errors))
    } else {
      Left(ProcessError.failure(code, resultCollector.errors))
    }

  extension (sysProcess: SysProcess) {

    def run(): Either[ProcessError, ProcessResult] =
      try {
        sysProcess match {
          case SingleSysProcess(baseDir, command, commands) =>
            val resultCollector = ResultCollector()
            val processBuilder  =
              baseDir.fold(
                sys.process.Process(command :: commands)
              )(dir => sys.process.Process(command :: commands, cwd = dir))

            val code = processBuilder ! resultCollector
            processResult(code, resultCollector)
        }
      } catch {
        case NonFatal(nonFatalThrowable) =>
          Left(ProcessError.failureWithNonFatal(nonFatalThrowable))
      }

  }

}

final class ResultCollector private (
  private val outs: ListBuffer[String],
  private val errs: ListBuffer[String]
) extends ProcessLogger
    derives CanEqual {

  def outputs: List[String] = outs.result()
  def errors: List[String]  = errs.result()

  override def out(s: => String): Unit = {
    outs += s
    ()
  }

  override def err(s: => String): Unit = {
    errs += s
    ()
  }

  override def buffer[T](f: => T): T = f

  override def toString: String =
    s"${getClass.getSimpleName}(outputs=${outputs.mkString("[", ",", "]")}, errors=${errors.mkString("[", ",", "]")})"
}

object ResultCollector {
  def apply(): ResultCollector = new ResultCollector(ListBuffer(), ListBuffer())

  def unapply(resultCollector: ResultCollector): Option[(List[String], List[String])] =
    Option((resultCollector.outputs, resultCollector.errors))
}

final case class ProcessResult(outputs: List[String]) derives CanEqual

enum ProcessError derives CanEqual {
  case Failure(code: Int, errors: List[String])
  case FailureWithNonFatal(throwable: Throwable)
}

object ProcessError {

  def failure(code: Int, errors: List[String]): ProcessError =
    ProcessError.Failure(code, errors)

  def failureWithNonFatal(throwable: Throwable): ProcessError =
    ProcessError.FailureWithNonFatal(throwable)

}
