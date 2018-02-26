package fr.uem.efluid.utils;

import java.util.function.Supplier;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class LazyLoadedJdbcTemplate extends JdbcTemplate {

	/**
	 * 
	 */
	public LazyLoadedJdbcTemplate(Supplier<DataSource> dtSupplier) {
		// TODO Auto-generated constructor stub
	}


}
