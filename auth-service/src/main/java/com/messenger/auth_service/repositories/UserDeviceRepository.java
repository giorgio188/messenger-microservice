package com.messenger.auth_service.repositories;

import com.messenger.auth_service.models.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Integer> {

    @Query("SELECT d FROM UserDevice d WHERE d.userId = :userId")
    List<UserDevice> findByUserId(@Param("userId") int userId);

    @Query("SELECT d FROM UserDevice d WHERE d.userId = :userId AND d.deviceDetails = :deviceDetails")
    Optional<UserDevice> findByUserIdAndDeviceDetails(
            @Param("userId") int userId,
            @Param("deviceDetails") String deviceDetails);

    @Query("SELECT d FROM UserDevice d WHERE d.lastLogin < :date")
    List<UserDevice> findByLastLoginDateBefore(@Param("date") Date date);


}
