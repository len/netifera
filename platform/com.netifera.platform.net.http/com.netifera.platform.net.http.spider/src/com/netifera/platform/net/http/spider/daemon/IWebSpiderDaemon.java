package com.netifera.platform.net.http.spider.daemon;

import java.util.Set;

import com.netifera.platform.api.daemon.IDaemon;
import com.netifera.platform.net.http.internal.spider.daemon.remote.WebSpiderConfiguration;
import com.netifera.platform.net.http.spider.IWebSpider;

public interface IWebSpiderDaemon extends IDaemon, IWebSpider {
	Set<String> getAvailableModules();
	WebSpiderConfiguration getConfiguration();
	void setConfiguration(WebSpiderConfiguration configuration);
	boolean isEnabled(String moduleName);
	void setEnabled(String moduleName, boolean enable);
	boolean isRunning();
	void start(long spaceId);
	void stop();
}
