package JJinBBang.app.domain.user.service.login;

import JJinBBang.app.domain.user.entity.Users;

public interface LoginService {
    // TODO : Google, Naver 등도 implement 해서 구현

    String getProviderName();

    String makeProviderId(String id);

    Users login(String oauthCode);

    Users signup(String oauthCode);
}
