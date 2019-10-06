package com.vngmedia.infochanel.api;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.vngmedia.infochanel.domain.InfoVideoUpload;
import com.vngmedia.infochanel.service.MappingInfoService;
import com.vngmedia.infochanel.untils.ChanelConstants;
import com.vngmedia.infochanel.untils.ExportDataUntil;

@Path("/")
public class VideosRestService {
	@GET
	@Path("/videos/chanel/{linkChanel}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInfoVideo(@PathParam("linkChanel") String chanelInput) {
		String chanel = ChanelConstants.HOME_YOUTOBE.concat(chanelInput);
		String path = "\\" + chanelInput + "_videos.csv";
		String type = "chanel|playlist";
		MappingInfoService service = new MappingInfoService();
		ExportDataUntil exp = new ExportDataUntil();
		/*Map<String, InfoVideoUpload> listVideo = new HashMap<String, InfoVideoUpload>();
		listVideo = service.mergeData(chanel);*/
		Map<String, InfoVideoUpload> listVideo = new HashMap<String, InfoVideoUpload>();
		InfoVideoUpload video =new InfoVideoUpload(1, "abc", "abc", "abc", "anv", "abc");
		InfoVideoUpload video1 =new InfoVideoUpload(2, "abc", "abc", "abc", "anv", "abc");
		InfoVideoUpload video2 =new InfoVideoUpload(3, "abc", "abc", "abc", "anv", "abc");
		listVideo.put("1", video);
		listVideo.put("2", video1);
		listVideo.put("3", video2);
		String pathVideo = exp.wirteFileCSV(path, listVideo);
		JSONObject json = exp.exportJson(pathVideo, type, listVideo);
		return Response.status(HttpURLConnection.HTTP_OK).entity(json.toString()).build();
	}
	
	@GET
	@Path("/playlists/chanel/{linkChanel}")
	public Response getMsg(@PathParam("linkChanel") String chanelInput) {
 
		String type = chanelInput;
 
		return Response.status(200).entity(chanelInput).build();
 
	}
	
	@GET
	@Path("/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response hello(@PathParam("param") String msg) {
 
		String output = "Jersey say : " + msg;
 
		return Response.status(200).entity(output).build();
 
	}
}
