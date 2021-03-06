package com.netifera.platform.net.services.basic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.netifera.platform.host.filesystem.File;
import com.netifera.platform.host.filesystem.IFileSystem;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.addresses.inet.TCPSocketAddress;

public class FTPFileSystem implements IFileSystem {

	private FTP ftp;
	private UsernameAndPassword credential;

	public FTPFileSystem(URI url) {
		InternetAddress address = InternetAddress.fromString(url.getHost());
		TCPSocketAddress socketAddress = new TCPSocketAddress(address, url.getPort());
		this.ftp = new FTP(socketAddress);

		String[] userInfo = url.getUserInfo().split(":");
		String username = userInfo[0];
		String password = userInfo.length > 1 ? userInfo[1] : "";
		this.credential = new UsernameAndPassword(username, password);
	}
	
	public FTPFileSystem(FTP ftp, UsernameAndPassword credential) {
		this.ftp = ftp;
		this.credential = credential;
	}
	
	public File createDirectory(String directoryName) throws IOException {
		FTPClient client = ftp.createClient(credential);
		try {
			if (client.makeDirectory(directoryName)) {
				File file = new File(this, directoryName, File.S_IFDIR, 0, 0);
				return file;
			}
		} finally {
			client.disconnect();
		}
		return null;
	}

	public boolean delete(String fileName) throws IOException {
		FTPClient client = ftp.createClient(credential);
		try {
			return client.deleteFile(fileName);
		} finally {
			client.disconnect();
		}
	}

	public boolean deleteDirectory(String directoryName) throws IOException {
		FTPClient client = ftp.createClient(credential);
		try {
			return client.removeDirectory(directoryName);
		} finally {
			client.disconnect();
		}
	}

	public boolean rename(String oldName, String newName) throws IOException {
		FTPClient client = ftp.createClient(credential);
		try {
			if (client.rename(oldName, newName)) {
/*				File oldFile = new File(this, oldName, File.FILE, 0, 0);
				File newFile = new File(this, newName, File.FILE, 0, 0);
				for (IFileSystemListener listener: listeners) {
					listener.removed(oldFile);
					listener.added(newFile);
				}
*/				return true;
			}
		} finally {
			client.disconnect();
		}
		return false;
	}
	
	private File convert(String directoryPath, FTPFile ftpFile) {
		int attributes = 0;
		if (ftpFile.isDirectory())
			attributes |= File.S_IFDIR;
		if (ftpFile.isFile())
			attributes |= File.S_IFDIR;
		return new File(this, directoryPath+"/"+ftpFile.getName(), attributes, ftpFile.getSize(), ftpFile.getTimestamp().getTimeInMillis());
	}

	private File[] convert(String directoryPath, FTPFile[] ftpFiles) {
		List<File> files = new ArrayList<File>();
		for (FTPFile ftpFile: ftpFiles)
			files.add(convert(directoryPath, ftpFile));
		return files.toArray(new File[files.size()]);
	}

	public File[] getDirectoryList(String directoryName) throws IOException {
		FTPClient client = ftp.createClient(credential);
		try {
//			client.pasv();
			FTPFile[] files = client.listFiles(directoryName);
			return convert(directoryName, files);
		} finally {
			client.disconnect();
		}
	}

	public String getNameSeparator() {
		return "/";
	}

	public InputStream getInputStream(String fileName) throws IOException {
		//TODO this is not complete, look at retrieveFileStream comment
		FTPClient client = ftp.createClient(credential);
		return client.retrieveFileStream(fileName);
	}

	public OutputStream getOutputStream(String fileName) throws IOException {
		//TODO this is not complete, look at storeFileStream comment
		FTPClient client = ftp.createClient(credential);
		return client.storeFileStream(fileName);
	}

	public File[] getRoots() {
		return new File[] {new File(this, "/", File.S_IFDIR, 0, 0)};
	}

	public File stat(String fileName) throws IOException {
/*		FTPClient client = ftp.createClient(credential);
		FTPFile[] files = client.listFiles(fileName);
		if (files.length > 1)
			return new File(this, fileName, File.S_IFDIR, 0, 0);
		return convert(files);
*/
		return null;
	}
	
	public void disconnect() {
	}
	
	@Override
	public String toString() {
		return ftp.toString();
	}
}
