package org.ftclub.cabinet.cabinet.newService;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ftclub.cabinet.cabinet.domain.Cabinet;
import org.ftclub.cabinet.cabinet.domain.CabinetStatus;
import org.ftclub.cabinet.cabinet.repository.CabinetRepository;
import org.ftclub.cabinet.dto.ActiveCabinetInfoEntities;
import org.ftclub.cabinet.exception.ExceptionStatus;
import org.ftclub.cabinet.exception.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CabinetQueryService {

	private final CabinetRepository cabinetRepository;

	public List<String> getAllBuildings() {
		return cabinetRepository.findAllBuildings();
	}

	public List<Integer> getAllFloorsByBuilding(String building) {
		return cabinetRepository.findAllFloorsByBuilding(building);
	}

	public int countCabinets(CabinetStatus status, Integer floor) {
		return cabinetRepository.countByStatusAndFloor(status, floor);
	}

	public List<Integer> getAllFloorsByBuildings(List<String> buildings) {
		return cabinetRepository.findAllFloorsByBuildings(buildings);
	}

	public Cabinet getCabinets(Long cabinetId) {
		Optional<Cabinet> cabinet = cabinetRepository.findById(cabinetId);
		return cabinet.orElseThrow(() -> new ServiceException(ExceptionStatus.NOT_FOUND_CABINET));
	}

	public Cabinet getCabinetsWithLock(Long cabinetId) {
		Optional<Cabinet> cabinet = cabinetRepository.findByIdWithLock(cabinetId);
		return cabinet.orElseThrow(() -> new ServiceException(ExceptionStatus.NOT_FOUND_CABINET));
	}

	public List<Cabinet> getCabinets(Integer visibleNum) {
		return cabinetRepository.findAllByVisibleNum(visibleNum);
	}

	public Page<Cabinet> getCabinets(Integer visibleNum, PageRequest pageable) {
		return cabinetRepository.findPaginationByVisibleNum(visibleNum, pageable);
	}

	public List<Cabinet> getCabinetsWithLock(List<Long> cabinetIds) {
		return cabinetRepository.findAllByIdsWithLock(cabinetIds);
	}

	public Cabinet getUserActiveCabinetWithLock(Long userId) {
		Optional<Cabinet> cabinet =
				cabinetRepository.findByUserIdAndLentHistoryEndedAtIsNullWithLock(userId);
		return cabinet.orElseThrow(() -> new ServiceException(ExceptionStatus.NOT_FOUND_CABINET));
	}

	public Cabinet findUserActiveCabinet(Long userId) {
		Optional<Cabinet> cabinet =
				cabinetRepository.findByUserIdAndLentHistoryEndedAtIsNull(userId);
		return cabinet.orElse(null);
	}

	public List<Cabinet> findAllCabinetsByBuildingAndFloor(String building, Integer floor) {
		return cabinetRepository.findAllByBuildingAndFloor(building, floor);
	}

	public List<String> findAllBuildings() {
		log.debug("Called findAllBuildings");
		return cabinetRepository.findAllBuildings();
	}

	public List<ActiveCabinetInfoEntities> findActiveCabinetInfoEntities(String building,
			Integer floor) {
		log.debug("Called findActiveCabinetInfoEntities");
		return cabinetRepository.findCabinetsActiveLentHistoriesByBuildingAndFloor(building, floor);
	}
}
