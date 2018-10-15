package com.vrem.wifianalyzer.wifi.fragmentWiFiHotspot.httpServer;

import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Log;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.wifi.common.ApLinkInfoUpdater;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.fragmentWiFiHotspot.ApLinkInfoAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class HTTPServer extends NanoHTTPD {
    public AssetManager asset_mgr;
    public String ip;
    private ApLinkInfoAdapter listAdapter;
    private List<String> list;
    private Handler handler;
    private int i = 0;
    public HTTPServer(Handler handler,ApLinkInfoAdapter listAdapter, List<String> promptList) {
        // 端口是8088，也就是说要通过http://127.0.0.1:8088来访当问
        super(9494);
        this.handler		= handler;
        this.listAdapter 	= listAdapter;
        this.list	  		= promptList;
    }

    //每当有客户端连接的时候都会进入这个方法当中
    public Response serve(String uri, Method method,
                          Map<String, String> header,
                          Map<String, String> parameters,
                          Map<String, String> files)
    {
        i = i+1;
        int len 	  = 0;
        byte[] buffer = null;
//		Log.d("Client ip", header.get("remote-addr"));
        String str 	  = parameters.get("NanoHttpd.QUERY_STRING"); //获取前置传来的ip port
        if (str != null){
            try {
                JSONObject jsonObject = new JSONObject(str);//封装为json数据
                Log.d("Cilent",jsonObject.get("ip")+" :"+jsonObject.get("port"));
                ip 			= jsonObject.get("ip").toString();
                final String port = jsonObject.get("port").toString();
//                PrefSingleton.getInstance().putString("ip",ip);
//                PrefSingleton.getInstance().putString("port",port);
//                PrefSingleton.getInstance().putString("url", "http://"+ip+":"+port+"");//将ip port存入存储类中，方便ApLinkInfoUpdater调用
//                new ApLinkInfoUpdater(MainContext.INSTANCE.getContext(),true).execute();//执行异步任务，发送数据给前置
                //调用线程更新UI
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                list.add("有设备接入，ip:"+ip+":"+port+"");
                                listAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 默认传入的url是以“/”开头的，需要删除掉，否则就变成了绝对路径
        String file_name   = uri.substring(1);

        // 默认的页面名称设定为index.html
        if(file_name.equalsIgnoreCase("")){
            file_name = "index.html";
        }
        try {
            //通过AssetManager直接打开文件进行读取操作
            InputStream in = asset_mgr.open(file_name, AssetManager.ACCESS_BUFFER);
            //假设单个网页文件大小的上限是1MB
            buffer = new byte[1024*1024];

            int temp=0;
            while((temp=in.read())!=-1){
                buffer[len]=(byte)temp;
                len++;
            }
            in.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 将读取到的文件内容返回给浏览器
        return new NanoHTTPD.Response(new String(buffer,0,len));
    }

    /*    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        Log.e("EEEEEEE","Method:"+method.toString());
        if (Method.GET.equals(method)) {
            //get方式
            String queryParams = session.getQueryParameterString();
            Log.e("EEEEEE",queryParams);
        } else if (Method.POST.equals(method)){
            //post方式
            Log.e("EEEEEE","into post");
            try {
                session.parseBody(new HashMap<String, String>());
                String body = session.getQueryParameterString();
                JSONObject jsonObject = new JSONObject(body);
                Log.d("接收到的数据",jsonObject.toString());

                //开始解析json数据
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ResponseException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return super.serve(session);
    }*/
}
