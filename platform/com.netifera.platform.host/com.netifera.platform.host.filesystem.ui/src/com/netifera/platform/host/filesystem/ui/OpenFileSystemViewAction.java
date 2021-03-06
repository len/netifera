package com.netifera.platform.host.filesystem.ui;

import java.net.URI;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.netifera.platform.host.filesystem.IFileSystem;
import com.netifera.platform.host.internal.filesystem.ui.Activator;
import com.netifera.platform.ui.actions.SpaceAction;

public abstract class OpenFileSystemViewAction extends SpaceAction {
	
	public OpenFileSystemViewAction(final String name) {
		super(name);
		setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/folders.png"));
	}

	public abstract URI getFileSystemURL();
	
	@Override
	public void run() {
		IFileSystem fileSystem = (IFileSystem) Activator.getInstance().getServiceFactory().create(IFileSystem.class, getFileSystemURL(), this.getSpace().getProbeId());
		
		try {
			IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
					FileSystemView.ID,
					"SecondaryFileSystem" + System.currentTimeMillis(),
					IWorkbenchPage.VIEW_ACTIVATE);
			((FileSystemView)view).setInput(fileSystem);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
