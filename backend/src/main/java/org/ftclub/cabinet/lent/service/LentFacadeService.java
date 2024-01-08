package org.ftclub.cabinet.lent.service;

<<<<<<< HEAD
import static org.ftclub.cabinet.cabinet.domain.LentType.PRIVATE;
import static org.ftclub.cabinet.cabinet.domain.LentType.SHARE;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

=======
>>>>>>> 3f3297013328376f063d03d50f9cf7b867f0926d
import lombok.RequiredArgsConstructor;
import org.ftclub.cabinet.alarm.domain.AlarmEvent;
import org.ftclub.cabinet.alarm.domain.LentSuccessAlarm;
import org.ftclub.cabinet.cabinet.domain.Cabinet;
import org.ftclub.cabinet.cabinet.domain.CabinetStatus;
import org.ftclub.cabinet.cabinet.domain.LentType;
import org.ftclub.cabinet.cabinet.service.CabinetCommandService;
import org.ftclub.cabinet.cabinet.service.CabinetQueryService;
import org.ftclub.cabinet.dto.*;
import org.ftclub.cabinet.lent.domain.LentHistory;
import org.ftclub.cabinet.log.LogLevel;
import org.ftclub.cabinet.log.Logging;
import org.ftclub.cabinet.mapper.CabinetMapper;
import org.ftclub.cabinet.mapper.LentMapper;
import org.ftclub.cabinet.user.domain.BanHistory;
import org.ftclub.cabinet.user.domain.BanType;
import org.ftclub.cabinet.user.domain.User;
import org.ftclub.cabinet.user.domain.UserSession;
import org.ftclub.cabinet.user.service.BanHistoryCommandService;
import org.ftclub.cabinet.user.service.BanHistoryQueryService;
import org.ftclub.cabinet.user.service.BanPolicyService;
import org.ftclub.cabinet.user.service.UserQueryService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.ftclub.cabinet.cabinet.domain.LentType.PRIVATE;
import static org.ftclub.cabinet.cabinet.domain.LentType.SHARE;

@Service
@RequiredArgsConstructor
@Logging(level = LogLevel.DEBUG)
public class LentFacadeService {

    private final LentRedisService lentRedisService;
    private final LentQueryService lentQueryService;
    private final LentCommandService lentCommandService;
    private final UserQueryService userQueryService;
    private final CabinetQueryService cabinetQueryService;
    private final CabinetCommandService cabinetCommandService;
    private final BanHistoryQueryService banHistoryQueryService;
    private final BanHistoryCommandService banHistoryCommandService;

    private final LentPolicyService lentPolicyService;
    private final BanPolicyService banPolicyService;
    private final ApplicationEventPublisher eventPublisher;

    private final LentMapper lentMapper;
    private final CabinetMapper cabinetMapper;


