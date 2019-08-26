package views.html.user

import lidraughts.api.Context
import lidraughts.app.templating.Environment._
import lidraughts.app.ui.ScalatagsTemplate._
import lidraughts.common.String.html.safeJsonValue
import lidraughts.rating.{ Perf, PerfType }
import lidraughts.perfStat.PerfStat
import lidraughts.user.User

import controllers.routes

object perfStat {

  def apply(
    u: User,
    rankMap: lidraughts.rating.UserRankMap,
    perfType: lidraughts.rating.PerfType,
    percentile: Option[Double],
    stat: PerfStat,
    data: play.api.libs.json.JsObject,
    ratingChart: Option[String]
  )(implicit ctx: Context) = views.html.base.layout(
    title = s"${u.username} ${perfType.name} stats",
    robots = false,
    moreJs = frag(
      jsAt("compiled/user.js"),
      ratingChart.map { rc =>
        frag(
          jsTag("chart/ratingHistory.js"),
          embedJsUnsafe(s"lidraughts.ratingHistoryChart($rc,'${perfType.name}');")
        )
      },
      jsAt(s"compiled/lidraughts.perfStat${isProd ?? (".min")}.js"),
      embedJsUnsafe(s"""$$(function() {
if (false) LidraughtsPerfStat(document.querySelector('.perf-stat__content'), {
data: ${safeJsonValue(data)}
});
});""")
    ),
    moreCss = cssTag("perf-stat")
  ) {
      main(cls := s"page-menu")(
        st.aside(cls := "page-menu__menu")(show.side(u, rankMap, perfType.some)),
        div(cls := s"page-menu__content box perf-stat ${perfType.key}")(
          div(cls := "box__top")(
            h1(
              a(href := routes.User.show(u.username))(u.username),
              span(perfType.name, " stats")
            ),
            div(cls := "box__top__actions")(
              u.perfs(perfType).nb > 0 option a(
                cls := "button button-empty text",
                dataIcon := perfType.iconChar,
                href := s"${routes.User.games(u.username, "search")}?perf=${perfType.id}"
              )("View the games"),
              bits.perfTrophies(u, rankMap.filterKeys(perfType==))
            )
          ),
          ratingChart.isDefined option div(cls := "rating-history")(spinner),
          div(cls := "box__pad perf-stat__content")(
            glicko(perfType, u.perfs(perfType), percentile),
            counter(stat.count),
            highlow(stat),
            resultStreak(),
            result(),
            playStreakNb(),
            playStreakTime()
          )
        )
      )
    }

  private def decimal(v: Double) = lidraughts.common.Maths.roundAt(v, 2)

  private def glicko(perfType: PerfType, perf: Perf, percentile: Option[Double]): Frag = st.section(cls := "glicko")(
    h2(
      "Rating: ",
      strong(title := "Yes, ratings have decimal accuracy.")(decimal(perf.glicko.rating).toString),
      ". ",
      span(cls := "details")(
        perf.glicko.provisional option span(title := "Not enough rated games have been played to establish a reliable rating.")("(provisional)"),
        percentile.map(decimal).filter(_ != 0.0 && !perf.glicko.provisional) map { percentile =>
          frag(
            "Better than ",
            a(href := routes.Stat.ratingDistribution(perfType.key))(
              strong(percentile.toString, "%"), " of ", perfType.name, " players"
            ),
            "."
          )
        }
      )
    ),
    p(
      "Progression over the last twelve games: ",
      span(cls := "progress")(
        if (perf.progress > 0) tag("green")(dataIcon := "N")(perf.progress)
        else if (perf.progress < 0) tag("red")(dataIcon := "M")(-perf.progress)
        else frag("none")
      ),
      ". ",
      "Rating deviation: ",
      strong(title := "Lower value means the rating is more stable. Above 110, the rating is considered provisional.")(decimal(perf.glicko.deviation).toString),
      "."
    )
  )

  private def pct(num: Int, denom: Int): String = {
    if (denom == 0) "0"
    else s"${Math.round(num * 100 / denom)}%"
  }

  private def counter(count: lidraughts.perfStat.Count): Frag = st.section(cls := "counter split")(
    div(
      table(
        tbody(
          tr(
            th("Total games"),
            td(count.all),
            td
          ),
          tr(cls := "full")(
            th("Rated games"),
            td(count.rated),
            td(pct(count.rated, count.all))
          ),
          tr(cls := "full")(
            th("Tournament games"),
            td(count.tour),
            td(pct(count.tour, count.all))
          ),
          tr(cls := "full")(
            th("Berserked games"),
            td(count.berserk),
            td(pct(count.berserk, count.tour))
          ),
          count.seconds > 0 option tr(cls := "full")(
            th("Time spent playing"),
            td(colspan := "2") {
              val hours = count.seconds / (60 * 60)
              val minutes = (count.seconds % (60 * 60)) / 60
              s"${hours}h, ${minutes}m"
            }
          )
        )
      )
    ),
    div(
      table(
        tbody(
          tr(
            th("Average opponent"),
            td(decimal(count.opAvg.avg).toString),
            td
          ),
          tr(cls := "full")(
            th("Victories"),
            td(tag("green")(count.win)),
            td(tag("green")(pct(count.win, count.all)))
          ),
          tr(cls := "full")(
            th("Draws"),
            td(count.draw),
            td(pct(count.draw, count.all))
          ),
          tr(cls := "full")(
            th("Defeats"),
            td(tag("red")(count.loss)),
            td(tag("red")(pct(count.loss, count.all)))
          ),
          tr(cls := "full")(
            th("Disconnections"),
            td(if (count.disconnects > count.all * 100 / 15) tag("red") else frag())(count.disconnects),
            td(pct(count.disconnects, count.all))
          )
        )
      )
    )
  )

  private def ratingAt(title: String, opt: Option[lila.perfStat.RatingAt], color: String)(implicit ctx: Context): Frag =
    opt match {
      case Some(r) => div(
        h2(title, ": ", strong(tag(color)(r.int))),
        a(cls := "glpt", href := routes.Round.watcher(r.gameId, "white"))(semanticDate(r.at))
      )
      case None => div(h2(title, ":"), " ", span("Not enough games played"))
    }

  private def highlow(stat: PerfStat)(implicit ctx: Context): Frag = st.section(cls := "highlow split")(
    ratingAt("Highest rating", stat.highest, "green"),
    ratingAt("Lowest rating", stat.lowest, "red")
  )

  private def resultStreak(): Frag = st.section(cls := "resultStreak split")()

  private def result(): Frag = st.section(cls := "result split")()

  private def playStreakNb(): Frag = st.section(cls := "playStreak")()

  private def playStreakTime(): Frag = st.section(cls := "playStreak")()
}
