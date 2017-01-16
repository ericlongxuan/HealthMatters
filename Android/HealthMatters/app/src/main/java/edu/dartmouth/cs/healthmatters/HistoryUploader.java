package edu.dartmouth.cs.healthmatters;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.ResponseHandler;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntity;
import ch.boye.httpclientandroidlib.entity.mime.content.ByteArrayBody;
import ch.boye.httpclientandroidlib.entity.mime.content.StringBody;
import ch.boye.httpclientandroidlib.impl.client.BasicResponseHandler;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;


public class HistoryUploader {

	public static boolean uploadFileApache(File uploadFile, String uploadurl) {
		boolean isSuccess = true;

		String filename = uploadFile.getName();
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(uploadurl);
		try {
			StringBody comment = new StringBody("Filename: " + filename);
			int size = (int) uploadFile.length();
			byte[] bytes = new byte[size];
			try {
				BufferedInputStream buf = new BufferedInputStream(new FileInputStream(uploadFile));
				buf.read(bytes, 0, bytes.length);
				buf.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ByteArrayBody byteArr = new ByteArrayBody(bytes,filename);
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("data", byteArr);
			reqEntity.addPart("comment", comment);
			httpPost.setEntity(reqEntity);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = httpclient.execute(httpPost, responseHandler);
			JSONObject result = new JSONObject(response);
			String retString = result.getString("result");
			if (retString.equals("SUCCESS")) {
				isSuccess = true;
			} else {
				isSuccess = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			isSuccess = false;
		}

		return isSuccess;
	}

}
