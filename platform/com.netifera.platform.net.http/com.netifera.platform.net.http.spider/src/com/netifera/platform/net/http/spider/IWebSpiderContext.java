package com.netifera.platform.net.http.spider;

import java.net.URI;

import com.netifera.platform.api.daemons.IModuleContext;
import com.netifera.platform.util.locators.TCPSocketLocator;

public interface IWebSpiderContext extends IModuleContext {

	TCPSocketLocator getSocketLocator();
	URI getBaseURL();

	IWebSpider getSpider();
}
