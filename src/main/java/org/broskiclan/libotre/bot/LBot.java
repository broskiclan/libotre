package org.broskiclan.libotre.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.SystemUtils;
import org.broskiclan.libotre.Libotre;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class LBot extends ListenerAdapter {

	private record MessageEventWrapper(MessageReceivedEvent event, String... args) {}

	private final JDA jda;

	static {
		String temp2;
		/*try {
			String temp;
			var sigar = new Sigar();
			int i = 1;
			try {
				StringBuilder builder = new StringBuilder();
				for(CpuInfo info : sigar.getCpuInfoList()) {
					if(i == 11) {
						builder.append("*Core list: 10/").append(sigar.getCpuInfoList().length);
					} else {
						System.out.printf("[%d]\n", i);
						builder
								.append("\t**Model** ")
								.append(info.getModel())
								.append("\t**MHz** ")
								.append(info.getMhz())
								.append("\t**Shipper** ")
								.append(info.getVendor())
								.append("\t**Cores** ")
								.append(info.getTotalCores())
								.append("\n");
						i++;
					}
				}
				temp = builder.toString();
			} catch(SigarException e) {
				temp = "Cannot get CPU information";
			}
			temp2 = temp;
		} catch(UnsatisfiedLinkError error) {*/
			temp2 = "*Unable to retrieve information*";
		//}
		processorList = temp2;
	}

	private final HashMap<String, Consumer<MessageEventWrapper>> guildCommandMap = new HashMap<>();
	private final HashMap<String, Consumer<MessageEventWrapper>> dmCommandMap = new HashMap<>();

	private static final String processorList;

	public LBot(JDA jda) {
		this.jda = jda;
		System.out.println("[INFO] Registering modules...");
		System.out.println("[INFO] Registering module DMS...");
		// cannot be disabled by any guilds
		System.out.println("[INFO] Registering module CORE...");
		guildCommandMap.put("hostconfig", messageEventWrapper -> {
			var channel = messageEventWrapper.event().getChannel();
			channel.sendMessage("""
					**No configuration implemented at the moment**""").queue();
		});
		System.out.println("[INFO] Registering module MODERATION...");
		System.out.println("[INFO] All found modules registered");
	}

	private @Nullable User getFromMention(@NotNull String s) {
		String u = null;
		if(s.startsWith("<@!") && s.endsWith(">")) {
			u = (s.split("<@!")[1]).split(">")[0];
		}
		if(u == null) return null;
		return jda.retrieveUserById(u).complete();
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if(event.getAuthor().isBot()) return;
		boolean isDMs = event.isFromType(ChannelType.PRIVATE);
		if(Objects.equals(getFromMention(event.getMessage().getContentRaw()), jda.getSelfUser())) {
			var c = event.getChannel();
			c.sendMessage("**Prefix:** `" + Libotre.getPrefix() + "`\n" +
							"**Running on host:** " + SystemUtils.getHostName() + "\n" +
							"**Running on JVM:** " + SystemUtils.JAVA_VM_NAME + "\n" +
							"**Running on OS:** " + SystemUtils.OS_NAME + "\n" +
							"**JDK shipped from:** " + SystemUtils.JAVA_VENDOR + "\n" +
							"**JVM Processors:** " + Runtime.getRuntime().availableProcessors() + "\n\n" +
							new Formatter().format("For host configuration information see `%shostconfig`", Libotre.getPrefix()))
					.queue();
			return;
		}
		if(!event.getMessage().getContentRaw().startsWith(Libotre.getPrefix())) return;

		var tokenizer = new StringTokenizer(event.getMessage().getContentRaw());
		String command = null;
		ArrayList<String> args = new ArrayList<>(5);
		while(tokenizer.hasMoreTokens()) {
			if(command == null) {
				command = tokenizer.nextToken().split(Pattern.quote(Libotre.getPrefix()))[1];
			} else {
				args.add(tokenizer.nextToken());
			}
		}
		Consumer<MessageEventWrapper> consumer;
		if(isDMs) consumer = dmCommandMap.get(command);
		else consumer = guildCommandMap.get(command);
		if(consumer != null) consumer.accept(new MessageEventWrapper(event, args.toArray(String[]::new)));
	}

}