package com.onlinemarket.dto;

import lombok.Data;

@Data
public class Pagination {

	private int pageNumber;
	private int pageSize;
	private int pages;
	private String sortBy;
	private String orderBy;
	private String searchBy;
}
