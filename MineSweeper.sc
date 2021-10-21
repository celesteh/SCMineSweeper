MineSweeperCell {

	var <x, <y, <>isBomb, <visible, <>marked, <neighbourCount, <>uncoverAction, <>bombClickAction, neighbours, <>finishedMoveAction, <>markAction;

	*new {|x, y, isBomb=false|
		^super.newCopyArgs(x, y, isBomb, false, false).init(); //not visible, not marked
	}

	init {
		neighbourCount = 0;
		//neighbours = parent.getNeighbours(x, y);
	}


	setCount{|parent|
		var count=0;

		//isBomb.if({
		//	count = -1;
		//}, {
		neighbours.isNil.if({
			neighbours = parent.getNeighbours(x, y);
		});

		neighbours.do({|neighbour|
			neighbour.isBomb.if({
				count = count + 1;
			})
		});

		//despite my effrots, it's counting itself, so...
		isBomb.if({
			count = count -1;
		});
		//});
		neighbourCount = count;
		^ count;
	}


	click {
		//"click".postln;
		//visible.not.if({
		visible = true;
		//"shouldAct".postln;
		isBomb.if({
			bombClickAction.value(this);
		}, {
			uncoverAction.value(this);
		});
		//"doneAction".postln;

		this.changed;
		//"signalled change".postln;
		//[x, y, this].postln;
		//neighbours.postln;
		(isBomb.not && (neighbourCount == 0)).if({
			neighbours.do({|n|
				//"loop".postln;
				//(isBomb.not && (neighbourCount == 0)).if({

				n.uncover
			})
		});
		//});

		finishedMoveAction.value(this);
	}

	uncover {
		//[x,y].postln;
		// not a click
		(visible.not && isBomb.not).if({
			visible = true;
			this.changed;
			(neighbourCount == 0).if({
				neighbours.do({|n|
					//"uncover loop".postln;
					//(isBomb.not && (neighbourCount == 0)).if({
					n.uncover
				})

			})
		})
	}

	asString {
		marked.if({
			^"X";
		});
		visible.if({
			isBomb.if({
				^"!";
			}, {
				(neighbourCount > 0).if({ // dont's show 0s right now
					^neighbourCount.asString;
				});
			});
		});
		^" ";
	}

	evolve { // Game of Life support  // isBomb = isAlive
		var startCount, changed=false, zeroNeighbour = false;

		visible.if({
			marked.if({
				isBomb.not.if({
					marked = false;
					changed = true;
				})
			});
			isBomb.if({
				marked = true;
				changed = true;
			}, {
				neighbours.do({|n|
					zeroNeighbour = (zeroNeighbour || (n.neighbourCount ==0 ));
				});

				zeroNeighbour.if({
					this.uncover;
					changed = true;
				})
			});
		});
		changed.if ({
			this.changed;
		});
	}

	toggleMark {
		marked = marked.not;

		(marked && isBomb).if ({ // is it correctly marked?
			visible = true;
		});
		marked.not.if({
			visible = false;
		});

		this.changed;
		markAction.value(this);
		finishedMoveAction.value(this);
		^marked;
	}



	gui {|layout, bounds|

		^MineSweeperCellGui(layout, bounds, this);
	}


}

