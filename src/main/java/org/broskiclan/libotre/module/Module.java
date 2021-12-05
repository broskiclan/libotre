package org.broskiclan.libotre.module;

import com.google.common.flogger.FluentLogger;
import org.broskiclan.cexl.ExtensionDefinition;
import org.jetbrains.annotations.NotNull;

public abstract class Module implements ExtensionDefinition {

	/**
	 * Returns the extension name.
	 * @return the name of the extension
	 */
	@Override
	public abstract @NotNull String getName();

	/**
	 * Called by the extension loader when
	 * loaded.
	 */
	@Override
	public abstract void onLoad();

	@Override
	public final @NotNull FluentLogger getLogger() {
		return FluentLogger.forEnclosingClass();
	}
}
