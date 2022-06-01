package lidraughts.practice

sealed trait PracticeGoal

object PracticeGoal {

  case object Win extends PracticeGoal
  case object Promote extends PracticeGoal
  case object Capture extends PracticeGoal
  case class CaptureAll(nbMoves: Int) extends PracticeGoal
  case class WinIn(nbMoves: Int) extends PracticeGoal
  case class PromoteIn(nbMoves: Int) extends PracticeGoal
  case class DrawIn(nbMoves: Int) extends PracticeGoal
  case class AutoDrawIn(nbMoves: Int) extends PracticeGoal // same wording as draw, but autodraw or threefold
  case class EqualIn(nbMoves: Int) extends PracticeGoal // same as draw, except wording
  case class EvalIn(cp: Int, nbMoves: Int) extends PracticeGoal

  private val WinR = """(?i)win""".r
  private val PromoteR = """(?i)promote""".r
  private val CaptureR = """(?i)capture""".r
  private val CaptureAllR = """(?i)capture all""".r
  private val WinInR = """(?i)win in (\d++)""".r
  private val PromoteInR = """(?i)promote in (\d++)""".r
  private val DrawInR = """(?i)draw in (\d++)""".r
  private val AutoDrawInR = """(?i)autodraw in (\d++)""".r
  private val EqualInR = """(?i)equal(?:ize)?+ in (\d++)""".r
  private val EvalInR = """(?i)((?:\+|-|)\d++)cp in (\d++)""".r

  private val MultiSpaceR = """\s{2,}+""".r

  def apply(chapter: lidraughts.study.Chapter): PracticeGoal =
    chapter.tags(_.Termination).map(v => MultiSpaceR.replaceAllIn(v.trim, " ")).flatMap {
      case CaptureR() => Capture.some
      case CaptureAllR() =>
        val sit = draughts.format.Forsyth.<<@(chapter.setup.variant, chapter.root.fen.value)
        sit map { s => CaptureAll(s.validMoveCount) }
      case WinR() => Win.some
      case WinInR(movesStr) => parseIntOption(movesStr) map WinIn.apply
      case PromoteR() => Promote.some
      case PromoteInR(movesStr) => parseIntOption(movesStr) map PromoteIn.apply
      case DrawInR(movesStr) => parseIntOption(movesStr) map DrawIn.apply
      case AutoDrawInR(movesStr) => parseIntOption(movesStr) map AutoDrawIn.apply
      case EqualInR(movesStr) => parseIntOption(movesStr) map EqualIn.apply
      case EvalInR(cpStr, movesStr) => for {
        cp <- parseIntOption(cpStr)
        moves <- parseIntOption(movesStr)
      } yield EvalIn(cp, moves)
      case _ => none
    } | Win // default to win
}
