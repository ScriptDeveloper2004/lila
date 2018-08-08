package lidraughts

import lidraughts.rating.Glicko

package object puzzle extends PackageObject {

  type PuzzleId = Int
  type RoundId = Int
  type Lines = List[Line]

  private[puzzle] def logger = lidraughts.log("puzzle")

  case class Result(win: Boolean) extends AnyVal {

    def loss = !win
    def glicko = if (win) Glicko.Result.Win else Glicko.Result.Loss
  }
}
