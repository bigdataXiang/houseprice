package com.svail.houseprice;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.svail.geotext.GeoQuery;
import com.svail.util.FileTool;
import com.svail.util.HTMLTool;
import com.svail.util.Tool;

import net.sf.json.JSONObject;

public class GeoCoding{
	 static String Folder="D:/Crawldata_BeiJing/5i5j/resold/0604/";
	 public static String[] filename={"0324","0401","0414","0428","0510","0520","0527"};
     public static String[] zhoubian={"0324_zhoubian","0401_zhoubian","0414_zhoubian","0510_zhoubian","0520_zhoubian","0527_zhoubian"};
	 public static String type="woaiwojia_resold";
	 public static void main(String argv[]) throws Exception{
		 for(int i=0;i<filename.length;i++){
			 if(i>1){
				 String path=Folder+type+filename[i]+"_Null.txt";
				 if(i<3){
					 processCSV(path,false);
				 }else{
					 processCSV(path,true);
				 }
			 }
			 
			 
		 }
		 
		 System.out.println("OK!");
		
	 }
	 public static void processCSV(String file,boolean json) throws IOException
		{
			
			Vector<String> pois = FileTool.Load(file, "utf-8");
			String request ="http://192.168.6.9:8080/p41?f=json";
			//http://geocode.svail.com:8080/p41?f=xml
			//http://192.168.6.9:8080/p41?f=json
			String parameters ="&within="+ java.net.URLEncoder.encode("北京市", "UTF-8")+ "&key=206DA5B15B5211E5BFE0B8CA3AF38727&queryStr=";

			boolean batch = true;
			Gson gson = new Gson();
			if (batch)
				request = "http://192.168.6.9:8080/p4b?";
			StringBuffer sb = new StringBuffer();
			int offset = 0;
			String poi="";
			int count = 0;
			Vector<String> validpois = new Vector<String>();
			for (int n = 0; n < pois.size(); n ++) {
				if (batch) {
					
					String rs = pois.get(n);				
					String BUILDING_NAME="";
					String ADDRESS="";
					String TITLE="";
					String COMMUNITY="";
					String REGION="";
					if(json){
						JSONObject obj=JSONObject.fromObject(rs);
						if(rs.indexOf("BUILDING_NAME")!=-1){
							BUILDING_NAME=obj.getString("BUILDING_NAME");
						}
						if(rs.indexOf("ADDRESS")!=-1){
							ADDRESS=obj.getString("ADDRESS");
						}
						if(rs.indexOf("TITLE")!=-1){
							TITLE=obj.getString("TITLE");
						}
						if(rs.indexOf("community")!=-1){
							COMMUNITY=obj.getString("community");
						}
						if(rs.indexOf("location")!=-1){
							REGION=obj.getString("location");
						}
					}else{
						if(rs.indexOf("BUILDING_NAME")!=-1){
							BUILDING_NAME=Tool.getStrByKey(rs,"<BUILDING_NAME>","</BUILDING_NAME>","</BUILDING_NAME>").replace(" ", "");
						}
						if(rs.indexOf("ADDRESS")!=-1){
							ADDRESS=Tool.getStrByKey(rs,"<ADDRESS>","</ADDRESS>","</ADDRESS>").replace(" ", "");
						}
						if(rs.indexOf("TITLE")!=-1){
							TITLE=Tool.getStrByKey(rs,"<TITLE>","</TITLE>","</TITLE>").replace(" ", "");
						}
						if(rs.indexOf("COMMUNITY")!=-1){
							COMMUNITY=Tool.getStrByKey(rs,"<COMMUNITY>","</COMMUNITY>","</COMMUNITY>").replace(" ", "");
						}
						if(rs.indexOf("REGION")!=-1){
							REGION=Tool.getStrByKey(rs,"<REGION>","</REGION>","</REGION>").replace("-", "");
						}
					}
					
					
					validpois.add(rs);
					count ++;
					String address=REGION+COMMUNITY;
					sb.append(address).append("\n");
					if (((count == 1000) ||  n == pois.size() - 1)) {
						String urlParameters = sb.toString();
						System.out.print("批量处理开始：");
						count = 0;
						byte[] postData;
						try {
							postData = (parameters + java.net.URLEncoder.encode(urlParameters,"UTF-8")).getBytes(Charset.forName("UTF-8"));
							int postDataLength = postData.length;
					            
							URL url = new URL(request);
							//System.out.println(request + urlParameters);
							HttpURLConnection cox = (HttpURLConnection) url.openConnection();
							cox.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; rv:11.0) like Gecko");
							cox.setDoOutput(true);
							cox.setDoInput(true);
							cox.setInstanceFollowRedirects(false);
							cox.setRequestMethod("POST");
							// cox.setRequestProperty("Accept-Encoding", "gzip");  
							cox.setRequestProperty("Content-Type",
									"application/x-www-form-urlencoded");
							cox.setRequestProperty("charset", "utf-8");
							cox.setRequestProperty("Content-Length",
									Integer.toString(postDataLength));
							cox.setUseCaches(false);
							
							try (DataOutputStream wr = new DataOutputStream(
									cox.getOutputStream())) {
								
								wr.write(postData);
								
								InputStream is = cox.getInputStream();
								if (is != null) {
									byte[] header = new byte[2];
									BufferedInputStream bis = new BufferedInputStream(is);
									bis.mark(2);
									int result = bis.read(header);

									// reset输入流到开始位置
									bis.reset();
									BufferedReader reader = null;
									// 判断是否是GZIP格式
									int ss = (header[0] & 0xff) | ((header[1] & 0xff) << 8);
									if (result != -1 && ss == GZIPInputStream.GZIP_MAGIC) {
										// System.out.println("为数据压缩格式...");
										reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(bis), "utf-8"));
									} else {
										// 取前两个字节
										reader = new BufferedReader(new InputStreamReader(bis, "utf-8"));
									}
									
									// 创建一个JsonParser
									JsonParser parser = new JsonParser();
									String txt ="";
									//通过JsonParser对象可以把json格式的字符串解析成一个JsonElement对象
									try {
										
										txt = reader.readLine();
										//System.out.println(txt);
										if (txt == null) {
											System.out.println("txt为null！");
											for(int i=0;i<validpois.size();i++){
												FileTool.Dump(validpois.get(i), file.replace(".txt", "") + "_NullException.txt", "UTF-8");
												
											}
										}
										else {
											int index1=txt .indexOf("chinesename");
											String index3=",}";
											if(index1!=-1&&index3!=null)
												txt =txt .replace(",}", "}");
											 JsonElement el = parser.parse(txt);
											// JsonElement el = parser.parse(tesobj.toString());
											//把JsonElement对象转换成JsonObject
											JsonObject jsonObj = null;
											if(el.isJsonObject())
											{
												jsonObj = el.getAsJsonObject();
												//System.out.println(jsonObj);
	  										    GeoQuery gq = gson.fromJson(jsonObj, GeoQuery.class);
	  										 // System.out.println(gq);
	  										//  System.out.println(gq.getResult());
	  										  System.out.println(gq.getResult().size());
												String lnglat = "";
												String Admin="";
												if (gq != null && gq.getResult() != null && gq.getResult().size() > 0)
												{
													System.out.println("这批数据没有问题！");
													for (int m = 0; m < gq.getResult().size(); m ++)
													{
														if (gq.getResult().get(m) != null && gq.getResult().get(m).getLocation() != null)
														{
															if(gq.getResult().get(m).getLocation().getRegion()!=null)
															Admin=gq.getResult().get(m).getLocation().getRegion().getProvince()+","+gq.getResult().get(m).getLocation().getRegion().getCity()+","+gq.getResult().get(m).getLocation().getRegion().getCounty()+","+gq.getResult().get(m).getLocation().getRegion().getTown();
															else
																Admin="暂无";
															//System.out.println(Admin);
															lnglat = "<Coor>" + gq.getResult().get(m).getLocation().getLng() + ";" + gq.getResult().get(m).getLocation().getLat()+"</Coor>";
															double longitude=gq.getResult().get(m).getLocation().getLng();
															double latitude=gq.getResult().get(m).getLocation().getLat();
															
															String poitemp= validpois.elementAt(m);
															if(json){
																JSONObject jobj=JSONObject.fromObject(poitemp);
																jobj.put("region",Admin);
																jobj.put("longitude",longitude);
																jobj.put("latitude", latitude);
																
																FileTool.Dump(jobj.toString(), file.replace(".txt", "") + "_result.txt", "UTF-8");
															}else{
																 poi=poitemp.substring(0, poitemp.indexOf("<POI>")+"<POI>".length())+lnglat+"<Reg>"+Admin+"</Reg>"+poitemp.substring(poitemp.indexOf("<POI>")+"<POI>".length());//,poitemp.indexOf("</id>")+"</id>".length()
																 FileTool.Dump(poi, file.replace(".txt", "") + "_result.txt", "UTF-8");
															}															
														}
														else
														{
															FileTool.Dump(validpois.elementAt(m), file.replace(".txt", "") + "_nonPostalCoor1.txt", "UTF-8");
															//System.out.print(validpois.elementAt(m));
														}
													}
												}
											}
										}

									}catch (JsonSyntaxException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										System.out.println(e.getMessage());
										System.out.println("存在JsonSyntaxException异常！");
										for(int i=0;i<validpois.size();i++){
											FileTool.Dump(validpois.get(i), file.replace(".txt", "") + "_JsonSyntax.txt", "UTF-8");
											
										}
										FileTool.Dump(txt, file.replace(".txt", "") + "_JsonSyntaxException.txt", "UTF-8");
									}catch(NullPointerException e){
										System.out.println(e.getMessage());
										for(int i=0;i<validpois.size();i++){
											FileTool.Dump(validpois.get(i), file.replace(".txt", "") + "_Null.txt", "UTF-8");
											
										}
									}

								}
							}

						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}catch(NullPointerException e){
							e.printStackTrace();
							FileTool.Dump(poi, file.replace(".txt", "") + "_Null1.txt", "UTF-8");
						}

						validpois.clear();
						sb.setLength(0);
					}

				} 
			}
			
		}
	public static String parseLngLat(String query,String folder) throws UnsupportedEncodingException{
		String request = "http://192.168.6.9:8080/p41?f=json";
		String parameters ="&key=206DA5B15B5211E5BFE0B8CA3AF38727&queryStr=";

		Gson gson = new Gson();
		String lnglat = "";
		String admin="";
		String uri = null;
		try {
			uri = request + parameters+ java.net.URLEncoder.encode(query, "UTF-8");
			String xml = HTMLTool.fetchURL(uri, "UTF-8", "post");
			
			if (xml.length()!=0)
			{
				int index1=xml.indexOf("chinesename");
				String index2="result':[{{'";
				String index3=",}}}";
				if(index1!=-1&&index2!=null&&index3!=null)
					xml=xml.replace("result':[{{'", "result':[{'").replace(",}}}", "}}]}");
				// 创建一个JsonParser
				JsonParser parser = new JsonParser();
		
				//通过JsonParser对象可以把json格式的字符串解析成一个JsonElement对象
				try {
					JsonElement el = parser.parse(xml);

					//把JsonElement对象转换成JsonObject
					JsonObject jsonObj = null;
					if(el.isJsonObject())
					{
						jsonObj = el.getAsJsonObject();
						GeoQuery gq = gson.fromJson(jsonObj, GeoQuery.class);
						
						if (gq != null && gq.getResult() != null && gq.getResult().size() > 0 && gq.getResult().get(0).getLocation() != null)
						{
							if(gq.getResult().get(0).getLocation().getRegion()!=null)
							admin="<Reg>"+gq.getResult().get(0).getLocation().getRegion().getProvince()+","+gq.getResult().get(0).getLocation().getRegion().getCity()+","+gq.getResult().get(0).getLocation().getRegion().getCounty()+","+gq.getResult().get(0).getLocation().getRegion().getTown()+"</Reg>";
							else
								admin="暂无";
							lnglat ="<Coor>"+gq.getResult().get(0).getLocation().getLng() + ";" + gq.getResult().get(0).getLocation().getLat()+ "</Coor>" ;
							
						}
					}
					
					
				}catch (JsonSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(e.getMessage());
					System.out.println("存在JsonSyntaxException异常！");
					FileTool.Dump(xml, folder.replace(".txt", "") + "_JsonSyntax.txt", "UTF-8");		
				}
           }
			return lnglat+admin;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		return null;
	}
	
}
