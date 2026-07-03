package com.reservation.web.service;

import com.reservation.web.entity.MileageSettingEntity;
import com.reservation.web.entity.UserEntity;
import com.reservation.web.repository.MileageSettingRepository;
import com.reservation.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MileageService {

    private static final long SINGLE_SETTING_ID = 1L;

    private final MileageSettingRepository mileageSettingRepository;
    private final UserRepository userRepository;

    @Transactional
    public MileageSettingEntity getSetting() {
        return mileageSettingRepository.findById(SINGLE_SETTING_ID)
                .orElseGet(() -> {
                    MileageSettingEntity setting = new MileageSettingEntity();
                    setting.setId(SINGLE_SETTING_ID);
                    setting.setEarningRatePercent(0.0);
                    return mileageSettingRepository.save(setting);
                });
    }

    @Transactional
    public MileageSettingEntity updateRate(Double earningRatePercent) {
        MileageSettingEntity setting = getSetting();
        double rate = earningRatePercent == null ? 0.0 : Math.max(0.0, Math.min(100.0, earningRatePercent));
        setting.setEarningRatePercent(rate);
        return mileageSettingRepository.save(setting);
    }

    @Transactional
    public int earnMileage(String userId, int baseAmount) {
        if (userId == null || userId.isBlank() || baseAmount <= 0) {
            return 0;
        }

        MileageSettingEntity setting = getSetting();
        double rate = setting.getEarningRatePercent() == null ? 0.0 : setting.getEarningRatePercent();
        int earned = (int) Math.round(baseAmount * rate / 100.0);
        if (earned <= 0) {
            return 0;
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("마일리지를 적립할 회원을 찾을 수 없습니다: " + userId));
        int currentMileage = user.getMileageBalance() == null ? 0 : user.getMileageBalance();
        user.setMileageBalance(currentMileage + earned);
        userRepository.save(user);
        return earned;
    }
}
