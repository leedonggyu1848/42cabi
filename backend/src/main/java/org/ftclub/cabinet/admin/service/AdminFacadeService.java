package org.ftclub.cabinet.admin.service;

import static java.util.stream.Collectors.toList;
import static org.ftclub.cabinet.cabinet.domain.CabinetStatus.AVAILABLE;
import static org.ftclub.cabinet.cabinet.domain.CabinetStatus.BROKEN;
import static org.ftclub.cabinet.cabinet.domain.CabinetStatus.FULL;
import static org.ftclub.cabinet.cabinet.domain.CabinetStatus.OVERDUE;
import static org.ftclub.cabinet.cabinet.domain.LentType.SHARE;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ftclub.cabinet.cabinet.domain.Cabinet;
import org.ftclub.cabinet.cabinet.newService.CabinetCommandService;
import org.ftclub.cabinet.cabinet.newService.CabinetQueryService;
import org.ftclub.cabinet.dto.BlockedUserPaginationDto;
import org.ftclub.cabinet.dto.CabinetDto;
import org.ftclub.cabinet.dto.CabinetFloorStatisticsResponseDto;
import org.ftclub.cabinet.dto.CabinetInfoPaginationDto;
import org.ftclub.cabinet.dto.CabinetInfoResponseDto;
import org.ftclub.cabinet.dto.CabinetSimpleDto;
import org.ftclub.cabinet.dto.CabinetSimplePaginationDto;
import org.ftclub.cabinet.dto.LentDto;
import org.ftclub.cabinet.dto.LentsStatisticsResponseDto;
import org.ftclub.cabinet.dto.OverdueUserCabinetDto;
import org.ftclub.cabinet.dto.OverdueUserCabinetPaginationDto;
import org.ftclub.cabinet.dto.UserBlockedInfoDto;
import org.ftclub.cabinet.dto.UserCabinetDto;
import org.ftclub.cabinet.dto.UserCabinetPaginationDto;
import org.ftclub.cabinet.dto.UserProfileDto;
import org.ftclub.cabinet.dto.UserProfilePaginationDto;
import org.ftclub.cabinet.exception.ExceptionStatus;
import org.ftclub.cabinet.exception.ServiceException;
import org.ftclub.cabinet.lent.domain.LentHistory;
import org.ftclub.cabinet.lent.service.LentCommandService;
import org.ftclub.cabinet.lent.service.LentPolicyService;
import org.ftclub.cabinet.lent.service.LentQueryService;
import org.ftclub.cabinet.lent.service.LentRedisService;
import org.ftclub.cabinet.mapper.CabinetMapper;
import org.ftclub.cabinet.mapper.LentMapper;
import org.ftclub.cabinet.mapper.UserMapper;
import org.ftclub.cabinet.user.domain.BanHistory;
import org.ftclub.cabinet.user.domain.BanType;
import org.ftclub.cabinet.user.domain.User;
import org.ftclub.cabinet.user.newService.BanHistoryCommandService;
import org.ftclub.cabinet.user.newService.BanHistoryQueryService;
import org.ftclub.cabinet.user.newService.BanPolicyService;
import org.ftclub.cabinet.user.newService.UserQueryService;
import org.ftclub.cabinet.utils.DateUtil;
import org.ftclub.cabinet.utils.ExceptionUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminFacadeService {

	private final AdminQueryService adminQueryService;
	private final AdminCommandService adminCommandService;
	private final CabinetQueryService cabinetQueryService;
	private final CabinetCommandService cabinetCommandService;
	private final UserQueryService userQueryService;
	private final BanHistoryQueryService banHistoryQueryService;
	private final BanHistoryCommandService banHistoryCommandService;
	private final LentQueryService lentQueryService;
	private final LentCommandService lentCommandService;
	private final LentRedisService lentRedisService;

	private final LentPolicyService lentPolicyService;
	private final BanPolicyService banPolicyService;

	private final CabinetMapper cabinetMapper;
	private final UserMapper userMapper;
	private final LentMapper lentMapper;


	@Transactional(readOnly = true)
	public CabinetSimplePaginationDto getCabinetsSimpleInfo(Integer visibleNum) {
		log.debug("Called getCabinetSimpleInfo {}", visibleNum);

		List<Cabinet> cabinets = cabinetQueryService.getCabinets(visibleNum);
		List<CabinetSimpleDto> result = cabinets.stream()
				.map(cabinetMapper::toCabinetSimpleDto).collect(toList());
		return cabinetMapper.toCabinetSimplePaginationDto(result, (long) cabinets.size());
	}

	@Transactional(readOnly = true)
	public CabinetInfoPaginationDto getCabinetInfo(Integer visibleNum) {
		log.debug("Called getCabinetInfo {}", visibleNum);

		List<Cabinet> cabinets = cabinetQueryService.getCabinets(visibleNum);
		List<Long> cabinetIds = cabinets.stream().map(Cabinet::getCabinetId)
				.collect(toList());
		List<LentHistory> lentHistories =
				lentQueryService.findCabinetsActiveLentHistories(cabinetIds);
		Map<Long, List<LentHistory>> lentHistoriesByCabinetId = lentHistories.stream()
				.collect(Collectors.groupingBy(LentHistory::getCabinetId));

		List<CabinetInfoResponseDto> result = cabinets.stream()
				.map(cabinet -> {
					Long cabinetId = cabinet.getCabinetId();
					List<LentDto> lents = null;
					if (lentHistoriesByCabinetId.containsKey(cabinetId)) {
						lents = lentHistoriesByCabinetId.get(cabinetId).stream()
								.map(lh -> lentMapper.toLentDto(lh.getUser(), lh))
								.collect(toList());
					}
					LocalDateTime sessionExpiredAt = lentRedisService.getSessionExpired(cabinetId);
					return cabinetMapper.toCabinetInfoResponseDto(cabinet, lents, sessionExpiredAt);
				}).sorted(Comparator.comparingInt(o -> o.getLocation().getFloor()))
				.collect(toList());
		return cabinetMapper.toCabinetInfoPaginationDto(result, (long) cabinets.size());
	}

	@Transactional(readOnly = true)
	public UserProfilePaginationDto getUsersProfile(String partialName, Pageable pageable) {
		log.debug("Called getUsersProfile {}", partialName);

		Page<User> users = userQueryService.getUsers(partialName, pageable);
		List<UserProfileDto> result = users.stream()
				.map(userMapper::toUserProfileDto).collect(toList());
		return userMapper.toUserProfilePaginationDto(result, users.getTotalElements());
	}

	@Transactional(readOnly = true)
	public UserCabinetPaginationDto getUserLentCabinetInfo(String partialName, Pageable pageable) {
		log.debug("Called getUserLentCabinetInfo {}", partialName);

		LocalDateTime now = LocalDateTime.now();
		Page<User> users = userQueryService.getUsers(partialName, pageable);
		List<Long> userIds = users.stream().map(User::getUserId).collect(toList());

		List<BanHistory> activeBanHistories =
				banHistoryQueryService.findActiveBanHistories(userIds, now);
		List<LentHistory> activeLentHistories =
				lentQueryService.findUsersActiveLentHistoriesAndCabinet(userIds);
		Map<Long, List<BanHistory>> banHistoriesByUserId = activeBanHistories.stream()
				.collect(Collectors.groupingBy(BanHistory::getUserId));
		Map<Long, List<LentHistory>> lentHistoriesByUserId = activeLentHistories.stream()
				.collect(Collectors.groupingBy(LentHistory::getUserId));

		List<UserCabinetDto> result = users.stream().map(user -> {
			List<BanHistory> banHistories = banHistoriesByUserId.get(user.getUserId());
			List<LentHistory> lentHistories = lentHistoriesByUserId.get(user.getUserId());
			BanHistory banHistory = (Objects.nonNull(banHistories) && !banHistories.isEmpty())
					? banHistories.get(0) : null;
			Cabinet cabinet = (Objects.nonNull(lentHistories) && !lentHistories.isEmpty())
					? lentHistories.get(0).getCabinet() : null;
			UserBlockedInfoDto blockedInfoDto = userMapper.toUserBlockedInfoDto(banHistory, user);
			CabinetDto cabinetDto = cabinetMapper.toCabinetDto(cabinet);
			return cabinetMapper.toUserCabinetDto(blockedInfoDto, cabinetDto);
		}).collect(toList());
		return cabinetMapper.toUserCabinetPaginationDto(result, users.getTotalElements());
	}

	public List<CabinetFloorStatisticsResponseDto> getAllCabinetsInfo() {
		log.debug("Called getAllCabinetsInfo");

		List<String> buildings = cabinetQueryService.getAllBuildings();
		List<Integer> floors = cabinetQueryService.getAllFloorsByBuildings(buildings);
		return floors.stream().map(floor -> {
			Integer used = cabinetQueryService.countCabinets(FULL, floor);
			Integer unused = cabinetQueryService.countCabinets(AVAILABLE, floor);
			Integer overdue = cabinetQueryService.countCabinets(OVERDUE, floor);
			Integer disabled = cabinetQueryService.countCabinets(BROKEN, floor);
			Integer total = used + overdue + unused + disabled;
			return cabinetMapper.toCabinetFloorStatisticsResponseDto(
					floor, total, used, overdue, unused, disabled);
		}).collect(Collectors.toList());
	}

	public LentsStatisticsResponseDto getLentCountStatistics(
			LocalDateTime startDate, LocalDateTime endDate) {
		log.debug("Called getLentCountStatistics startDate : {} endDate : {}", startDate, endDate);

		ExceptionUtil.throwIfFalse(startDate.isBefore(endDate),
				new ServiceException(ExceptionStatus.INVALID_ARGUMENT));
		int lentStartCount = lentQueryService.countLentOnDuration(startDate, endDate);
		int lentEndCount = lentQueryService.countReturnOnDuration(startDate, endDate);
		return cabinetMapper.toLentsStatisticsResponseDto(
				startDate, endDate, lentStartCount, lentEndCount);
	}

	public BlockedUserPaginationDto getAllBanUsers(Pageable pageable) {
		log.debug("Called getAllBanUsers");

		LocalDateTime now = LocalDateTime.now();
		Page<BanHistory> banHistories =
				banHistoryQueryService.findActiveBanHistories(now, pageable);
		List<UserBlockedInfoDto> result = banHistories.stream()
				.map(b -> userMapper.toUserBlockedInfoDto(b, b.getUser()))
				.collect(Collectors.toList());
		return userMapper.toBlockedUserPaginationDto(result, banHistories.getTotalElements());
	}

	public OverdueUserCabinetPaginationDto getOverdueUsers(Pageable pageable) {
		log.debug("Called getOverdueUsers");

		LocalDateTime now = LocalDateTime.now();
		List<LentHistory> lentHistories = lentQueryService.findOverdueLentHistories(now, pageable);
		List<OverdueUserCabinetDto> result = lentHistories.stream()
				.map(lh -> cabinetMapper.toOverdueUserCabinetDto(lh, lh.getUser(), lh.getCabinet(),
						DateUtil.calculateTwoDateDiff(now, lh.getExpiredAt()))
				).collect(Collectors.toList());
		return cabinetMapper.toOverdueUserCabinetPaginationDto(result, (long) lentHistories.size());
	}

	@Transactional
	public void endUserLent(Long userId) {
		log.debug("Called endUserLent: {}", userId);

		LocalDateTime now = LocalDateTime.now();
		LentHistory userLentHistory = lentQueryService.getUserActiveLentHistoryWithLock(userId);
		List<LentHistory> cabinetLentHistories =
				lentQueryService.findCabinetActiveLentHistories(userLentHistory.getCabinetId());
		Cabinet cabinet =
				cabinetQueryService.getCabinetsWithLock(userLentHistory.getCabinetId());

		int userRemainCount = cabinetLentHistories.size() - 1;
		cabinetCommandService.changeUserCount(cabinet, userRemainCount);
		lentCommandService.endLent(userLentHistory, now);
		lentRedisService.setPreviousUserName(
				cabinet.getCabinetId(), userLentHistory.getUser().getName());

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
	public void endCabinetLent(List<Long> cabinetIds) {
		log.debug("Called endCabinetsLent: {}", cabinetIds);

		LocalDateTime now = LocalDateTime.now();
		List<Cabinet> cabinets = cabinetQueryService.getCabinetsWithLock(cabinetIds);
		List<LentHistory> lentHistories =
				lentQueryService.findCabinetsActiveLentHistories(cabinetIds);
		Map<Long, List<LentHistory>> lentHistoriesByCabinetId = lentHistories.stream()
				.collect(Collectors.groupingBy(LentHistory::getCabinetId));
		cabinets.forEach(cabinet -> {
			List<LentHistory> cabinetLentHistories =
					lentHistoriesByCabinetId.get(cabinet.getCabinetId());
			lentRedisService.setPreviousUserName(
					cabinet.getCabinetId(), cabinetLentHistories.get(0).getUser().getName());
		});
		lentCommandService.endLent(lentHistories, now);
		cabinetCommandService.changeUserCount(cabinets, 0);
	}
}
