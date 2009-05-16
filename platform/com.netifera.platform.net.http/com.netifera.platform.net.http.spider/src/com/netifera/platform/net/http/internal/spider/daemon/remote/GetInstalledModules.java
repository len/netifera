package com.netifera.platform.net.http.internal.spider.daemon.remote;

import java.util.Set;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class GetInstalledModules extends ProbeMessage {
	
	private static final long serialVersionUID = -7604877647651415508L;

	public final static String ID = "GetInstalledModules";

	private final Set<String> modules;
	
	public GetInstalledModules() {
		super(ID);
		modules = null;
	}
	
	public GetInstalledModules createResponse(Set<String> modules) {
		return new GetInstalledModules(modules, getSequenceNumber());
	}
	
	private GetInstalledModules(Set<String> modules, int sequenceNumber) {
		super(ID);
		this.modules = modules;
		setSequenceNumber(sequenceNumber);
		markAsResponse();
	}
	
	public Set<String> getModules() {
		return modules;
	}
}
