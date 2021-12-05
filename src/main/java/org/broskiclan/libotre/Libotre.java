package org.broskiclan.libotre;

import lombok.Getter;
import lombok.Setter;
import org.broskiclan.libotre.bot.LBot;
import org.broskiclan.libotre.store.LConfiguration;
import org.broskiclan.libotre.log.Logger;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Formatter;
import java.util.Objects;

public final class Libotre {

	public static final String VERSION = "1.0.0";

	private static final Logger logger = new Logger();
	@NotNull
	@Getter
	private static String prefix = "!";
	@NotNull
	@Getter
	@Setter
	private static Path dbPath = Paths.get("db/");

	@Contract(pure = true)
	public static void printBuild() {
		var compiler = System.getProperty("java.compiler");
		logger.info("Libotre 1.0.0 [" + (compiler != null ? compiler : "interpreter") + "] on " + System.getProperty("os.arch"));
		logger.info("Running on JDK shipped by " + System.getProperty("java.vendor") + " in JVM \"" +  System.getProperty("java.vm.name") + "\"");
	}

	/**
	 * Changes the prefix of Libotre to the specified prefix.
	 * This method is <em>caller-sensitive</em>: if this method
	 * is not called by {@link Libotre} or {@link LConfiguration},
	 * an {@link IllegalCallerException} will be thrown.
	 * @param prefix the prefix to set the bot to use.
	 * @throws IllegalCallerException if the caller class is neither
	 * {@link LConfiguration} or {@link LBot}.
	 * @throws NullPointerException if prefix is null.
	 */
	public static void setPrefix(@NotNull String prefix) {
		if(
				StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() == LConfiguration.class ||
				StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() == LBot.class
		) {
			Libotre.prefix = Objects.requireNonNull(prefix, "IDK");
		} else throw new IllegalCallerException();
	}

	@Contract(pure = true)
	public static void printLicenseHeader() {
		Ansi ansi = Ansi.ansi();
		System.out.println(ansi.fgYellow().a("""
						[LICENSE] Libotre 1.0.0 - Copyright (C) 2021 The Libotre Authors
						[LICENSE] This program comes with ABSOLUTELY NO WARRANTY; for details
						[LICENSE] type 'warranty'.
						[LICENSE] This is free software, and you are welcome to redistribute it
						[LICENSE] under certain conditions; type 'conditions' for details.""").reset());
	}

	private Libotre() {}

}
