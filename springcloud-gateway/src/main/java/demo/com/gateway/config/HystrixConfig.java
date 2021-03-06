package demo.com.gateway.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;

//@Configuration
public class HystrixConfig {

//    @Bean
    public HystrixMetricsStreamServlet hystrixMetricsStreamServlet(){
        return new HystrixMetricsStreamServlet();
    }

//    @Bean
    public ServletRegistrationBean registration(HystrixMetricsStreamServlet servlet){
        ServletRegistrationBean registrationBean = new ServletRegistrationBean();
        registrationBean.setServlet(servlet);
        registrationBean.setEnabled(true);//是否启用该registrationBean
        registrationBean.addUrlMappings("/hystrix.stream");
        return registrationBean;
    }
}