    @Transactional(readOnly = true)
    public LentHistoryPaginationDto getMyLentLog(UserSessionDto user, Pageable pageable) {
        Page<LentHistory> lentHistories =
                lentQueryService.findUserActiveLentHistories(user.getUserId(), pageable);
        List<LentHistoryDto> result = lentHistories.stream()
                .sorted(Comparator.comparing(LentHistory::getStartedAt).reversed())
                .map(lentHistory -> lentMapper.toLentHistoryDto(
                        lentHistory,
                        lentHistory.getUser(),
                        lentHistory.getCabinet()))
                .collect(Collectors.toList());
        return lentMapper.toLentHistoryPaginationDto(result, lentHistories.getTotalElements());
    }

<<<<<<< HEAD
	@Transactional(readOnly = true)
	public MyCabinetResponseDto getMyLentInfo(@UserSession UserSessionDto user) {
		Cabinet userActiveCabinet = cabinetQueryService.findUserActiveCabinet(user.getUserId());
		Long cabinetId;
		List<LentDto> lentDtoList;
		if (Objects.isNull(userActiveCabinet)) {
			cabinetId = lentRedisService.findCabinetJoinedUser(user.getUserId());
			if (Objects.isNull(cabinetId)) {
				return null;
			}
			List<Long> usersInCabinet = lentRedisService.findUsersInCabinet(cabinetId);
			List<User> userList = userQueryService.getUsers(usersInCabinet);
			userActiveCabinet = cabinetQueryService.findCabinets(cabinetId);
			lentDtoList = userList.stream()
					.map(u -> lentMapper.toLentDto(u, null)).collect(Collectors.toList());
		} else {
			cabinetId = userActiveCabinet.getId();
			List<LentHistory> lentHistories =
					lentQueryService.findCabinetActiveLentHistories(cabinetId);
			lentDtoList = lentHistories.stream()
					.map(lh -> lentMapper.toLentDto(lh.getUser(), lh)).collect(Collectors.toList());
		}
		String shareCode = lentRedisService.getShareCode(cabinetId);
		LocalDateTime sessionExpiredAt = lentRedisService.getSessionExpired(cabinetId);
		String previousUserName = lentRedisService.getPreviousUserName(cabinetId);
		return cabinetMapper.toMyCabinetResponseDto(userActiveCabinet, lentDtoList,
				shareCode, sessionExpiredAt, previousUserName);
	}
=======
    @Transactional(readOnly = true)
    public MyCabinetResponseDto getMyLentInfo(@UserSession UserSessionDto user) {
        Cabinet userActiveCabinet = cabinetQueryService.findUserActiveCabinet(user.getUserId());
        Long cabinetId;
        List<LentDto> lentDtoList;
        if (Objects.isNull(userActiveCabinet)) {
            cabinetId = lentRedisService.findCabinetJoinedUser(user.getUserId());
            if (Objects.isNull(cabinetId)) {
                return null;
            }
            List<Long> usersInCabinet = lentRedisService.findUsersInCabinet(cabinetId);
            List<User> userList = userQueryService.getUsers(usersInCabinet);
            userActiveCabinet = cabinetQueryService.findCabinets(cabinetId);
            lentDtoList = userList.stream()
                    .map(u -> lentMapper.toLentDto(u, null)).collect(Collectors.toList());
        } else {
            cabinetId = userActiveCabinet.getCabinetId();
            List<LentHistory> lentHistories =
                    lentQueryService.findCabinetActiveLentHistories(cabinetId);
            lentDtoList = lentHistories.stream()
                    .map(lh -> lentMapper.toLentDto(lh.getUser(), lh)).collect(Collectors.toList());
        }
        String shareCode = lentRedisService.getShareCode(cabinetId);
        LocalDateTime sessionExpiredAt = lentRedisService.getSessionExpired(cabinetId);
        String previousUserName = lentRedisService.getPreviousUserName(cabinetId);
        return cabinetMapper.toMyCabinetResponseDto(userActiveCabinet, lentDtoList,
                shareCode, sessionExpiredAt, previousUserName);
    }
>>>>>>> 3f3297013328376f063d03d50f9cf7b867f0926d

    @Transactional(readOnly = true)
    public List<ActiveLentHistoryDto> getAllActiveLentHistories() {
        LocalDateTime now = LocalDateTime.now();
        List<LentHistory> lentHistories = lentQueryService.findAllActiveLentHistories();
        return lentHistories.stream()
                .map(lh -> lentMapper.toActiveLentHistoryDto(lh,
                        lh.getUser(),
                        lh.getCabinet(),
                        lh.isExpired(now),
                        lh.getDaysUntilExpiration(now)))
                .collect(Collectors.toList());
    }

    /*------------------------------------------  CUD  -------------------------------------------*/

    @Transactional
    public void startLentCabinet(Long userId, Long cabinetId) {
        LocalDateTime now = LocalDateTime.now();
        User user = userQueryService.getUser(userId);
        Cabinet cabinet = cabinetQueryService.findCabinetsWithLock(cabinetId);
        int lentCount = lentQueryService.countUserActiveLent(userId);
        List<BanHistory> banHistories = banHistoryQueryService.findActiveBanHistories(userId, now);
        int userCount = lentQueryService.countCabinetUser(cabinetId);

        lentPolicyService.verifyCabinetLentCount(
                cabinet.getLentType(), cabinet.getMaxUser(), userCount);
        lentPolicyService.verifyCabinetType(cabinet.getLentType(), PRIVATE);
        lentPolicyService.verifyUserForLent(new UserVerifyRequestDto(user.getRole(),
                user.getBlackholedAt(), lentCount, cabinetId, cabinet.getStatus(), banHistories));
        lentPolicyService.verifyCabinetForLent(cabinet.getStatus(), cabinet.getLentType());

<<<<<<< HEAD
		LocalDateTime expiredAt = lentPolicyService.generateExpirationDate(now, PRIVATE, 1);
		lentCommandService.startLent(user.getId(), cabinet.getId(), now, expiredAt);
		cabinetCommandService.changeStatus(cabinet, CabinetStatus.FULL);
		cabinetCommandService.changeUserCount(cabinet, lentCount + 1);
	}
=======
        LocalDateTime expiredAt = lentPolicyService.generateExpirationDate(now, PRIVATE, 1);
        lentCommandService.startLent(user.getUserId(), cabinet.getCabinetId(), now, expiredAt);
        cabinetCommandService.changeStatus(cabinet, CabinetStatus.FULL);
        cabinetCommandService.changeUserCount(cabinet, lentCount + 1);
        eventPublisher.publishEvent(AlarmEvent.of(userId, new LentSuccessAlarm(
                cabinet.getCabinetPlace().getLocation(), cabinet.getVisibleNum(), expiredAt)));
    }
