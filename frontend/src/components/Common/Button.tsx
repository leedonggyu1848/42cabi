import React from "react";
import styled, { css } from "styled-components";

interface ButtonInterface {
  onClick(event: React.MouseEvent<HTMLButtonElement>): void;
  text: string;
  theme: string;
  disabled?: boolean;
  iconSrc?: string;
  iconAlt?: string;
}

const Button = (props: ButtonInterface) => {
  return (
    <ButtonContainerStyled
      onClick={props.onClick}
      theme={props.theme}
      disabled={props.disabled}
    >
      {props.iconSrc && (
        <ButtonIconStyled src={props.iconSrc} alt={props.iconAlt || ""} />
      )}
      {props.text}
    </ButtonContainerStyled>
  );
};

const ButtonContainerStyled = styled.button`
  max-width: 240px;
  width: 100%;
  height: 60px;
  padding: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 10px;
  margin-bottom: 15px;
  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
  &:last-child {
    margin-bottom: 0;
  }
  ${(props) =>
    props.theme === "fill" &&
    css`
      background: var(--main-color);
      color: var(--white);
    `}
  ${(props) =>
    props.theme === "line" &&
    css`
      background: var(--white);
      color: var(--main-color);
      border: 1px solid var(--main-color);
    `}
  ${(props) =>
    props.theme === "lightGrayLine" &&
    css`
      background: var(--white);
      color: var(--line-color);
      border: 1px solid var(--line-color);
    `}
  ${(props) =>
    props.theme === "grayLine" &&
    css`
      background: var(--white);
      color: var(--gray-color);
      border: 1px solid var(--gray-color);
    `}
  ${(props) =>
    props.theme === "smallGrayLine" &&
    css`
      max-width: 200px;
      height: 40px;
      background: var(--white);
      color: var(--gray-color);
      font-size: 0.875rem;
      border: 1px solid var(--gray-color);
    `}

  @media (max-height: 745px) {
    margin-bottom: 8px;
  }
`;

const ButtonIconStyled = styled.img`
  width: 24px;
  height: 24px;
  margin-right: 10px;
  background-image: url("data:image/svg+xml,%3Csvg width='25' height='24' viewBox='0 0 25 24' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M7.50512 4H17.5051C21.3351 4 22.4051 4.92 22.4951 8.5C20.5651 8.5 19.0051 10.07 19.0051 12C19.0051 13.93 20.5651 15.49 22.4951 15.5C22.4051 19.08 21.3351 20 17.5051 20H7.50512C3.50512 20 2.50512 19 2.50512 15V9C2.50512 5 3.50512 4 7.50512 4Z' stroke='%237b7b7b' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3Cpath d='M15.5068 4V7.5' stroke='%237b7b7b' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3Cpath d='M15.5068 16.5V20' stroke='%237b7b7b' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3Cpath d='M9.47498 9.33016L8.85498 10.5802C8.79498 10.7002 8.67498 10.7902 8.54498 10.8102L7.16498 11.0102C6.82498 11.0602 6.68498 11.4802 6.93498 11.7202L7.93498 12.6902C8.03498 12.7802 8.07498 12.9202 8.05498 13.0602L7.81498 14.4302C7.75498 14.7702 8.11498 15.0302 8.41498 14.8702L9.64498 14.2202C9.76498 14.1602 9.91498 14.1602 10.035 14.2202L11.265 14.8702C11.575 15.0302 11.925 14.7702 11.865 14.4302L11.625 13.0602C11.605 12.9202 11.645 12.7902 11.745 12.6902L12.735 11.7202C12.985 11.4802 12.845 11.0602 12.505 11.0102L11.125 10.8102C10.985 10.7902 10.875 10.7102 10.815 10.5802L10.205 9.33016C10.065 9.02016 9.62498 9.02016 9.47498 9.33016Z' stroke='%237b7b7b' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E%0A");

  @media (max-height: 745px) {
    margin-bottom: 8px;
  }
`;

export default Button;
