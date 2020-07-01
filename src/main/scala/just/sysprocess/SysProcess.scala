package just.sysprocess

import java.io.File

import scala.util.control.NonFatal

import scala.collection.mutable.ListBuffer
import scala.sys.process.ProcessLogger

/**
 * Copied from sbt-devoops and modified.
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

  def run(sysProcess: SysProcess): ProcessResult = try (sysProcess match {
    case SingleSysProcess(baseDir, command, commands) =>
      val resultCollector = ResultCollector()
      val processBuilder = baseDir.fold(
          sys.process.Process(command :: commands)
        )(
          dir => sys.process.Process(command :: commands, cwd = dir)
        )
      val code = processBuilder ! resultCollector
      ProcessResult.processResult(code, resultCollector)
  }) catch {
    case NonFatal(nonFatalThrowable) =>
      ProcessResult.failureWithNonFatal(nonFatalThrowable)
  }

}

final class ResultCollector private (
  private val outs: ListBuffer[String],
  private val errs: ListBuffer[String]
) extends ProcessLogger {

  def outputs: List[String] = outs.result
  def errors: List[String] = errs.result

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

sealed trait ProcessResult

object ProcessResult {

  final case class Success(outputs: List[String]) extends ProcessResult

  final case class Failure(code: Int, errors: List[String]) extends ProcessResult

  final case class FailureWithNonFatal(throwable: Throwable) extends ProcessResult

  def success(outputs: List[String]): ProcessResult =
    Success(outputs)

  def failure(code: Int, errors: List[String]): ProcessResult =
    Failure(code, errors)

  def failureWithNonFatal(throwable: Throwable): ProcessResult =
    FailureWithNonFatal(throwable)


  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def processResult(code: Int, resultCollector: ResultCollector): ProcessResult =
    if (code == 0) {
      /* Why concatenate outputs and errors in success?
       * Sometimes 'errors' has some part of success result. :(
       */
      success(resultCollector.outputs ++ resultCollector.errors )
    } else {
      failure(code, resultCollector.errors)
    }

  def toEither[A, B](
    processResult: ProcessResult
  )(
    resultToEither: ProcessResult => Either[A, B]
  ): Either[A, B] =
    resultToEither(processResult)

  def toOption[A](
    processResult: ProcessResult
  )(
    resultToOption: ProcessResult => Option[A]
  ): Option[A] =
    resultToOption(processResult)

}
