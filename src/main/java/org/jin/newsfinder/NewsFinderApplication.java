package org.jin.newsfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NewsFinderApplication {
    public static void main(String[] args) {
        // 시간대 없는 피드 날짜(rome.properties)를 서버 위치와 상관없이 한국 시간으로 해석하기 위해
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SpringApplication.run(NewsFinderApplication.class, args);
    }
}
