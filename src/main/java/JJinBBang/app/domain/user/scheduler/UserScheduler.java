package JJinBBang.app.domain.user.scheduler;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalDateTime;
import java.util.List;

import static JJinBBang.app.domain.user.service.UsersServiceImpl.SYSTEM_DELETE_ID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DynamicScheduler implements SchedulingConfigurer {

	private final UsersRepository usersRepository;

	@Value("${user.deletion.grace-days:30}")
	private int graceDays;

	@Value("${user.deletion.scheduler.hour:0}")
	private int schedulerHour;

	@Value("${user.deletion.scheduler.minute:0}")
	private int schedulerMinute;

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		String cron = String.format("0 %d %d * * *", schedulerMinute, schedulerHour);
		taskRegistrar.addTriggerTask(
				() -> {
					LocalDateTime deadline = LocalDateTime.now().minusDays(graceDays);
					List<Users> due = usersRepository.findAllDeletionDue(deadline);
					for (Users u : due) {
						if (!u.getProviderId().equals(SYSTEM_DELETE_ID)) {
							usersRepository.delete(u);
							log.info("Deleted user: {}", u.getId());
						}
					}
				},
				triggerContext -> new CronTrigger(cron).nextExecutionTime(triggerContext)
		);
	}
}
