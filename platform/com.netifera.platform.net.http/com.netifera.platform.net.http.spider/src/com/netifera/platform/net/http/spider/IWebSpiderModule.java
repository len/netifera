package com.netifera.platform.net.http.spider;

import com.netifera.platform.api.daemons.IModule;

public interface IWebSpiderModule extends IModule {
	void start(IWebSpiderContext context);
	void handle(IWebSpiderContext context, HTTPRequest request, HTTPResponse response);
	void stop();
}
