package fr.uem.efluid.services.types;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import fr.uem.efluid.model.entities.FunctionalDomain;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class DomainDiffDisplay<T extends DiffDisplay<?>> implements Comparable<DomainDiffDisplay<?>> {

	private UUID domainUuid;

	private String domainName;

	private List<T> preparedContent;

	/**
	 * @return the preparedContent
	 */
	public List<T> getPreparedContent() {
		return this.preparedContent;
	}

	/**
	 * @param preparedContent
	 *            the preparedContent to set
	 */
	public void setPreparedContent(List<T> preparedContent) {
		this.preparedContent = preparedContent;
	}

	/**
	 * @return the domainUuid
	 */
	public UUID getDomainUuid() {
		return this.domainUuid;
	}

	/**
	 * @param domainUuid
	 *            the domainUuid to set
	 */
	public void setDomainUuid(UUID domainUuid) {
		this.domainUuid = domainUuid;
	}

	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return this.domainName;
	}

	/**
	 * @param domainName
	 *            the domainName to set
	 */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	/**
	 * @return
	 */
	public boolean isEmptyDiff() {
		return this.preparedContent.stream().allMatch(d -> d.getDiff().isEmpty());
	}

	/**
	 * @return
	 */
	public long getTotalCount() {
		return this.preparedContent != null
				? this.preparedContent.stream().flatMap(d -> d.getDiff() != null ? d.getDiff().stream() : Stream.of()).count()
				: 0;
	}

	/**
	 * Can be partially completed (uuid only) : get other dict entry properties from its
	 * entity
	 * 
	 * @param entity
	 */
	public void completeFromEntity(FunctionalDomain entity) {

		if (entity != null) {
			setDomainName(entity.getName());
			setDomainUuid(entity.getUuid());
		}
	}

	/**
	 * @param o
	 * @return
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DomainDiffDisplay<?> o) {

		return this.getDomainName().compareTo(o.getDomainName());
	}

}
