package org.ftclub.cabinet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.ftclub.cabinet.user.domain.LentExtensionType;

@Getter
@AllArgsConstructor
@ToString
public class LentExtensionResponseDto {

	private long lentExtensionId;
	private String name;
	private int extensionPeriod;
	private String expiredAt;
	private LentExtensionType lentExtensionType;
}