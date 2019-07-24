package com.inspur.bean;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "bde_nifi_task")
public class Organ implements Serializable {
	@Id
	@Column(name = "id")
	private String id;
	@Column(name = "name")
	private String name;
	@Column(name = "parent_id")
	private String parentId;
	@Column(name = "is_parent")
	private String isParent;
	@Column(name = "user_id")
	private String userId;
	@Column(name = "status")
	private Integer status;
	@Column(name = "create_time")
	private String createTime;
	@Transient
	private String font;

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getFont() {
		return font;
	}

	public void setFont(String font) {
		this.font = font;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	// 设置属性的getter、setter
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getIsParent() {
		return isParent;
	}

	public void setIsParent(String isParent) {
		this.isParent = isParent;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}
