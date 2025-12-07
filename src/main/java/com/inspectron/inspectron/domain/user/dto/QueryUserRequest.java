package com.inspectron.inspectron.domain.user.dto;

import com.inspectron.inspectron.domain.user.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryUserRequest extends SearchRequest {

    private UserRole role;

    private Boolean disabled;
}
