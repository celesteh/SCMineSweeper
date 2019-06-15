MineSweeperCell {

	var <x, <y, <>isBomb, <visible, <>marked, <neighbourCount, <>uncoverAction, <>bombClickAction, neighbours;

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
		neighbours.do({|n|
			//"loop".postln;
			(isBomb.not && (neighbourCount == 0)).if({

				n.uncover
			})
		});
		//});
	}

	uncover {
		// not a click
		(visible.not && isBomb.not).if({
			visible = true;
			this.changed;
			neighbours.do({|n|
				//"uncover loop".postln;
				(isBomb.not && (neighbourCount == 0)).if({
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
		visible.if({
			marked.if({
				isBomb.not.if({
					marked = false;
				})
			});
			isBomb.if({
				marked = true;
			});
		});
		this.changed;
	}


	gui {|layout, bounds|

		^MineSweeperCellGui(layout, bounds, this);
	}


}

MineSweeper {

	var cells, <>uncoverAction, <>bombClickAction;

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
					});
				});
			});
		});
		cells.do({|row|
			row.do({|cell|
				cell.setCount(this);
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

	var <game, diamond, dict, action, <>baseFreq, <>ampScale, semaphore;

	*new {|gameClass, x, y, bombs|
		^super.new.init(gameClass,x, y, bombs);
	}

	init {|gameClass = \MineSweeper,x ,y ,bombs|


		game = gameClass.asClass;
		game = game.new(x,y,bombs);
		diamond = Diamond.size(x.min(y));

		baseFreq=330;
		ampScale = ((1/(x*y))/4);

		dict = Dictionary.new;

		semaphore = Semaphore.new;

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


		game.uncoverAction = {|cell|
			cell.addDependant(this);
			action.(cell);
		};

		game.addDependant(this);
	}

	amp { |scale, neighbours, visible|
		visible.not.if({
			^0
		}, {
			^(scale * neighbours)
		});
	}

	update {|changed, changer|
		changed.isKindOf(MineSweeperCell).if({
			action.(changed);
		}, {
			//this.stopAll;
			changer.isKindOf(MineSweeperCell).if({
				action.(changer);
			});
		});
	}

	stopAll {

		dict.do({|syn|
			syn[1].set(\gate, 0)
		})
	}


}