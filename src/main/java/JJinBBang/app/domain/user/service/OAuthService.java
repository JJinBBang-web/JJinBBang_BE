package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.user.entity.Users;

public interface OAuthService {

    Users login(String oauthProvider, String oauthCode, String redirectUri);

    Users signup(Users user);
}
