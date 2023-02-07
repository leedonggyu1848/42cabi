import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import SearchBarList from "@/components/TopNav/SearchBar/SearchBarList/SearchBarList";
import {
  axiosSearchByCabinetNum,
  axiosSearchByIntraId,
} from "@/api/axios/axios.custom";
import useOutsideClick from "@/hooks/useOutsideClick";

const SearchBar = () => {
  const navigate = useNavigate();
  const searchWrap = useRef<HTMLDivElement>(null);
  const searchInput = useRef<HTMLInputElement>(null);
  const [searchListById, setSearchListById] = useState<any[]>([]);
  const [searchListByNum, setSearchListByNum] = useState<any[]>([]);
  const [totalLength, setTotalLength] = useState<number>(0);
  const [isFocus, setIsFocus] = useState<boolean>(true);
  const [targetIndex, setTargetIndex] = useState<number>(-1);
  const [searchValue, setSearchValue] = useState<string>("");

  const searchClear = () => {
    setSearchListById([]);
    setSearchListByNum([]);
    setTotalLength(0);
    setTargetIndex(-1);
    if (searchInput.current) {
      searchInput.current.value = "";
      setSearchValue("");
    }
  };

  const SearchBarButtonHandler = () => {
    if (searchInput.current) {
      const searchValue = searchInput.current.value;
      if (searchValue.length <= 0) {
        searchClear();
        return alert("검색어를 입력해주세요.");
      } else if (isNaN(Number(searchValue)) && searchValue.length <= 1) {
        searchClear();
        return alert("두 글자 이상의 검색어를 입력해주세요.");
      } else {
        navigate({
          pathname: "search",
          search: `?q=${searchInput.current.value}`,
        });
        searchClear();
      }
    }
  };

  const debounce = (func: Function, wait: number) => {
    let timeout: NodeJS.Timeout;

    return function executedFunction(...args: any[]) {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  };

  const searchInputHandler = async () => {
    if (searchInput.current) {
      setSearchValue(searchInput.current.value);
      const searchValue = searchInput.current.value;
      if (searchValue.length <= 0) {
        setSearchListById([]);
        setSearchListByNum([]);
        setTotalLength(0);
        setTargetIndex(-1);
        return;
      }
      if (isNaN(Number(searchValue))) {
        // intra_ID 검색
        if (searchValue.length <= 1) {
          setSearchListById([]);
          setTotalLength(0);
          setTargetIndex(-1);
        } else {
          const searchResult = await axiosSearchByIntraId(searchValue);
          setSearchListByNum([]);
          setSearchListById(searchResult.data.result);
          setTotalLength(searchResult.data.total_length);
        }
      } else {
        // cabinetnumber 검색
        if (searchValue.length <= 0) {
          setSearchListByNum([]);
          setTotalLength(0);
        } else {
          const searchResult = await axiosSearchByCabinetNum(
            Number(searchValue)
          );
          setSearchListById([]);
          setSearchListByNum(searchResult.data.result);
          setTotalLength(searchResult.data.total_length);
        }
      }
    }
  };

  const cancelHandler = () => {
    searchClear();
    document.getElementById("searchBar")!.classList.remove("on");
    document.getElementById("topNavLogo")!.classList.remove("pushOut");
    document.getElementById("topNavButtonGroup")!.classList.remove("pushOut");
    document.getElementById("topNavWrap")!.classList.remove("pushOut");
  };

  // outside click
  useOutsideClick(searchWrap, () => {
    setIsFocus(false);
  });

  const valueChangeHandler = () => {
    if (isNaN(Number(searchInput.current!.value))) {
      return searchListById[targetIndex].intra_id;
    } else {
      return searchInput.current!.value;
    }
  };

  // searchInput value change
  useEffect(() => {
    if (targetIndex !== -1) {
      searchInput.current!.value = valueChangeHandler();
      setSearchValue(searchInput.current!.value);
    }
  }, [targetIndex]);

  const onKeyHandler = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      SearchBarButtonHandler();
    } else if (e.key == "ArrowUp") {
      if (totalLength > 0) {
        setTargetIndex((prev) =>
          prev > 0
            ? prev - 1
            : Math.max(searchListById.length, searchListByNum.length) - 1
        );
      }
    } else if (e.key == "ArrowDown") {
      if (totalLength > 0) {
        setTargetIndex((prev) =>
          prev < Math.max(searchListById.length, searchListByNum.length) - 1
            ? prev + 1
            : 0
        );
      }
    }
  };

  return (
    <SearchBarWrapperStyled id="searchBar">
      <SearchBarStyled>
        <SearchBarInputStyled
          ref={searchInput}
          type="text"
          placeholder="Search"
          onFocus={() => {
            setIsFocus(true);
          }}
          onChange={debounce(searchInputHandler, 300)}
          onKeyDown={onKeyHandler}
        ></SearchBarInputStyled>
        <SearchButtonStyled onClick={SearchBarButtonHandler} />
      </SearchBarStyled>
      <CancelButtonStyled onClick={cancelHandler}>취소</CancelButtonStyled>
      {searchInput.current?.value && totalLength > 0 && (
        <>
          <SearchBarList
            searchListById={searchListById}
            searchListByNum={searchListByNum}
            searchWord={searchValue}
            searchClear={searchClear}
            totalLength={totalLength}
            targetIndex={targetIndex}
          />
        </>
      )}
    </SearchBarWrapperStyled>
  );
};

const SearchBarWrapperStyled = styled.div`
  position: relative;
`;

const SearchBarStyled = styled.div`
  position: relative;
  width: 100%;
`;

const SearchBarInputStyled = styled.input`
  width: 300px;
  height: 40px;
  border: 1px solid var(--white);
  border-radius: 10px;
  text-align: left;
  padding: 0 20px;
  color: var(--white);
  background-color: rgba(255, 255, 255, 0.2);
  &::placeholder {
    color: var(--white);
  }
`;

const SearchButtonStyled = styled.button`
  background: url("/src/assets/images/searchWhite.svg") no-repeat 50% 50%;
  width: 32px;
  height: 32px;
  position: absolute;
  top: 4px;
  right: 14px;
`;

const CancelButtonStyled = styled.button`
  min-width: 60px;
  width: 60px;
  height: 40px;
  overflow: hidden;
  display: none;
  @media screen and (max-width: 768px) {
    display: block;
  }
`;

export default SearchBar;
