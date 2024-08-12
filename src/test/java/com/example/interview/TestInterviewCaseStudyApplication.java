package com.example.interview;

import org.springframework.boot.SpringApplication;

public class TestInterviewCaseStudyApplication {

	public static void main(String[] args) {
		SpringApplication.from(InterviewCaseStudyApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
