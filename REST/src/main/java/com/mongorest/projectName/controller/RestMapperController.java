package com.mongorest.projectName.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import javax.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.mongorest.projectName.model.Process1;

@RestController
public class RestMapperController {
    private static Properties serviceProp;
    private static FileOutputStream output;
    private static JSONArray requests;

    @RequestMapping(value = "/process/", method = RequestMethod.POST)
    public String process1(@RequestBody final Process1 process1,HttpServletRequest request) {


        JSONObject jobject = new JSONObject();
        try {
            ObjectMapper json= new ObjectMapper();


                jobject.put("time", new Date());
                jobject.put("method", request.getMethod());
                jobject.put("headers",request.getHeader("Content-Type"));
                jobject.put("path", request.getRequestURI());
                if(request.getQueryString()!=null){
                jobject.put("query", request.getQueryString());
                }
                else {
                    jobject.put("query", "No query string associated");
                }
                jobject.put("body", json.writeValueAsString(process1));
                int randomNum = ThreadLocalRandom.current().nextInt(15000, 30000 + 1);
                Thread.sleep(randomNum);
                jobject.put("duration", randomNum/1000);
                System.out.println(requests);
                System.out.println(jobject);
                requests.put(jobject);
                serviceProp.setProperty("requests",requests.toString());
                serviceProp.store(output,null);
                return jobject.toString();

        } catch (final Exception e) {
            e.printStackTrace();
            return jobject.toString();
        }
    }

    @RequestMapping(value = "/stats/", method = RequestMethod.POST)
    public String stats(@RequestBody final Process1 process1,HttpServletRequest request) {


        JSONObject jobject = new JSONObject();
        try {
            int post=0,get=0,put=0;
            double responseTimeAvg=0;
            double responseTimeAvgMinute=0;
            double responseTimeAvgHour=0;
            int totalResponseTime=0;
            int totalResponseTimeMinute=0;
            int totalResponseTimeHour=0;
            int totalRequests=requests.length();
            int requestsPerMinute=0;
            int requestsPerHour=0;

            String current= new Date().toString();
            System.out.println(current);
            for(int i=0;i<totalRequests;i++){

                totalResponseTime= totalResponseTime + requests.getJSONObject(i).getInt("duration");
                String htttpMethod=requests.getJSONObject(i).getString("method");
                String time= requests.getJSONObject(i).getString("time");
                SimpleDateFormat dt = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
                System.out.println(dt.parse(time).getTime()+"==="+dt.parse(current).getTime() );
                long diffinTimeToMinutes=(dt.parse(current).getTime()-dt.parse(time).getTime())/(1000*60);
                long diffinTimeToSeconds=(dt.parse(current).getTime()-dt.parse(time).getTime())/(1000);
                System.out.println("DiFF in seconds"+diffinTimeToSeconds);
                System.out.println("DiFF in minutes"+diffinTimeToMinutes);
                if(htttpMethod.equals("POST")){
                    ++post;
                    if(diffinTimeToSeconds<=60) {

                        ++requestsPerMinute;
                        totalResponseTimeMinute=totalResponseTimeMinute+requests.getJSONObject(i).getInt("duration");
                    }
                        if(diffinTimeToMinutes<=60) {

                            ++requestsPerHour;
                            totalResponseTimeHour=totalResponseTimeHour+requests.getJSONObject(i).getInt("duration");

                            }


                }
                else if(htttpMethod.equals("GET")){
                    ++get;
                }
                else if(htttpMethod.equals("PUT")){
                    ++put;
                }


            }
            responseTimeAvg= totalResponseTime/totalRequests;
            if(requestsPerMinute>0){
            responseTimeAvgMinute= totalResponseTimeMinute/requestsPerMinute;
            } else {responseTimeAvgMinute=0;}
            if(requestsPerHour>0){
            responseTimeAvgHour= totalResponseTimeHour/requestsPerHour;
            } else {responseTimeAvgHour=0;}
            JSONObject forPastHour= new JSONObject();
            forPastHour.put("Number of Requests", requestsPerHour);
            forPastHour.put("Avarage response time", responseTimeAvgHour);

            JSONObject forPastMinute= new JSONObject();
            forPastMinute.put("Number of Requests", requestsPerMinute);
            forPastMinute.put("Avarage response time", responseTimeAvgMinute);

            jobject.put("status", "success");
            jobject.put("Total number of requests", totalRequests);
            jobject.put("Avarage Response time", responseTimeAvg);
            jobject.put("Active number of requests", get+" GET requests, "+post+" POST requests, "+put+" PUT requests, ");
            jobject.put("Number of requests and average response times, in the past hour POST", forPastHour);
            jobject.put("Number of requests and average response times, in the past minute POST", forPastMinute);

            return jobject.toString();

        } catch (final Exception e) {
            e.printStackTrace();
            return jobject.toString();
        }
    }

    public RestMapperController(){

        System.out.println("================Reading from Application Properties for maintaining the presistent sesssion==========");
        try {
        serviceProp = new Properties();
        String home = System.getProperty("user.home");
        String propFile = "projectName.properties";

        if (new File(home + "/." + propFile).exists()) {
            FileInputStream inputStream = new FileInputStream(new File(home + "/." + propFile));
            output = new FileOutputStream(new File(home + "/." + propFile),true);
            serviceProp.load(inputStream);
            inputStream.close();
            System.out.println("Application is reading configuration from " + home);
        } else {
            System.out.println("Application property file does not exist");
        }
        if(serviceProp.getProperty("requests")!=null)
        {
        requests=new JSONArray(serviceProp.getProperty("requests"));
        }
        else {
            requests=new JSONArray();
        }
        }
        catch (Exception e) {
            e.printStackTrace();

        }
    }

}
