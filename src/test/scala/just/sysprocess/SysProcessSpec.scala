package just.sysprocess

import hedgehog._
import hedgehog.runner._

import java.io.{File, IOException}

/** @author Kevin Lee
  * @since 2020-07-01
  */
object SysProcessSpec extends Properties {

  override def tests: List[Test] = List(
    example("SysProcess.run - success case", testSysProcessRunSuccessCase),
    example("SysProcess.run - failure case", testSysProcessRunFailureCase),
    example("SysProcess.run - failure with NonFatal case", testSysProcessRunFailureWithNonFatalCase),
    example("SysProcess.run (extension) - success case", testSysProcessRunExtensionSuccessCase),
    example("SysProcess.run (extension) - failure case", testSysProcessRunExtensionFailureCase),
    example(
      "SysProcess.run (extension) - failure with NonFatal case",
      testSysProcessRunExtensionFailureWithNonFatalCase
    )
  )

  val resourcesPath = "src/test/resources"

  def testSysProcessRunSuccessCase: Result = {

    @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
    val expected: Either[ProcessError, ProcessResult] = Right(
      ProcessResult(List("test1.txt", "test2.txt", "test3.txt"))
    )

    val actual = SysProcess.run(
      SysProcess.singleSysProcess(Option(new File(resourcesPath)), "ls")
    )

    actual ==== expected
  }

  def testSysProcessRunFailureCase: Result = {

    val expectedMessageContent = List("ls", "test4.txt", "No such file or directory")

    val actual = SysProcess.run(
      SysProcess.singleSysProcess(Option(new File(resourcesPath)), "ls", "-l", "test4.txt")
    )

    actual match {
      case Left(ProcessError.Failure(code, errors)) =>
        Result.all(
          List(
            Result.assert(code > 0).log("expect non-zero code"),
            Result
              .assert(expectedMessageContent.forall(errors.mkString.contains))
              .log(s"It should contain all of ${expectedMessageContent.mkString("[", ",", "]")}")
          )
        )

      case _ =>
        Result.failure
    }
  }

  def testSysProcessRunFailureWithNonFatalCase: Result = {

    val actual = SysProcess.run(
      SysProcess.singleSysProcess(Option(new File(resourcesPath)), "xyz")
    )

    actual match {
      case Left(ProcessError.FailureWithNonFatal(ex: IOException)) =>
        ex.getMessage ==== s"""Cannot run program "xyz" (in directory "$resourcesPath"): error=2, No such file or directory"""

      case _ =>
        Result.failure
    }
  }

  def testSysProcessRunExtensionSuccessCase: Result = {

    @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
    val expected: Either[ProcessError, ProcessResult] = Right(
      ProcessResult(List("test1.txt", "test2.txt", "test3.txt"))
    )

    val actual = SysProcess.singleSysProcess(Option(new File(resourcesPath)), "ls").run()

    actual ==== expected
  }

  def testSysProcessRunExtensionFailureCase: Result = {

    val expectedMessageContent = List("ls", "test4.txt", "No such file or directory")

    val actual = SysProcess.singleSysProcess(Option(new File(resourcesPath)), "ls", "-l", "test4.txt").run()

    actual match {
      case Left(ProcessError.Failure(code, errors)) =>
        Result.all(
          List(
            Result.assert(code > 0).log("expect non-zero code"),
            Result
              .assert(expectedMessageContent.forall(errors.mkString.contains))
              .log(s"It should contain all of ${expectedMessageContent.mkString("[", ",", "]")}")
          )
        )

      case _ =>
        Result.failure
    }
  }

  def testSysProcessRunExtensionFailureWithNonFatalCase: Result = {

    val actual = SysProcess.singleSysProcess(Option(new File(resourcesPath)), "xyz").run()

    actual match {
      case Left(ProcessError.FailureWithNonFatal(ex: IOException)) =>
        ex.getMessage ==== s"""Cannot run program "xyz" (in directory "$resourcesPath"): error=2, No such file or directory"""

      case _ =>
        Result.failure
    }
  }

}
