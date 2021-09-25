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
sealed trait SysProcess

object SysProcess {

  final case class SingleSysProcess(
    baseDir: Option[File],
    command: String,
    commands: List[String]
  ) extends SysProcess

  def singleSysProcess(baseDir: Option[File], command: String, commands: String*): SysProcess =
    SingleSysProcess(baseDir, command, commands.toList)

  @SuppressWarnings(Array("org.wartremover.warts.Equals", "org.wartremover.warts.Nothing"))
  def processResult(code: Int, resultCollector: ResultCollector): Either[ProcessError, ProcessResult] =
    if (code == 0) {
      /* Why concatenate outputs and errors in success?
       * Sometimes 'errors' has some part of success result. :(
       */
      Right(ProcessResult(resultCollector.outputs ++ resultCollector.errors))
    } else {
      Left(ProcessError.failure(code, resultCollector.errors))
    }

  implicit final class SysProcessOps(private val sysProcess: SysProcess) extends AnyVal {
    @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
    def run(): Either[ProcessError, ProcessResult] =
      try (sysProcess match {
        case SingleSysProcess(baseDir, command, commands) =>
          val resultCollector = ResultCollector()
          val processBuilder  =
            baseDir.fold(
              sys.process.Process(command :: commands)
            )(dir => sys.process.Process(command :: commands, cwd = dir))

          val code = processBuilder ! resultCollector
          processResult(code, resultCollector)
      })
      catch {
        case NonFatal(nonFatalThrowable) =>
          Left(ProcessError.failureWithNonFatal(nonFatalThrowable))
      }

  }

}

final class ResultCollector private (
  private val outs: ListBuffer[String],
  private val errs: ListBuffer[String]
) extends ProcessLogger {

  def outputs: List[String] = outs.result()
  def errors: List[String]  = errs.result()

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  override def out(s: => String): Unit = {
    outs += s
    ()
  }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  override def err(s: => String): Unit = {
    errs += s
    ()
  }

  override def buffer[T](f: => T): T = f

  override def toString: String =
    s"${getClass.getSimpleName}(outputs=${outputs.mkString("[", ",", "]")}, errors=${errors.mkString("[", ",", "]")})"
}

object ResultCollector {
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  def apply(): ResultCollector = new ResultCollector(ListBuffer(), ListBuffer())

  def unapply(resultCollector: ResultCollector): Option[(List[String], List[String])] =
    Option((resultCollector.outputs, resultCollector.errors))
}

final case class ProcessResult(outputs: List[String])

sealed trait ProcessError

object ProcessError {

  final case class Failure(code: Int, errors: List[String]) extends ProcessError

  final case class FailureWithNonFatal(throwable: Throwable) extends ProcessError

  def failure(code: Int, errors: List[String]): ProcessError =
    Failure(code, errors)

  def failureWithNonFatal(throwable: Throwable): ProcessError =
    FailureWithNonFatal(throwable)

}
