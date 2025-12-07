package com.inspectron.inspectron.service;

import com.inspectron.inspectron.domain.auth.dto.CredentialsRequest;
import com.inspectron.inspectron.domain.user.dto.CreateUserRequest;
import com.inspectron.inspectron.domain.user.dto.PaginationRequest;
import com.inspectron.inspectron.domain.user.dto.QueryUserRequest;
import com.inspectron.inspectron.domain.user.dto.UpdatePasswordRequest;
import com.inspectron.inspectron.domain.user.dto.UpdateUserRequest;
import com.inspectron.inspectron.domain.user.dto.UserPageResponse;
import com.inspectron.inspectron.domain.user.dto.UserResponse;
import com.inspectron.inspectron.domain.user.entity.User;
import com.inspectron.inspectron.domain.user.entity.UserSpecifications;
import com.inspectron.inspectron.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String FIRST_ADMIN_ENROLLMENT = "000001";
    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("name");

    private final UserRepository userRepository;

    @Transactional
    public Optional<User> checkCredentials(CredentialsRequest credentials) {
        return userRepository
                .findByEnrollmentAndDisabledFalse(credentials.getEnrollment())
                .filter(user -> user.matchesPassword(credentials.getPassword()))
                .map(user -> {
                    boolean isFirstAccess = credentials.getPassword().equals(user.getEnrollment());
                    user.setFirstAccess(isFirstAccess);
                    return userRepository.save(user);
                });
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        validateCreate(request);

        User user = User.builder()
                .name(request.getName())
                .email(StringUtils.hasText(request.getEmail()) ? request.getEmail() : null)
                .enrollment(request.getEnrollment())
                .role(request.getRole())
                .disabled(false)
                .firstAccess(true)
                .tutorial(true)
                .build();
        user.encodePassword(request.getEnrollment());

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(UpdateUserRequest request) {
        User user = userRepository
                .findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid-id"));

        validateUpdate(request, user.getId(), user.getEnrollment());

        user.setName(request.getName());
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail() : null);

        if (!FIRST_ADMIN_ENROLLMENT.equals(user.getEnrollment())) {
            user.setEnrollment(request.getEnrollment());
            user.setRole(request.getRole());
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateLoggedUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user-not-found"));

        user.setName(request.getName());
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail() : null);
        user.setEnrollment(request.getEnrollment());

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void updateUserPassword(UUID userId, String newPassword) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid-id"));

        user.encodePassword(newPassword);
        userRepository.save(user);
    }

    @Transactional
    public void updateLoggedUserPassword(UUID userId, UpdatePasswordRequest request) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user-not-found"));

        if (request.getNewPassword().equals(user.getEnrollment())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "password-cannot-be-enrollment");
        }

        if (!user.matchesPassword(request.getCurrentPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid-currentPassword");
        }

        user.encodePassword(request.getNewPassword());
        user.setFirstAccess(false);
        userRepository.save(user);
    }

    @Transactional
    public UserResponse toggleDisable(UUID userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user-not-found"));

        if (!FIRST_ADMIN_ENROLLMENT.equals(user.getEnrollment())) {
            user.setDisabled(!user.isDisabled());
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse disableTutorial(UUID userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user-not-found"));

        user.setTutorial(false);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user-not-found"));

        if (!FIRST_ADMIN_ENROLLMENT.equals(user.getEnrollment())) {
            userRepository.delete(user);
        }
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return userRepository
                .findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user-not-found"));
    }

    @Transactional(readOnly = true)
    public Optional<User> findEntityById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public UserPageResponse findAll(QueryUserRequest filter, PaginationRequest pagination) {
        QueryUserRequest safeFilter = filter != null ? filter : new QueryUserRequest();
        PaginationRequest safePagination = pagination != null ? pagination : new PaginationRequest();

        Pageable pageable = buildPageable(safePagination);

        Specification<User> specification = Specification.where(UserSpecifications.search(safeFilter.getSearch()))
            .and(UserSpecifications.hasRole(safeFilter.getRole()))
            .and(UserSpecifications.disabled(safeFilter.getDisabled()));

        Page<User> page = userRepository.findAll(specification, pageable);
        List<UserResponse> result = page.getContent().stream().map(UserResponse::from).toList();
        return new UserPageResponse(result, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public UserResponse findLoggedUser(UUID userId) {
        return userRepository
                .findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "user-not-found"));
    }

    private void validateCreate(CreateUserRequest request) {
        if (userRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name-exists");
        }

        if (userRepository.existsByEnrollment(request.getEnrollment())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "enrollment-exists");
        }

        if (StringUtils.hasText(request.getEmail())
                && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email-exists");
        }
    }

    private void validateUpdate(UpdateUserRequest request, UUID userId, String currentEnrollment) {
        if (userRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name-exists");
        }

        if (StringUtils.hasText(request.getEmail())
                && userRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email-exists");
        }

        if (!FIRST_ADMIN_ENROLLMENT.equals(currentEnrollment)
                && userRepository.existsByEnrollmentAndIdNot(request.getEnrollment(), userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "enrollment-exists");
        }
    }

    private Pageable buildPageable(PaginationRequest pagination) {
        int page = Math.max(pagination.getPage(), 0);
        int size = Math.max(pagination.getSize(), 1);
        Sort sort = resolveSort(pagination.getSort());
        return PageRequest.of(page, size, sort);
    }

    private Sort resolveSort(String sortParam) {
        if (!StringUtils.hasText(sortParam)) {
            return Sort.by(Sort.Direction.ASC, "name");
        }

        String[] parts = sortParam.split(",");
        String column = parts[0];
        String direction = parts.length > 1 ? parts[1] : "asc";

        if (!ALLOWED_SORT_COLUMNS.contains(column)) {
            column = "name";
        }

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(sortDirection, column);
    }
}
