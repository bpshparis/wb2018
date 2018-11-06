package com.ekaly.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class Cmd {

	String name;
	String path;
	String cmd;
	Integer nr = 0;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public Integer getNr() {
		return nr;
	}
	public void setNr(Integer nr) {
		this.nr = nr;
	}
	
	public Map<String, Object> run() throws InterruptedException, IOException{
		
		Map<String, Object> result = new HashMap<String, Object>();

		ProcessBuilder pb = new ProcessBuilder();
		pb.directory(Paths.get(path).toFile());
		pb.command(StringUtils.split(cmd));
	
	    Process process = pb.start();
	
	    java.io.InputStream is = process.getInputStream();
	    InputStreamReader isr = new InputStreamReader(is);
	    BufferedReader br = new BufferedReader(isr);
	    String line;
	    int count = 0;
	    StringBuffer sb = new StringBuffer();
	    while ((line = br.readLine()) != null) {
	    	if(count++ >= nr) {
	    		sb.append(line);
	    	}
	    }
	    
	    //Wait to get exit value
		int exitValue = process.waitFor();
		result.put("RC", exitValue);
		result.put("OUTPUT", sb.toString());
		
		return result;
		
	}
}
