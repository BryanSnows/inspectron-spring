package com.inspectron.inspectron.api.domain.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRequest {

    @Pattern(
            regexp = "^(?!.*[~!#$%^&*()|+=?;:<>{}\\[\\\\\\]])",
            message = "No special characters")
    private String search;
}
