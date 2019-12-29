package controllers

import play.api.libs.json._
import scala.concurrent.duration._

import lidraughts.app._
import lidraughts.common.HTTPRequest
import lidraughts.timeline.Entry.entryWrites
import views._

object Timeline extends LidraughtsController {

  def home = Auth { implicit ctx => me =>
    lidraughts.mon.http.response.timeline.count()
    negotiate(
      html =
        if (HTTPRequest.isXhr(ctx.req))
          Env.timeline.entryApi.userEntries(me.id)
            .logTimeIfGt(s"timeline site entries for ${me.id}", 10 seconds)
            .map { html.timeline.entries(_) }
        else
          Env.timeline.entryApi.moreUserEntries(me.id, 30)
            .logTimeIfGt(s"timeline site more entries (30) for ${me.id}", 10 seconds)
            .map { html.timeline.more(_) },
      _ => {
        val nb = (getInt("nb") | 10) atMost 20
        Env.timeline.entryApi.moreUserEntries(me.id, nb)
          .logTimeIfGt(s"timeline mobile $nb for ${me.id}", 10 seconds)
          .map { es => Ok(Json.obj("entries" -> es)) }
      }
    ).mon(_.http.response.timeline.time)
  }

  def unsub(channel: String) = Auth { implicit ctx => me =>
    Env.timeline.unsubApi.set(channel, me.id, ~get("unsub") == "on")
  }
}
