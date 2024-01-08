package matgo.global.util;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matgo.member.domain.entity.Member;
import matgo.member.domain.entity.Region;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.domain.repository.RegionRepository;
import matgo.member.domain.type.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RegionRepository regionRepository;
    private final MemberRepository memberRepository;

    @Value("${admin.email}")
    private String adminEmail;
    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Region> regions = regionRepository.findAll();
        if (regions.isEmpty()) {
            List<Region> regionList = List.of(
              new Region("효자동1가"),
              new Region("효자동2가"),
              new Region("효자동3가"),
              new Region("평화동1가"),
              new Region("평화동2가"),
              new Region("금암동"),
              new Region("덕진동1가"),
              new Region("덕진동2가"),
              new Region("만성동"),
              new Region("반월동"),
              new Region("산정동"),
              new Region("송천동1가"),
              new Region("송천동2가"),
              new Region("여의동"),
              new Region("여의동2가"),
              new Region("우아동1가"),
              new Region("우아동2가"),
              new Region("우아동3가"),
              new Region("원동"),
              new Region("중동"),
              new Region("장동"),
              new Region("전미동1가"),
              new Region("전미동2가"),
              new Region("인후동1가"),
              new Region("인후동2가"),
              new Region("진북동"),
              new Region("팔복동1가"),
              new Region("팔복동2가"),
              new Region("호성동1가"),
              new Region("호성동2가"),
              new Region("경원동1가"),
              new Region("경원동2가"),
              new Region("경원동3가"),
              new Region("고사동"),
              new Region("교동"),
              new Region("다가동1가"),
              new Region("다가동3가"),
              new Region("다가동4가"),
              new Region("동서학동"),
              new Region("삼천동1가"),
              new Region("삼천동2가"),
              new Region("색장동"),
              new Region("서노송동"),
              new Region("서서학동"),
              new Region("서신동"),
              new Region("전동"),
              new Region("전동3가"),
              new Region("중노송동"),
              new Region("중앙동1가"),
              new Region("중앙동2가"),
              new Region("중앙동3가"),
              new Region("중앙동4가")
            );
            regionRepository.saveAll(regionList);
        }

        // admin
        Optional<Member> existAdmin = memberRepository.findByEmail(adminEmail);

        if (existAdmin.isPresent()) {
            return;
        }
        Member admin = Member.builder()
                             .nickname("admin")
                             .email(adminEmail)
                             .password(adminPassword)
                             .isActive(true)
                             .profileImage("admin")
                             .role(UserRole.ROLE_ADMIN)
                             .region(regionRepository.findById(10L).orElseThrow())
                             .build();

        memberRepository.save(admin);
    }
}
