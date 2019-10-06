package com.vngmedia.infochanel.untils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import com.vngmedia.infochanel.domain.InfoVideoUpload;

public class ExportDataUntil {
	
	public JSONObject exportJson(String path, String type, Map<String, InfoVideoUpload> listVideo) {
		JSONObject jsonData = new JSONObject();
		String fileExport = path;
		try {
			for (Map.Entry<String, InfoVideoUpload> video : listVideo.entrySet()) {
				jsonData.append("data", new Gson().toJson(video.getValue()));
			}
			jsonData.put("fileExport", fileExport);
			jsonData.append("type", type);
			return jsonData;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public String wirteFileCSV(String path, Map<String, InfoVideoUpload> listVideo) {
		File fileFolder = new File(ChanelConstants.PATH_FILE_EXPORT_VIDEO);
		
		System.out.println(fileFolder.exists());
		if (!fileFolder.exists()) {
			fileFolder.mkdirs();
		}
		File fileData = new File(fileFolder.getAbsolutePath().concat(path));
		if (fileData.exists()) {
			return "exist file";
		} else {
			try {
				System.setProperty("file.encoding", "UTF-8");
				FileOutputStream fileStream = new FileOutputStream(fileData);
				OutputStreamWriter writerStream = new OutputStreamWriter(fileStream, "UTF-8");
				CSVWriter writer = new CSVWriter(writerStream);
				String [] header = {"Id", "Tên video", "Độ dài", "Kênh", "Playlist"};
				writer.writeNext(header);
				
				for (Map.Entry<String, InfoVideoUpload> video: listVideo.entrySet()) {
					String [] data = {String.valueOf(video.getValue().getId()), video.getValue().getNameVideo(), video.getValue().getTimeVideo(), video.getValue().getChanel(), video.getValue().getPlayList()};
					writer.writeNext(data);
				}
				writer.flush();
				writer.close();
				return fileData.getAbsolutePath();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "write erroe";
			}
		}
	}
	
	public String getCurrentDate () {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        return dateFormat.format(date).toString();
	}
	public static void main(String[] args) {
		String path = "\\video.csv";
		ExportDataUntil ex = new ExportDataUntil();
		Map<String, InfoVideoUpload> list = new HashMap<String, InfoVideoUpload>();
		InfoVideoUpload video =new InfoVideoUpload(1, "abc", "abc", "abc", "anv", "abc");
		InfoVideoUpload video1 =new InfoVideoUpload(2, "abc", "abc", "abc", "anv", "abc");
		InfoVideoUpload video2 =new InfoVideoUpload(3, "abc", "abc", "abc", "anv", "abc");
		list.put("1", video);
		list.put("2", video1);
		list.put("3", video2);
		String pathVideo = ex.wirteFileCSV(path, list);
		System.out.println(ex.exportJson(pathVideo, "chanel|Playlist", list));
		
        System.out.println(ex.getCurrentDate());
	}
}
