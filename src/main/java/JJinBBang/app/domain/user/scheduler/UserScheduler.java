package JJinBBang.app.domain.user.scheduler;

import static JJinBBang.app.domain.user.service.UsersServiceImpl.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserScheduler {

	private final UsersRepository usersRepository;


	@Value("${user.deletion.grace-days:30}") // 기본값 30일
	private int graceDays;


	/**
	 * [2단계] 스케줄러: disabledAt로부터 N일 지난 계정 영구 삭제.
	 * 매일 새벽 3시 실행 예시(CRON은 환경에 맞게 조정)
	 */
	@Scheduled(cron = "0 0 3 * * *")
	@Transactional
	public void finalizeDeletionBatch() {
		LocalDateTime deadline = LocalDateTime.now().minusDays(graceDays);
		List<Users> due = usersRepository.findAllDeletionDue(deadline);

		// 작성 데이터는 1단계에서 이미 재매핑되어 있으므로 여기선 유저만 삭제
		for (Users u : due) {
			if (!u.getProviderId().equals(SYSTEM_DELETE_ID)) {
				usersRepository.delete(u);
			}
		}
	}
}