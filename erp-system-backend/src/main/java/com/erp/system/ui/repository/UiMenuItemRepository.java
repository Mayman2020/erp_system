package com.erp.system.ui.repository;

import com.erp.system.ui.domain.UiMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UiMenuItemRepository extends JpaRepository<UiMenuItem, String> {

    List<UiMenuItem> findAllByOrderByParentIdAscSortOrderAsc();
}
