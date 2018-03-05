package com.rsisland.plugin.teleportplugin;

public class TPPlayer
{
	private Policy tpReceivePolicy;
	
	public void setTpReceivePolicy(Policy tpReceivePolicy)
	{
		this.tpReceivePolicy = tpReceivePolicy;
	}
	
	public Policy getTpReceivePolicy()
	{
		if(tpReceivePolicy == null)
		{
			//TODO: Impl Request
			return Policy.ACCEPT;
		}
		return tpReceivePolicy;
	}
}
