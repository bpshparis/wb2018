package com.ekaly.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.ekaly.tools.BasicAuthInterceptor;
import com.ekaly.tools.Tools;
import com.ekaly.tools.UnsafeOkHttpClient;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Main4 {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, KeyManagementException, NoSuchAlgorithmException {
		// TODO Auto-generated method stub

		Path projectPath = Paths.get("/opt/wks/wb2018");
		
		Properties props = new Properties();
		props.load(new FileInputStream(projectPath + "/WebContent/res/conf.properties"));
		
		Path jsonPath = Paths.get(projectPath + "/s2t.resp.json");
		
		String json = new String(Files.readAllBytes(jsonPath));
		
		Map<Long, Map<String, Object>> whoSaidWhatWhen = Tools.whoSaidWhatWhen(json.toString());
		System.out.println(Tools.toJSON(whoSaidWhatWhen));
		
    	Map<String, List<Map<String, String>>> utterances = new HashMap<String, List<Map<String, String>>>();
    	List<Map<String, String>> sentences = new ArrayList<Map<String, String>>();
    	
    	for(Map.Entry<Long, Map<String, Object>> obj: whoSaidWhatWhen.entrySet()){
    	
    		Map<String, String> sentence = new HashMap<String, String>();
    		sentence.put("text", (String) obj.getValue().get("sentence"));
    		sentence.put("user", (String) String.valueOf(obj.getValue().get("speaker")));
    		sentences.add(sentence);
    	}
		
    	utterances.put("utterances", sentences);
		
		String url = Tools.getCredentialFromVS("tone_analyzer").get("url");
		String apikey = Tools.getCredentialFromVS("tone_analyzer").get("apikey");
		
		System.out.println(url);
		System.out.println(apikey);
		
		OkHttpClient ta0 = new UnsafeOkHttpClient().getUnsafeOkHttpClient().newBuilder()
				.addInterceptor(new BasicAuthInterceptor("apikey", apikey))
			    .build();
		
		HttpUrl.Builder urlBuilder = HttpUrl.parse(url + props.getProperty("TA_TONE_CHAT_METHOD")).newBuilder();
		urlBuilder.addQueryParameter("version", props.getProperty("TA_VERSION"));
		
		Request.Builder requestBuilder = new Request.Builder()
			.addHeader("Content-Language", props.getProperty("TA_ACCEPT_LANGUAGE"))
			.addHeader("Accept-Language", props.getProperty("TA_ACCEPT_LANGUAGE"))
			.addHeader("Content-Type", props.getProperty("TA_CONTENT_TYPE"))
			.url(urlBuilder.toString());		

		RequestBody body = RequestBody.create(MediaType.parse(props.getProperty("TA_CONTENT_TYPE")), Tools.toJSON(utterances).getBytes());		
		
		Request request = requestBuilder
			.post(body)
			.build();
		
		Response response = ta0.newCall(request).execute();

		String analysis = response.body().string();
		
		System.out.println(analysis);
		
		List<Map<String, Object>> us = (List<Map<String, Object>>) Tools.fromJSON(analysis).get("utterances_tone"); 

		System.out.println(whoSaidWhatWhen);
		
		for(Map<String, Object> u: us) {

			Long id = Long.parseLong(String.valueOf((Integer) u.get("utterance_id")));
			Map<String, Object> obj = whoSaidWhatWhen.get(id);
			
			List<Map<String, Object>> tones = new ArrayList<Map<String, Object>>();
			tones = (List<Map<String, Object>>) u.get("tones");
			
			Map<String, Double> tonesMap = new HashMap<String, Double>();
			
    		if(tones != null) {
        		for(Map<String, Object> tone: tones) {
        			tonesMap.put((String) tone.get("tone_id"), (double)tone.get("score"));
        		}
    		}	        			
			
    		if(obj != null) {
    			obj.put("tones", tonesMap);
    			obj.put("_tones", tones);
    		}

		}
		
    	
		Files.write(Paths.get(projectPath + "/results.json"), Tools.toJSON(whoSaidWhatWhen).getBytes());
		
		
		
	}

}
