package fr.uem.efluid.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.model.entities.User;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface UserRepository extends JpaRepository<User, String> {

	/**
	 * @param login
	 * @return
	 */
	User findByLogin(String login);

	/**
	 * @param token
	 * @return
	 */
	User findByToken(String token);
}
