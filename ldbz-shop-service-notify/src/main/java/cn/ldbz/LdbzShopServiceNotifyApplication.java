package cn.ldbz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.alibaba.dubbo.spring.boot.annotation.EnableDubboConfiguration;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;

@EnableDubboConfiguration
@EnableApolloConfig
@SpringBootApplication
public class LdbzShopServiceNotifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(LdbzShopServiceNotifyApplication.class, args);
	}
	
}
