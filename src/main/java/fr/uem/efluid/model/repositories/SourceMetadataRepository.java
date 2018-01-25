package fr.uem.efluid.model.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Repository // TM is NOT standard one
public class SourceMetadataRepository {

	@SuppressWarnings("unused")
	@Autowired(required = false)
	private JdbcTemplate jdbc;

	// Here access to metadata on source BDD
}
