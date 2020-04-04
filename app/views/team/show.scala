package views.html.team

import lidraughts.api.Context
import lidraughts.app.templating.Environment._
import lidraughts.app.ui.ScalatagsTemplate._
import lidraughts.common.paginator.Paginator
import lidraughts.common.String.html.richText

import controllers.routes

object show {

  def apply(t: lidraughts.team.Team, members: Paginator[lidraughts.team.MemberWithUser], info: lidraughts.app.mashup.TeamInfo)(implicit ctx: Context) =
    bits.layout(
      title = t.name,
      openGraph = lidraughts.app.ui.OpenGraph(
        title = s"${t.name} team",
        url = s"$netBaseUrl${routes.Team.show(t.id).url}",
        description = shorten(t.description, 152)
      ).some
    )(
        main(cls := "page-menu")(
          bits.menu(none),
          div(cls := "team-show page-menu__content box team-show")(
            div(cls := "box__top")(
              h1(cls := "text", dataIcon := "f")(t.name, " ", em("TEAM")),
              div(
                if (t.disabled) span(cls := "staff")("CLOSED")
                else trans.nbMembers.plural(t.nbMembers, strong(t.nbMembers.localize))
              )
            ),
            (info.mine || t.enabled) option div(cls := "team-show__content")(
              st.section(cls := "team-show__meta")(
                p(trans.teamLeader(), ": ", userIdLink(t.createdBy.some))
              ),

              div(cls := "team-show__members")(
                st.section(cls := "recent-members")(
                  h2(trans.teamRecentMembers()),
                  div(cls := "userlist infinitescroll")(
                    pagerNext(members, np => routes.Team.show(t.id, np).url),
                    members.currentPageResults.map { member =>
                      div(cls := "paginated")(userLink(member.user))
                    }
                  )
                )
              ),
              st.section(cls := "team-show__desc")(
                richText(t.description),
                t.location.map { loc =>
                  frag(br, trans.location(), ": ", richText(loc))
                },
                info.hasRequests option div(cls := "requests")(
                  h2(info.requests.size, " join requests"),
                  views.html.team.request.list(info.requests, t.some)
                )
              ),
              st.section(cls := "team-show__actions")(
                (t.enabled && !info.mine) option frag(
                  if (info.requestedByMe) strong("Your join request is being reviewed by the team leader")
                  else ctx.me.??(_.canTeam) option
                    postForm(cls := "inline", action := routes.Team.join(t.id))(
                      submitButton(cls := "button button-green")(trans.joinTeam.txt())
                    )
                ),
                (info.mine && !info.createdByMe) option
                  postForm(cls := "quit", action := routes.Team.quit(t.id))(
                    submitButton(cls := "button button-empty button-red confirm")(trans.quitTeam.txt())
                  ),
                (info.createdByMe || isGranted(_.Admin)) option
                  a(href := routes.Team.edit(t.id), cls := "button button-empty text", dataIcon := "%")(trans.settings()),
                info.createdByMe option frag(
                  a(href := routes.Tournament.teamBattleForm(t.id), cls := "button button-empty text", dataIcon := "g")(
                    span(
                      strong("Team battle"),
                      em("A battle of multiple teams, each players scores points for their team")
                    )
                  ),
                  a(href := s"${routes.Tournament.form()}?team=${t.id}", cls := "button button-empty text", dataIcon := "g")(
                    span(
                      strong("Team tournament"),
                      em("An arena tournament that only members of your team can join")
                    )
                  )
                )
              ),
              div(cls := "team-show__tour-forum")(
                info.teamBattles.nonEmpty option frag(
                  st.section(cls := "team-show__tour")(
                    h2(dataIcon := "g", cls := "text")(
                      trans.tournaments()
                    ),
                    views.html.tournament.teamBattle.list(info.teamBattles)
                  )
                ),
                NotForKids {
                  st.section(cls := "team-show__forum")(
                    h2(dataIcon := "d", cls := "text")(
                      a(href := teamForumUrl(t.id))(trans.forum()),
                      " (", info.forumNbPosts, ")"
                    ),
                    info.forumPosts.take(10).map { post =>
                      st.article(
                        p(cls := "meta")(
                          a(href := routes.ForumPost.redirect(post.postId))(post.topicName),
                          em(
                            userIdLink(post.userId, withOnline = false),
                            " ",
                            momentFromNow(post.createdAt)
                          )
                        ),
                        p(shorten(post.text, 200))
                      )
                    },
                    a(cls := "more", href := teamForumUrl(t.id))(t.name, " ", trans.forum(), " »")
                  )
                }
              )
            )
          )
        )
      )
}
