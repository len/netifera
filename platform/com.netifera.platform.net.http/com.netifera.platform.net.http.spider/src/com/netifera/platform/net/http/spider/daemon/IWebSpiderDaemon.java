package com.netifera.platform.net.http.spider.daemon;

import com.netifera.platform.api.daemons.IModularDaemon;
import com.netifera.platform.net.http.internal.spider.daemon.remote.WebSpiderConfiguration;
import com.netifera.platform.net.http.spider.IWebSpider;
import com.netifera.platform.net.http.spider.impl.WebSite;

public interface IWebSpiderDaemon extends IModularDaemon, IWebSpider {
	WebSpiderConfiguration getConfiguration();
	void setConfiguration(WebSpiderConfiguration configuration);
	
	boolean isEnabled(WebSite site);
	void setEnabled(WebSite site, boolean enable);
}
