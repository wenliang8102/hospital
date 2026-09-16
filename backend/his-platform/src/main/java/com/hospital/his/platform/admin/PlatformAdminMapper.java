package com.hospital.his.platform.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PlatformAdminMapper {
    List<RoleView> findRoles();

    long countUsers(@Param("keyword") String keyword, @Param("enabled") Boolean enabled);

    List<UserAccountRow> findUsers(
            @Param("keyword") String keyword,
            @Param("enabled") Boolean enabled,
            @Param("offset") long offset,
            @Param("limit") int limit);

    Optional<UserAccountRow> findUserById(Long id);

    int insertUser(UserAccountDraft draft);

    int updateUser(
            @Param("id") Long id,
            @Param("displayName") String displayName,
            @Param("employeeId") Long employeeId,
            @Param("enabled") boolean enabled);

    int updateEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);

    List<Long> findRoleIdsByCodes(@Param("codes") List<String> codes);

    List<RoleAssignmentRow> findRolesByUserIds(@Param("userIds") List<Long> userIds);

    int deleteUserRoles(Long userId);

    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
