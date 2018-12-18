package com.xtool.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
//import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.openfeign.EnableFeignClients;
//import org.springframework.context.annotation.ComponentScan;

import com.xtool.service.EnableIot808ServiceClients;
//import org.springframework.context.annotation.Bean;

//import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
//import com.xtool.iot808data.order.EnableOrderMaintainer;

@SpringBootApplication
@EnableFeignClients(basePackages= {"com.xtool.service"})/* VERY IMPORTANT !! */
@EnableHystrix
@EnableCircuitBreaker
@EnableIot808ServiceClients
//@ComponentScan(basePackages={"com.xtool.order","com.xtool.service"})
public class Iot808OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(Iot808OrderServiceApplication.class, args);
	}
	/*
	@Bean
    public ServletRegistrationBean<HystrixMetricsStreamServlet> getHystrixMetricsStreamServlet() {
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean<HystrixMetricsStreamServlet> registrationBean = new ServletRegistrationBean<HystrixMetricsStreamServlet>(streamServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }*/
}
