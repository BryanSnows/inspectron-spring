package com.inspectron.inspectron.api.domain.user.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPageResponse {
    private final List<UserResponse> result;
    private final long total;
}
