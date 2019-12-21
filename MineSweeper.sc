MineSweeperCell {

	var <x, <y, <>isBomb, <visible, <>marked, <neighbourCount, <>uncoverAction, <>bombClickAction, neighbours, <>finishedMoveAction;

	*new {|x, y, isBomb=false|
		^super.newCopyArgs(x, y, isBomb, false, false); //not visible, not marked
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


	gui {|layout, bounds|

		^MineSweeperCellGui(layout, bounds, this);
	}


}

MineSweeper {

	var cells, <>uncoverAction, <>bombClickAction, <>finishedMoveAction;

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

	uncover{|cell|
		uncoverAction.value(cell);
	}

	bombClick{|cell|
		bombClickAction.value(cell);
	}

	finishedMove{|cell|
		finishedMoveAction.value(this, cell);
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




}


// gui
/*
MineSweeperCellGui : View {

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

//parent.isKindOf(View).if({
//	view = parent;
//}, {
//	view = parent.view;
//});

//view.bounds=bounds;
mouseDownAction = { "click".postln;
model.click;
StaticText.new(this,this.bounds).string_(model.asString).stringColor = Color.black;
this.background = Color.white;
this.parent.model.step;
};

this.background = Color.rand;

}

update {
/*
//(model.visible || model.marked).if({
this.children.do({|child|
child.postln;
child.isKindOf(StaticText).if({
child.string = model.asString;
});
});
*/
this.removeAll;
StaticText.new(this/*,this.bounds*/).string_(model.asString).stringColor = Color.black;
//"update".postln;
this.refresh;
//})
}



guiBody {  arg layout,bounds;

/*
var r;
// we refer to the model and
// access its variable howFast.
// if its a simple number, it will display
// using the default ObjectGui class, which
// will simply show its value as a string.
//model.howFast.gui(layout);
this.view.postln;
this.view.mouseDownAction = { "click".postln; model.click };
r = layout.layRight(300,300); // allocate yourself some space
Button(layout.win,r)
.action_({ arg butt;
model.click;
});
*/
model.asString.gui(layout);
}

}
*/

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

		//parent.isKindOf(View).if({
		//	view = parent;
		//}, {
		//	view = parent.view;
		//});

		//view.bounds=bounds;
		//mouseDownAction = { "click".postln;
		//	model.click;
		//	StaticText.new(this,bounds.width@bounds.height).string_(model.asString);
		/*
		b = Button(w, Rect(20, 20, 340, 30))
		.states_([
		["there is suffering", Color.black, Color.red],
		["the origin of suffering", Color.white, Color.black],
		["the cessation of suffering", Color.red, Color.white],
		["there is a path to cessation of suffering", Color.blue, Color.clear]
		])
		.action_({ arg butt;
		butt.value.postln;
		});
		*/
		//};
		this.states_([
			["", Color.black, Color.rand],
			[model.asString, Color.black, Color.white]
		]);
		this.action_({|b| model.click; }); //b.value.postln;  model.postln;});

		//this.background = Color.rand;

	}

	update {
		//"update".postln;
		model.marked.if({
			this.states = [[model.asString, Color.black, Color.rand],[model.asString, Color.black, Color.rand]];
		},{
			model.visible.if({
				//StaticText.new(this,bounds).string_(model.asString);
				//this.states = [[""],[model.asString]];
				this.string = model.asString;
				this.states_([[model.asString, Color.black, Color.white]]);
			})
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

	var <game, diamond, playing, changed_cells, action, <>baseFreq, <>ampScale, semaphore, group, task, <running;

	*new {|gameClass, x, y, bombs, group|
		^super.new.init(gameClass,x, y, bombs, group);
	}

	init {|gameClass = \MineSweeper,x ,y ,bombs, grp|


		game = gameClass.asClass;
		game = game.new(x,y,bombs);
		diamond = Diamond.size(x.min(y));

		baseFreq=330;
		ampScale = 1/(bombs); //((1/(x*y))/1) *1.2;  // /2 was quiet, so *1.2

		//playing = Dictionary.new;
		playing = Array.newClear(x);
		changed_cells=[];
		running = false;

		semaphore = Semaphore.new;
		/*
		action = {|cell|

		var syn, amp, set;


		set = {
		{
		0.0.rrand(0.1).wait;
		semaphore.wait;
		amp = this.amp(ampScale,cell.neighbourCount,cell.visible);
		dict[cell].isNil.if({
		dict.put(cell, [amp,
		Synth(\sin, [\freq, baseFreq*diamond.getInterval(cell.x,cell.y), \amp, amp]);
		]);
		}, {
		dict[cell][0] = amp;
		dict[cell][1].set(\amp, amp);
		});
		semaphore.signal;
		}.fork
		};


		amp = this.amp(ampScale,cell.neighbourCount,cell.visible);
		dict[cell].isNil.if({

		set.value;
		}, {
		(dict[cell][0] != amp).if({
		set.value;

		});
		});


		};
		*/

		action = {|cell|

			var amp, name;

			//changed_cells.includes(cell).not.if({
			//"add".postln;
			(cell.visible || cell.marked).if({
				/*
				AppClock.sched(0.rrand(0.1), {

					semaphore.wait;

					changed_cells.includes(cell).not.if({
						changed_cells = changed_cells ++ cell;
					});
					//"added".postln;
					semaphore.signal;
					nil;
				});//.fork;
				*/
				(running && playing[cell.x].notNil).if ({
					amp = this.amp(ampScale,cell.neighbourCount,cell.visible, cell.isBomb);
					name = this.pr_argName(cell.y);
					playing[cell.x].set(name.asSymbol, amp*8); // * 15
				});
			})
			//});
		};

		game.uncoverAction = {|cell|
			//"uncover action".postln;
			cell.addDependant(this);
			action.(cell);
		};

		game.addDependant(this);

		//game.finishedMoveAction = {
		//	this.start
		//}

		this.start;
	}

	amp { |scale, neighbours, visible, bomb|
		(visible.not || bomb).if({
			^0
		}, {
			^(scale.ampdb + neighbours).dbamp
		});
	}

	pr_argName{|x|
		^"a" ++ x.asString.padLeft(3, "0");
	}

	update {|changed, changer|
		//"update".postln;
		changed.isKindOf(MineSweeperCell).if({
			action.(changed);
		}, {
			//this.stopAll;
			changer.isKindOf(MineSweeperCell).if({
				action.(changer);
			});
		});
	}


	pr_makeSynthDef {

		var argList, sinList, string, sds;

		sds = diamond.identities.size.collect({|x|
			argList = diamond.identities.size.collect({|y| this.pr_argName(y) });
		//argList = argList.flatten;


			string = "SynthDef(\\MineSweeper"++x++", { arg gate=1, out=0, freq=330," + argList.join("=0, ") ++ """;
var env, sins, splay;
env = EnvGen.kr(Env.asr(releaseTime:5), gate, doneAction:2);
sins = [""";

		//sinList = diamond.identities.size.collect({|x| diamond.identities.size.collect({|y|
		sinList = argList.collect({ |name|
			var y, fraction;
			//name = this.pr_argName(x, y);
			y = name[1..3].asInt;
			//y = name[4..6].asInt;
			fraction = diamond.getInterval(x, y);
				"SinOsc.ar(freq * %, 0, LagUD.kr(%, %, 3) * LFPulse.kr(%, 0, %))".format(
					fraction * 2.pow((x%10)-2), // fraction adjust by octave based on postiion
					name, // name of amplitude variable
					fraction/2, // up lag
					fraction * 2.pow(-3.rrand(3)), // pulse rate based on fracting in random octave
					0.2.rrand(0.6)); // pulse width is random
		});//});
		//sinList = sinList.flatten;



		string = string + sinList.join(", ");
			string = string + "];\nsplay = Splay.ar(sins) * SinOsc.kr(% * SinOsc.kr(%, 0, 0.1,0.95), %, 0.5, 0.4);\n".format(
				20.0.rrand(50).reciprocal, // rate
				5.0.rrand(20).reciprocal,
				0.0.rrand(2pi));// phase
			string = string ++ """Out.ar(out, splay.tanh * env);
});
""";
		string.postln;
		string;
		});

		^sds

	}


	start {

		var def;

		running.not.if({
			running = true;

			task = Task({
				var cells, amp, syn, srv, thresh = 0.0004,count = 0, clear = false, modified_cells;

				modified_cells = [];

				group.notNil.if({
					srv = group.server;
				}, {
					srv = Server.default;
				});

				srv.waitForBoot({

					// make synthdef
					this.pr_makeSynthDef().do({|def|
						srv.sync;
						def = def.interpret;
						//def.add(srv);
						def.send(srv);
					});
					srv.sync;
					playing = playing.size.collect({|x| Synth("MineSweeper"++x);});

					/*

					{running}.while({
						//"% to analyse".format(changed_cells.size).postln;


						srv.sync;

						clear.if({
							0.1.wait;
						}, {
							0.01.rrand(0.2).wait;
						});


						semaphore.wait;
						//cell = changed_cells.pop;
						//cells = modified_cells ++ 30.collect({ changed_cells.pop; }).reject({|cell| cell.isNil });
						cells = 10.rrand(28).min(changed_cells.size).collect({ changed_cells.pop; }).reject({|cell| cell.isNil });
						//modified_cells = [];
						semaphore.signal;

						//cells.reject({|cell| cell.isNil });

						(cells.size == 0).if({
							clear = true
						}, {

							srv.makeBundle(1.0.rrand(3.0), {
								cells.do({|cell|
									cell.notNil.if({
										//modified_cells.includes(cell).not.if({
										//	modified_cells = modified_cells ++ cell;

										clear.if({
											"processing queue %".format(Date.getDate).postln;
										});
										clear = false;

										amp = this.amp(ampScale,cell.neighbourCount,cell.visible, cell.isBomb);
										//amp.postln;

										playing[cell].isNil.if({
											(amp >= thresh).if({
												playing.put(cell, [amp,
													Synth(\sin, [\freq, baseFreq*diamond.getInterval(cell.x,cell.y),
														\amp, amp,
														\pan, -0.8.rrand(0.8),
														\wait, 20
													], group);
												]);
												//0.001.rrand(0.2).wait;
												//count = count+1;
												//0.001.rrand(0.2).wait;
											});
										}, {
											(amp >= thresh).if({
												playing[cell][0]=amp;
												playing[cell][1].set(\amp, amp);
												//0.001.rrand(0.01).wait;
											},{
												syn = playing.removeAt(cell)[1];
												syn.set(\gate, 0);
												////0.01.rrand(0.1).wait;
											})
										})
									});
								}, { //nil cell
									clear.not.if({
										"cleared queue %".format(Date.getDate).postln;
										clear = true;
									});
									//0.1.wait
								});
								//});
							}); // end make bundle
							"playing size is %".format(playing.size).postln;
						});


						//changed_cells=[];
						/*
						new_amps.keysValuesDo({|key, value|
						playing[key].isNil.if({ // no synth
						(value >= 0.001).if({
						playing.put(key, [value, nil
						//test without sound
						//Synth(\sin, [\freq, baseFreq*diamond.getInterval(cell.x,cell.y), \amp, value], group);
						]);
						count = count +1;
						});
						}, {
						(playing[key][0] != value).if({

						(value >= 0.001).if({ // loud enough to keep alive
						playing[key][0] = value;
						//test without sound
						//playing[key][1].set(\amp, value);
						}, {
						syn = playing.removeAt(key)[1]; // or too quiet -> kill it
						// test without sound
						//syn.set(\gate, 0);
						});
						})
						});
						});
						new_amps = Dictionary.new;
						*/
						//semaphore.signal;
						//"echo finished sinished_round. started % synths".format(count).postln;//runInTerminal;
					});*/
				});

			});
			task.play
		});

	}

	stop {
		{
			running = false;
			semaphore.wait;
			//playing.do({|syn|
			//	syn[1].set(\gate, 0)
			//});
			//playing = Dictionary.new;
			playing = playing.collect({|syn|
				syn.set(\gate, 0);
				nil;
			});
			semaphore.signal;
			group.notNil.if({
				group.set(\gate, 0)
			});
			2.wait;
			group.notNil.if({
				group.freeAll
			});

		}.fork
	}

	stopAll{ this.stop }

}