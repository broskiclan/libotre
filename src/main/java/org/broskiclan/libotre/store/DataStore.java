package org.broskiclan.libotre.store;

import lombok.SneakyThrows;
import org.broskiclan.libotre.Libotre;
import org.broskiclan.libotre.Main;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lmdbjava.DbiFlags.MDB_CREATE;

public final class DataStore {

	private final Env<ByteBuffer> env;
	private final Dbi<ByteBuffer> db;
	private static boolean shutdownHookAdded = false;

	public DataStore() {
		if(!shutdownHookAdded) {
			Runtime.getRuntime().addShutdownHook(new DataSaveWorker());
			shutdownHookAdded = true;
		}
		System.out.println("[INFO] Creating guild configuration database...");
		this.env = Env.create()
				.setMapSize(10_485_760)
				.setMaxDbs(1)
				.open(Libotre.getDbPath().toFile());
		this.db = env.openDbi("", MDB_CREATE);
	}

	public void save() {
		// db.put(ByteBuffer.wrap());
	}

	private class DataSaveWorker extends Thread {

		private static final AtomicInteger count = new AtomicInteger(0);
		private final int id;

		public DataSaveWorker() {
			this.id = count.getAndIncrement();
			this.setDaemon(true);
		}

		@SuppressWarnings("BusyWait")
		@SneakyThrows
		@Override
		public void run() {
			// this is a daemon thread: this thread will
			// be killed once all running threads are
			// daemons.
			while(true) {
				save();
				if(count.get() > 1 && id == 0) {
					sleep(Main.getSaveInterval());
				} else {
					return;
				}
			}
		}

	}

}
