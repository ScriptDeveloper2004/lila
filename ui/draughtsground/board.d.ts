import { State } from './state';
import * as cg from './types';
export declare type Callback = (...args: any[]) => void;
export declare function callUserFunction(f: Callback | undefined, ...args: any[]): void;
export declare function toggleOrientation(state: State): void;
export declare function reset(state: State): void;
export declare function setPieces(state: State, pieces: cg.PiecesDiff): void;
export declare function unsetPremove(state: State): void;
export declare function unsetPredrop(state: State): void;
export declare function calcCaptKey(pieces: cg.Pieces, startX: number, startY: number, destX: number, destY: number): cg.Key | undefined;
export declare function baseMove(state: State, orig: cg.Key, dest: cg.Key): cg.Piece | boolean;
export declare function baseNewPiece(state: State, piece: cg.Piece, key: cg.Key, force?: boolean): boolean;
export declare function userMove(state: State, orig: cg.Key, dest: cg.Key): boolean;
export declare function dropNewPiece(state: State, orig: cg.Key, dest: cg.Key, force?: boolean): void;
export declare function selectSquare(state: State, key: cg.Key, force?: boolean): void;
export declare function setSelected(state: State, key: cg.Key): void;
export declare function unselect(state: State): void;
export declare function canMove(state: State, orig: cg.Key, dest: cg.Key): boolean;
export declare function isDraggable(state: State, orig: cg.Key): boolean;
export declare function playPremove(state: State): boolean;
export declare function playPredrop(state: State, validate: (drop: cg.Drop) => boolean): boolean;
export declare function cancelMove(state: State): void;
export declare function stop(state: State): void;
export declare function getKeyAtDomPos(pos: cg.NumberPair, asWhite: boolean, bounds: ClientRect): cg.Key | undefined;
