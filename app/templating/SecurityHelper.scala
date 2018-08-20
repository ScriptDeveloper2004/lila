package lidraughts.app
package templating

import play.twirl.api.Html

import lidraughts.security.{ Permission, Granter }
import lidraughts.user.{ User, UserContext }

trait SecurityHelper {

  def isGranted(permission: Permission.Selector)(implicit ctx: UserContext): Boolean =
    isGranted(permission(Permission))

  def isGranted(permission: Permission)(implicit ctx: UserContext): Boolean =
    ctx.me ?? Granter(permission)

  def isGranted(permission: Permission.Selector, user: User): Boolean =
    isGranted(permission(Permission), user)

  def isGranted(permission: Permission, user: User): Boolean =
    Granter(permission)(user)

  def reportScore(score: lidraughts.report.Report.Score) = Html {
    s"""<div class="score ${score.color}" title="Report score">${score.value.toInt}</div>"""
  }
  // def reportScore(score: lidraughts.report.Report.Score) = Html {
  //   s"""<div class="score"><i>Score</i><strong>${score.value.toInt}</strong></div>"""
  // }
}
