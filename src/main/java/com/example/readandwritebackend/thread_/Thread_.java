package com.example.readandwritebackend.thread_;

import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

/**
 * @author 苏聪杰
 * @Description
 * @date 2021/7/26
 */
public class Thread_ extends  Thread {
    private Jedis jedis = new Jedis();
    private HttpServletRequest request;
    private MultipartFile file;
    private Boolean state=false;
    private int fre = 1000;

    {
        jedis.auth("123456");
        jedis.select(10);
    }

    public Thread_(HttpServletRequest request, MultipartFile file) {
        this.request = request;
        this.file = file;
    }

    public void setFre(int fre) {
        this.fre = fre;
    }

    public void changeState() {
       this.state=!this.state;
    }

    @Override
    public synchronized void run() {
        //为线程命名
        this.setName(file.getOriginalFilename());
        Map<String, Object> result = new HashMap();
        //获取文件名
        String orignName = file.getOriginalFilename();
        if (!orignName.endsWith(".csv")) {
            result.put("status", "error");
            result.put("msg", "文件类型不对");
            Thread.interrupted();
        }
        String filefloder = "D://upload/";
        File folder = new File(filefloder);
        //本地文件
        File localfile = new File(folder, orignName);
        try {
            if (!folder.exists()) {
                folder.mkdirs();
            }
            if(!localfile.exists()){
                //将file转化为指定文件
                this.file.transferTo(localfile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = null;
        String str = null;
        int index = 0;
        //数组用于存贮key
        String[] strTitle = null;
        //数组用来存value
        String[] strContent;
        try {
            //读取key
            bufferedReader = new BufferedReader(new FileReader(localfile));
            str = bufferedReader.readLine();
            //内容切割
            strTitle = str.split(",");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //读取value内容
            bufferedReader = new BufferedReader(new FileReader(localfile));
            while ((str = bufferedReader.readLine()) != null) {
                if(this.state){
                    LockSupport.park(this);
                }
                //由于读的时候是从第一行开始读的，但是csv文件的第一行是key，
                // 所以我们从第二行开始读value，index表示行数
                if (index > 0) {
                    //内容切割
                    strContent = str.split(",");
                    HashMap<String, String> hashMap = new HashMap<String, String>();
                    for (int i = 0; i < strContent.length; i++) {
                        //将每一行的内容输入的hashmap中，
                        hashMap.put(strTitle[i], strContent[i]);
                        System.out.println(strTitle[i] + "=" + strContent[i]);
                        //System.out.println(this.pause);
                    }
                    Thread.sleep(fre);
                    //读完一行后 存入redis中，之后循环读取下一行内容
                    jedis.hmset("dataSource", hashMap);
                }
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        result.put("status", "success");
    }
}