package com.ekaly.tools;

import java.util.ArrayList;
import java.util.List;

public class Resource {

	String service = "";
	String region = "";
	String instance = "";
	List<Credential> credentials = new ArrayList<Credential>();
	
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getInstance() {
		return instance;
	}
	public void setInstance(String instance) {
		this.instance = instance;
	}
	public List<Credential> getCredentials() {
		return credentials;
	}
	public void setCredentials(List<Credential> credentials) {
		this.credentials = credentials;
	}
	
}