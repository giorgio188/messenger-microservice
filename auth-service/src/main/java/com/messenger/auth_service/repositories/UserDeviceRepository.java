package com.messenger.auth_service.repositories;

import com.messenger.auth_service.models.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Integer> {

    List<UserDevice> findByUserId(int userId);

    Optional<UserDevice> findByUserIdAndDeviceDetails(int userId, String deviceDetails);

    List<UserDevice> findByLastLoginDateBefore(Date date);

}
