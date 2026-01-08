package JJinBBang.app.global.jwt.service;

import JJinBBang.app.domain.user.entity.Users;

public interface JwtTokenGenerator {
	String generate(Users user);
}
