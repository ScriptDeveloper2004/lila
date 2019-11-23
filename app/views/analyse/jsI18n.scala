package views.html.analyse

import lidraughts.api.Context
import lidraughts.app.templating.Environment._
import lidraughts.common.String.html.safeJson
import lidraughts.i18n.{ I18nKeys => trans }

object jsI18n {

  def apply()(implicit ctx: Context) = safeJson(i18nJsObject(translations))

  private val translations = List(
    trans.flipBoard,
    trans.gameAborted,
    trans.whiteResigned,
    trans.blackResigned,
    trans.whiteLeftTheGame,
    trans.blackLeftTheGame,
    trans.draw,
    trans.timeOut,
    trans.playingRightNow,
    trans.whiteIsVictorious,
    trans.blackIsVictorious,
    trans.promotion,
    trans.variantEnding,
    trans.analysis,
    trans.boardEditor,
    trans.continueFromHere,
    trans.playWithTheMachine,
    trans.playWithAFriend,
    trans.openingExplorer,
    trans.inaccuracies,
    trans.mistakes,
    trans.blunders,
    trans.averageCentipieceLoss,
    trans.goodMove,
    trans.viewTheSolution,
    trans.youNeedAnAccountToDoThat,
    // ceval (also uses gameOver)
    trans.depthX,
    trans.usingServerAnalysis,
    trans.loadingEngine,
    trans.cloudAnalysis,
    trans.goDeeper,
    trans.showThreat,
    trans.gameOver,
    trans.inLocalBrowser,
    trans.toggleLocalEvaluation,
    // action menu
    trans.menu,
    trans.preferences,
    trans.inlineNotation,
    trans.computerAnalysis,
    trans.enable,
    trans.bestMoveArrow,
    trans.evaluationGauge,
    trans.infiniteAnalysis,
    trans.removesTheDepthLimit,
    trans.multipleLines,
    trans.cpus,
    trans.memory,
    trans.delete,
    trans.deleteThisImportedGame,
    trans.replayMode,
    trans.slow,
    trans.fast,
    trans.realtimeReplay,
    trans.byCPL,
    // context menu
    trans.promoteVariation,
    trans.makeMainLine,
    trans.deleteFromHere,
    // practice (also uses checkmate, draw)
    trans.practiceWithComputer,
    trans.goodMove,
    trans.inaccuracy,
    trans.mistake,
    trans.blunder,
    trans.threefoldRepetition,
    trans.anotherWasX,
    trans.bestWasX,
    trans.youBrowsedAway,
    trans.resumePractice,
    trans.whiteWinsGame,
    trans.blackWinsGame,
    trans.theGameIsADraw,
    trans.yourTurn,
    trans.computerThinking,
    trans.seeBestMove,
    trans.hideBestMove,
    trans.getAHint,
    trans.evaluatingYourMove,
    // retrospect (also uses youBrowsedAway, bestWasX, evaluatingYourMove)
    trans.learnFromYourMistakes,
    trans.learnFromThisMistake,
    trans.skipThisMove,
    trans.next,
    trans.xWasPlayed,
    trans.findBetterMoveForWhite,
    trans.findBetterMoveForBlack,
    trans.resumeLearning,
    trans.youCanDoBetter,
    trans.tryAnotherMoveForWhite,
    trans.tryAnotherMoveForBlack,
    trans.solution,
    trans.waitingForAnalysis,
    trans.noMistakesFoundForWhite,
    trans.noMistakesFoundForBlack,
    trans.doneReviewingWhiteMistakes,
    trans.doneReviewingBlackMistakes,
    trans.doItAgain,
    trans.reviewWhiteMistakes,
    trans.reviewBlackMistakes,
    // explorer (also uses gameOver, checkmate, stalemate, draw, variantEnding)
    trans.openingExplorerAndTablebase,
    trans.openingExplorer,
    trans.xOpeningExplorer,
    trans.move,
    trans.games,
    trans.variantLoss,
    trans.variantWin,
    trans.insufficientMaterial,
    trans.capture,
    trans.pawnMove,
    trans.close,
    trans.winning,
    trans.unknown,
    trans.losing,
    trans.drawn,
    trans.timeControl,
    trans.averageElo,
    trans.database,
    trans.recentGames,
    trans.topGames,
    trans.whiteDrawBlack,
    trans.averageRatingX,
    trans.masterDbExplanation,
    trans.nextCaptureOrPawnMoveInXHalfMoves,
    trans.noGameFound,
    trans.maybeIncludeMoreGamesFromThePreferencesMenu,
    trans.winPreventedBy50MoveRule,
    trans.lossSavedBy50MoveRule,
    trans.allSet,
    // advantage and movetime charts
    trans.advantage,
    trans.nbSeconds,
    trans.opening,
    trans.middlegame,
    trans.endgame
  )
}
