package matgo.member.dto.response;

import matgo.member.domain.entity.Member;

public record MemberResponse(
  Long id,
  String profileImage,
  String nickname
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
          member.getId(),
          member.getProfileImage(),
          member.getNickname()
        );
    }
}