MineSweeper {

	var cells, <>uncoverAction, <>bombClickAction, <>finishedMoveAction, <>markAction;

	*new{| x, y, bombs|
		^super.new.init(x, y, bombs);
	}

	init{| x, y, bombs|

		var x_coord, y_coord, cell;

		cells = Array.fill2D(x, y, {|r, c|
			cell = MineSweeperCell(r, c);
			cell.uncoverAction_({|cell| this.uncover(cell) });
			cell.bombClickAction_({|cell| this.bombClick(cell)});
			cell.addDependant(this);
			cell.finishedMoveAction_({|cell| this.finishedMove(cell)});
			cell.markAction_({|cell| this.markCell(cell)});
		});

		{bombs > 0}.while({

			x_coord = x.rand;
			y_coord = y.rand;

			cell = this.at(x_coord, y_coord);
			cell.notNil.if({
				cell.isBomb.not.if({
					cell.isBomb = true;
					bombs = bombs-1;
				})
			})
		});

		cells.flat.do({|cell| cell.setCount(this); });

	}


	at{| x, y|
		var row;
		row = cells[x];
		row.notNil.if({
			^row[y];
		});
		^nil
	}

	getNeighbours{|x, y|
		var row, neighbours, arr,neighbourX, neighbourY;

		neighbours = [];

		arr = [-1, 0, 1];
		arr.do({|xoff|
			arr.do({|yoff|
				(xoff == yoff == 0).not.if({ // don't coutn self
					neighbourX = x+ xoff;
					neighbourY = y + yoff;
					((neighbourX >= 0 ) && (neighbourY >= 0)).if({
						row = cells[neighbourX];
						row.notNil.if({
							row[neighbourY].notNil.if({

								neighbours = neighbours ++ row[neighbourY]
							})
						})
					})
				})
			})
		})
		^neighbours;
	}


	getVisible {
		var visible;

		visible = cells.collect({|row|
			row.select({|cell|
				cell.visible
			})
		});
	}

	visible {
		var visible_cells;
		visible_cells=[];
		cells.do({|rows|
			rows.do({|cell|
				cell.visible.if({
					visible_cells = visible_cells.add(cell);
				})
			})
		});
		^visible_cells;
	}

	marked {
		var marked_cells;
		marked_cells=[];
		cells.do({|rows|
			rows.do({|cell|
				cell.marked.if({
					marked_cells = marked_cells.add(cell);
				})
			})
		});
		^marked_cells;
	}


	uncover{|cell|
		uncoverAction.value(cell);
	}

	bombClick{|cell|
		bombClickAction.value(cell);
	}

	finishedMove{|cell|
		finishedMoveAction.value(this, cell);
	}

	markCell{|cell|
		markAction.value(this,cell);
	}

	asString {
		var str;

		str = "";
		cells.do({|row|
			row.do({|cell|
				str = str + cell.asString;
			});
			str = str + "\n";
		});

		^str
	}

	click{|x,y|
		var cell;
		cell = this.at(x,y);
		cell.notNil.if({
			cell.click;
		});
	}

	mark{|x,y|
		var cell;
		cell = this.at(x,y);
		cell.notNil.if({
			cell.marked = true;
		});
	}

	unmark{|x,y|
		var cell;
		cell = this.at(x,y);
		cell.notNil.if({
			cell.marked = false;
		});
	}


	gui {|layout, bounds|

		^MineSweeperGui(layout, bounds, this, cells)
	}

	update{|cell|
		this.changed(cell);
	}


}

