package dopamine.backend.member.request;

import dopamine.backend.level.entity.Level;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class MemberEditDto {
    private Long kakaoId;
    private String nickname;
    private String refreshToken;
    private Level level;
    private Long levelId;
}
