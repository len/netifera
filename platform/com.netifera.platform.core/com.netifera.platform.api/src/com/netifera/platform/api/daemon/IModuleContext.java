package com.netifera.platform.api.daemon;

import com.netifera.platform.api.log.ILogger;

public interface IModuleContext {
	
	/**
	 * Returns the id value of the realm where entities should be placed.
	 * Realms are subsets of the entire data model where each entity is required to
	 * be uniquely identified.  As an example, two identical IP addresses can
	 * exist in the model but they must be in separate realms.  This allows 
	 * for representation of different private internal networks that have
	 * the same allocation of network ranges (ie: 192.168.x.x).
	 * 
	 * @return The id of the current realm.
	 */
	long getRealm();
	
	/**
	 * Returns the id value of the space that tools and sniffing modules
	 * should create entities in.
	 * In the user interface spaces are displayed as a collection of
	 * tabbed windows that the user can navigate.
	 * 
	 * @return The id value for the space where new entities should be displayed.
	 */
	long getSpaceId();

	ILogger getLogger();
}
