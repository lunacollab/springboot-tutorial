package com.example.demo.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.request.RoleRequest;
import com.example.demo.dto.response.RoleResponse;
import com.example.demo.entity.Permission;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    public RoleResponse create(RoleRequest request) {
        if (isAdminRole(request)) {
            if (request.getPermissions() == null || request.getPermissions().isEmpty()) {
                request.setPermissions(getAdminPermissions());
            }
        } else {
            if (request.getPermissions() == null || request.getPermissions().isEmpty()) {
                request.setPermissions(getNonAdminPermissions());
            } else {
                request.setPermissions(request.getPermissions().stream()
                        .filter(permission -> !permission.equals("APPROVE_POST") && !permission.equals("REJECT_POST"))
                        .collect(Collectors.toSet()));
            }
        }
        var role = roleMapper.toRole(request);
        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    private boolean isAdminRole(RoleRequest request) {
        return "ADMIN".equalsIgnoreCase(request.getName());
    }

    private Set<String> getAdminPermissions() {
        List<String> allPermissionNames = permissionRepository.findAll().stream()
                .map(Permission::getName)
                .filter(permissionName -> !"FIXED_POST".equals(permissionName))
                .collect(Collectors.toList());

        allPermissionNames.add("APPROVE_POST");
        allPermissionNames.add("CREATE_POST");
        allPermissionNames.add("REJECT_POST");

        return new HashSet<>(allPermissionNames);
    }

    private Set<String> getNonAdminPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add("CREATE_POST");
        permissions.add("FIXED_POST");
        permissions.addAll(getRandomPermissions(2));
        return permissions;
    }

    public Set<String> getRandomPermissions(int maxSize) {
        List<String> allPermissionNames =
                permissionRepository.findAll().stream().map(Permission::getName).collect(Collectors.toList());
        Collections.shuffle(allPermissionNames);
        return allPermissionNames.stream().limit(maxSize).collect(Collectors.toSet());
    }

    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    public void delete(String role) {
        roleRepository.deleteById(role);
    }
}
