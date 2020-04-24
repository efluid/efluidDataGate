package fr.uem.efluid.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.model.entities.User;

import java.util.Optional;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface UserRepository extends JpaRepository<User, String> {

	Optional<User> findByLogin(String login);

	Optional<User>  findByToken(String token);
}
