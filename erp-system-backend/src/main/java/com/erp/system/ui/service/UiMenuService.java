package com.erp.system.ui.service;

import com.erp.system.ui.domain.UiMenuItem;
import com.erp.system.ui.dto.MenuNodeDto;
import com.erp.system.ui.repository.UiMenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UiMenuService {

    private final UiMenuItemRepository uiMenuItemRepository;

    @Transactional(readOnly = true)
    public List<MenuNodeDto> getMenuForUser(Authentication authentication) {
        Set<String> userRoles = extractRoles(authentication);
        List<UiMenuItem> flat = uiMenuItemRepository.findAllByOrderByParentIdAscSortOrderAsc();
        Set<String> visibleIds = new HashSet<>();
        for (UiMenuItem row : flat) {
            if (canSee(row, userRoles)) {
                visibleIds.add(row.getId());
            }
        }
        Map<String, List<UiMenuItem>> byParent = new HashMap<>();
        for (UiMenuItem row : flat) {
            if (!visibleIds.contains(row.getId())) {
                continue;
            }
            String p = row.getParentId() == null ? "" : row.getParentId();
            byParent.computeIfAbsent(p, k -> new ArrayList<>()).add(row);
        }
        for (List<UiMenuItem> list : byParent.values()) {
            list.sort(Comparator.comparing(UiMenuItem::getSortOrder, Comparator.nullsLast(Integer::compareTo)));
        }
        List<UiMenuItem> roots = new ArrayList<>(byParent.getOrDefault("", List.of()));
        roots.sort(Comparator.comparing(UiMenuItem::getSortOrder, Comparator.nullsLast(Integer::compareTo)));
        List<MenuNodeDto> out = new ArrayList<>();
        for (UiMenuItem root : roots) {
            out.add(toDto(root, byParent, visibleIds));
        }
        return out.stream()
                .filter(node -> node.getType() == null || !"group".equals(node.getType()) || (node.getChildren() != null && !node.getChildren().isEmpty()))
                .collect(Collectors.toList());
    }

    private MenuNodeDto toDto(UiMenuItem row, Map<String, List<UiMenuItem>> byParent, Set<String> visibleIds) {
        List<UiMenuItem> kids = byParent.getOrDefault(row.getId(), List.of());
        List<MenuNodeDto> childDtos = new ArrayList<>();
        for (UiMenuItem c : kids) {
            if (visibleIds.contains(c.getId())) {
                childDtos.add(toDto(c, byParent, visibleIds));
            }
        }
        return MenuNodeDto.builder()
                .id(row.getId())
                .title(row.getTitleKey())
                .type(row.getItemType())
                .icon(row.getIcon())
                .url(row.getUrl())
                .external(Boolean.TRUE.equals(row.getExternal()))
                .target(Boolean.TRUE.equals(row.getTargetBlank()))
                .classes(row.getItemClasses())
                .breadcrumbs(row.getBreadcrumbsFlag())
                .children(childDtos.isEmpty() ? null : childDtos)
                .build();
    }

    private static Set<String> extractRoles(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .collect(Collectors.toSet());
    }

    private static boolean canSee(UiMenuItem row, Set<String> userRoles) {
        String csv = row.getRolesCsv();
        if (csv == null || csv.isBlank()) {
            return true;
        }
        List<String> required = Arrays.stream(csv.split(","))
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .toList();
        if (required.isEmpty()) {
            return true;
        }
        Set<String> upperUser = userRoles.stream()
                .map(r -> r.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return required.stream().anyMatch(upperUser::contains);
    }
}
