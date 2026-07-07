package com.taghub.tag_server.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class VoteRequest {
	private UUID textId;
	private Integer labelIndex;
}