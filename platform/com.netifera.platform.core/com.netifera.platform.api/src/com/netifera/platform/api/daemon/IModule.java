package com.netifera.platform.api.daemon;

public interface IModule {

	/**
	 * Return a <code>String</code> which describes this module.
	 * This is the name which will appear in the configuration panel
	 * of the daemon in the UI.
	 * 
	 * @return String description of this module.
	 */
	String getName();
}
