package com.list.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
public class TodoListApplication extends AsyncConfigurerSupport {

	public static void main(String[] args) {
		SpringApplication.run(TodoListApplication.class, args);
	}

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("mail-sender-");
		executor.initialize();
		return executor;
	}

	// TODO: девалидация токена
	// TODO: придумать как убрать условные конструкции в контроллерах
	// TODO: добавить профили для конфигурации Spring
	// TODO: интеграционные тесты и юнит тесты для контроллеров
	// TODO: выделить общий интерфейс для нотификации, реализацией будет нотификация по емейлу и вебсокетам
    // TODO: нужна валидация для dto?
}

