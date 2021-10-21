(
var inst, gui, group;

Server.supernova;

s.waitForBoot({

	SynthDef(\sin, {|freq=440,gate=1,pan=0,amp=0.2, out=0, wait = 2|
		var env, sin, panner, alive, death, death_gate;//, imp;

		//imp = Impulse.kr((wait+1).reciprocal);
		//death_gate = EnvGen.kr(Env([1,1],[1], releaseNode:0))+Changed.kr(amp);
		death_gate = EnvGen.kr(Env([1,1,0], [wait, 1], 0)) + Lag.kr(Changed.kr(amp), wait);
		death = EnvGen.kr(Env.cutoff(wait), death_gate);
		alive = gate * death;
		//(Slew.kr(Changed.kr(amp+imp+gate) + imp, 1, wait.reciprocal) + imp);

		//wait.poll;
		//death_gate.poll;

		env = EnvGen.kr(Env.asr(1,1,1.5), alive, doneAction:2);
		sin = SinOsc.ar(freq, 0, Lag.kr(amp,1));
		panner = Pan2.ar(sin, pan, env);

		Out.ar(out, panner);
	}).add;

	group = ParGroup(s);

	s.sync;

	inst = MineSweeperTuningDiamond(\GameOfLifeSweeper, 40, 40, 300.rrand(199), group);
	//inst = MineSweeperSonify(\GameOfLifeSweeper, 40, 40, 300.rrand(199));
	//inst = GameOfLifeSweeper(40, 40, 300.rrand(199));


	"Do gui".postln;
	gui = inst.game.gui(nil,Window.screenBounds);
	//gui = inst.gui(nil,Window.screenBounds);
	gui.fullScreen;
	gui.front;
	gui.onClose = { inst.stop };

	inst.pr_makeSynthDef().do({|def|
		s.sync;
		//def.postln;
		def = def.interpret;
		//def.add(srv);
		def.load(s); // get ready for subsequent runs
	});

	s.meter;


});
)