GameOfLifeSweeper : MineSweeper {


	*new{| x=41, y=16, bombs=99, type=true|
		^super.new(x, y, bombs).makeGenerator(type);
	}

	makeGenerator {|type|
		var generators, genArr, len, start, max=0, should=true;

		//type.notNil.if({
		should = (type == false).not;
		//});

		should.if({
			generators = IdentityDictionary.new;
			generators.put(\gosper,
				[
					"........................O...........",
					"......................O.O...........",
					"............OO......OO............OO",
					"...........O...O....OO............OO",
					"OO........O.....O...OO..............",
					"OO........O...O.OO....O.O...........",
					"..........O.....O.......O...........",
					"...........O...O....................",
					"............OO......................"]
			);

			generators.put(\space_rake,
				[
					"...........OO.....OOOO",
					".........OO.OO...O...O",
					".........OOOO........O",
					"..........OO.....O..O",
					"",
					"........O",
					".......OO........OO",
					"......O.........O..O",
					".......OOOOO....O..O",
					"........OOOO...OO.OO",
					"...........O....OO",
					"",
					"",
					"",
					"..................OOOO",
					"O..O.............O...O",
					"....O................O",
					"O...O............O..O",
					".OOOO"]
			);

			generators.put(\backrake_2,
				[
					"...O",
					"..OOO",
					".OO.O.....O",
					".OOO.....OOO",
					"..OO....O..OO...OOO",
					"........OOO....O..O",
					"..................O",
					"..................O",
					"..................O",
					"..OOO............O",
					"..O..O",
					"..O",
					"..O",
					"...O",
					"",
					"",
					"",
					"",
					"",
					"",
					"OOO",
					"O..O...........O",
					"O.............OOO",
					"O............OO.O",
					"O............OOO",
					".O............OO"
			]);

			generators.put(\gourmet,
				[
					"..........OO........",
					"..........O.........",
					"....OO.OO.O....OO...",
					"..O..O.O.O.....O....",
					"..OO....O........O..",
					"................OO..",
					"....................",
					"................OO..",
					"O.........OOO..O.O..",
					"OOO.......O.O...O...",
					"...O......O.O....OOO",
					"..O.O..............O",
					"..OO................",
					"....................",
					"..OO................",
					"..O........O....OO..",
					"....O.....O.O.O..O..",
					"...OO....O.OO.OO....",
					".........O..........",
					"........OO.........."
			]);


			type.isKindOf(String).if({ type = type.asSymbol });
			type.isKindOf(Symbol).if({
				generators.keys.includes(type).if({
					genArr = generators[type];
				});
			});
			genArr.isNil.if({
				type = generators.keys.choose;
				genArr = generators[type];
			});



			len = genArr.size;
			genArr.do({|row|
				(row.size > max).if({ max = row.size });
			});

			//we'll want to place this random later, but for now, clear everything and pout it at 0,0

			// clear all

			//cells.do({|row|
			//	row.size.postln;
			//	row.do({|cell|
			//		cell.isBomb = false;
			//	});
			//});

			start = (cells.size - max).rand;
			genArr.do({|str, row|
				str.do({|char, i|
					cells[i+start][row].isBomb = (char == $O);
				})
			});
		})

		// put in a generator
		/*
		//[0,2].do({|i|
		//	cells.size.do({|j|
		//		cells[j][i].isBomb = false;
		//	});
		//});
		[0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,0,0,0,1,1,1,0,0,0,0,0,0,1,1,1,1,1,1,1,0,1,1,1,1,1,0].do({|val, index|
		cells[index][1].notNil.if({
		(val==1).postln;
		cells[index][1].isBomb = (val == 1);
		})
		});
		*/
	}


	step{
		var lastCount;

		cells.do({|row|
			row.do({|cell|
				/*
				Any live cell with fewer than two live neighbours dies, as if by underpopulation.
				Any live cell with two or three live neighbours lives on to the next generation.
				Any live cell with more than three live neighbours dies, as if by overpopulation.
				Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
				*/
				cell.isBomb.if({
					((cell.neighbourCount < 2) || (cell.neighbourCount > 3)).if({
						cell.isBomb = false;
					});
				}, {
					(cell.neighbourCount == 3).if({
						cell.isBomb = true;
						cell.changed;
					});
				});
			});
		});
		cells.do({|row|
			row.do({|cell|
				lastCount = cell.neighbourCount;
				(lastCount != cell.setCount(this)).if({
					cell.changed;
				});
				cell.evolve;
			})
		});
	}


	click{|x, y|

		super.click(x,y);
		//this.step();
	}

	uncover{ |cell|

		super.uncover(cell);
		this.step();
	}

	markCell{|game, cell|

		super.markCell(this, cell);
		this.step();
	}





}




MineSweeperCellGui : Button {

	var <model;//, <>bounds;

	*new {|parent, bounds, model|

		^super.new(parent,bounds).model_(model);
	}

	model_ {|mod|

		//var view;

		//super.init(parent, bounds);//, model);
		//this.bounds = bounds;

		model = mod;
		model.addDependant(this);

		this.states_([
			["", Color.black, Color.rand],
			[model.asString, Color.black, Color.white]
		]);
		//this.action_({|b| model.click; }); //b.value.postln;  model.postln;});

		//this.background = Color.rand;

		this.mouseDownAction_({|b,x,y,modifiers,buttonNumber|
			(buttonNumber == 0).if ({ // left
				model.click;
			}, {
				"right click".postln;
				model.toggleMark;
			})
		});

	}

	update {
		//"update".postln;
		model.marked.if({
			this.states = [[model.asString, Color.black, Color.rand],[model.asString, Color.black, Color.rand]];
		},{
			this.string = model.asString;

			model.visible.if({
				//StaticText.new(this,bounds).string_(model.asString);
				//this.states = [[""],[model.asString]];

				this.states_([[model.asString, Color.black, Color.white]]);
			});
		})
	}



}


