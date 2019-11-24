package com.photoapp.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

//This is created for study purposes
@ExtendWith(SpringExtension.class)
@SpringBootTest
class UtilsTest {

	@Autowired
	Utils utils;

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testGenerateUserId() {
		String userid = utils.generateUserId(30);
		String userid2 = utils.generateUserId(30);

		assertNotNull(userid);
		assertNotNull(userid2);

		assertTrue(userid.length() == 30);
		assertTrue(!userid.equalsIgnoreCase(userid2));
	}

	@Test	
	void testHasTokenNotExpired() {
		String token = utils.generateEmailVerificationToken("478zxncmafa");
		assertNotNull(token);

		boolean hasTokenExpired = Utils.hasTokenExpired(token);
		assertFalse(hasTokenExpired);
	}

	@Test
	void testHasTokenExpired() {
		String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI0Nzh6eG5jbWFmYSIsIm"
				+ "V4cCI6MTU3MzYwMDI3M30.xiyAhuefshcDVBIwGQR7qLL86eVGgUR0mAss3ycaE0jY"
				+ "yHPg7_mpwMyen1hBqCFzr6EME9olJePthuxH_4Q1kg";
		
		assertNotNull(expiredToken);

		boolean hasTokenExpired = Utils.hasTokenExpired(expiredToken);
		assertTrue(hasTokenExpired);
	}

}
