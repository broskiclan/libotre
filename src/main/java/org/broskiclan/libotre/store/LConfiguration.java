package org.broskiclan.libotre.store;

import lombok.Getter;

/**
 *
 */
public final class LConfiguration {

	@Getter private static final LConfiguration instance = new LConfiguration();

	@Getter private boolean debug;

	private LConfiguration() {
		debug = false;
	}

}
