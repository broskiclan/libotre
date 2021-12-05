package org.broskiclan.libotre;

import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.text.StringEscapeUtils;
import org.broskiclan.libotre.bot.LBot;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static picocli.CommandLine.*;

/**
 * The main class for Libotre.
 */
@Command(
		name = "libotre",
		description = "A versatile Discord bot aiming to provide a modular experience that every paid " +
				"subscription provides, for free!",
		version = Libotre.VERSION,
		mixinStandardHelpOptions = true
)
public final class Main implements Callable<Integer> {

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";

	private LBot bot;

	@Option(
			names = {"--token", "-t"},
			required = true,
			description = "The bot token that Libotre will send calls to."
	)
	private static String botToken;

	@Option(
			names = {"--dbPath", "-d"},
			description = "The directory that Libotre will create an instance-wide database of guild configurations in.",
			defaultValue = "_NULL_"
	)
	@Nullable
	private static Path databasePath;

	@Option(
			names = {"--interval", "-i"},
			description = "How long Libotre's data-save worker should wait until it saves the current status of Libotre (again).",
			defaultValue = "300000"
	)
	@Getter
	private static long saveInterval;

	private static long inServers = 0;

	/**
	 * Computes a result, or throws an exception if unable to do so.
	 *
	 * @return computed result
	 */
	@SneakyThrows
	@Override
	@NotNull
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public Integer call() {
		AnsiConsole.systemInstall();
		AnsiConsole.err().println(ANSI_RED + """
				[WARN] ==================================================
				[WARN] !!! IT IS INSECURE TO RUN LIBOTRE IN ROOT MODE !!!
				[WARN] --------------------------------------------------
				[WARN] If you are not running Libotre with superuser
				[WARN] privileges, you may safely ignore this warning.
				[WARN] ==================================================
				""" + ANSI_RESET);
		Libotre.printLicenseHeader();
		JDA jda;
		if(botToken.contains(" ")) {
			AnsiConsole.err().println(ANSI_RED + "[ERROR] Invalid token: tokens cannot contain whitespaces." + ANSI_RESET);
			return 0;
		}
		if(databasePath != null) Libotre.setDbPath(databasePath);
		else databasePath = Libotre.getDbPath();
		try {
			Files.createDirectories(databasePath);
		} catch(IOException ex) {
			if(databasePath != null) {
				AnsiConsole.err().println(ANSI_RED + "[WARN] Unable to create given directory: defaulting to standard db/ path" + ANSI_RESET);
				try {
					Files.createDirectories(databasePath);
				} catch(IOException ex2) {
					AnsiConsole.err().println(ANSI_RED + "[ERROR] Unable to create default database directory" + ANSI_RESET);
					return 1;
				}
			} else {
				AnsiConsole.err().println(ANSI_RED + "[ERROR] Unable to create default database directory" + ANSI_RESET);
				return 1;
			}
		}
		try {
			jda = JDABuilder.createDefault(botToken)
					.setActivity(Activity.playing("initialization"))
					.build();
		} catch(LoginException e) {
			AnsiConsole.err().println(ANSI_RED + "[ERROR] Cannot login to token " + botToken + ANSI_RESET);
			return 1;
		} catch(ErrorResponseException e) {
			var code = e.getErrorCode();
			if(code == -1) AnsiConsole.err().println(ANSI_RED + "[ERROR] Unable to connect to the Internet" + ANSI_RESET);
			else AnsiConsole.err().println(ANSI_RED + "[ERROR] Discord servers returned error code " + code + ANSI_RESET);
			if(e.getMeaning() != null) AnsiConsole.err().println(ANSI_RED + "[ERROR] Cause: " + e.getMeaning() + ANSI_RESET);
			return 1;
		}
		var stopwatch = new StopWatch();
		AnsiConsole.out().println("[INFO] Added Libotre base listener to JDA");
		jda.addEventListener(new LBot(jda));
		AnsiConsole.out().println("[INFO] Awaiting connection...");
		stopwatch.start();
		try {
			jda.awaitReady();
		} catch(InterruptedException e) {
			stopwatch.stop();
			AnsiConsole.err().println(ANSI_RED + "[ERROR] JDA initialization interrupted" + ANSI_RESET);
			return 0;
		}

		stopwatch.stop();
		AnsiConsole.out().println("[INFO] Libotre connected in " + stopwatch.getTime(TimeUnit.MILLISECONDS) + "ms");
		AnsiConsole.out().println("[INFO] This Libotre instance is in " + inServers + " as of " + LocalDateTime.now());
		this.bot = new LBot(jda);
		long i = 0;
		var scanner = new Scanner(System.in);
		String line;
		System.out.println("\n\n\n\n");
		while(true) {
			AnsiConsole.out().print(Ansi.ansi().fg(Ansi.Color.GREEN).a(SystemUtils.getHostName()).fg(Ansi.Color.YELLOW).a(":libotre ").fg(Ansi.Color.CYAN).a(VERSION).reset().a(" $ "));
			try {
				line = scanner.nextLine();
			} catch(NoSuchElementException exception) {
				break;
			}
			boolean toExit = false;
			switch(line) {

				case "exit" -> {
					AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("[INFO] Terminating Libotre...").reset().toString());
					toExit = true;
				}
				case "servers" -> AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("[INFO] Libotre is in " + inServers + " servers.").reset().toString());

				case "warranty" -> AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.YELLOW).a("""
						[LICENSE] This program is distributed in the hope that it will be useful,
						[LICENSE] but WITHOUT ANY WARRANTY; without even the implied warranty of
						[LICENSE] MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
						[LICENSE] GNU General Public License for more details.""").reset().toString());

				case "conditions" -> AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.YELLOW).a("""
						[LICENSE] Please refer to the GNU GPL v3
						[LICENSE] (https://www.gnu.org/licenses/gpl-3.0.en.html)
						[LICENSE] for more information""").reset().toString());

				case "name" -> AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(jda.getSelfUser().getName()).reset().toString());
				case "source" -> AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("https://github.com/broskiclan/libotre").reset().toString());
				default -> {
					if(line.trim().length() > 0) {
						// tokenize string
						StringTokenizer tokenizer = new StringTokenizer(line);
						String command = null;
						ArrayList<String> args = new ArrayList<>();
						while(tokenizer.hasMoreTokens()) {
							var token = tokenizer.nextToken();
							if(command == null) command = token;
							else args.add(token);
						}
						if(command != null) {
							if(command.equalsIgnoreCase("name")) {
								jda.getSelfUser().getManager().setName(args.get(0));
							} else if(command.equalsIgnoreCase("avatar")) {
								Path path;
								try {
									path = Paths.get(StringEscapeUtils.escapeJava(args.get(0)));
									jda.getSelfUser().getManager().setAvatar(Icon.from(path.toFile()));
								} catch(InvalidPathException pathException) {
									AnsiConsole.out().println(ANSI_RED + "[ERROR] Cannot resolve path " + args.get(0) + ANSI_RESET);
								} catch(IllegalArgumentException argumentException) {
									AnsiConsole.out().println(ANSI_RED + "[ERROR] Path " + args.get(0) + " is of an unsupported type." + ANSI_RESET);
								}
							} else AnsiConsole.out().println(ANSI_RED + "[ERROR] Unknown command" + ANSI_RESET);
						}
						System.out.println();
					}
				}
			}
			if(toExit) {
				break;
			}
		}
		AnsiConsole.systemUninstall();
		return 0;
	}

	public static void main(String[] args) {
		System.exit(new CommandLine(new Main()).execute(args));
	}

	private static class ServerMonitorThread extends Thread {

		private final JDA jda;
		private final Formatter formatter = new Formatter();

		public ServerMonitorThread(JDA jda) {
			this.jda = jda;
		}

		@SneakyThrows
		@Override
		public void run() {
			inServers = jda.getGuildCache().size();
			jda.getPresence().setActivity(Activity.streaming(formatter.format("in %d servers", inServers).toString(), "https://github.com"));
			Thread.sleep(180000);
		}
	}

}
