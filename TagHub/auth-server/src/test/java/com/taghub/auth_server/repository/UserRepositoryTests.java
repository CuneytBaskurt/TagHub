package com.taghub.auth_server.repository;

import com.taghub.auth_server.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class UserRepositoryTests {

	@Autowired
	private UserRepository userRepository;

	@Test
	public void UserRepository_SaveAll_ReturnSavedUser(){

		User user = User.builder()
				.email("test@gmail.com")
				.password("test_password")
				.createdAt(LocalDateTime.now())
				.username("test_user").build();

		User savedUser = userRepository.save(user);

		Assertions.assertNotNull(savedUser);
		Assertions.assertEquals("test@gmail.com",savedUser.getEmail());
	}

	@Test
	public void UserRepository_GetAll_ReturnMoreThanOneUser(){

		User user = User.builder()
				.username("User1")
				.email("User1@gmail.com")
				.createdAt(LocalDateTime.now())
				.password("UserPassword1")
				.build();

		User user2 = User.builder()
				.username("User2")
				.email("User2@gmail.com")
				.createdAt(LocalDateTime.now())
				.password("UserPassword2")
				.build();

		userRepository.save(user);
		userRepository.save(user2);

		List<User> userList = userRepository.findAll();
		Assertions.assertNotNull(userList);
		Assertions.assertEquals(2, userList.size());

	}

	@Test
	public void UserRepository_findById_ReturnUser(){

		User user = User.builder()
				.username("User1")
				.email("User1@gmail.com")
				.createdAt(LocalDateTime.now())
				.password("UserPassword1")
				.build();

		userRepository.save(user);

		Optional<User> userList = userRepository.findById(user.getId());

		Assertions.assertNotNull(userList);

	}

	@Test
	public void UserRepository_updateUser_ReturnUserNotNull(){

		User user = User.builder()
				.username("User1")
				.email("User1@gmail.com")
				.createdAt(LocalDateTime.now())
				.password("UserPassword1")
				.build();

		userRepository.save(user);

		User userSave = userRepository.findById(user.getId()).get();
		userSave.setEmail("User2@gmail.com");
		userSave.setUsername("User2");

		User updatedUser = userRepository.save(userSave);

		Assertions.assertNotNull(updatedUser.getUsername());
		Assertions.assertNotNull(updatedUser.getEmail());
	}

	@Test
	public void UserRepository_deleteUser_ReturnUserIsEmpty(){

		User user = User.builder()
				.username("User1")
				.email("User1@gmail.com")
				.createdAt(LocalDateTime.now())
				.password("UserPassword1")
				.build();

		User savedUser = userRepository.save(user);

		userRepository.deleteById(savedUser.getId());

		Optional<User> userReturn = userRepository.findById(savedUser.getId());

		Assertions.assertTrue(userReturn.isEmpty(), "Optional must be Null!");

	}

}
