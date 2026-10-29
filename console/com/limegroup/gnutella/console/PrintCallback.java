package com.limegroup.gnutella.console;

import java.io.File;
import java.io.PrintStream;
import java.util.Set;

import com.limegroup.gnutella.ActivityCallback;
import com.limegroup.gnutella.Connection;
import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.FileManagerEvent;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.Uploader;
import com.limegroup.gnutella.chat.Chatter;
import com.limegroup.gnutella.search.HostData;
import com.limegroup.gnutella.security.User;
import com.limegroup.gnutella.version.UpdateInformation;

public class PrintCallback implements ActivityCallback {
    private final PrintStream out;

    public PrintCallback(PrintStream out) {
        this.out = out;
    }

	public void acceptChat(Chatter ctr) {}

	public void addDownload(Downloader d) {
		//TODO: add to some list or something
		out.println(d+" added");
	}

	public void addressStateChanged() {}

	public void addSharedDirectory(File directory, File parent) {
		out.println("now sharing "+parent+File.separator.toString()+directory);
	}

	public void addSharedFile(FileDesc file, File parent) {}

	public void addUpload(Uploader u) {
		// TODO Auto-generated method stub
		
	}

	public void browseHostFailed(GUID guid) {
		out.println("couldn't browse host "+guid);
	}

	public void chatErrorMessage(Chatter chatter, String str) {}

	public void chatUnavailable(Chatter chatter) {}

	public void clearSharedFiles() {
		out.println("shared files cleared.");
	}

	public void componentLoading(String component) {
		out.println("loading "+component);
	}

	public void connectionClosed(Connection c) {
		// TODO Auto-generated method stub
	}

	public void connectionInitialized(Connection c) {
		// TODO Auto-generated method stub
	}

	public void connectionInitializing(Connection c) {
		// TODO Auto-generated method stub
	}

	public void downloadsComplete() {
		// TODO Auto-generated method stub
	}

	public void fileManagerLoaded() {
		// TODO Auto-generated method stub
	}

	public String getHostValue(String key) {
		return null;
	}

	public User getUserAuthenticationInfo(String host) {
		return null;
	}

	public void handleFileManagerEvent(FileManagerEvent evt) {
		// TODO Auto-generated method stub
	}

	public void handleQueryResult(RemoteFileDesc rfd, HostData data, Set locs) {
		// TODO make look better
		out.println(rfd.getFileName()+"   "+rfd.getSize()+"  locs:"+locs.size());
	}

	public void handleQueryString(String query) {}

	public void handleSharedFileUpdate(File file) {}

	public boolean isQueryAlive(GUID guid) {
		return true;
	}

	public void promptAboutCorruptDownload(Downloader dloader) {
		out.println(dloader.getFileName()+ " is corrupt :(");
	}

	public void receiveMessage(Chatter chr) {}

	public void removeDownload(Downloader d) {
		// TODO Auto-generated method stub
		
	}

	public void removeUpload(Uploader u) {
		// TODO Auto-generated method stub
		
	}

	public void restoreApplication() {}

	public void setAnnotateEnabled(boolean enabled) {}

	public void showDownloads() {
		// TODO Auto-generated method stub
		
	}

	public void updateAvailable(UpdateInformation info) {
		// TODO Auto-generated method stub
		out.println(info.getUpdateTitle());
		out.println(info.getUpdateText());
		out.println("go here: "+info.getUpdateURL());
	}

	public void uploadsComplete() {
		// TODO Auto-generated method stub
		
	}

	public boolean warnAboutSharingSensitiveDirectory(File dir) {
		out.println("warning, sharing possibly sensitive "+dir);
		return true;
	}
}