>>>>>>> 3f3297013328376f063d03d50f9cf7b867f0926d

    @Transactional
    public void startLentShareCabinet(Long userId, Long cabinetId, String shareCode) {
        LocalDateTime now = LocalDateTime.now();
        Cabinet cabinet = cabinetQueryService.findCabinetsWithLock(cabinetId);
        int userCount = lentQueryService.countCabinetUser(cabinetId);
        lentPolicyService.verifyCabinetLentCount(
                cabinet.getLentType(), cabinet.getMaxUser(), userCount);
        lentPolicyService.verifyCabinetType(cabinet.getLentType(), SHARE);

        List<BanHistory> banHistories = banHistoryQueryService.findActiveBanHistories(userId, now);
        int lentCount = lentQueryService.countUserActiveLent(userId);
        User user = userQueryService.getUser(userId);
        lentPolicyService.verifyUserForLent(new UserVerifyRequestDto(user.getRole(),
                user.getBlackholedAt(), lentCount, cabinetId, cabinet.getStatus(), banHistories));

        Long attemptCount = lentRedisService.getAttemptCountOnShareCabinet(cabinetId, userId);
        lentPolicyService.verifyAttemptCountOnShareCabinet(attemptCount);

<<<<<<< HEAD
		boolean isExist = lentRedisService.isInCabinetSession(cabinetId);
		if (!isExist) {
			lentPolicyService.verifyCabinetForLent(cabinet.getStatus(), cabinet.getLentType());
			shareCode = lentRedisService.createCabinetSession(cabinetId);
			cabinetCommandService.changeStatus(cabinet, CabinetStatus.IN_SESSION);
		}
		lentRedisService.joinCabinetSession(cabinetId, userId, shareCode);
		if (lentRedisService.isCabinetSessionFull(cabinetId)) {
			List<Long> userIdsInCabinet = lentRedisService.getUsersInCabinetSession(cabinetId);
			LocalDateTime expiredAt =
					lentPolicyService.generateExpirationDate(now, SHARE, userIdsInCabinet.size());
			lentCommandService.startLent(userIdsInCabinet, cabinet.getId(), now, expiredAt);
			lentRedisService.clearCabinetSession(cabinetId);
			cabinetCommandService.changeStatus(cabinet, CabinetStatus.FULL);
			cabinetCommandService.changeUserCount(cabinet, userIdsInCabinet.size());
=======
        boolean isExist = lentRedisService.isInCabinetSession(cabinetId);
        if (!isExist) {
            lentPolicyService.verifyCabinetForLent(cabinet.getStatus(), cabinet.getLentType());
            shareCode = lentRedisService.createCabinetSession(cabinetId);
            cabinetCommandService.changeStatus(cabinet, CabinetStatus.IN_SESSION);
        }
        lentRedisService.joinCabinetSession(cabinetId, userId, shareCode);
        if (lentRedisService.isCabinetSessionFull(cabinetId)) {
            List<Long> userIdsInCabinet = lentRedisService.getUsersInCabinetSession(cabinetId);
            LocalDateTime expiredAt =
                    lentPolicyService.generateExpirationDate(now, SHARE, userIdsInCabinet.size());
            lentCommandService.startLent(userIdsInCabinet, cabinet.getCabinetId(), now, expiredAt);
            lentRedisService.clearCabinetSession(cabinetId);
            cabinetCommandService.changeStatus(cabinet, CabinetStatus.FULL);
            cabinetCommandService.changeUserCount(cabinet, userIdsInCabinet.size());
>>>>>>> 3f3297013328376f063d03d50f9cf7b867f0926d

            LentSuccessAlarm alarm = new LentSuccessAlarm(
                    cabinet.getCabinetPlace().getLocation(), cabinet.getVisibleNum(), expiredAt);
            eventPublisher.publishEvent(AlarmEvent.of(userId, alarm));
        }
    }

    @Transactional
    public void startLentClubCabinet(Long userId, Long cabinetId) {
        LocalDateTime now = LocalDateTime.now();
        // TODO : ClubUser 추가 이후 userId로 ClubUser 검증 로직 필요(Policy)
        Cabinet cabinet = cabinetQueryService.findCabinets(cabinetId);
        int userCount = lentQueryService.countCabinetUser(cabinetId);

        lentPolicyService.verifyCabinetLentCount(
                cabinet.getLentType(), cabinet.getMaxUser(), userCount);
        lentPolicyService.verifyCabinetType(cabinet.getLentType(), LentType.CLUB);
        lentPolicyService.verifyCabinetForLent(cabinet.getStatus(), cabinet.getLentType());
        LocalDateTime expiredAt =
                lentPolicyService.generateExpirationDate(now, cabinet.getLentType(), 1);
        lentCommandService.startLent(userId, cabinetId, now, expiredAt);
        cabinetCommandService.changeUserCount(cabinet, userCount + 1);
    }

    @Transactional
    public void endUserLent(Long userId, String memo) {
        LocalDateTime now = LocalDateTime.now();
        LentHistory userLentHistory = lentQueryService.getUserActiveLentHistoryWithLock(userId);
        List<LentHistory> cabinetLentHistories =
                lentQueryService.findCabinetActiveLentHistories(userLentHistory.getCabinetId());
        Cabinet cabinet =
                cabinetQueryService.findCabinetsWithLock(userLentHistory.getCabinetId());

<<<<<<< HEAD
		int userRemainCount = cabinetLentHistories.size() - 1;
		cabinetCommandService.changeUserCount(cabinet, userRemainCount);
		lentCommandService.endLent(userLentHistory, now);
		lentRedisService.setPreviousUserName(
				cabinet.getId(), userLentHistory.getUser().getName());
		if (Objects.nonNull(memo)) {
			cabinetCommandService.updateMemo(cabinet, memo);
		}
=======
        int userRemainCount = cabinetLentHistories.size() - 1;
        cabinetCommandService.changeUserCount(cabinet, userRemainCount);
        lentCommandService.endLent(userLentHistory, now);
        lentRedisService.setPreviousUserName(
                cabinet.getCabinetId(), userLentHistory.getUser().getName());
        if (Objects.nonNull(memo)) {
            cabinetCommandService.updateMemo(cabinet, memo);
        }
>>>>>>> 3f3297013328376f063d03d50f9cf7b867f0926d

        LocalDateTime endedAt = userLentHistory.getEndedAt();
        BanType banType = banPolicyService.verifyBan(endedAt, userLentHistory.getExpiredAt());
        if (!banType.equals(BanType.NONE)) {
            LocalDateTime unbannedAt = banPolicyService.getUnBannedAt(
                    endedAt, userLentHistory.getExpiredAt());
            banHistoryCommandService.banUser(userId, endedAt, unbannedAt, banType);
        }
        if (cabinet.isLentType(SHARE)) {
            LocalDateTime expiredAt = lentPolicyService.adjustSharCabinetExpirationDate(
                    userRemainCount, now, userLentHistory);
            cabinetLentHistories.stream().filter(lh -> !lh.equals(userLentHistory))
                    .forEach(lh -> lentCommandService.setExpiredAt(lh, expiredAt));
        }
    }

    @Transactional
    public void updateLentCabinetInfo(Long userId, String title, String memo) {
        Cabinet cabinet = cabinetQueryService.getUserActiveCabinetWithLock(userId);
        cabinetCommandService.updateTitle(cabinet, title);
        cabinetCommandService.updateMemo(cabinet, memo);
    }

    @Transactional
    public void cancelShareCabinetLent(Long userId, Long cabinetId) {
        lentRedisService.deleteUserInCabinetSession(cabinetId, userId);
        if (lentRedisService.isCabinetSessionEmpty(cabinetId)) {
            Cabinet cabinet = cabinetQueryService.findCabinetsWithLock(cabinetId);
            cabinetCommandService.changeStatus(cabinet, CabinetStatus.AVAILABLE);
        }
    }

    @Transactional
    public void shareCabinetSessionExpired(Long cabinetId) {
        Cabinet cabinet = cabinetQueryService.findCabinetsWithLock(cabinetId);
        List<Long> usersInCabinetSession = lentRedisService.getUsersInCabinetSession(cabinetId);
        if (lentPolicyService.verifyUserCountOnShareCabinet(usersInCabinetSession.size())) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiredAt = lentPolicyService.generateExpirationDate(
                    now, SHARE, usersInCabinetSession.size());
            cabinetCommandService.changeStatus(cabinet, CabinetStatus.FULL);
            lentCommandService.startLent(usersInCabinetSession, cabinetId, now, expiredAt);
        } else {
            cabinetCommandService.changeStatus(cabinet, CabinetStatus.AVAILABLE);
        }
        lentRedisService.confirmCabinetSession(cabinetId, usersInCabinetSession);
    }
}
