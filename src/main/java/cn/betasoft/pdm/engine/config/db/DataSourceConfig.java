package cn.betasoft.pdm.engine.config.db;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

	@Autowired
	private Environment env;

	@Primary
	@Bean(name = "dataSource")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "quartzDataSource")
	@ConfigurationProperties(prefix = "quartz.datasource")
	public DataSource quartzDataSource() {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName(env.getRequiredProperty("quartz.datasource.driverClassName"));
		hikariConfig.setJdbcUrl(env.getRequiredProperty("quartz.datasource.url"));
		hikariConfig.setUsername(env.getRequiredProperty("quartz.datasource.username"));
		hikariConfig.setPassword(env.getRequiredProperty("quartz.datasource.password"));

		hikariConfig.setMaximumPoolSize(Integer.parseInt(env.getRequiredProperty("quartz.datasource.poolSize")));
		hikariConfig.setConnectionTestQuery("SELECT 1");
		hikariConfig.setPoolName("springHikariCP");

		HikariDataSource dataSource = new HikariDataSource(hikariConfig);
		return dataSource;
	}

}
