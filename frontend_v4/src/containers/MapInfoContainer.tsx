import React, { useState } from "react";
import styled from "styled-components";
import exitButton from "@/assets/images/exitButton.svg";
import MapGridContainer from "./MapGridContainer";
import useDetailInfo from "@/hooks/useDetailInfo";
import { useRecoilValue } from "recoil";
import { currentLocationFloorState } from "@/recoil/selectors";

const SelectContainer = ({
  floor,
  setFloor,
  floorInfo,
}: {
  floor: number;
  setFloor: React.Dispatch<React.SetStateAction<number>>;
  floorInfo: number[];
}) => {
  const onClickFloorOption = () => {
    document.getElementById("mapFloorOptionBox")?.classList.toggle("on");
  };

  const selectFloor = (info: string) => {
    const floorInfo = parseInt(info);
    onClickFloorOption();
    setFloor(floorInfo);
  };

  return (
    <div style={{ position: "relative" }}>
      <CurrentFloorStyled onClick={onClickFloorOption}>
        {`${floor}층`}
      </CurrentFloorStyled>
      <OptionsContainer selectFloor={selectFloor} floorInfo={floorInfo} />
    </div>
  );
};

const CurrentFloorStyled = styled.div`
  background: url("src/assets/images/select.svg") var(--main-color) no-repeat
    80% 55%;
  color: white;
  cursor: pointer;
  width: 65px;
  height: 35px;
  line-height: 35px;
  text-indent: 12px;
  border-radius: 10px;
  margin-bottom: 50px;
  &:hover {
    opacity: 0.9;
  }
`;

const OptionsContainerStyled = styled.div`
  position: absolute;
  left: 0;
  top: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);
  overflow: hidden;
  z-index: 99;
  display: none;
  &.on {
    display: block;
  }
`;

const OptionStyled = styled.div`
  width: 65px;
  height: 35px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--white);
  color: black;
  cursor: pointer;
`;

const OptionsContainer: React.FC<{
  selectFloor: Function;
  floorInfo: number[];
}> = ({ floorInfo, selectFloor }) => {
  return (
    <OptionsContainerStyled id="mapFloorOptionBox">
      {floorInfo.map((info, idx) => (
        <OptionStyled onClick={() => selectFloor(info)} key={idx}>
          {info}층
        </OptionStyled>
      ))}
    </OptionsContainerStyled>
  );
};

const MapInfoContainer = () => {
  const { closeMap } = useDetailInfo();
  const floorInfo = useRecoilValue(currentLocationFloorState);
  const [floor, setFloor] = useState(floorInfo[0]);
  return (
    <MapInfoContainerStyled id="mapInfo">
      <HeaderStyled>
        <H2Styled>지도</H2Styled>
        <img
          onClick={closeMap}
          src={exitButton}
          style={{ width: "24px", cursor: "pointer" }}
        />
      </HeaderStyled>
      <SelectContainer
        floor={floor}
        setFloor={setFloor}
        floorInfo={floorInfo}
      />
      <MapGridContainer floor={floor} />
    </MapInfoContainerStyled>
  );
};

const H2Styled = styled.h2`
  font-size: 1.5rem;
`;

const SelectStyled = styled.select`
  background: var(--main-color);
  padding: 5px 10px;
  border-radius: 5px;
  margin-bottom: 50px;
  & > option {
    background: var(--main-color);
    padding: 5px 10px;
    border-radius: 5px;
  }
`;

const HeaderStyled = styled.div`
  display: flex;
  justify-content: space-between;
  width: 100%;
  align-items: center;
  padding: 40px 20px 40px 40px;
  color: black;
  font-weight: bold;
`;

const MapInfoContainerStyled = styled.div`
  position: fixed;
  top: 80px;
  right: 0;
  min-width: 330px;
  width: 330px;
  height: calc(100% - 80px);
  z-index: 9;
  transform: translateX(120%);
  transition: transform 0.3s ease-in-out;
  box-shadow: 0 0 40px 0 var(--bg-shadow);
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--white);
`;

export default MapInfoContainer;
