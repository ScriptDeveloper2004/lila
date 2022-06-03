import * as xhr from '../studyXhr';
import { prop } from 'common';
import { storedProp } from 'common/storage';
import { animationDuration } from 'draughtsground/anim';
import makeSuccess from './studyPracticeSuccess';
import makeSound from './sound';
import { readOnlyProp } from '../../util';
import { StudyPracticeData, Goal, StudyPracticeCtrl } from './interfaces';
import { StudyData, StudyCtrl } from '../interfaces';
import AnalyseCtrl from '../../ctrl';

export default function(root: AnalyseCtrl, studyData: StudyData, data: StudyPracticeData): StudyPracticeCtrl {

  const goal = prop<Goal>(root.data.practiceGoal!),
    nbMoves = prop(0),
    // null = ongoing, true = win, false = fail
    success = prop<boolean | null>(null),
    resetJump = prop(false),
    sound = makeSound(),
    analysisUrl = prop(''),
    autoNext = storedProp('practice-auto-next', true);

  function onLoad() {
    root.showAutoShapes = readOnlyProp(true);
    root.showGauge = readOnlyProp(true);
    root.showComputer = readOnlyProp(true);
    goal(root.data.practiceGoal!);
    nbMoves(0);
    success(null);
    const chapter = studyData.chapter;
    history.replaceState(null, chapter.name, data.url + '/' + chapter.id);
    analysisUrl('/analysis/' + root.data.game.variant.key + '/' + root.node.fen.replace(/ /g, '_') + '?color=' + root.bottomColor());

    // mark chapters withonly info as completed when opened
    if (!studyData.chapter.practice && !studyData.chapter.gamebook) {
      saveNbMoves(studyData.chapter.id);
    }
  }
  onLoad();

  function computeNbMoves(): number {
    if (root.isCaptureAllPractice()) {
      const rootNode = root.tree.root;
      return rootNode.children.filter(c => c.uci && c.ply === rootNode.ply + 1).length;
    }
    let plies = root.node.ply - root.tree.root.ply;
    if (root.bottomColor() !== root.data.player.color) plies--;
    return Math.ceil(plies / 2);
  }

  function getStudy(): StudyCtrl {
    return root.study!;
  }

  function checkSuccess(): void {
    const gamebook = getStudy().gamebookPlay();
    if (gamebook) {
      if (gamebook.state.feedback === 'end') onVictory();
      return;
    }
    if (!getStudy().data.chapter.practice) {
      return saveNbMoves();
    }
    if (success() !== null) return;
    nbMoves(computeNbMoves());
    const res = success(makeSuccess(root, goal(), nbMoves()));
    if (res) onVictory();
    else if (res === false) onFailure();
  }

  function onVictory(): void {
    saveNbMoves();
    sound.success();
    if (autoNext()) {
      // no autonext if gamebookplay ends with a comment
      const gamebook = getStudy().gamebookPlay();
      if (!gamebook?.state.comment) {
        setTimeout(goToNext, 1000);
      }
    }
  }

  function saveNbMoves(id?: string): void {
    const chapterId = id || getStudy().currentChapter().id,
      former = data.completion[chapterId];
    if (typeof former === 'undefined' || nbMoves() < former) {
      data.completion[chapterId] = nbMoves();
      xhr.practiceComplete(chapterId, nbMoves());
    }
  }

  function goToNext() {
    const next = getStudy().nextChapter();
    if (next) getStudy().setChapter(next.id);
  }

  function onFailure(): void {
    root.node.fail = true;
    sound.failure();
  }

  function captureProgress() {
    if (!root.isCaptureAllPractice() || goal().moves! <= 1) return [];
    const rootNode = root.tree.root;
    if (root.node.ply !== rootNode.ply && !resetJump() && !success()) return [];
    return rootNode.children
            .filter(c => c.uci && c.ply === rootNode.ply + 1)
            .map(c => c.uci!.slice(-2));
  }

  return {
    onLoad,
    onJump() {
      // reset failure state if no failed move found in mainline history
      if (success() === false && !root.nodeList.find(n => !!n.fail)) success(null);
      checkSuccess();
      if (resetJump()) {
        if (success()) resetJump(false);
        else setTimeout(() => {
          resetJump(false);
          root.jump('');
          root.redraw();
        }, animationDuration(root.draughtsground.state) + 300);
      }
    },
    onUserMove() {
      if (root.isCaptureAllPractice() && root.node.captLen === 1) {
        resetJump(true);
        sound.progress();
      }
    },
    onCeval: checkSuccess,
    data,
    goal,
    success,
    captureProgress,
    nbMoves,
    reset() {
      root.tree.root.children = [];
      root.userJump('');
      root.practice!.reset();
      onLoad();
      root.practice!.resume();
    },
    isWhite: root.bottomIsWhite,
    analysisUrl,
    autoNext,
    goToNext
  };
}
