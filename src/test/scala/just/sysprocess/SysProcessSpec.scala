package just.sysprocess

import java.io.{File, IOException}

import hedgehog._
import hedgehog.runner._


/**
 * @author Kevin Lee
 * @since 2020-07-01
 */
object SysProcessSpec extends Properties {

  override def tests: List[Test] = List(
    example("SysProcess.run - success case", testSysProcessRunSuccessCase),
    example("SysProcess.run - failure case", testSysProcessRunFailureCase),
    example("SysProcess.run - failure with NonFatal case", testSysProcessRunFailureWithNonFatalCase)
  )

  val resourcesPath = "src/test/resources"

  def testSysProcessRunSuccessCase: Result = {

    val expected = ProcessResult.success(List("test1.txt", "test2.txt", "test3.txt"))
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
      case ProcessResult.Failure(code, errors) =>
        Result.all(List(
          Result.assert(code > 0).log("expect non-zero code"),
          Result.assert(expectedMessageContent.forall(errors.mkString.contains))
            .log(s"It should contain all of ${expectedMessageContent.mkString("[", ",", "]")}")
        ))
      case _ =>
        Result.failure
    }
  }

  def testSysProcessRunFailureWithNonFatalCase: Result = {

    val actual = SysProcess.run(
      SysProcess.singleSysProcess(Option(new File(resourcesPath)), "xyz")
    )

    actual match {
      case ProcessResult.FailureWithNonFatal(ex: IOException) =>
        ex.getMessage ==== s"""Cannot run program "xyz" (in directory "$resourcesPath"): error=2, No such file or directory"""

      case _ =>
        Result.failure
    }
  }

}
