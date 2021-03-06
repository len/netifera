package com.netifera.platform.net.tools.bruteforce;

import com.netifera.platform.api.tools.ToolException;
import com.netifera.platform.net.internal.tools.bruteforce.Activator;
import com.netifera.platform.net.model.UserEntity;
import com.netifera.platform.net.services.auth.CredentialsVerifier;
import com.netifera.platform.net.services.auth.TCPCredentialsVerifier;
import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.util.addresses.inet.TCPSocketAddress;

public class IMAPAuthBruteforcer extends UsernameAndPasswordBruteforcer {
	private TCPSocketAddress target;
	
	@Override
	protected void setupToolOptions() throws ToolException {
		target = (TCPSocketAddress) context.getConfiguration().get("target");
		context.setTitle("Bruteforce authentication on IMAP @ "+target);
		super.setupToolOptions();
	}
	
	@Override
	public void authenticationSucceeded(Credential credential) {
		UsernameAndPassword up = (UsernameAndPassword) credential;
		Activator.getInstance().getNetworkEntityFactory().createUsernameAndPassword(context.getRealm(), context.getSpaceId(), target, up.getUsernameString(), up.getPasswordString());
		
		//XXX is this good? the imap user might not be a local user
		UserEntity user = Activator.getInstance().getNetworkEntityFactory().createUser(context.getRealm(), context.getSpaceId(), target.getNetworkAddress(), up.getUsernameString());
		user.setPassword(up.getPasswordString());
		user.update();
		
		super.authenticationSucceeded(credential);
	}
	
	@Override
	public CredentialsVerifier createCredentialsVerifier() {
		TCPCredentialsVerifier verifier = new IMAPCredentialsVerifier(target);
		
		verifier.setMaximumConnections((Integer) context.getConfiguration().get("maximumConnections"));
		return verifier;
	}
}
