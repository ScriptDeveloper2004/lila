package lidraughts.bot

import play.api.libs.json._

import draughts.format.FEN

import lidraughts.game.JsonView._
import lidraughts.game.{ Game, Pov, GameRepo }

final class BotJsonView(
    lightUserApi: lidraughts.user.LightUserApi
) {

  def gameFull(game: Game): Fu[JsObject] = GameRepo.withInitialFen(game) flatMap gameFull

  def gameFull(wf: Game.WithInitialFen): Fu[JsObject] =
    gameState(wf) map { state =>
      gameImmutable(wf) ++ Json.obj(
        "type" -> "gameFull",
        "state" -> state
      )
    }

  def gameImmutable(wf: Game.WithInitialFen): JsObject = {
    import wf._
    Json.obj(
      "id" -> game.id,
      "variant" -> game.variant,
      "clock" -> game.clock.map(_.config),
      "speed" -> game.speed.key,
      "perf" -> game.perfType.map { p =>
        Json.obj("name" -> p.name)
      },
      "rated" -> game.rated,
      "createdAt" -> game.createdAt,
      "white" -> playerJson(game.whitePov),
      "black" -> playerJson(game.blackPov),
      "initialFen" -> fen.fold("startpos")(_.value)
    )
      .add("tournamentId" -> game.tournamentId)
  }

  def gameState(wf: Game.WithInitialFen): Fu[JsObject] = {
    import wf._
    draughts.format.UciDump(game.pdnMoves, fen.map(_.value), game.variant).future map { uciMoves =>
      Json.obj(
        "type" -> "gameState",
        "moves" -> uciMoves.mkString(" "),
        "wtime" -> millisOf(game.whitePov),
        "btime" -> millisOf(game.blackPov),
        "winc" -> game.clock.??(_.config.increment.millis),
        "binc" -> game.clock.??(_.config.increment.millis)
      )
        .add("rematch" -> game.next)
    }
  }

  def chatLine(username: String, text: String, player: Boolean) = Json.obj(
    "type" -> "chatLine",
    "room" -> player.fold("player", "spectator"),
    "username" -> username,
    "text" -> text
  )

  private def playerJson(pov: Pov) = {
    val light = pov.player.userId flatMap lightUserApi.sync
    Json.obj()
      .add("aiLevel" -> pov.player.aiLevel)
      .add("id" -> light.map(_.id))
      .add("name" -> light.map(_.name))
      .add("title" -> light.map(_.title))
      .add("rating" -> pov.player.rating)
      .add("provisional" -> pov.player.provisional)
  }

  private def millisOf(pov: Pov): Int =
    pov.game.clock.fold(Int.MaxValue)(_.remainingTime(pov.color).millis.toInt)

  private implicit val clockConfigWriter: OWrites[draughts.Clock.Config] = OWrites { c =>
    Json.obj(
      "initial" -> c.limit.millis,
      "increment" -> c.increment.millis
    )
  }
}
