package com.inspectron.inspectron.api.domain.user.dto;

import com.inspectron.inspectron.api.domain.user.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryUserRequest extends SearchRequest {

    private UserRole role;

    private Boolean disabled;
}
