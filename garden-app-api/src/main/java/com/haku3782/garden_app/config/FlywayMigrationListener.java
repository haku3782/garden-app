package com.haku3782.garden_app.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Spring Boot 4 ではFlywayの自動マイグレーション機能（FlywayAutoConfiguration）が
 * フレームワークから削除されたため、ApplicationContext構築前の段階で手動実行する。
 *
 * <p>ApplicationEnvironmentPreparedEvent は application.yaml / application.local.yaml の
 * 読み込みが完了した直後、かつ JPA・Hibernate の Bean 生成より前に発火するため、
 * ここでマイグレーションを完了させればHibernateの ddl-auto: validate と競合しない。
 */
public class FlywayMigrationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment env = event.getEnvironment();
        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");
        boolean baselineOnMigrate = env.getProperty("spring.flyway.baseline-on-migrate", Boolean.class, false);

        Flyway.configure()
                .dataSource(url, username, password)
                .baselineOnMigrate(baselineOnMigrate)
                .load()
                .migrate();
    }
}
