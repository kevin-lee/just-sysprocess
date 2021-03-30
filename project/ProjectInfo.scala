import wartremover.WartRemover.autoImport.{Wart, Warts}

object ProjectInfo {
  final case class ProjectName(projectName: String) extends AnyVal

  def commonWarts(scalaBinaryVersion: String): Seq[wartremover.Wart] = scalaBinaryVersion match {
    case "2.13" | "2.12" | "2.11" =>
      Warts.all
    case _ =>
      Seq.empty
  }

}
