package views.html
package user

import lidraughts.api.Context
import lidraughts.app.templating.Environment._
import lidraughts.app.ui.ScalatagsTemplate._
import lidraughts.common.String.html.richText
import lidraughts.user.User

import controllers.routes

object bots {

  def apply(users: List[User])(implicit ctx: Context) = {

    val title = s"${users.size} Online bots"

    views.html.base.layout(
      title = title,
      moreCss = frag(cssTag("slist"), cssTag("user.list")),
      wrapClass = "full-screen-force"
    )(
        main(cls := "page-menu bots")(
          user.bits.communityMenu("bots"),
          div(cls := "bots page-menu__content box")(
            div(cls := "box__top")(h1(title)),
            table(cls := "slist slist-pad")(
              tbody(
                users.sortBy { u =>
                  (if (u.isVerified) -1 else 1, -u.playTime.??(_.total))
                } map { u =>
                  tr(
                    td(userLink(u)),
                    u.profile
                      .ifTrue(ctx.noKid)
                      .ifTrue(!u.troll || ctx.is(u))
                      .flatMap(_.nonEmptyBio)
                      .map { bio =>
                        td(shorten(bio, 400))
                      } | td,
                    td(cls := "rating")(u.perfs.bestPerfs(3).map { p =>
                      showPerfRating(u, p._1)
                    }),
                    u.playTime.fold(td) { playTime =>
                      td(
                        p(
                          cls := "text",
                          dataIcon := "C",
                          st.title := trans.tpTimeSpentPlaying.txt(showPeriod(playTime.totalPeriod))
                        )(showPeriod(playTime.totalPeriod)),
                        playTime.nonEmptyTvPeriod.map { tvPeriod =>
                          p(
                            cls := "text",
                            dataIcon := "1",
                            st.title := trans.tpTimeSpentOnTV.txt(showPeriod(tvPeriod))
                          )(showPeriod(tvPeriod))
                        }
                      )
                    },
                    if (ctx is u) td
                    else {
                      td(
                        a(
                          dataIcon := "U",
                          cls := List("button button-empty text" -> true),
                          st.title := trans.challengeToPlay.txt(),
                          href := s"${routes.Lobby.home()}?user=${u.username}#friend"
                        )(trans.play())
                      )
                    }
                  )
                }
              )
            )
          )
        )
      )
  }

}
