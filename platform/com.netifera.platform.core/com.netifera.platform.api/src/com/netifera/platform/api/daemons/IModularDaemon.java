package com.netifera.platform.api.daemons;

import java.util.Set;

public interface IModularDaemon extends IDaemon {
	Set<String> getInstalledModules();
	void setEnabled(String moduleName, boolean enabled);
	boolean isEnabled(String moduleName);
}
