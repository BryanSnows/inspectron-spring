package com.inspectron.inspectron.domain.user.entity;

import com.inspectron.inspectron.domain.user.enums.UserRole;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class UserSpecifications {

    private UserSpecifications() {}

    public static Specification<User> search(String search) {
        if (!StringUtils.hasText(search)) {
            return null;
        }
        final String like = "%" + search.toLowerCase() + "%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), like),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), like),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("enrollment")), like));
    }

    public static Specification<User> hasRole(UserRole role) {
        if (role == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("role"), role);
    }

    public static Specification<User> disabled(Boolean disabled) {
        if (disabled == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                Boolean.TRUE.equals(disabled)
                        ? criteriaBuilder.isTrue(root.get("disabled"))
                        : criteriaBuilder.isFalse(root.get("disabled"));
    }
}
