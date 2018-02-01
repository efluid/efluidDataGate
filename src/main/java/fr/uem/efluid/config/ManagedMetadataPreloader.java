package fr.uem.efluid.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import fr.uem.efluid.model.repositories.DatabaseDescriptionRepository;

/**
 * Force-load the metadata for Managed database
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Configuration
@Order(100)
public class ManagedMetadataPreloader {

	@Autowired
	private DatabaseDescriptionRepository metas;
	
	@PostConstruct
	public void preload(){
		this.metas.getTables();
	}
}
