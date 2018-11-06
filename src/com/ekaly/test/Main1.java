package com.ekaly.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class Main1 {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		// TODO Auto-generated method stub
	
		try {
		    PrintStream out = System.out;
		    Path path  = Paths.get("/tmp");
		    int NR = 4;
		    String[] cmd = StringUtils.split("/usr/local/bin/ibmcloud service key-show ta0 user0");
		    
			ProcessBuilder pb = new ProcessBuilder();
			pb.directory(path.toFile());
			pb.command(cmd);
		
		    Process process = pb.start();
		
		    //Read out dir output
		    java.io.InputStream is = process.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    String line;
		    //out.printf("\n#### START running %s ####", Arrays.toString(cmds));
		    int count = 0;
		    StringBuffer sb = new StringBuffer();
		    while ((line = br.readLine()) != null) {
		    	if(count++ >= NR) {
		    		sb.append(line);
		    	}
		    }
		    //Wait to get exit value
			int exitValue = process.waitFor();
			out.print("RC=" + exitValue);
			out.print(sb.toString());
			out.printf("\n#### Command %s has been launched. Execute screen -x to watch progress ####", Arrays.toString(cmd), exitValue);
			}
		catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
		catch (IOException ioe){
		    ioe.printStackTrace(System.err);
		}
	}
}
