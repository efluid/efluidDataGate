package fr.uem.efluid.model.entities;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.uem.efluid.model.shared.ExportAwareTableLink;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Entity
@Table(name = "link")
public class TableLink extends ExportAwareTableLink<DictionaryEntry> {

	@Id
	private UUID uuid;

	private String columnFrom;

	@NotNull
	private String tableTo;

	private String columnTo;

	private String name;

	private String ext1ColumnTo;

	private String ext2ColumnTo;

	private String ext3ColumnTo;

	private String ext4ColumnTo;

	private String ext1ColumnFrom;

	private String ext2ColumnFrom;

	private String ext3ColumnFrom;

	private String ext4ColumnFrom;

	@NotNull
	private LocalDateTime createdTime;

	@NotNull
	private LocalDateTime updatedTime;

	private LocalDateTime importedTime;

	@ManyToOne(optional = false)
	private DictionaryEntry dictionaryEntry;

	/**
	 * @param uuid
	 */
	public TableLink(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	/**
	 * 
	 */
	public TableLink() {
		super();
	}

	/**
	 * @return the uuid
	 */
	@Override
	public UUID getUuid() {
		return this.uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the columnFrom
	 */
	@Override
	public String getColumnFrom() {
		return this.columnFrom;
	}

	/**
	 * @param columnFrom
	 *            the columnFrom to set
	 */
	public void setColumnFrom(String columnFrom) {
		this.columnFrom = columnFrom;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the tableTo
	 */
	@Override
	public String getTableTo() {
		return this.tableTo;
	}

	/**
	 * @param tableTo
	 *            the tableTo to set
	 */
	public void setTableTo(String tableTo) {
		this.tableTo = tableTo;
	}

	/**
	 * @return the ext1ColumnTo
	 */
	@Override
	public String getExt1ColumnTo() {
		return this.ext1ColumnTo;
	}

	/**
	 * @param ext1ColumnTo
	 *            the ext1ColumnTo to set
	 */
	public void setExt1ColumnTo(String ext1ColumnTo) {
		this.ext1ColumnTo = ext1ColumnTo;
	}

	/**
	 * @return the ext2ColumnTo
	 */
	@Override
	public String getExt2ColumnTo() {
		return this.ext2ColumnTo;
	}

	/**
	 * @param ext2ColumnTo
	 *            the ext2ColumnTo to set
	 */
	public void setExt2ColumnTo(String ext2ColumnTo) {
		this.ext2ColumnTo = ext2ColumnTo;
	}

	/**
	 * @return the ext3ColumnTo
	 */
	@Override
	public String getExt3ColumnTo() {
		return this.ext3ColumnTo;
	}

	/**
	 * @param ext3ColumnTo
	 *            the ext3ColumnTo to set
	 */
	public void setExt3ColumnTo(String ext3ColumnTo) {
		this.ext3ColumnTo = ext3ColumnTo;
	}

	/**
	 * @return the ext4ColumnTo
	 */
	@Override
	public String getExt4ColumnTo() {
		return this.ext4ColumnTo;
	}

	/**
	 * @param ext4ColumnTo
	 *            the ext4ColumnTo to set
	 */
	public void setExt4ColumnTo(String ext4ColumnTo) {
		this.ext4ColumnTo = ext4ColumnTo;
	}

	/**
	 * @return the ext1ColumnFrom
	 */
	@Override
	public String getExt1ColumnFrom() {
		return this.ext1ColumnFrom;
	}

	/**
	 * @param ext1ColumnFrom
	 *            the ext1ColumnFrom to set
	 */
	public void setExt1ColumnFrom(String ext1ColumnFrom) {
		this.ext1ColumnFrom = ext1ColumnFrom;
	}

	/**
	 * @return the ext2ColumnFrom
	 */
	@Override
	public String getExt2ColumnFrom() {
		return this.ext2ColumnFrom;
	}

	/**
	 * @param ext2ColumnFrom
	 *            the ext2ColumnFrom to set
	 */
	public void setExt2ColumnFrom(String ext2ColumnFrom) {
		this.ext2ColumnFrom = ext2ColumnFrom;
	}

	/**
	 * @return the ext3ColumnFrom
	 */
	@Override
	public String getExt3ColumnFrom() {
		return this.ext3ColumnFrom;
	}

	/**
	 * @param ext3ColumnFrom
	 *            the ext3ColumnFrom to set
	 */
	public void setExt3ColumnFrom(String ext3ColumnFrom) {
		this.ext3ColumnFrom = ext3ColumnFrom;
	}

	/**
	 * @return the ext4ColumnFrom
	 */
	@Override
	public String getExt4ColumnFrom() {
		return this.ext4ColumnFrom;
	}

	/**
	 * @param ext4ColumnFrom
	 *            the ext4ColumnFrom to set
	 */
	public void setExt4ColumnFrom(String ext4ColumnFrom) {
		this.ext4ColumnFrom = ext4ColumnFrom;
	}

	/**
	 * @return the columnTo
	 */
	@Override
	public String getColumnTo() {
		return this.columnTo;
	}

	/**
	 * @param columnTo
	 *            the columnTo to set
	 */
	public void setColumnTo(String columnTo) {
		this.columnTo = columnTo;
	}

	/**
	 * @return the createdTime
	 */
	@Override
	public LocalDateTime getCreatedTime() {
		return this.createdTime;
	}

	/**
	 * @param createdTime
	 *            the createdTime to set
	 */
	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	/**
	 * @return the importedTime
	 */
	@Override
	public LocalDateTime getImportedTime() {
		return this.importedTime;
	}

	/**
	 * @param importedTime
	 *            the importedTime to set
	 */
	public void setImportedTime(LocalDateTime importedTime) {
		this.importedTime = importedTime;
	}

	/**
	 * @return the updatedTime
	 */
	@Override
	public LocalDateTime getUpdatedTime() {
		return this.updatedTime;
	}

	/**
	 * @param updatedTime
	 *            the updatedTime to set
	 */
	public void setUpdatedTime(LocalDateTime updatedTime) {
		this.updatedTime = updatedTime;
	}

	/**
	 * @return the dictionaryEntry
	 */
	@Override
	public DictionaryEntry getDictionaryEntry() {
		return this.dictionaryEntry;
	}

	/**
	 * @param dictionaryEntry
	 *            the dictionaryEntry to set
	 */
	public void setDictionaryEntry(DictionaryEntry dictionaryEntry) {
		this.dictionaryEntry = dictionaryEntry;
	}

	/**
	 * @return the columnFrom for indexed position
	 */
	public String getColumnFrom(int index) {

		switch (index) {
		case 0:
			return getColumnFrom();

		case 1:
			return getExt1ColumnFrom();

		case 2:
			return getExt2ColumnFrom();

		case 3:
			return getExt3ColumnFrom();

		case 4:
		default:
			return getExt4ColumnFrom();
		}
	}

	/**
	 * @return the columnFrom for indexed position
	 */
	public void setColumnFrom(int index, String col) {

		switch (index) {
		case 0:
			setColumnFrom(col);
			break;
		case 1:
			setExt1ColumnFrom(col);
			break;
		case 2:
			setExt2ColumnFrom(col);
			break;
		case 3:
			setExt3ColumnFrom(col);
			break;
		case 4:
		default:
			setExt4ColumnFrom(col);
			break;
		}
	}

	/**
	 * @return the columnTo for indexed position
	 */
	public void setColumnTo(int index, String col) {

		switch (index) {
		case 0:
			setColumnTo(col);
			break;
		case 1:
			setExt1ColumnTo(col);
			break;
		case 2:
			setExt2ColumnTo(col);
			break;
		case 3:
			setExt3ColumnTo(col);
			break;
		case 4:
		default:
			setExt4ColumnTo(col);
			break;
		}
	}

	/**
	 * @return the columnTo for indexed position
	 */
	public String getColumnTo(int index) {

		switch (index) {
		case 0:
			return getColumnTo();

		case 1:
			return getExt1ColumnTo();

		case 2:
			return getExt2ColumnTo();

		case 3:
			return getExt3ColumnTo();

		case 4:
		default:
			return getExt4ColumnTo();
		}
	}

	/**
	 * @return
	 */
	public Stream<String> columnFroms() {

		// For composite, use advanced building from iterator
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new ColumnFromIterator(this), 0), false);
	}

	/**
	 * @return
	 */
	public Stream<String> columnTos() {

		// For composite, use advanced building from iterator
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new ColumnToIterator(this), 0), false);
	}

	/**
	 * @return
	 */
	public boolean isCompositeKey() {
		return getExt1ColumnFrom() != null || getExt1ColumnTo() != null;
	}

	/**
	 * @param raw
	 * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
	 */
	@Override
	public void deserialize(String raw) {

		SharedOutputInputUtils.fromJson(raw)
				.applyUUID("uid", v -> setUuid(v))
				.applyLdt("cre", v -> setCreatedTime(v))
				.applyLdt("upd", v -> setUpdatedTime(v))
				.applyString("nam", v -> setName(v))
				.applyString("cfr", v -> setColumnFrom(v))
				.applyString("cto", v -> setColumnTo(v))
				.applyString("tto", v -> setTableTo(v))
				.applyString("cf1", v -> setExt1ColumnFrom(v))
				.applyString("cf2", v -> setExt2ColumnFrom(v))
				.applyString("cf3", v -> setExt3ColumnFrom(v))
				.applyString("cf4", v -> setExt4ColumnFrom(v))
				.applyString("ct1", v -> setExt1ColumnTo(v))
				.applyString("ct2", v -> setExt2ColumnTo(v))
				.applyString("ct3", v -> setExt3ColumnTo(v))
				.applyString("ct4", v -> setExt4ColumnTo(v))
				.applyUUID("dic", v -> setDictionaryEntry(new DictionaryEntry(v)));
	}

	/**
	 * <p>
	 * For easy use of composite key model
	 * </p>
	 * 
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	private static final class ColumnFromIterator implements Iterator<String> {

		private int max = 0;
		private int pos = 0;

		private final TableLink lin;

		/**
		 * @param dic
		 */
		public ColumnFromIterator(TableLink lin) {
			super();
			this.lin = lin;

			// Standard key - not composite
			if (!lin.isCompositeKey()) {
				this.max = 1;
			}

			// Composite, search for key defs
			else {
				if (lin.getExt4ColumnFrom() != null) {
					this.max = 5;
				} else if (lin.getExt3ColumnFrom() != null) {
					this.max = 4;
				} else if (lin.getExt2ColumnFrom() != null) {
					this.max = 3;
				} else {
					this.max = 2;
				}
			}
		}

		/**
		 * @return
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return this.pos < this.max;
		}

		/**
		 * @return
		 * @see java.util.Iterator#next()
		 */
		@Override
		public String next() {

			switch (this.pos) {
			case 0:
				this.pos++;
				return this.lin.getColumnFrom();
			case 1:
				this.pos++;
				return this.lin.getExt1ColumnFrom();
			case 2:
				this.pos++;
				return this.lin.getExt2ColumnFrom();
			case 3:
				this.pos++;
				return this.lin.getExt3ColumnFrom();
			case 4:
			default:
				this.pos++;
				return this.lin.getExt4ColumnFrom();
			}
		}

	}

	/**
	 * <p>
	 * For easy use of composite key model
	 * </p>
	 * 
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	private static final class ColumnToIterator implements Iterator<String> {

		private int max = 0;
		private int pos = 0;

		private final TableLink lin;

		/**
		 * @param dic
		 */
		public ColumnToIterator(TableLink lin) {
			super();
			this.lin = lin;

			// Standard key - not composite
			if (!lin.isCompositeKey()) {
				this.max = 1;
			}

			// Composite, search for key defs
			else {
				if (lin.getExt4ColumnTo() != null) {
					this.max = 5;
				} else if (lin.getExt3ColumnTo() != null) {
					this.max = 4;
				} else if (lin.getExt2ColumnTo() != null) {
					this.max = 3;
				} else {
					this.max = 2;
				}
			}
		}

		/**
		 * @return
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return this.pos < this.max;
		}

		/**
		 * @return
		 * @see java.util.Iterator#next()
		 */
		@Override
		public String next() {

			switch (this.pos) {
			case 0:
				this.pos++;
				return this.lin.getColumnTo();
			case 1:
				this.pos++;
				return this.lin.getExt1ColumnTo();
			case 2:
				this.pos++;
				return this.lin.getExt2ColumnTo();
			case 3:
				this.pos++;
				return this.lin.getExt3ColumnTo();
			case 4:
			default:
				this.pos++;
				return this.lin.getExt4ColumnTo();
			}
		}

	}

}
