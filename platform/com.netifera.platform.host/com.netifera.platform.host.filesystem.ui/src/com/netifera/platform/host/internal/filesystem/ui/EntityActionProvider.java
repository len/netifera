package com.netifera.platform.host.internal.filesystem.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.host.filesystem.FileSystemLocator;
import com.netifera.platform.host.filesystem.spider.IFileSystemSpiderModule;
import com.netifera.platform.host.filesystem.tools.FileSystemHarvester;
import com.netifera.platform.host.filesystem.ui.OpenFileSystemViewAction;
import com.netifera.platform.tools.options.IntegerOption;
import com.netifera.platform.tools.options.MultipleStringOption;
import com.netifera.platform.tools.options.StringOption;
import com.netifera.platform.ui.actions.SpaceAction;
import com.netifera.platform.ui.actions.ToolAction;
import com.netifera.platform.ui.api.actions.IEntityActionProvider;

public class EntityActionProvider implements IEntityActionProvider {

	private ILogger logger;
	private IProbeManagerService probeManager;
	final private List<IFileSystemSpiderModule> modules = new ArrayList<IFileSystemSpiderModule>();
	
	public List<IAction> getActions(IShadowEntity shadow) {
		List<IAction> answer = new ArrayList<IAction>();
		
		FileSystemLocator fileSystemLocator = (FileSystemLocator) shadow.getAdapter(FileSystemLocator.class);
		if (fileSystemLocator != null) {
			ToolAction harvester = new ToolAction("Harvest File System", FileSystemHarvester.class.getName());
			harvester.addFixedOption(new StringOption("target", "Target", "Target File System", "file:///"));
			harvester.addOption(new MultipleStringOption("modules", "Modules", "Harvesting modules to activate during this havesting session", "Modules", getAvailableFileSystemSpiderModules()));
			harvester.addOption(new IntegerOption("maximumThreads", "Maximum threads", "Maximum number of threads", 5));
			harvester.addOption(new IntegerOption("bufferSize", "Buffer size", "Maximum bytes to fetch for each file", 1024*16));
			answer.add(harvester);
		}
		
		return answer;
	}

	public List<IAction> getQuickActions(IShadowEntity shadow) {
		List<IAction> answer = new ArrayList<IAction>();
		
		final FileSystemLocator fileSystemLocator = (FileSystemLocator) shadow.getAdapter(FileSystemLocator.class);
		if (fileSystemLocator != null) {
			SpaceAction action = new OpenFileSystemViewAction("Browse File System") {
				@Override
				public URI getFileSystemURL() {
					return fileSystemLocator.getURL();
				}
			};
			answer.add(action);
		}
		
		return answer;
	}

	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("File System Actions");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}
	protected void setProbeManager(IProbeManagerService probeManager) {
		this.probeManager = probeManager;
	}
	
	protected void unsetProbeManager(IProbeManagerService probeManager) {
		
	}
	
	private String[] getAvailableFileSystemSpiderModules() {
		List<String> names = new ArrayList<String>();
		for (IFileSystemSpiderModule module: modules)
			names.add(module.getName());
		return names.toArray(new String[names.size()]);
	}
	
	protected void registerFileSystemSpiderModule(IFileSystemSpiderModule module) {
		this.modules.add(module);
	}
	
	protected void unregisterFileSystemSpiderModule(IFileSystemSpiderModule module) {
		this.modules.remove(module);
	}
}