MineSweeperGui : View {

	var <model, cellGuis;//, <>bounds;

	*new { arg parent, bounds, model, cells;

		^super.new(parent,bounds).init(bounds, model,cells);

	}



	init {|bounds, mod, cells|

		var width, height, cellW, cellH, rect, bound;


		//bounds.isNil.if({ bounds = parent.bounds; this.bounds = bounds; });
		model = mod;
		//view = parent.view;
		//view.bounds=bounds;
		bound = bounds?this.bounds;

		//this.decorator = FlowLayout(bound);
		//this.decorator.bounds =bound;


		width = cells.size;
		height = cells[0].size;

		cellW = bound.width/width;
		cellH = bound.height/height;
		cellGuis = Array.fill2D(width, height, {|r, c|

			rect = Rect(r*cellW, c*cellH, cellW, cellH);

			//rect.postln;
			//[r,c, rect].postln;
			cells[r][c].gui(this, rect)
		});
		//k= Array.fill(16,{|i| Knob(w,Rect((i%4)*100+10,i.div(4)*100+10,80,80)).background_(Color.rand)});


	}

	resize{|bounds|
		var width, height, cellW, cellH, rect;

		super.resize(bounds);

		width = cellGuis.size;
		height = cellGuis[0].size;

		cellW = bounds.width/width;
		cellH = bounds.height/height;
		width.do({|r|
			height.do({|c|

				rect = Rect(r*cellW, c*cellH, cellW, cellH);
				cellGuis[r][c].resizeToBounds(rect);
			});
		});

	}


}

MineSweeperSonify {
	var <game, >action, <>is_running, <semaphore, >start_action, >stop_action;

	*new {|gameClass, x, y, bombs|
		^super.new.init(gameClass,x, y, bombs);
	}

	init {|gameClass = \MineSweeper,x ,y ,bombs|


		game = gameClass.asClass;
		game = game.new(x,y,bombs);

		semaphore = Semaphore.new;

		is_running = false;

		game.uncoverAction = {|cell|
			//"uncover action".postln;
			cell.addDependant(this);
			this.update(cell);
		};

		game.addDependant(this);

	}

	update {|changed, changer|

		var cell;


		//"updateSon".postln;


		changed.isKindOf(MineSweeperCell).if({
			cell = changed;
		}, {
			changer.isKindOf(MineSweeperCell).if({
				cell = changer;
			});
		});

		(cell.notNil && action.notNil).if({
			action.(cell, this);
		});
	}

	start {|...arr|
		start_action.notNil.if({
			start_action.(*arr);
		});
	}

	stop {|...arr|
		stop_action.notNil.if({
			stop_action.(*arr);
		});
	}
}


