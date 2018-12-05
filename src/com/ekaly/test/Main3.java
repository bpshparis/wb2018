package com.ekaly.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import com.ekaly.tools.BasicAuthInterceptor;
import com.ekaly.tools.Tools;
import com.ekaly.tools.UnsafeOkHttpClient;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Main3 {

	public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, IOException {
		
		Path projectPath = Paths.get("/opt/wks/wb2018");
		
		Properties props = new Properties();
		props.load(new FileInputStream(projectPath + "/WebContent/res/conf.properties"));
		
		String url = Tools.getCredentialFromVS("speech_to_text").get("url");
		String apikey = Tools.getCredentialFromVS("speech_to_text").get("apikey");
    	
		OkHttpClient s2tOkHttp = new UnsafeOkHttpClient().getUnsafeOkHttpClient().newBuilder()
				.addInterceptor(new BasicAuthInterceptor("apikey", apikey))
			    .build();
			
		HttpUrl.Builder urlBuilder = HttpUrl.parse(url + props.getProperty("S2T_RECOGNIZE_METHOD")).newBuilder();
		urlBuilder.addQueryParameter("model", props.getProperty("S2T_MODEL"));
		urlBuilder.addQueryParameter("speaker_labels", props.getProperty("S2T_SPEAKER_LABELS"));
		
		Request.Builder s2trb = new Request.Builder()
			.addHeader("Content-Type", props.getProperty("S2T_CONTENT_TYPE"))
			.addHeader("Transfer-Encoding", props.getProperty("S2T_TRANSFER_ENCODING"))		
			.url(urlBuilder.toString());		
		
		Path soundPath = Paths.get(projectPath + "/WebContent/playlist/Talking-To-A-Difficult-Customer.mp3");
		
//		RequestBody body = new MultipartBody.Builder()
//			.setType(MultipartBody.FORM)
//	        .addFormDataPart("file", soundPath.toString(), RequestBody.create(MediaType.parse(props.getProperty("S2T_CONTENT_TYPE")), soundPath.toFile()))
//	        .build();

		RequestBody body = RequestBody.create(MediaType.parse(props.getProperty("S2T_CONTENT_TYPE")), Files.readAllBytes(soundPath));
		
		Request s2tr = s2trb
			.post(body)
			.build();
		
		Response s2tResponse = s2tOkHttp.newCall(s2tr).execute();
		Files.write(Paths.get(projectPath + "/s2t.resp.json"), s2tResponse.body().string().getBytes());
		
		
		

		
		
//		options = new IamOptions.Builder()
//				  .apiKey("fLbevJlOcM3EiQYH7lzhfM-iwe3pHOrP0g49OPj0zHhX-")
//				  .build();
//		
//		ToneAnalyzer service = new ToneAnalyzer("2017-09-21", "apikey", "fLbevJlOcM3EiQYH7lzhfM-iwe3pHOrP0g49OPj0zHhX-");
//		service.setEndPoint("https://gateway-wdc.watsonplatform.net/tone-analyzer/api");
//	
//		String text =
//		  "I know the times are difficult! Our sales have been "
//		      + "disappointing for the past three quarters for our data analytics "
//		      + "product suite. We have a competitive data analytics product "
//		      + "suite in the industry. But we need to do our job selling it! "
//		      + "We need to acknowledge and fix our sales challenges. "
//		      + "We can’t blame the economy for our lack of execution! "
//		      + "We are missing critical sales opportunities. "
//		      + "Our product is in no way inferior to the competitor products. "
//		      + "Our clients are hungry for analytical tools to improve their "
//		      + "business outcomes. Economy has nothing to do with it.";
//	
//		// Call the service and get the tone
//		ToneOptions toneOptions = new ToneOptions.Builder()
//		  .html(text)
//		  .build();
//	
//		ToneAnalysis tone = service.tone(toneOptions).execute();
//		System.out.println(tone);
		
	}
	
}
