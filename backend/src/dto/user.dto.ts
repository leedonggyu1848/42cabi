/**
 * 유저의 기본적인 정보를 저장
 */
export class UserDto {
  user_id: number; // 42 고유 ID
  intra_id: string; // 42 로그인 ID
  email?: string; // 42 이메일 ID (확장성을 위해 옵셔널 필드로 지정)
}
