package lidraughts.playban

import com.github.blemale.scaffeine.{ Cache, Scaffeine }
import scala.concurrent.duration._

import draughts.Color
import lidraughts.game.Game
import lidraughts.message.{ MessageApi, ModPreset }
import lidraughts.user.{ User, UserRepo }

private final class SandbagWatch(messenger: MessageApi, bus: lidraughts.common.Bus) {

  import SandbagWatch._

  def apply(game: Game, loser: Color): Fu[Boolean] = game.rated ?? {
    game.userIds.map { userId =>
      (records getIfPresent userId, isSandbag(game, loser, userId)) match {
        case (None, false) => funit
        case (Some(record), false) => updateRecord(userId, record + Good)
        case (record, true) => updateRecord(userId, (record | newRecord) + Sandbag)
      }
    }.sequenceFu.void inject isSandbag(game)
  }

  private def sendMessage(userId: User.ID): Funit = for {
    mod <- UserRepo.Lidraughts
    user <- UserRepo byId userId
  } yield (mod zip user).headOption.?? {
    case (m, u) =>
      lidraughts.log("sandbag").info(s"https://lidraughts.org/@/${u.username}")
      bus.publish(lidraughts.hub.actorApi.mod.AutoWarning(u.id, ModPreset.sandbagAuto.subject), 'autoWarning)
      messenger.sendPreset(m, u, ModPreset.sandbagAuto).void
  }

  private def updateRecord(userId: User.ID, record: Record) =
    if (record.immaculate) fuccess(records invalidate userId)
    else {
      records.put(userId, record)
      record.alert ?? sendMessage(userId)
    }

  private val records: Cache[User.ID, Record] = Scaffeine()
    .expireAfterWrite(3 hours)
    .build[User.ID, Record]

  private def isSandbag(game: Game, loser: Color, userId: User.ID): Boolean =
    game.playerByUserId(userId).exists {
      _ == game.player(loser) && isSandbag(game)
    }

  private def isSandbag(game: Game): Boolean = game.turns <= {
    6
  }
}

private object SandbagWatch {

  sealed trait Outcome
  case object Good extends Outcome
  case object Sandbag extends Outcome

  val maxOutcomes = 7

  case class Record(outcomes: List[Outcome]) {

    def +(outcome: Outcome) = copy(outcomes = outcome :: outcomes.take(maxOutcomes - 1))

    def alert = latestIsSandbag && outcomes.count(Sandbag ==) >= 3

    def latestIsSandbag = outcomes.headOption.exists(Sandbag ==)

    def immaculate = outcomes.size == maxOutcomes && outcomes.forall(Good ==)
  }

  val newRecord = Record(Nil)
}