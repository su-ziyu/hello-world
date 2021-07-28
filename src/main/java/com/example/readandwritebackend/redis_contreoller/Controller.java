package com.example.readandwritebackend.redis_contreoller;

import com.example.readandwritebackend.redis_utils.RedisUtil;
import com.example.readandwritebackend.thread_.Thread_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * @author 苏聪杰
 * @Description
 * @date 2021/7/22s
 */
@SuppressWarnings("all")
@RestController
@CrossOrigin("*")
public class Controller {
    private static HashMap hashMap = new HashMap();

    @Autowired
    public RedisUtil redisUtil;

    @PostMapping("/upload")
    public void get(@RequestParam("file") MultipartFile[] files, HttpServletRequest request) {
        for (MultipartFile file : files) {
            String name = file.getOriginalFilename();
            Thread_ thread_ = new Thread_(request, file);
            thread_.start();
            hashMap.put(name, thread_);
        }

    }
    @GetMapping("/stopAll")
    public void stop() {
        System.out.println("停止所有文件读取");
        if (!hashMap.isEmpty()) {
            for (Object o : hashMap.values()) {
                Thread_ thread = (Thread_) o;
                thread.stop();
            }
        }
    }

    @GetMapping("/delete/{name}")
    public void delete(@PathVariable String name) {
        System.out.println("删除读取文件" + name);
        if (!hashMap.isEmpty()) {
            Thread_ thread = (Thread_) hashMap.get(name);
            thread.stop();
        }
    }

    @GetMapping("/pause/{name}")
    public void pause(@PathVariable String name) {
        System.out.println(name);
        if (hashMap.get(name) != null) {
            Thread_ t = (Thread_) hashMap.get(name);
            t.changeState();
        } else {
            System.out.println("线程未启动");
        }
    }

    @GetMapping("/continue/{name}")
    public void cont(@PathVariable String name) {
        System.out.println(name);
        if (hashMap.get(name) != null) {
            Thread_ t = (Thread_) hashMap.get(name);
            t.changeState();
            LockSupport.unpark(t);
        }
    }

    @GetMapping("/frequency/{name}&{fre}")
    public void modifyfre(@PathVariable String name, @PathVariable int fre) {
        if (hashMap.get(name) != null) {
            Thread_ thread_ = (Thread_) hashMap.get(name);
            fre = fre * 1000;
            thread_.setFre(fre);
        }
    }
}
