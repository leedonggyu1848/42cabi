package org.ftclub.cabinet.cabinet.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static org.ftclub.cabinet.cabinet.domain.CabinetStatus.AVAILABLE;
import static org.ftclub.cabinet.cabinet.domain.CabinetStatus.PENDING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ftclub.cabinet.cabinet.domain.Cabinet;
import org.ftclub.cabinet.cabinet.domain.CabinetStatus;
import org.ftclub.cabinet.cabinet.domain.Grid;
import org.ftclub.cabinet.cabinet.domain.LentType;
import org.ftclub.cabinet.cabinet.repository.CabinetOptionalFetcher;
import org.ftclub.cabinet.dto.ActiveCabinetInfoEntities;
import org.ftclub.cabinet.dto.BuildingFloorsDto;
import org.ftclub.cabinet.dto.CabinetClubStatusRequestDto;
import org.ftclub.cabinet.dto.CabinetDto;
import org.ftclub.cabinet.dto.CabinetInfoPaginationDto;
import org.ftclub.cabinet.dto.CabinetInfoResponseDto;
import org.ftclub.cabinet.dto.CabinetPaginationDto;
import org.ftclub.cabinet.dto.CabinetPendingResponseDto;
import org.ftclub.cabinet.dto.CabinetPreviewDto;
import org.ftclub.cabinet.dto.CabinetSimpleDto;
import org.ftclub.cabinet.dto.CabinetSimplePaginationDto;
import org.ftclub.cabinet.dto.CabinetStatusRequestDto;
import org.ftclub.cabinet.dto.CabinetsPerSectionResponseDto;
import org.ftclub.cabinet.dto.LentDto;
import org.ftclub.cabinet.dto.LentHistoryDto;
import org.ftclub.cabinet.dto.LentHistoryPaginationDto;
import org.ftclub.cabinet.lent.domain.LentHistory;
import org.ftclub.cabinet.lent.repository.LentOptionalFetcher;
import org.ftclub.cabinet.lent.repository.LentRedis;
import org.ftclub.cabinet.mapper.CabinetMapper;
import org.ftclub.cabinet.mapper.LentMapper;
import org.ftclub.cabinet.user.domain.User;
import org.ftclub.cabinet.user.repository.UserOptionalFetcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class CabinetFacadeServiceImpl implements CabinetFacadeService {

	private final CabinetService cabinetService;
	private final CabinetOptionalFetcher cabinetOptionalFetcher;
	private final LentOptionalFetcher lentOptionalFetcher;
	private final CabinetMapper cabinetMapper;
	private final LentMapper lentMapper;
	private final LentRedis lentRedis;
	private final UserOptionalFetcher userOptionalFetcher;

	/*-------------------------------------------READ-------------------------------------------*/

	/**
	 * {@inheritDoc}
	 * <p>
	 * 존재하는 모든 건물들을 가져오고, 각 건물별 층 정보들을 가져옵니다.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<BuildingFloorsDto> getBuildingFloorsResponse() {
		log.debug("getBuildingFloorsResponse");
		List<String> allBuildings = cabinetOptionalFetcher.findAllBuildings();
		return allBuildings.stream()
				.map(building -> {
					List<Integer> floors = cabinetOptionalFetcher.findAllFloorsByBuilding(building);
					return cabinetMapper.toBuildingFloorsDto(building, floors);
				})
				.collect(Collectors.toList());
	}

	private List<LentDto> findShareInSessionUsers(String cabinetId) {
		List<LentDto> lentDtos = new ArrayList<>();
		List<String> userIds = lentRedis.getAllUserInCabinet(cabinetId);
		for (String userId : userIds) {
			String userName = userOptionalFetcher.findUser(Long.valueOf(userId)).getName();
			lentDtos.add(new LentDto(Long.valueOf(userId), userName, null, null, null));
		}
		return lentDtos;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public CabinetInfoResponseDto getCabinetInfo(Long cabinetId) {
		log.debug("getCabinetInfo");
		List<LentDto> lentDtos = new ArrayList<>();
		List<LentHistory> lentHistories = lentOptionalFetcher.findAllActiveLentByCabinetId(
				cabinetId);
		// 현재 대여중인 사물함이 아닌경우, 패스 (lentDto 에 Null)
		for (LentHistory lentHistory : lentHistories) {
			User findUser = lentHistory.getUser();
			lentDtos.add(lentMapper.toLentDto(findUser, lentHistory));
		}
		if (lentHistories.isEmpty()) {
			// 세션중인 공유사물함인지 확인
			lentDtos = findShareInSessionUsers(cabinetId.toString());
		}
		return cabinetMapper.toCabinetInfoResponseDto(cabinetOptionalFetcher.findCabinet(cabinetId),
				lentDtos, lentRedis.getCabinetExpiredAt(cabinetId.toString()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public CabinetSimplePaginationDto getCabinetsSimpleInfoByVisibleNum(Integer visibleNum) {
		log.debug("getCabinetsSimpleInfoByVisibleNum");
		PageRequest page = PageRequest.of(0, Integer.MAX_VALUE);
		Page<Cabinet> allCabinetsByVisibleNum = cabinetOptionalFetcher.findPaginationByVisibleNum(
				visibleNum, page);
		List<CabinetSimpleDto> cabinetsSimpleDtoList = allCabinetsByVisibleNum.stream()
				.map(cabinetMapper::toCabinetSimpleDto).collect(Collectors.toList());
		return new CabinetSimplePaginationDto(cabinetsSimpleDtoList,
				allCabinetsByVisibleNum.getTotalElements());
	}

	private String checkCabinetTitle(Cabinet cabinet, List<LentHistory> lentHistories) {
		if (cabinet.getTitle() != null && !cabinet.getTitle().isEmpty()) {
			// 12/2 패치 : 개인 사물함도 타이틀이 있을 경우 타이틀을 노출합니다.
			return cabinet.getTitle();
		} else if (!lentHistories.isEmpty() && lentHistories.get(0).getUser() != null) {
			return lentHistories.get(0).getUser().getName();

		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<CabinetsPerSectionResponseDto> getCabinetsPerSection(String building,
			Integer floor) {
		log.debug("getCabinetsPerSection");
		List<ActiveCabinetInfoEntities> currentLentCabinets = cabinetOptionalFetcher
				.findCabinetsActiveLentHistoriesByBuildingAndFloor(building, floor);
		Map<Cabinet, List<LentHistory>> cabinetLentHistories = currentLentCabinets.stream().
				collect(groupingBy(ActiveCabinetInfoEntities::getCabinet,
						mapping(ActiveCabinetInfoEntities::getLentHistory,
								Collectors.toList())));

		List<Cabinet> allCabinetsOnSection =
				cabinetOptionalFetcher.findAllCabinetsByBuildingAndFloor(building, floor);
		Map<String, List<CabinetPreviewDto>> cabinetPreviewsBySection = new LinkedHashMap<>();
		allCabinetsOnSection.stream()
				.sorted(Comparator.comparing(Cabinet::getVisibleNum))
				.forEach(cabinet -> {
					String section = cabinet.getCabinetPlace().getLocation().getSection();
					List<LentHistory> lentHistories =
							cabinetLentHistories.getOrDefault(cabinet, Collections.emptyList());
					String title = checkCabinetTitle(cabinet, lentHistories);
					cabinetPreviewsBySection.computeIfAbsent(section, k -> new ArrayList<>())
							.add(cabinetMapper.toCabinetPreviewDto(cabinet, lentHistories.size(),
									title));
				});
		return cabinetPreviewsBySection.entrySet().stream()
				.map(entry -> cabinetMapper.toCabinetsPerSectionResponseDto(entry.getKey(),
						entry.getValue()))
				.collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public CabinetPaginationDto getCabinetPaginationByLentType(LentType lentType, Integer page,
			Integer size) {
		log.debug("getCabinetPaginationByLentType");
		if (size <= 0) {
			size = Integer.MAX_VALUE;
		}
		PageRequest pageable = PageRequest.of(page, size);
		Page<Cabinet> cabinets = cabinetOptionalFetcher.findPaginationByLentType(lentType,
				pageable);
		List<CabinetDto> cabinetDtos = cabinets.toList().stream()
				.map(cabinetMapper::toCabinetDto)
				.collect(Collectors.toList());
		return cabinetMapper.toCabinetPaginationDtoList(cabinetDtos,
				cabinets.getTotalElements());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public CabinetPaginationDto getCabinetPaginationByStatus(CabinetStatus status, Integer page,
			Integer size) {
		log.debug("getCabinetPaginationByStatus");
		if (size <= 0) {
			size = Integer.MAX_VALUE;
		}
		PageRequest pageable = PageRequest.of(page, size);
		Page<Cabinet> cabinets = cabinetOptionalFetcher.findPaginationByStatus(status, pageable);
		List<CabinetDto> cabinetDtos = cabinets.toList().stream()
				.map((cabinet) -> cabinetMapper.toCabinetDto(cabinet))
				.collect(Collectors.toList());
		return cabinetMapper.toCabinetPaginationDtoList(cabinetDtos,
				cabinets.getTotalElements());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public CabinetPaginationDto getCabinetPaginationByVisibleNum(Integer visibleNum, Integer page,
			Integer size) {
		log.debug("getCabinetPaginationByVisibleNum");
		if (size <= 0) {
			size = Integer.MAX_VALUE;
		}
		PageRequest pageable = PageRequest.of(page, size);
		Page<Cabinet> cabinets = cabinetOptionalFetcher.findPaginationByVisibleNum(visibleNum,
				pageable);
		List<CabinetDto> cabinetDtos = cabinets.stream()
				.map(cabinetMapper::toCabinetDto).collect(Collectors.toList());
		return cabinetMapper.toCabinetPaginationDtoList(cabinetDtos,
				cabinets.getTotalElements());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public LentHistoryPaginationDto getCabinetLentHistoriesPagination(Long cabinetId, Integer page,
			Integer size) {
		log.debug("getCabinetLentHistoriesPagination");
		if (size <= 0) {
			size = Integer.MAX_VALUE;
		}
		PageRequest pageable = PageRequest.of(page, size,
				Sort.by(Sort.Direction.DESC, "startedAt"));
		Page<LentHistory> lentHistories = lentOptionalFetcher.findPaginationByCabinetId(cabinetId,
				pageable);
		return lentMapper.toLentHistoryPaginationDto(
				generateLentHistoryDtoList(lentHistories.toList()),
				lentHistories.getTotalElements());
	}

	/**
	 * 사물함들의 정보와 각각의 대여 정보들을 가져옵니다.
	 *
	 * @param cabinetIds 사물함 id 리스트
	 * @return 사물함 정보 리스트
	 */
	@Transactional(readOnly = true)
	public List<CabinetInfoResponseDto> getCabinetInfoBundle(List<Long> cabinetIds) {
		log.debug("getCabinetInfoBundle");
		List<CabinetInfoResponseDto> result = new ArrayList<>();
		for (Long cabinetId : cabinetIds) {
			CabinetInfoResponseDto cabinetInfo = getCabinetInfo(cabinetId);
			if (CabinetInfoResponseDto.isValid(cabinetInfo)) {
				result.add(cabinetInfo);
			}
		}
		// Sorting ASC by Cabinet Floor
		Comparator<CabinetInfoResponseDto> floorComparator = Comparator.comparing(
				dto -> dto.getLocation().getFloor());

		Collections.sort(result, floorComparator);
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public CabinetInfoPaginationDto getCabinetsInfo(Integer visibleNum) {
		log.debug("getCabinetsInfo");
		PageRequest page = PageRequest.of(0, Integer.MAX_VALUE);
		Page<Cabinet> allCabinetsByVisibleNum = cabinetOptionalFetcher.findPaginationByVisibleNum(
				visibleNum, page);
		List<Long> collect = allCabinetsByVisibleNum.map(cabinet -> cabinet.getCabinetId())
				.stream().collect(Collectors.toList());
		return new CabinetInfoPaginationDto(getCabinetInfoBundle(collect),
				allCabinetsByVisibleNum.getTotalElements());
	}

	/**
	 * LentHistory를 이용해 LentHistoryDto로 매핑하여 반환합니다.
	 * ToDo : new -> mapper 쓰기 + query service 분리
	 *
	 * @param lentHistories 대여 기록 리스트
	 * @return LentHistoryDto 리스트
	 */
	private List<LentHistoryDto> generateLentHistoryDtoList(
			List<LentHistory> lentHistories) {
		log.debug("generateLentHistoryDtoList");
		return lentHistories.stream()
				.map(e -> lentMapper.toLentHistoryDto(e,
						e.getUser(),
						e.getCabinet()))
				.collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public CabinetPendingResponseDto getPendingCabinets(String building) {
		log.debug("getPendingCabinets");
		final LocalDate yesterday = LocalDateTime.now().minusDays(1).toLocalDate();
		List<Cabinet> buildingCabinets =
				cabinetOptionalFetcher.findPendingCabinetsNotLentTypeAndStatus(
						building, LentType.CLUB, List.of(AVAILABLE, PENDING));
		List<Long> cabinetIds = buildingCabinets.stream()
				.filter(cabinet -> cabinet.isStatus(PENDING))
				.map(Cabinet::getCabinetId).collect(Collectors.toList());
		Map<Integer, List<CabinetPreviewDto>> cabinetFloorMap =
				cabinetOptionalFetcher.findAllFloorsByBuilding(building).stream()
						.collect(toMap(key -> key, value -> new ArrayList<>()));
		Map<Long, List<LentHistory>> lentHistoriesMap =
				lentOptionalFetcher.findAllByCabinetIdsAfterDate(yesterday, cabinetIds)
						.stream().collect(groupingBy(LentHistory::getCabinetId));
		buildingCabinets.forEach(cabinet -> {
			Integer floor = cabinet.getCabinetPlace().getLocation().getFloor();
			if (cabinet.isStatus(AVAILABLE)) {
				cabinetFloorMap.get(floor).add(cabinetMapper.toCabinetPreviewDto(cabinet, 0, null));
			}
			if (cabinet.isStatus(PENDING)) {
				LocalDateTime latestEndedAt = lentHistoriesMap.get(cabinet.getCabinetId()).stream()
						.map(LentHistory::getEndedAt)
						.max(LocalDateTime::compareTo).orElse(null);
				if (latestEndedAt != null && latestEndedAt.toLocalDate().isEqual(yesterday)) {
					cabinetFloorMap.get(floor)
							.add(cabinetMapper.toCabinetPreviewDto(cabinet, 0, null));
				}
			}
		});
		return cabinetMapper.toCabinetPendingResponseDto(cabinetFloorMap);
	}

	/*--------------------------------------------CUD--------------------------------------------*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void updateCabinetStatusNote(Long cabinetId, String statusNote) {
		cabinetService.updateStatusNote(cabinetId, statusNote);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void updateCabinetTitle(Long cabinetId, String title) {
		cabinetService.updateTitle(cabinetId, title);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void updateCabinetGrid(Long cabinetId, Integer row, Integer col) {
		cabinetService.updateGrid(cabinetId, Grid.of(row, col));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void updateCabinetVisibleNum(Long cabinetId, Integer visibleNum) {
		cabinetService.updateVisibleNum(cabinetId, visibleNum);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void updateCabinetBundleStatus(CabinetStatusRequestDto cabinetStatusRequestDto) {
		CabinetStatus status = cabinetStatusRequestDto.getStatus();
		LentType lentType = cabinetStatusRequestDto.getLentType();
		for (Long cabinetId : cabinetStatusRequestDto.getCabinetIds()) {
			if (status != null) {
				cabinetService.updateStatus(cabinetId, cabinetStatusRequestDto.getStatus());
			}
			if (lentType != null) {
				cabinetService.updateLentType(cabinetId, cabinetStatusRequestDto.getLentType());
			}
		}
	}


	/**
	 * 사물함에 동아리 유저를 대여 시킵니다.
	 *
	 * @param cabinetClubStatusRequestDto
	 */
	@Override
	@Transactional
	public void updateCabinetClubStatus(CabinetClubStatusRequestDto cabinetClubStatusRequestDto) {
		cabinetService.updateClub(cabinetClubStatusRequestDto.getCabinetId(),
				cabinetClubStatusRequestDto.getUserId(),
				cabinetClubStatusRequestDto.getStatusNote());
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public void updateCabinetBundleLentType(List<Long> cabinetIds, LentType lentType) {
//		for (Long cabinetId : cabinetIds) {
//			cabinetService.updateLentType(cabinetId, lentType);
//		}
//	}

	/**
	 * CACHE 적용되어 사용될 코드
	 @Transactional(readOnly = true)
	 public List<CabinetsPerSectionResponseDto> getCabinetsPerSection2(String building,
	 Integer floor) {
	 log.debug("getCabinetsPerSection2");
	 List<Cabinet> cabinetsByBuildingAndFloor2 = cabinetOptionalFetcher.findCabinetsByBuildingAndFloor2(
	 building, floor);
	 List<ActiveCabinetInfoEntities> currentLentCabinets = cabinetOptionalFetcher.findCabinetsActiveLentHistoriesByBuildingAndFloor2(
	 building, floor);
	 //		List<ActiveCabinetInfoEntities> currentLentCabinets = cabinetOptionalFetcher.findCabinetsActiveLentHistoriesByBuildingAndFloor(building, floor);
	 List<Cabinet> allCabinetsByBuildingAndFloor = cabinetOptionalFetcher.findAllCabinetsByBuildingAndFloor(
	 building, floor);

	 // 층별 / 건물로 가져온 Cabinet 은 cache
	 // Cabinet 기준으로 lentHistory 를 조회
	 // LentHistory와 연결된 User 조회

	 Map<Cabinet, List<LentHistory>> cabinetLentHistories = currentLentCabinets.stream().
	 collect(Collectors.groupingBy(ActiveCabinetInfoEntities::getCabinet,
	 Collectors.mapping(ActiveCabinetInfoEntities::getLentHistory,
	 Collectors.toList())));

	 Map<String, List<CabinetPreviewDto>> cabinetPreviewsBySection = new HashMap<>();
	 cabinetLentHistories.forEach((cabinet, lentHistories) -> {
	 String section = cabinet.getCabinetPlace().getLocation().getSection();
	 CabinetPreviewDto preview = createCabinetPreviewDto(cabinet, lentHistories);
	 if (cabinetPreviewsBySection.containsKey(section)) {
	 cabinetPreviewsBySection.get(section).add(preview);
	 } else {
	 List<CabinetPreviewDto> previews = new ArrayList<>();
	 previews.add(preview);
	 cabinetPreviewsBySection.put(section, previews);
	 }
	 });
	 allCabinetsByBuildingAndFloor.forEach(cabinet -> {
	 if (!cabinetLentHistories.containsKey(cabinet)) {
	 String section = cabinet.getCabinetPlace().getLocation().getSection();
	 CabinetPreviewDto preview = createCabinetPreviewDto(cabinet,
	 Collections.emptyList());
	 if (cabinetPreviewsBySection.containsKey(section)) {
	 cabinetPreviewsBySection.get(section).add(preview);
	 } else {
	 List<CabinetPreviewDto> previews = new ArrayList<>();
	 previews.add(preview);
	 cabinetPreviewsBySection.put(section, previews);
	 }
	 }
	 });
	 cabinetPreviewsBySection.values().forEach(cabinetList -> cabinetList.sort(
	 Comparator.comparing(CabinetPreviewDto::getVisibleNum)));
	 return cabinetPreviewsBySection.entrySet().stream()
	 .sorted(Comparator.comparing(entry -> entry.getValue().get(0).getVisibleNum()))
	 .map(entry -> cabinetMapper.toCabinetsPerSectionResponseDto(entry.getKey(),
	 entry.getValue()))
	 .collect(Collectors.toList());
	 }
	 **/
}
