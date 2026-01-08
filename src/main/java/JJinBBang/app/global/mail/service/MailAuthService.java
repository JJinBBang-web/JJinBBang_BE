package JJinBBang.app.global.mail.service;

import JJinBBang.app.global.mail.exception.MailInternalException;
import JJinBBang.app.global.mail.exception.MailInvalidException;

public interface MailAuthService {

	/**
	 * 주어진 유저 아이디와 이메일로 인증 코드를 생성해서 전송합니다.
	 * 인증 코드는 application.yml -> mail-auth.expiration-time (밀리초) 동안 유효합니다.
	 * 인증 코드는 application.yml -> mail-auth.auth-code-length 길이로 생성됩니다.
	 * 이미 인증 코드가 발급된 경우에는 기존 인증 코드를 삭제하고 새로 발급합니다.
	 *
	 * @param userId 인증 코드 발급을 요청한 유저의 아이디
	 * @param email  인증 코드를 받을 이메일 주소
	 * @throws MailInternalException 메일 발송 실패 등의 내부 오류
	 * @throws MailInvalidException  이메일 형식 또는 도메인 검증 실패
	 */
	void sendAuthCode(Long userId, String email);

	/**
	 * 사용자가 제출한 인증 코드가 저장된 코드와 일치하는지 검증합니다.
	 * userId 키 값에 대한 인증코드가 존재하지 않거나 만료되었으면 예외를 발생시킵니다.
	 * 저장된 인증코드에 대해 이메일과 인증코드가 일치하는지 검증합니다.
	 *
	 * @param userId   인증 코드 발급을 요청한 유저의 아이디
	 * @param email    인증 대상 이메일
	 * @param authCode 사용자 입력 코드
	 * @return 검증 성공 시 true, 실패 또는 만료 시 false
	 * @throws MailInvalidException 코드 미발급·만료
	 */
	boolean verifyAuthCode(Long userId, String email, String authCode);

	/**
	 * 주어진 userId에 대한 인증코드를 삭제합니다.
	 * 
	 * @param userId 인증코드를 발급받은 유저의 아이디
	 */
	void deleteAuthCode(Long userId);
}
