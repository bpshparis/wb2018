package com.ekaly.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ekaly.tools.Cmd;
import com.ekaly.tools.Tools;
import com.fasterxml.jackson.core.type.TypeReference;

public class Main2 {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
//		Cmd cmd = new Cmd();
//		cmd.setName("TA0_KEY");
//		cmd.setCmd("/usr/local/bin/ibmcloud service key-show ta0 user0");
//		cmd.setNr(4);
//		cmd.setPath("/tmp");
		
		Path path = Paths.get("/opt/wks/ftwta/WebContent/res/IC_LI_CMD.json");

		Cmd cmd = (Cmd) Tools.fromJSON(path.toFile(), new TypeReference<Cmd>(){});
		
		System.out.println(Tools.toJSON(cmd));
		
		try {
//			Map<String, Object> output = Tools.fromJSON((String) cmd.run().get("OUTPUT"));
			int rc = (int) cmd.run().get("RC");
			if(rc == 0) {
				path = Paths.get("/opt/wks/ftwta/WebContent/res/taKeyCmd.json");
				cmd = (Cmd) Tools.fromJSON(path.toFile(), new TypeReference<Cmd>(){});
				System.out.println(cmd.run().get("OUTPUT"));
			}
			
			
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
