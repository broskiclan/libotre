package org.broskiclan.libotre.log;

import org.fusesource.jansi.Ansi;

public class Logger {

	private final Ansi ansi = Ansi.ansi();

	public void info(String s) {
		header("INFO", Ansi.Color.CYAN);
		System.out.println(s);
		ansi.reset();
	}

	/**
	 * Prints out a level<br>
	 * {@code printf("[%s]", level)}<br>
	 * in the given color
	 * @param level The level to be printed.
	 * @param color The color to print the level in.
	 */
	private void header(String level, Ansi.Color color) {
		var a = ansi.a("[").fg(color).a(level).reset().a("] ");
		System.out.print(a.toString());
		a.reset();
	}

}
