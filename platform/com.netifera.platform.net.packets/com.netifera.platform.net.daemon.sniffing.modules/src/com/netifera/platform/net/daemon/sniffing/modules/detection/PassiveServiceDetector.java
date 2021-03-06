package com.netifera.platform.net.daemon.sniffing.modules.detection;

import java.nio.ByteBuffer;
import java.util.Map;

import com.netifera.platform.net.daemon.sniffing.IIPSniffer;
import com.netifera.platform.net.daemon.sniffing.IPacketModuleContext;
import com.netifera.platform.net.daemon.sniffing.IStreamModuleContext;
import com.netifera.platform.net.daemon.sniffing.ITCPBlockSniffer;
import com.netifera.platform.net.internal.daemon.sniffing.modules.Activator;
import com.netifera.platform.net.model.INetworkEntityFactory;
import com.netifera.platform.net.packets.tcpip.IP;
import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.packets.tcpip.UDP;
import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.services.credentials.Password;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.net.services.detection.IClientDetectorService;
import com.netifera.platform.net.services.detection.IServerDetectorService;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferConfig;
import com.netifera.platform.net.sniffing.stream.ISessionKey;
import com.netifera.platform.util.addresses.inet.InternetSocketAddress;
import com.netifera.platform.util.addresses.inet.TCPSocketAddress;
import com.netifera.platform.util.addresses.inet.UDPSocketAddress;

public class PassiveServiceDetector implements ITCPBlockSniffer, IIPSniffer {
	
	public IPacketFilter getFilter() {
		return null;
	}

	public String getName() {
		return "Passive Service Detector";
	}

	public void initialize(IBlockSnifferConfig config) {
		config.setTotalLimit(1024);
	}

	public void handleBlock(IStreamModuleContext ctx, ByteBuffer clientData, ByteBuffer serverData) {
		handleTCPBlock(ctx, clientData, serverData);
	}
	
	private void handleTCPBlock(IStreamModuleContext ctx, ByteBuffer clientData, ByteBuffer serverData) {
		final ISessionKey key = ctx.getKey();
		final long realm = ctx.getRealm();
		final long space = ctx.getSpaceId();
		Map<String,String> clientInfo, serverInfo;
		TCPSocketAddress socketAddress = new TCPSocketAddress(key.getServerAddress(), key.getServerPort());
		
		clientInfo = Activator.getInstance().getClientDetector().detect("tcp", key.getServerPort(), clientData, serverData);

		clientData.rewind();
		serverData.rewind();
		
		serverInfo = Activator.getInstance().getServerDetector().detect("tcp", key.getServerPort(), clientData, serverData);

		String serviceType = null;
		if(serverInfo != null) {
			serviceType = serverInfo.get("serviceType");
		} else if (clientInfo != null) {
			serviceType = clientInfo.get("serviceType");
		}

		if (serviceType != null) {
			Activator.getInstance().getNetworkEntityFactory().createService(realm, space, socketAddress, serviceType, serverInfo);
			Activator.getInstance().getNetworkEntityFactory().createClient(realm, space, key.getClientAddress(), serviceType, clientInfo, socketAddress);
			
			clientData.rewind();
			serverData.rewind();

			sniffCredentials(socketAddress, serviceType, clientData, serverData, realm, space);
		}
	}

	public void handleIPv4Packet(IPv4 ipv4, IPacketModuleContext context) {
		handleIPPacket(ipv4, context);
	}
	
	public void handleIPv6Packet(IPv6 ipv6, IPacketModuleContext context) {
		handleIPPacket(ipv6, context);
	}
	
	private void handleIPPacket(IP ip, IPacketModuleContext ctx) {
		final long realm = ctx.getRealm();
		final long space = ctx.getSpaceId();
		final IClientDetectorService clientDetector = Activator.getInstance().getClientDetector();
		final IServerDetectorService serverDetector = Activator.getInstance().getServerDetector();
		final INetworkEntityFactory factory = Activator.getInstance().getNetworkEntityFactory();
		
		if(clientDetector == null || serverDetector == null || factory == null)
			return;

		if (!ip.getSourceAddress().isUnspecified())
			factory.createAddress(realm, space, ip.getSourceAddress());
		
		if (ip.getNextHeader() instanceof UDP) {
			UDP udp = (UDP) ip.getNextHeader();
			ByteBuffer empty = ByteBuffer.allocate(0);
			Map<String,String> clientInfo, serverInfo;
			if(udp.payload() == null) return;
			
			clientInfo = clientDetector.detect("udp", udp.getDestinationPort(), udp.payload().toByteBuffer(), empty);
			if (clientInfo != null) {
				UDPSocketAddress socketAddress = new UDPSocketAddress(ip.getDestinationAddress(), udp.getDestinationPort());
				String serviceType = clientInfo.get("serviceType");
				if (isToUnicast(ip))
					factory.createService(realm, space, socketAddress, serviceType, null);
				if (isFromUnicast(ip))
					factory.createClient(realm, space, ip.getSourceAddress(), serviceType, clientInfo, socketAddress);
				sniffCredentials(socketAddress, serviceType, udp.payload().toByteBuffer(), empty, realm, space);
			} else {
				serverInfo = serverDetector.detect("udp", udp.getSourcePort(), empty, udp.payload().toByteBuffer());
				if (serverInfo != null) {
					UDPSocketAddress socketAddress = new UDPSocketAddress(ip.getSourceAddress(), udp.getSourcePort());
					String serviceType = serverInfo.get("serviceType");
					if (isFromUnicast(ip))
						factory.createService(realm, space, socketAddress, serviceType, serverInfo);
					if (isToUnicast(ip))
						factory.createClient(realm, space, ip.getDestinationAddress(), serviceType, null, socketAddress);
					sniffCredentials(socketAddress, serviceType, empty, udp.payload().toByteBuffer(), realm, space);
				}
			}
		}
	}

	private boolean isFromUnicast(IP ip) {
		if (!ip.getSourceAddress().isUniCast())
			return false;
		if (ip instanceof IPv4) {
			int source = ((IPv4)ip).getSourceAddress().toInteger();
			int destination = ((IPv4)ip).getDestinationAddress().toInteger();
			return !((source & destination) == destination && (source | destination) == source);
		}
		return true;
	}

	private boolean isToUnicast(IP ip) {
		if (!ip.getDestinationAddress().isUniCast())
			return false;
		if (ip instanceof IPv4) {
			int source = ((IPv4)ip).getSourceAddress().toInteger();
			int destination = ((IPv4)ip).getDestinationAddress().toInteger();
			return !((source & destination) == source && (source | destination) == destination);
		}
		return true;
	}

	private void sniffCredentials(InternetSocketAddress address, String serviceType, ByteBuffer clientData, ByteBuffer serverData, long realm, long view) {
		Credential credential = Activator.getInstance().getCredentialSniffer().sniff(serviceType, clientData, serverData);
		if(credential != null) {
			if (credential instanceof UsernameAndPassword) {
				UsernameAndPassword c = (UsernameAndPassword) credential;
				Activator.getInstance().getNetworkEntityFactory().createUsernameAndPassword(realm, view, address, c.getUsernameString(), c.getPasswordString());
			} else if (credential instanceof Password) {
				Password c = (Password) credential;
				Activator.getInstance().getNetworkEntityFactory().createPassword(realm, view, address, c.getPasswordString());
			}
		}
	}
}
