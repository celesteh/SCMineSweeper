(
var inst, gui, group;

Server.supernova;

s.waitForBoot({

	SynthDef(\sin, {|freq=440,gate=1,pan=0,amp=0.2, out=0|
		var env, sin, panner;
		env = EnvGen.kr(Env.asr(1,1,1.5), gate, doneAction:2);
		sin = SinOsc.ar(freq, 0, Lag.kr(amp,1));
		panner = Pan2.ar(sin, pan, env);

		Out.ar(out, panner);
	}).add;

	group = ParGroup(s);

	s.sync;

	inst = MineSweeperSonify(\GameOfLifeSweeper, 40, 40, 300.rrand(199), group);
	gui = inst.game.gui(nil,Rect(0,0,3000,2000));
	gui.front;
	gui.onClose = { inst.stopAll };


	s.meter;


});
)