MineSweeperTuningDiamond : MineSweeperSonify {

	var diamond, playing, changed_cells, <>baseFreq, <>ampScale, group, task, synth, subgroup,
	last_update, max_synth_size;

	*new {|gameClass, x, y, bombs, group|
		^super.new(gameClass,x, y, bombs).initTD(x,y, bombs, group);
	}

	initTD {|x, y, bombs, grp|

		var size, finishedMoveAction;

		"init".postln;

		size = x.max(y);

		////game = gameClass.asClass;
		////game = game.new(x,y,bombs);
		diamond = Diamond.size(size);
		////(gameClass + " class").postln;

		////super.init(gameClass,x ,y ,bombs);
		////this.superPerform(\init, gameClass,x ,y ,bombs);

		baseFreq=330;
		ampScale = 1/(bombs); //((1/(x*y))/1) *1.2;  // /2 was quiet, so *1.2

		////playing = Dictionary.new;
		playing = Dictionary.new;//Array.newClear(size.asInteger);
		changed_cells=[];
		is_running = false;

		max_synth_size = 20;



		finishedMoveAction = {|game/*, cell*/|

			var amp, name, count, ratio, existing, actionable, cellShouldPlay, neighbours, cells;

			//nil.foo; // get stack trace

			{

			semaphore.wait;
			playing = Dictionary.new;

			actionable = true;  // are we going to take action?

				cells = [game.visible(), game.marked()].flatten;

				("visible or marked cells" + cells.size).postln;

				cells.do({|cell|

				cellShouldPlay = true;
				neighbours = cell.neighbourCount;
			neighbours.isKindOf(SimpleNumber).not.if({ // uninitialised
				//neighbours = 0;
				cellShouldPlay = false;
			});
				cellShouldPlay = cellShouldPlay && (cell.isBomb || (neighbours > 0) );


				ratio = diamond.getInterval(cell.x, cell.y);
				existing = playing[ratio];

				(cellShouldPlay).if({
					actionable = true; // we can see the cell
				} , {
					existing.notNil.if({
						actionable = (existing >= 0.001); // it's playing right now already
					});
				});


				//"actionable".postln;

				amp = this.amp(ampScale,cell.neighbourCount,cell.visible, cell.isBomb);
				//amp.postln;

				existing = playing[ratio];
				existing.isNil.if({
					existing = 0;
				});

				(cellShouldPlay).if({
					amp = amp + existing;
				} , {
					amp = (existing - amp).max(0); // always be 0 or bigger
				});

				playing[ratio] = amp;

			});

			semaphore.signal; // this is a producer

				is_running.if({
					//{
						semaphore.wait;
						//name = this.pr_argName(ratio);
						//synth.set(name.asSymbol, amp);
						subgroup.set(\gate, 0);
						//srv.wait;
						subgroup.server.sync;

						this.pr_playSynth;
						0.1.wait;
						semaphore.signal;
					//}.fork;
				});

				"finished finishedMoveAction".postln;

			}.fork;

		};


		start_action =  {

			var def;

			"starting".postln;

			this.is_running.not.if({
				this.is_running = true;

				task = Task({
					var cells, amp, syn, srv, thresh = 0.0004,count = 0, clear = false, modified_cells, wait;

					modified_cells = [];

					group.notNil.if({
						srv = group.server;
					}, {
						srv = Server.default;
					});

					srv.waitForBoot({

						wait = 15;

						group.isNil.if({
							group = ParGroup(srv);
						});
						srv.sync;
						subgroup = ParGroup(group);

						// make synthdef
						this.pr_makeSynthDef().do({|def|
							srv.sync;
							//def.postln;
							def = def.interpret;
							////def.add(srv);
							def.send(srv);
							////1.wait;
						});
						srv.sync;
						//playing = playing.size.collect({|x| Synth("MineSweeper"++x);});
						//synth = Synth("MineSweeper");
						this.pr_playSynth;

						inf.do({
							(wait + wait.rand).wait;
							// make synthdef
							this.pr_makeSynthDef().do({|def|
								srv.sync;
								//def.postln;
								def = def.interpret;
								//def.add(srv);
								semaphore.wait;
								def.send(srv);
								semaphore.signal;

								//playing[i].set(\gate, 0);
								//playing[i] = Synth("MineSweeper"++i);
								//("MineSweeper"++i).postln;
								//synth.set(\gate, 0);
								//synth = Synth("MineSweeper");

							});
							//wait.wait;
							semaphore.wait;
							(Date.getDate.rawSeconds - last_update.rawSeconds > 4).if ({
								srv.sync;
								subgroup.set(\gate, 0);
								srv.sync;
								this.pr_playSynth;
							});
							semaphore.signal;
						});
					});
				});
				task.play;

				// do some auditing
				{
					inf.do({
						40.wait;
						finishedMoveAction.(game);
					});
				}.fork;
			});

		};

		this.start;
		game.finishedMoveAction = finishedMoveAction;


		stop_action = {
			{
				is_running = false;
				task.stop;
				semaphore.wait;
				//playing.do({|syn|
				//	syn[1].set(\gate, 0)
				//});
				//playing = Dictionary.new;
				//playing = playing.collect({|syn|
					synth.set(\gate, 0);
				//	nil;
				//});
				semaphore.signal;
				group.notNil.if({
					group.set(\gate, 0)
				});
				2.wait;
				group.notNil.if({
					group.freeAll
				});

			}.fork
		};

		"end init".postln;
	}


	amp { |scale, neighbours, visible, bomb|
		(visible.not || bomb).if({
			^0
		}, {
			^(scale.ampdb + neighbours).dbamp
		});
	}

	pr_argName{|x, size=7|
		^"a" ++ x.asString[0..(size-1)].padLeft(size, "0").replace(".", "_");
	}

	pr_playSynth{

		var actuallyPlaying, synthargs, size, keys, srv, letter, subkeys, amp, wait;

		"pr_playSynth".postln;

		actuallyPlaying = playing.select({|amp| amp.abs > 0.001}); // -60 db
		keys = actuallyPlaying.keys.asArray;
		keys.postln;
		keys.size.postln;

		wait = 0;

		group.notNil.if({
			srv = group.server;
		}, {
			srv = Server.default;
			group = ParGroup(srv);
			{srv.sync;}.try;
			subgroup = ParGroup(group);
		});

		amp = 16 * (keys.size / (max_synth_size*6)).ceil.reciprocal;

		{keys.size > 0}.while ({

			//{wait.wait}.try;

			subkeys = keys[0..(max_synth_size-1)];

			synthargs = subkeys.collect({|ratio, index|
				keys.removeAt(0); // keep knocking down the array
				[
					("r"++index).asSymbol, ratio,
					("a"++index).asSymbol, actuallyPlaying.at(ratio);
				]
			});

			size = subkeys.size;
			//synthargs = synthargs.add([("r"++size).asSymbol, 1, ("a"++size).asSymbol, 0]);
			synthargs = synthargs.add([\amp, amp]);
			synthargs = synthargs.flat;

			synthargs.postln;
			((synthargs.size/4)-1).postln;


			//{srv.sync}.try; // Threading
			{0.0001.rrand(0.1).wait;}.try;

			letter = $a.asInteger + 5.rand;

			synth = Synth("MineSweeper"++size++letter.asAscii.asString, synthargs, subgroup);
			last_update = Date.getDate();

			//wait = wait + 0.001.rrand(0.01);
		});
	}


	pr_makeSynthDef{|freq = 300|

		var actuallyPlaying, base, min, defs, letter;
		base = freq * 2.pow(-1);

		actuallyPlaying = playing.select({|amp| amp.abs > 0.001}); // -60 db

		min = actuallyPlaying.size.max(1);
		//defs = (min..(min+10)).collect({|i|


		defs = 5.collect ({|i|
			letter = ($a.asInteger + i).asAscii.asString;
			max_synth_size.collect({|j|
				this.pr_makeSynthDefSize((j+1), base, freq * 2.pow(i-1), letter);
			});
		});


		^defs.flatten;
	}

	pr_makeSynthDefSize{|size, base, freq, id|

		var rvar, avar, sinList, argList, string, divisions;



		sinList = [];

		argList = size.collect({|index|
			rvar = "r"++index;
			avar = "a"++index;

			sinList = sinList.add ("SinOsc.ar(freq * %, 0, %) * LagUD.kr(%/2, %*2, 3) * LFPulse.kr(% * 2.pow(-3.rrand(3)), 0, %)   * AmpComp.ir(% * freq, %) ".format(
				rvar,
				avar, // name of amplitude variable
				rvar, // up lag
				rvar, // down lag
				rvar , // pulse rate based on fracting in random octave
		        0.2.rrand(0.6), // pulse width is random
				rvar,
				base//, // Fletcher Munson
			));

			rvar + "=1," + avar + "=0";
		});

		string = "SynthDef(\\MineSweeper" ++ size ++ id ++", { arg gate=1, out=0, amp=1, freq=%,".format(freq) + argList.join(",\n") ++ """;
var env, sins, splay;
env = EnvGen.kr(Env.adsr(releaseTime:2.0.rrand(5)), gate, doneAction:2)* 2;
sins = [""";

		string = string + sinList.join(", ");
		string = string + """];
splay = Splay.ar(sins) * amp;
Out.ar(out, splay.tanh * env);
})""";


		^string;
	}

/*
	pr_makeSynthDefOld {|count|

		var argList, sinList, string, sds, base, freq=330, dedup, ratio;

		base = freq * 2.pow(-1);


		dedup = Dictionary.new;

		diamond.identities.size.do({|x|
			diamond.identities.size.do({|y|
				ratio = diamond.getInterval(x,y);
				dedup.put(this.pr_argName(ratio), ratio);
			})
		});



		argList = dedup.keys.asArray;
		"num sines is %".format(argList.size).postln;

		string = "SynthDef(\\MineSweeper, { arg gate=1, out=0, freq=%,".format(freq) + argList.join("=0,\n") ++ """;
var env, sins, splay;
env = EnvGen.kr(Env.asr(releaseTime:4.0.rrand(8)), gate, doneAction:2);
sins = [""";
		sinList = argList.collect({ |name|
			ratio = dedup.at(name);
			"SinOsc.ar(freq * %, 0, %) * LagUD.kr(%, %, 3) * LFPulse.kr(%, 0, %) * AmpComp.ir(%, %) ".format(
				ratio,
				name, // name of amplitude variable
				ratio/2, // up lag
				ratio *2, // down lag
				ratio * 2.pow(-3.rrand(3)), // pulse rate based on fracting in random octave
		        0.2.rrand(0.6), // pulse width is random
				ratio * freq,
				base//, // Fletcher Munson
			);
		});

		string = string + sinList.join(", ");
		string = string + "];\nsplay = Splay.ar(sins);\nOut.ar(out, splay.tanh * env);\n})";

		^string;
	}

	*/

	stopAll{ this.stop }

}