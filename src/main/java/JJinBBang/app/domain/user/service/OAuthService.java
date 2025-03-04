package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.user.entity.Users;

public interface OAuthService {

    Users login(String oauthProvider, String oauthAccessToken);

    Users signup(String oauthProvider, String oauthAccessToken);
}
